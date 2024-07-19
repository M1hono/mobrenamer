package org.m1hono.mobrenamer.events;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.m1hono.mobrenamer.MobRenamer;
import org.m1hono.mobrenamer.datapack.MobDef;
import org.m1hono.mobrenamer.datapack.MobDefManager;
import org.m1hono.mobrenamer.datapack.NameConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Mod.EventBusSubscriber(modid = MobRenamer.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerEvents.class);
    private static final MobDefManager MOB_DEF_MANAGER = new MobDefManager();

    @SubscribeEvent
    public static void onServerReloading(AddReloadListenerEvent event) {
        event.addListener(MOB_DEF_MANAGER);
    }

    @SubscribeEvent
    public static void onMobSpawn(@NotNull MobSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Mob)) {
            return;
        }
        ResourceLocation entityType = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (entityType == null) {
            return;
        }

        MobDef mobDef = MobRenamer.getMobDefManager().getMobDef(entityType);
        if (mobDef == null) {
            return;
        }

        NameConfig selectedName = selectName(mobDef, entity);
        if (selectedName != null) {
            entity.setCustomName(selectedName.name());
            entity.setCustomNameVisible(true);
            applyAttributes(entity, selectedName.attributes());
        }
    }

    private static NameConfig selectName(MobDef mobDef, LivingEntity entity) {
        List<NameConfig> validNames = mobDef.names().stream()
                .filter(nameConfig -> isValidForLocation(nameConfig, entity))
                .toList();

        if (validNames.isEmpty()) {
            return null;
        }

        if (mobDef.alwaysNamed()) {
            double totalProbability = validNames.stream().mapToDouble(NameConfig::probability).sum();
            double randomValue = Math.random() * totalProbability;
            double cumulativeProbability = 0.0;
            for (NameConfig nameConfig : validNames) {
                cumulativeProbability += nameConfig.probability();
                if (randomValue < cumulativeProbability) {
                    return nameConfig;
                }
            }
            return validNames.get(validNames.size() - 1); // Fallback to last name if rounding errors occur
        } else {
            for (NameConfig nameConfig : validNames) {
                if (Math.random() < nameConfig.probability()) {
                    return nameConfig;
                }
            }
            return null;
        }
    }

    private static boolean isValidForLocation(NameConfig nameConfig, LivingEntity entity) {
        BlockPos pos = entity.blockPosition();
        ServerLevel level = (ServerLevel) entity.level();

//        if (!nameConfig.structures().isEmpty()) {
//            boolean foundMatchingStructure = level.structureManager().getAllStructuresAt(pos).keySet().stream()
//                    .map(ResourceKey::location)
//                    .anyMatch(nameConfig.structures()::contains);
//            if (!foundMatchingStructure) {
//                return false;
//            }
//        }

        if (!nameConfig.biomes().isEmpty()) {
            Optional<ResourceKey<Biome>> biomeKey = level.getBiome(pos).unwrapKey();
            if (biomeKey.isEmpty() || !nameConfig.biomes().contains(biomeKey.get().location())) {
                return false;
            }
        }

        if (!nameConfig.dimensions().isEmpty() && !nameConfig.dimensions().contains(level.dimension().location())) {
            return false;
        }

        return true;
    }

    private static void applyAttributes(LivingEntity entity, Map<ResourceLocation, Double> attributes) {
        attributes.forEach((attribute, value) -> {
            AttributeInstance attributeInstance = entity.getAttribute(Objects.requireNonNull(ForgeRegistries.ATTRIBUTES.getValue(attribute)));
            if (attributeInstance != null) {
                attributeInstance.addTransientModifier(new AttributeModifier(UUID.randomUUID(), attribute.toString(), value, AttributeModifier.Operation.ADDITION));
            }
        });
    }
}