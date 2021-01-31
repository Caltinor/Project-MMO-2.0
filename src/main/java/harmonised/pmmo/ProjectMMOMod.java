package harmonised.pmmo;

import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import com.feed_the_beast.ftbquests.quest.task.TaskType;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import harmonised.pmmo.commands.PmmoCommand;
import harmonised.pmmo.config.AutoValues;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.events.ChunkDataHandler;
import harmonised.pmmo.events.RegisterHandler;
import harmonised.pmmo.events.WorldTickHandler;
import harmonised.pmmo.ftb_quests.SkillTask;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.proxy.ClientHandler;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.GameRules;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoader;
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
        if( ModList.get().isLoaded( "ftbquests" ) )
        {
            FMLJavaModLoadingContext.get().getModEventBus().addGenericListener( TaskType.class, RegisterHandler::handleFTBQRegistryTaskType );
            FMLJavaModLoadingContext.get().getModEventBus().addGenericListener( RewardType.class, RegisterHandler::handleFTBQRegistryRewardType );
        }
        MinecraftForge.EVENT_BUS.addListener( this::serverAboutToStart );
        MinecraftForge.EVENT_BUS.addListener( this::serverStart );

        Config.init();
    }

    private void modsLoading( FMLCommonSetupEvent event )
    {
        XP.initValues();

//        PmmoStats.registerStats();
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
        if( Config.forgeConfig.autoGenerateValuesEnabled.get() )
            AutoValues.setAutoValues();
        ChunkDataHandler.init();
    }

    private void serverStart( FMLServerStartingEvent event )
    {
        PmmoSavedData.init( event.getServer() );
        Config.initServer();
        PmmoCommand.register( event.getCommandDispatcher() );
        WorldTickHandler.refreshVein();
        if( Config.forgeConfig.craftReqEnabled.get() )
            event.getServer().getGameRules().get( GameRules.DO_LIMITED_CRAFTING ).set(true, event.getServer() );
    }
}
