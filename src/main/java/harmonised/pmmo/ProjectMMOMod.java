package harmonised.pmmo;

import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import harmonised.pmmo.commands.PmmoCommand;
import harmonised.pmmo.config.AutoValues;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.events.ChunkDataHandler;
import harmonised.pmmo.events.RegisterHandler;
import harmonised.pmmo.events.WorldTickHandler;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.proxy.ClientHandler;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;

import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

@Mod( Reference.MOD_ID )
public class ProjectMMOMod
{
    private static String PROTOCOL_VERSION = "1";
    public static boolean serverStarted = false, jeiLoaded = false, tinkersLoaded = false;
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
        if( ModList.get().isLoaded( "ftbquests" ) )
        {
            FMLJavaModLoadingContext.get().getModEventBus().addGenericListener( TaskType.class, RegisterHandler::handleFTBQRegistryTaskType );
            FMLJavaModLoadingContext.get().getModEventBus().addGenericListener( RewardType.class, RegisterHandler::handleFTBQRegistryRewardType );
        }
        jeiLoaded = ModList.get().isLoaded( "jei" );
        tinkersLoaded = ModList.get().isLoaded( "tconstruct" );

        MinecraftForge.EVENT_BUS.addListener( this::serverAboutToStart );
        MinecraftForge.EVENT_BUS.addListener( this::serverStart );
        MinecraftForge.EVENT_BUS.addListener( this::serverStarted );
        MinecraftForge.EVENT_BUS.addListener( this::registerCommands );

//        DistExecutor.runWhenOn(Dist.DEDICATED_SERVER, () -> Requirements::init );

        Config.init();
    }

    private void modsLoading( FMLCommonSetupEvent event )
    {
        XP.initValues();

        NetworkHandler.registerPackets();
        MinecraftForge.EVENT_BUS.register( harmonised.pmmo.events.EventHandler.class );
        MinecraftForge.EVENT_BUS.register( harmonised.pmmo.skills.AttributeHandler.class );
    }

    private void clientLoading( FMLClientSetupEvent event )
    {
        ClientHandler.init();
    }

    private void serverAboutToStart( FMLServerAboutToStartEvent event )
    {
        serverStarted = false;
//        ModList.get().isLoaded( "ftbquests" );
        JsonConfig.init();
        if( Config.forgeConfig.autoGenerateValuesEnabled.get() )
            AutoValues.setAutoValues();
        ChunkDataHandler.init();

        Config.initServer();
        WorldTickHandler.refreshVein();
    }

    private void serverStarted( FMLServerStartedEvent event )
    {
        serverStarted = true;
    }

    private void registerCommands( RegisterCommandsEvent event )
    {
        PmmoCommand.register( event.getDispatcher() );
    }

    private void serverStart( FMLServerStartingEvent event )
    {
        PmmoSavedData.init( event.getServer() );
        if( Config.forgeConfig.craftReqEnabled.get() )
            event.getServer().getGameRules().get( GameRules.DO_LIMITED_CRAFTING ).set(true, event.getServer() );
    }
}
