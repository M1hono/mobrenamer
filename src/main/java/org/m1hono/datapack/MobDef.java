package org.m1hono.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record MobDef(
        ResourceLocation type,
        Component name,
        Map<ResourceLocation, Double> attributes
) {
    public static final Codec<Component> COMPONENT_CODEC = Codec.STRING.xmap(
            Component.Serializer::fromJson,
            Component.Serializer::toJson
    );
    public static final Codec<MobDef> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("type").forGetter(MobDef::type),
            COMPONENT_CODEC.fieldOf("name").forGetter(MobDef::name),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.DOUBLE).fieldOf("attributes").forGetter(MobDef::attributes)
    ).apply(instance, MobDef::new));
}