package moe.gensoukyo.mcgproject.core;


import moe.gensoukyo.mcgproject.common.feature.backpack.BackpackCore;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

import java.io.File;

/**
 * @author SQwatermark
 * @date 2020/2/14
 */
@Mod(modid = MCGProject.ID, name = MCGProject.NAME, version = MCGProject.VERSION)
public class MCGProject {

    @Mod.Instance(MCGProject.ID)
    public static MCGProject INSTANCE;

    public static final String ID = "mcgproject";
    public static final String NAME = "MCGProject";
    public static final String VERSION = "1.1.0";

    public static Logger logger;
    public static File modConfigDi;

    public static final String[] CODERS = {"SQwatermark", "drzzm32"};
    public static final String[] ARTISTS = {"A1181899594", "河豚骨拉面", "MCG旧版本画师"};

    @SidedProxy(clientSide = "moe.gensoukyo.mcgproject.core.ClientProxy",
            serverSide = "moe.gensoukyo.mcgproject.core.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new BackpackCore.BackpackCommand());
        event.registerServerCommand(new BackpackCore.BackpackManageCommand());
    }

    @Mod.EventHandler
    @SideOnly(Side.CLIENT)
    public void loadComplete(FMLLoadCompleteEvent event)
    {
        //设置窗口标题（加载完成时）
        Display.setTitle("Minecraft幻想乡1.12.2");
    }

}