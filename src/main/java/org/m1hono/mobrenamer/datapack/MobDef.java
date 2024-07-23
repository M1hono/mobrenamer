package org.m1hono.mobrenamer.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record MobDef(
        ResourceLocation type,
        boolean alwaysNamed,
        List<NameConfig> names
) {

    public static final Codec<MobDef> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("type").forGetter(MobDef::type),
            Codec.BOOL.fieldOf("always_named").forGetter(MobDef::alwaysNamed),
            NameConfig.CODEC.listOf().fieldOf("names").forGetter(MobDef::names)
    ).apply(instance, MobDef::new));
}