package org.m1hono.datapack;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MobDefManager extends SimplePreparableReloadListener<Map<ResourceLocation, MobDefData>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FOLDER = "data/mobrenamer";
    private final Map<ResourceLocation, MobDefData> loader = new HashMap<>();

    @Override
    protected @NotNull Map<ResourceLocation, MobDefData> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        pProfiler.startTick();
        pProfiler.push("Loading mob definitions");

        Map<ResourceLocation, MobDefData> definitions = new HashMap<>();
        LOGGER.info("Searching for mob definitions in folder: {}", FOLDER);
        for (Map.Entry<ResourceLocation, Resource> entry : pResourceManager.listResources(FOLDER, location -> location.getPath().endsWith(".json")).entrySet()) {
            ResourceLocation location = entry.getKey();
            LOGGER.info("Found resource: {}", location);
            try (BufferedReader reader = entry.getValue().openAsReader()) {
                JsonElement jsonElement = JsonParser.parseReader(reader);
                MobDefData.CODEC.parse(JsonOps.INSTANCE, jsonElement)
                        .resultOrPartial(error -> LOGGER.warn("Failed to parse mob definition {}: {}", location, error))
                        .ifPresent(mobDefData -> {
                            definitions.put(location, mobDefData);
                            LOGGER.info("Successfully loaded mob definition from {}", location);
                        });
            } catch (Exception e) {
                LOGGER.warn("Failed to read mob definition {}: {}", location, e.getMessage());
            }
        }

        LOGGER.info("Loaded {} mob definitions", definitions.size());
        pProfiler.pop();
        pProfiler.endTick();
        return definitions;
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, MobDefData> mobDefData, @NotNull ResourceManager pResourceManager, @NotNull ProfilerFiller pProfiler) {
        loader.clear();
        loader.putAll(mobDefData);
        LOGGER.info("Applied {} mob definitions", loader.size());
    }

    public MobDefData getMobDef(ResourceLocation location) {
        return loader.get(location);
    }

    public Map<ResourceLocation, MobDefData> getAllMobDefs() {
        return Collections.unmodifiableMap(loader);
    }
}