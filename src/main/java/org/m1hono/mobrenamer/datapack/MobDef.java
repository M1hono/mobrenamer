package org.m1hono.mobrenamer.datapack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record MobDef(
        ResourceLocation type,
        boolean alwaysNamed,
        List<NameConfig> names
) {
    public MobDef {
        names = new ArrayList<>(names);
        names.sort(Comparator.comparingInt(NameConfig::priority).reversed());
    }

    public static final Codec<MobDef> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("type").forGetter(MobDef::type),
            Codec.BOOL.fieldOf("always_named").forGetter(MobDef::alwaysNamed),
            NameConfig.CODEC.listOf().fieldOf("names").forGetter(MobDef::names)
    ).apply(instance, MobDef::new));

    public MobDef merge(MobDef other) {
        boolean mergedAlwaysNamed = this.alwaysNamed || other.alwaysNamed;
        List<NameConfig> mergedNames = new ArrayList<>(this.names);
        mergedNames.addAll(other.names);
        mergedNames.sort(Comparator.comparingInt(NameConfig::priority).reversed());
        return new MobDef(this.type, mergedAlwaysNamed, mergedNames);
    }
}