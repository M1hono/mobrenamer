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

public class MobDefManager extends SimplePreparableReloadListener<Map<ResourceLocation, MobDef>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FOLDER = "definitions";
    private final Map<ResourceLocation, MobDef> loader = new HashMap<>();

    @Override
    protected @NotNull Map<ResourceLocation, MobDef> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        pProfiler.startTick();
        pProfiler.push("Loading mob definitions");

        Map<ResourceLocation, MobDef> tempLoader = new HashMap<>();
        LOGGER.info("Searching for mob definitions in namespace: {}", FOLDER);
        Map<ResourceLocation, Resource> resources = pResourceManager.listResources(FOLDER, location -> location.getPath().endsWith(".json"));
        LOGGER.info("Found {} resources", resources.size());

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation location = entry.getKey();
            LOGGER.info("Processing resource: {}", location);
            try (BufferedReader reader = entry.getValue().openAsReader()) {
                JsonElement jsonElement = JsonParser.parseReader(reader);
                MobDefData.CODEC.parse(JsonOps.INSTANCE, jsonElement)
                        .resultOrPartial(error -> LOGGER.error("Failed to parse mob definitions {}: {}", location, error))
                        .ifPresent(mobDefData -> {
                            for (Map.Entry<ResourceLocation, MobDef> defEntry : mobDefData.mobDefinitions().entrySet()) {
                                ResourceLocation type = defEntry.getKey();
                                MobDef def = defEntry.getValue();
                                if (tempLoader.containsKey(type)) {
                                    tempLoader.computeIfPresent(type, (k, existingDef) -> existingDef.merge(def));
                                } else {
                                    tempLoader.put(type, def);
                                }
                                LOGGER.info("Successfully loaded mob definition for type: {}", type);
                            }
                        });
            } catch (Exception e) {
                LOGGER.error("Failed to read mob definitions {}: {}", location, e.getMessage(), e);
            }
        }

        // Sort NameConfigs by priority for each MobDef
        for (MobDef mobDef : tempLoader.values()) {
            mobDef.names().sort(Comparator.comparingInt(NameConfig::priority).reversed());
        }
        LOGGER.info("Loaded {} mob definition.", tempLoader);
        LOGGER.info("Loaded {} mob definitions", tempLoader.size());
        pProfiler.pop();
        pProfiler.endTick();
        return tempLoader;
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, MobDef> mobDefs, @NotNull ResourceManager pResourceManager, @NotNull ProfilerFiller pProfiler) {
        loader.clear();
        loader.putAll(mobDefs);
        LOGGER.info("Applied {} mob definitions", loader.size());
    }

    public MobDef getMobDef(ResourceLocation type) {
        return loader.get(type);
    }

    public Map<ResourceLocation, MobDef> getAllMobDefs() {
        return Collections.unmodifiableMap(loader);
    }
}