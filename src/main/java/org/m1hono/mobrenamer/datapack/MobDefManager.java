package org.m1hono.mobrenamer.datapack;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.util.*;

public class MobDefManager extends SimplePreparableReloadListener<List<MobDef>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FOLDER = "mobrenamer";
    private final Map<ResourceLocation, MobDef> loader = new HashMap<>();

    @Override
    protected @NotNull List<MobDef> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        pProfiler.startTick();
        pProfiler.push("Loading mob definitions");

        List<MobDef> definitions = new ArrayList<>();
        LOGGER.info("Searching for mob definitions in namespace: {}", FOLDER);
        Map<ResourceLocation, Resource> resources = pResourceManager.listResources(FOLDER, location -> location.getPath().endsWith(".json"));
        LOGGER.info("Found {} resources", resources.size());

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation location = entry.getKey();
            LOGGER.info("Processing resource: {}", location);
            try (BufferedReader reader = entry.getValue().openAsReader()) {
                JsonElement jsonElement = JsonParser.parseReader(reader);
                MobDef.CODEC.parse(JsonOps.INSTANCE, jsonElement)
                        .resultOrPartial(error -> LOGGER.warn("Failed to parse mob definition {}: {}", location, error))
                        .ifPresent(mobDef -> {
                            definitions.add(mobDef);
                            LOGGER.info("Successfully loaded mob definition from {}", location);
                        });
            } catch (Exception e) {
                LOGGER.error("Failed to read mob definition {}: {}", location, e.getMessage(), e);
            }
        }

        LOGGER.info("Loaded {} mob definition files", definitions.size());
        pProfiler.pop();
        pProfiler.endTick();
        return definitions;
    }

    @Override
    protected void apply(@NotNull List<MobDef> mobDefs, @NotNull ResourceManager pResourceManager, @NotNull ProfilerFiller pProfiler) {
        loader.clear();
        for (MobDef def : mobDefs) {
            loader.put(def.type(), def);
        }
        LOGGER.info("Applied {} mob definitions", loader.size());
    }

    public MobDef getMobDef(ResourceLocation type) {
        return loader.get(type);
    }

    public Map<ResourceLocation, MobDef> getAllMobDefs() {
        return Collections.unmodifiableMap(loader);
    }
}