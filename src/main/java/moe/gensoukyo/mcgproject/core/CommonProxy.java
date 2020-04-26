package moe.gensoukyo.mcgproject.core;

import moe.gensoukyo.mcgproject.common.entity.EntityKaginawa;
import moe.gensoukyo.mcgproject.common.feature.BetterSign;
import moe.gensoukyo.mcgproject.common.feature.CustomNPCsHook;
import moe.gensoukyo.mcgproject.common.feature.MoreBannerPatterns;
import moe.gensoukyo.mcgproject.common.feature.NoChicken;
import moe.gensoukyo.mcgproject.common.feature.futuremc.FMBlock;
import moe.gensoukyo.mcgproject.common.feature.musicplayer.MP3Player;
import moe.gensoukyo.mcgproject.common.feature.musicplayer.MusicThread;
import moe.gensoukyo.mcgproject.common.feature.musicplayer.StreamStopper;
import moe.gensoukyo.mcgproject.common.init.*;
import moe.gensoukyo.mcgproject.common.network.NetworkWrapper;
import moe.gensoukyo.mcgproject.common.util.EntityPool;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class CommonProxy {

    public WeakHashMap<Entity, EntityKaginawa> kagimap = new WeakHashMap<>();
    public MusicThread musicThread = new MusicThread();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(ModItem.instance());
        MinecraftForge.EVENT_BUS.register(ModBlock.instance());
        MinecraftForge.EVENT_BUS.register(FMBlock.instance());
        MinecraftForge.EVENT_BUS.register(ModArmor.instance());
        MinecraftForge.EVENT_BUS.register(ModEntity.instance());
        MinecraftForge.EVENT_BUS.register(ModTileEntity.instance());
        MinecraftForge.EVENT_BUS.register(BetterSign.instance());
        MinecraftForge.EVENT_BUS.register(EntityPool.instance());
        MinecraftForge.EVENT_BUS.register(NoChicken.instance());
        MinecraftForge.EVENT_BUS.register(StreamStopper.instance());
        new NetworkWrapper(event);
        if (Loader.isModLoaded("customnpcs")) {
            MCGProject.logger.info("Register CustomNPCs Hook");
            MinecraftForge.EVENT_BUS.register(CustomNPCsHook.instance());
        }
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        //添加旗帜图案
        new MoreBannerPatterns();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }

    public float getJukeboxVolume() {
        return 0;
    }

}
