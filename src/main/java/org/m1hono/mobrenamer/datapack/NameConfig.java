package org.m1hono.mobrenamer.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public record NameConfig(
        Component name,
        double probability,
        List<ResourceLocation> structures,
        List<ResourceLocation> biomes,
        List<ResourceLocation> dimensions,
        Map<ResourceLocation, Double> attributes
) {
    public static final Codec<Component> COMPONENT_CODEC = Codec.STRING.xmap(
            Component.Serializer::fromJson,
            Component.Serializer::toJson
    );

    public static final Codec<NameConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            COMPONENT_CODEC.fieldOf("name").forGetter(NameConfig::name),
            Codec.DOUBLE.fieldOf("probability").forGetter(NameConfig::probability),
            ResourceLocation.CODEC.listOf().optionalFieldOf("structures", List.of()).forGetter(NameConfig::structures),
            ResourceLocation.CODEC.listOf().optionalFieldOf("biomes", List.of()).forGetter(NameConfig::biomes),
            ResourceLocation.CODEC.listOf().optionalFieldOf("dimensions", List.of()).forGetter(NameConfig::dimensions),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.DOUBLE).fieldOf("attributes").forGetter(NameConfig::attributes)
    ).apply(instance, NameConfig::new));
}