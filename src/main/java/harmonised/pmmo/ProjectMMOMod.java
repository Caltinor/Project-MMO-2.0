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
import harmonised.pmmo.network.WebHandler;
import harmonised.pmmo.proxy.ClientHandler;
import harmonised.pmmo.util.Util;
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
import net.minecraftforge.fml.loading.JarVersionLookupHandler;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod( Reference.MOD_ID )
public class ProjectMMOMod
{
    public static final Logger LOGGER = LogManager.getLogger();

    private static String PROTOCOL_VERSION = "1";
    public static boolean serverStarted = false, jeiLoaded = false, tinkersLoaded = false, dynamicTreesLoaded = false;
    public static SimpleChannel HANDLER = NetworkRegistry.ChannelBuilder
            .named( new ResourceLocation( Reference.MOD_ID, "main_channel" ) )
            .clientAcceptedVersions( PROTOCOL_VERSION::equals )
            .serverAcceptedVersions( PROTOCOL_VERSION::equals )
            .networkProtocolVersion( () -> PROTOCOL_VERSION )
            .simpleChannel();


    public ProjectMMOMod()
    {
        WebHandler.updateInfo();
        FMLJavaModLoadingContext.get().getModEventBus().addListener( this::modsLoading );
        FMLJavaModLoadingContext.get().getModEventBus().addListener( this::clientLoading );
        if( ModList.get().isLoaded( "ftbquests" ) )
        {
            FMLJavaModLoadingContext.get().getModEventBus().addGenericListener( TaskType.class, RegisterHandler::handleFTBQRegistryTaskType );
            FMLJavaModLoadingContext.get().getModEventBus().addGenericListener( RewardType.class, RegisterHandler::handleFTBQRegistryRewardType );
        }
        jeiLoaded = ModList.get().isLoaded( "jei" );
        tinkersLoaded = ModList.get().isLoaded( "tconstruct" );
        dynamicTreesLoaded = ModList.get().isLoaded( "dynamictrees" );

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
        ChunkDataHandler.init();

        Config.initServer();
        WorldTickHandler.refreshVein();
    }

    private void serverStarted( FMLServerStartedEvent event )
    {
        if( Config.forgeConfig.autoGenerateValuesEnabled.get() )
            AutoValues.setAutoValues();
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

    public static String getCurrentVersion()
    {
        if( Util.isProduction() )
        {
            String currentVersion = ProjectMMOMod.class.getPackage().getImplementationVersion();
            int dashIndex = currentVersion.indexOf( '-' );
            if( dashIndex != -1 )
                currentVersion = currentVersion.substring( dashIndex + 1 );
            return currentVersion;
        }
        else
            return "3.55";
    }

    public static boolean isVersionBehind()
    {
        String latestVersionString = WebHandler.getLatestVersion();
        if( latestVersionString == null )
            return false;   //Offline
        String currentVersionString = getCurrentVersion();

        String[] latestPmmoVersion = latestVersionString.split( "[.]" );
        String[] currentPmmoVersion = currentVersionString.split( "[.]" );

        LOGGER.info( "Project MMO version: \"" + currentVersionString + "\" Latest: \"" + latestVersionString + "\"" );

        for( int i = 0; i < latestPmmoVersion.length; i++ )
        {
            if( currentPmmoVersion.length <= i )
                return true;
            try
            {
                int latestNumber = Integer.parseInt( latestPmmoVersion[ i ] );
                int givenNumber = Integer.parseInt( currentPmmoVersion[ i ] );
                if( givenNumber > latestNumber )
                    return false;
                if( givenNumber < latestNumber )
                    return true;
            }
            catch( Exception e )
            {
                System.out.println( "Error parsing latest/given version number" );
                return false;
            }
        }
        return false;
    }
}
