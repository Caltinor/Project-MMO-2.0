package harmonised.pmmo;

import harmonised.pmmo.commands.PmmoCommand;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.events.ChunkDataHandler;
import harmonised.pmmo.events.WorldTickHandler;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.proxy.ClientHandler;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;

import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

@Mod( Reference.MOD_ID )
public class ProjectMMOMod
{
    private static String PROTOCOL_VERSION = "1";
    public static SimpleChannel HANDLER = NetworkRegistry.ChannelBuilder
            .named( new ResourceLocation( Reference.MOD_ID, "main_channel" ) )
            .clientAcceptedVersions( PROTOCOL_VERSION::equals )
            .serverAcceptedVersions( PROTOCOL_VERSION::equals )
            .networkProtocolVersion( () -> PROTOCOL_VERSION )
            .simpleChannel();


    public ProjectMMOMod()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener( this::modsLoading );
        FMLJavaModLoadingContext.get().getModEventBus().addListener( this::clientLoading );
        MinecraftForge.EVENT_BUS.addListener( this::serverAboutToStart );
        MinecraftForge.EVENT_BUS.addListener( this::registerCommands );
        MinecraftForge.EVENT_BUS.addListener( this::serverStart );

//        DistExecutor.runWhenOn(Dist.DEDICATED_SERVER, () -> Requirements::init );

        PmmoCommand.init();
        Config.init();
    }

    private void modsLoading( FMLCommonSetupEvent event )
    {
        XP.initValues();

        NetworkHandler.registerPackets();
        MinecraftForge.EVENT_BUS.register( harmonised.pmmo.events.EventHandler.class );
        MinecraftForge.EVENT_BUS.register( harmonised.pmmo.skills.AttributeHandler.class );
        JsonConfig.init();
    }

    private void clientLoading( FMLClientSetupEvent event )
    {
        ClientHandler.init();
    }

    private void serverAboutToStart( FMLServerAboutToStartEvent event )
    {
        ChunkDataHandler.init();
    }

    private void registerCommands( RegisterCommandsEvent event )
    {
        PmmoCommand.register( event.getDispatcher() );
    }

    private void serverStart( FMLServerStartingEvent event )
    {
        Config.initServer();
        WorldTickHandler.refreshVein();
        if( Config.forgeConfig.craftReqEnabled.get() )
            event.getServer().getGameRules().get( GameRules.DO_LIMITED_CRAFTING ).set(true, event.getServer() );
    }
}
