package harmonised.pmmo;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.events.ChunkDataHandler;
import harmonised.pmmo.events.ServerStoppingHandler;
import harmonised.pmmo.events.WorldTickHandler;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.proxy.ClientHandler;
import harmonised.pmmo.proxy.CommonProxy;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;

import net.minecraft.world.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.libraries.ModList;

@Mod( modid = Reference.MOD_ID )
public class ProjectMMOMod
{
    public ProjectMMOMod()
    {
//        FMLJavaModLoadingContext.get().getModEventBus().addListener( this::modsLoading );
//        FMLJavaModLoadingContext.get().getModEventBus().addListener( this::clientLoading );
//        if( ModList.get().isLoaded( "ftbquests" ) )
//            FMLJavaModLoadingContext.get().getModEventBus().addGenericListener( TaskType.class, RegisterHandler::handleFTBQRegistry );
//        MinecraftForge.EVENT_BUS.addListener( this::serverAboutToStart );
//        MinecraftForge.EVENT_BUS.addListener( this::serverStart );

//        DistExecutor.runWhenOn(Dist.DEDICATED_SERVER, () -> Requirements::init );

//        PmmoCommand.init();
        //COUT
        Config.init();
    }

    @SidedProxy( clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.COMMON_PROXY_CLASS )
    public static CommonProxy proxy;

    @Mod.EventHandler
    public static void postInit( FMLPostInitializationEvent event )
    {
        proxy.postInit( event );
    }

    @Mod.EventHandler
    public static void serverStart( FMLServerStartingEvent event )
    {
        if( Config.forgeConfig.autoGenerateValuesEnabled.get() )
            JsonConfig.setAutoValues();
        ChunkDataHandler.init();

        PmmoSavedData.init( event.getServer() );
        Config.initServer();
//        PmmoCommand.register( event.getCommandDispatcher() );
        WorldTickHandler.refreshVein();
        AttributeHandler.init();
//        if( Config.forgeConfig.craftReqEnabled.get() )
//            event.getServer().getGameRules().get( GameRules.DO_LIMITED_CRAFTING ).set(true, event.getServer() );

    }

    @Mod.EventHandler
    public static void serverStopping( FMLServerStoppingEvent event )
    {
        ServerStoppingHandler.handleServerStop( event );
    }

    @Mod.EventHandler
    public static void preInit( FMLPreInitializationEvent event )
    {
        NetworkHandler.init();
    }

    @Mod.EventHandler
    public static void init( FMLInitializationEvent event )
    {
        XP.initValues();

//        PmmoStats.registerStats();
        NetworkHandler.init();
        MinecraftForge.EVENT_BUS.register( harmonised.pmmo.events.EventHandler.class );
        MinecraftForge.EVENT_BUS.register( harmonised.pmmo.skills.AttributeHandler.class );
        JsonConfig.init();
    }
}
