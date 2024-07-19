package org.m1hono.events;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.m1hono.MobRenamer;
import org.m1hono.datapack.MobDef;
import org.m1hono.datapack.MobDefData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = MobRenamer.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerEvents.class);

    @SubscribeEvent
    public static void onMobSpawn(MobSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity == null) {
            return;
        }
        ResourceLocation entityType = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        LOGGER.info("Spawned entity type: {}", entityType);
        if (entityType != null && entityType.toString().equals("minecraft:zombie")) {
            LOGGER.info("Processing zombie spawn");
            for (MobDefData mobDefData : MobRenamer.getMobDefManager().getAllMobDefs().values()) {
                LOGGER.info("Checking MobDefData: {}", mobDefData);
                MobDef mobDef = mobDefData.mobDefinitions().get(entityType);
                if (mobDef != null) {
                    LOGGER.info("Found matching MobDef: {}", mobDef);
                    Component customName = mobDef.name();
                    LOGGER.info("Setting custom name: {}", customName);
                    entity.setCustomName(customName);
                    entity.setCustomNameVisible(true);
                    mobDef.attributes().forEach((attribute, value) -> {
                        LOGGER.info("Modifying attribute: {} = {}", attribute, value);
                        AttributeInstance attributeInstance = entity.getAttribute(Objects.requireNonNull(ForgeRegistries.ATTRIBUTES.getValue(attribute)));
                        if (attributeInstance == null) {
                            LOGGER.warn("Attribute instance is null for: {}", attribute);
                            return;
                        }
                        attributeInstance.addTransientModifier(new AttributeModifier(UUID.randomUUID(), attribute.toString(), value, AttributeModifier.Operation.ADDITION));
                    });
                    break;
                }
            }
        }
    }
}