package harmonised.pmmo.events;

import harmonised.pmmo.ProjectMMOMod;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.network.WebHandler;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.proxy.ClientHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class PlayerConnectedHandler
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static List<UUID> lapisPatreons      = new ArrayList<UUID>()
    {{
        add( UUID.fromString( "e4c7e475-c1ff-4f94-956c-ac5be02ce04a" ) );	//LUCIFER
    }};
    public static List<UUID> dandelionPatreons  = new ArrayList<UUID>()
    {{
        add( UUID.fromString( "8eb0578d-c113-49d3-abf6-a6d36f6d1116" ) );	//TYRIUS
        add( UUID.fromString( "554b53b8-d0fa-409e-ab87-2a34bf83e506" ) );	//JOERKIG
        add( UUID.fromString( "2ea5efa1-756b-4c9e-9605-7f53830d6cfa" ) );	//DIDIS
        add( UUID.fromString( "21bb554a-f339-48ef-80f7-9a5083172892" ) );	//JUDICIUS
    }};
    public static List<UUID> ironPatreons       = new ArrayList<UUID>()
    {{
        add( UUID.fromString( "0bc51f06-9906-41ea-9fb4-7e9be169c980" ) );	//STRESSINDICATOR
        add( UUID.fromString( "5bfdb948-7b66-476a-aefe-d45e4778fb2d" ) );	//DADDY_P1G
        add( UUID.fromString( "edafb5eb-9ccb-4121-bef7-e7ffded64ee3" ) );	//LEWDCINA
        add( UUID.fromString( "8d2460f3-c840-4b8e-a2d2-f7d5168cbdeb" ) );	//QSIDED
    }};
    public static Set<UUID> muteList           = new HashSet<UUID>()
    {{
        add( UUID.fromString( "21bb554a-f339-48ef-80f7-9a5083172892" ) );	//Do not greet (specified by Patreon)
    }};

    public static void handlePlayerConnected( PlayerEvent.PlayerLoggedInEvent event )
    {
        PlayerEntity player = event.getPlayer();
        if( !player.world.isRemote() )
        {
            UUID uuid = player.getUniqueID();
            boolean showWelcome = Config.forgeConfig.showWelcome.get();
            boolean showPatreonWelcome = Config.forgeConfig.showPatreonWelcome.get();

            PmmoSavedData.get().setName( player.getDisplayName().getString(), uuid );
            migratePlayerDataToWorldSavedData( player );
            XP.syncPlayer( player );
            awardScheduledXp( uuid );

            if( Config.forgeConfig.warnOutdatedVersion.get() && ProjectMMOMod.isVersionBehind() )
            {
                Style style = XP.getColorStyle( 0xaa3333 ).setUnderlined( true );
                String updateMsg = WebHandler.getLatestMessage();
                if( updateMsg != null )
                    style.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new StringTextComponent( updateMsg ) ) );
                TranslationTextComponent textComp = new TranslationTextComponent( "pmmo.outdatedVersion", WebHandler.getLatestVersion(), ProjectMMOMod.getCurrentVersion() );
                textComp.setStyle( style );
                player.sendStatusMessage( textComp, false );
            }

            if( !muteList.contains( uuid ) )
            {
                if( lapisPatreons.contains( uuid ) )
                {
                    player.getServer().getPlayerList().getPlayers().forEach( (thePlayer) ->
                    {
                        thePlayer.sendStatusMessage( new TranslationTextComponent( "pmmo.lapisPatreonWelcome", thePlayer.getDisplayName().getString() ).setStyle( XP.textStyle.get( "cyan" ) ), false );
                    });
                }
                else if( showPatreonWelcome )
                {
                    if( dandelionPatreons.contains( uuid ) )
                        player.sendStatusMessage( new TranslationTextComponent( "pmmo.dandelionPatreonWelcome", player.getDisplayName().getString() ).setStyle( XP.textStyle.get( "yellow" ) ), false );
                    else if( ironPatreons.contains( uuid ) )
                        player.sendStatusMessage( new TranslationTextComponent( "pmmo.ironPatreonWelcome", player.getDisplayName().getString() ).setStyle( XP.textStyle.get( "grey" ) ), false );
                }

                if( showWelcome )
                    player.sendStatusMessage( new TranslationTextComponent( "pmmo.welcomeText", new TranslationTextComponent( "pmmo.clickMe" ).setStyle( XP.getColorStyle( 0xff00ff ).setUnderlined( true ).setClickEvent( new ClickEvent( ClickEvent.Action.RUN_COMMAND, "/pmmo help" ) ).setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent( "pmmo.openInfo" ) ) ) ) ), false );
            }
        }
        else
            ClientHandler.hiscoreMap = new HashMap<>();
    }

    private static void migratePlayerDataToWorldSavedData( PlayerEntity player )
    {
        if( player.getPersistentData().contains( Reference.MOD_ID ) )
        {
            CompoundNBT pmmoTag = player.getPersistentData().getCompound( Reference.MOD_ID );
            CompoundNBT tag;
            UUID uuid = player.getUniqueID();
            Map<String, Double> map;

            LOGGER.info( "Migrating Player " + player.getDisplayName().getString() + " Pmmo Data from PlayerData to WorldSavedData" );

            if( pmmoTag.contains( "skills" ) )
            {
                tag = pmmoTag.getCompound( "skills" );
                for( String key : tag.keySet() )
                {
                    Skill.setXp( key, uuid, Skill.getXp( key, uuid ) + tag.getDouble( key ) );
                    LOGGER.info( "Adding " + tag.getDouble( key ) + " xp in " + key );
                }
            }

            if( pmmoTag.contains( "preferences" ) )
            {
                tag = pmmoTag.getCompound( "preferences" );
                map = Config.getPreferencesMap( player );
                for( String key : tag.keySet() )
                {
                    map.put( key, tag.getDouble( key ) );
                }
            }

            if( pmmoTag.contains( "abilities" ) )
            {
                tag = pmmoTag.getCompound( "abilities" );
                map = Config.getAbilitiesMap( player );
                for( String key : tag.keySet() )
                {
                    map.put( key, tag.getDouble( key ) );
                }
            }

            player.getPersistentData().remove( Reference.MOD_ID );
            LOGGER.info( "Migrated Player " + player.getDisplayName().getString() + " Done" );
            PmmoSavedData.get().setDirty( true );
        }
    }

    private static void awardScheduledXp( UUID uuid )
    {
        Map<String, Double> scheduledXp = PmmoSavedData.get().getScheduledXpMap( uuid );

        if( scheduledXp.size() > 0 )
            LOGGER.info( "Awarding Scheduled Xp for: " + PmmoSavedData.get().getName( uuid ) );

        for( Map.Entry<String, Double> entry : scheduledXp.entrySet() )
        {
            Skill.addXp( entry.getKey(), uuid, entry.getValue(), "scheduledXp", false, false );
            LOGGER.info( "+" + entry.getValue() + " in " + entry.getKey().toString() );
        }
        PmmoSavedData.get().removeScheduledXpUuid( uuid );
    }
}