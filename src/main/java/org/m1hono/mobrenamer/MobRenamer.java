package org.m1hono.mobrenamer;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.m1hono.mobrenamer.datapack.MobDefManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MobRenamer.MODID)
public class MobRenamer {
    public static final String MODID = "mobrenamer";
    private static final Logger LOGGER = LoggerFactory.getLogger(MobRenamer.class);
    public MobRenamer() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
    }
}