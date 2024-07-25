package org.m1hono.mobrenamer.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.MobSpawnType;

import java.util.List;
import java.util.Map;

public record NameConfig(
        Component name,
        double probability,
        List<ResourceLocation> structures,
        List<ResourceLocation> biomes,
        List<ResourceLocation> dimensions,
        Map<ResourceLocation, Double> attributes,
        List<MobSpawnType> spawnTypes,
        int priority
) {
    public static final Codec<Component> COMPONENT_CODEC = ExtraCodecs.JSON.xmap(
            Component.Serializer::fromJson,
            Component.Serializer::toJsonTree
    );

    private static final Codec<MobSpawnType> SPAWN_TYPE_CODEC = Codec.STRING.comapFlatMap(
            string -> {
                try {
                    return DataResult.success(MobSpawnType.valueOf(string.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    return DataResult.error(() -> "Unknown spawn type: " + string);
                }
            },
            Enum::name
    );

    public static final Codec<NameConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            COMPONENT_CODEC.fieldOf("name").forGetter(NameConfig::name),
            Codec.DOUBLE.fieldOf("probability").forGetter(NameConfig::probability),
            ResourceLocation.CODEC.listOf().optionalFieldOf("structures", List.of()).forGetter(NameConfig::structures),
            ResourceLocation.CODEC.listOf().optionalFieldOf("biomes", List.of()).forGetter(NameConfig::biomes),
            ResourceLocation.CODEC.listOf().optionalFieldOf("dimensions", List.of()).forGetter(NameConfig::dimensions),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.DOUBLE).optionalFieldOf("attributes", Map.of()).forGetter(NameConfig::attributes),
            SPAWN_TYPE_CODEC.listOf().optionalFieldOf("spawnTypes", List.of()).forGetter(NameConfig::spawnTypes),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(NameConfig::priority)
    ).apply(instance, NameConfig::new));
}