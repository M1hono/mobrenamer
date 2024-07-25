package org.m1hono.mobrenamer.events;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.Structure;
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
    private static final int MAX_ITERATIONS = 100;

    @SubscribeEvent
    public static void onServerReloading(AddReloadListenerEvent event) {
        event.addListener(MOB_DEF_MANAGER);
        LOGGER.info("MobDefManager added as a reload listener");
    }

    @SubscribeEvent
    public static void onMobSpawn(@NotNull MobSpawnEvent.FinalizeSpawn event) {
        Mob entity = event.getEntity();
        ResourceLocation entityType = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (entityType == null) {
            return;
        }
        MobDef mobDef = MOB_DEF_MANAGER.getMobDef(entityType);
        if (mobDef == null) {
            return;
        }
        NameConfig selectedName = selectName(mobDef, entity, event.getSpawnType());
        if (selectedName != null) {
            entity.setCustomName(selectedName.name());
            entity.setCustomNameVisible(true);
            applyAttributes(entity, selectedName.attributes());
        }
    }

    private static NameConfig selectName(MobDef mobDef, LivingEntity entity, MobSpawnType spawnType) {
        List<NameConfig> allNames = mobDef.names();
        int size = allNames.size();
        System.out.println("Size: " + size);

        if (size == 0) {
            return null;
        }

        if (size == 1) {
            NameConfig singleConfig = allNames.get(0);
            if (isValidForLocation(singleConfig, entity) && isValidForSpawnType(singleConfig, spawnType)) {
                return singleConfig;
            }
            return mobDef.alwaysNamed() ? singleConfig : null;
        }

        int iterations = Math.min(MAX_ITERATIONS, (int) (Math.log(size) / Math.log(2)) * 10);
        Set<Integer> checkedIndices = new HashSet<>();
        Random random = new Random();

        for (int i = 0; i < iterations; i++) {
            int index;
            if (i < size) {
                index = i;
            } else {
                index = random.nextInt(size);
            }
            while (checkedIndices.contains(index)) {
                index = (index + 1) % size;
            }
            checkedIndices.add(index);
            NameConfig nameConfig = allNames.get(index);
            if (isValidForLocation(nameConfig, entity) && isValidForSpawnType(nameConfig, spawnType)) {
                return nameConfig;
            }
        }

        if (mobDef.alwaysNamed()) {
            return allNames.get(0);
        }

        return null;
    }

    private static boolean isValidForLocation(NameConfig nameConfig, LivingEntity entity) {
        BlockPos pos = entity.blockPosition();
        ServerLevel level = (ServerLevel) entity.level();

        if (!nameConfig.structures().isEmpty()) {
            Map<Structure, LongSet> structuresAtPos = getStructuresAtPos(level, pos);
            boolean inSpecifiedStructure = structuresAtPos.keySet().stream()
                    .anyMatch(structure -> {
                        ResourceLocation structureId = level.registryAccess()
                                .registryOrThrow(Registries.STRUCTURE)
                                .getKey(structure);
                        return nameConfig.structures().contains(structureId);
                    });
            if (!inSpecifiedStructure) {
                return false;
            }
        }

        if (!nameConfig.biomes().isEmpty()) {
            ResourceLocation biomeId = level.getBiome(pos).unwrapKey().orElseThrow().location();
            if (!nameConfig.biomes().contains(biomeId)) {
                return false;
            }
        }

        if (!nameConfig.dimensions().isEmpty() && !nameConfig.dimensions().contains(level.dimension().location())) {
            return false;
        }

        return true;
    }

    private static boolean isValidForSpawnType(NameConfig nameConfig, MobSpawnType spawnType) {
        return nameConfig.spawnTypes().isEmpty() || nameConfig.spawnTypes().contains(spawnType);
    }

    private static Map<Structure, LongSet> getStructuresAtPos(ServerLevel level, BlockPos pos) {
        StructureManager structureManager = level.structureManager();
        Map<Structure, LongSet> structuresAtPos = structureManager.getAllStructuresAt(pos);
        if (structuresAtPos.isEmpty()) {
            return new HashMap<>();
        }
        return structuresAtPos;
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