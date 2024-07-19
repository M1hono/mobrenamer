package org.m1hono.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public record MobDefData(Map<ResourceLocation, MobDef> mobDefinitions) {
    public static final Codec<MobDefData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(ResourceLocation.CODEC, MobDef.CODEC).fieldOf("mob_definitions").forGetter(MobDefData::mobDefinitions)
    ).apply(instance, MobDefData::new));
}