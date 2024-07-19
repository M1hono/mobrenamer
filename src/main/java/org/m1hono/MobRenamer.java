package org.m1hono;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.m1hono.datapack.MobDefManager;
import org.slf4j.Logger;

@Mod(MobRenamer.MODID)
public class MobRenamer {
    public static final String MODID = "data/mobrenamer";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final MobDefManager MOB_DEF_MANAGER = new MobDefManager();
    private static MobRenamer instance;

    public MobRenamer() {
        instance = this;
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::onAddReloadListeners);
    }

    public static MobDefManager getMobDefManager() {
        return MOB_DEF_MANAGER;
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("MobRenamer common setup");
    }

    private void onAddReloadListeners(AddReloadListenerEvent event) {
        LOGGER.info("Adding MobDefManager as a reload listener");
        event.addListener(MOB_DEF_MANAGER);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("MobRenamer server starting");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("MobRenamer client setup");
        }
    }
}