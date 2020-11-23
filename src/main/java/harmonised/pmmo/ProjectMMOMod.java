package harmonised.pmmo;

import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.events.ChunkDataHandler;
import harmonised.pmmo.events.ServerStoppingHandler;
import harmonised.pmmo.events.WorldTickHandler;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.proxy.ClientProxy;
import harmonised.pmmo.proxy.CommonProxy;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.relauncher.Side;

@Mod( modid = Reference.MOD_ID, name = Reference.NAME, version = Reference.VERSION )
public class ProjectMMOMod
{
    public ProjectMMOMod()
    {
//        if( ModList.get().isLoaded( "ftbquests" ) )
//            FMLJavaModLoadingContext.get().getModEventBus().addGenericListener( TaskType.class, RegisterHandler::handleFTBQRegistry );
//        DistExecutor.runWhenOn(Dist.DEDICATED_SERVER, () -> Requirements::init );

//        PmmoCommand.init();
        //COUT
        FConfig.init();
    }

    @SidedProxy( clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.COMMON_PROXY_CLASS )
    public static CommonProxy proxy;

    @Mod.EventHandler
    public static void preInit( FMLPreInitializationEvent event )
    {
        JsonConfig.configFile = event.getSuggestedConfigurationFile();
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

    @Mod.EventHandler
    public static void postInit( FMLPostInitializationEvent event )
    {
        if( event.getSide().equals( Side.CLIENT ) )
            ClientProxy.postInit( event );
    }

    @Mod.EventHandler
    public static void serverStart( FMLServerStartingEvent event )
    {
//        if( FConfig.autoGenerateValuesEnabled )
//            JsonConfig.setAutoValues();
        //COUT AUTO VALUES
        ChunkDataHandler.init();

        PmmoSavedData.init( event.getServer() );
        FConfig.initServer();
//        PmmoCommand.register( event.getCommandDispatcher() );
        WorldTickHandler.refreshVein();
        AttributeHandler.init();
//        if( FConfig.craftReqEnabled.get() )
//            event.getServer().getGameRules().get( GameRules.DO_LIMITED_CRAFTING ).set(true, event.getServer() );

    }

    @Mod.EventHandler
    public static void serverStopping( FMLServerStoppingEvent event )
    {
        ServerStoppingHandler.handleServerStop( event );
    }
}
