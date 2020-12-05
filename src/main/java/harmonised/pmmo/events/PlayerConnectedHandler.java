package harmonised.pmmo.events;

import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class PlayerConnectedHandler
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static List<UUID> lapisPatreons      = new ArrayList<>();
    public static List<UUID> dandelionPatreons  = new ArrayList<>();
    public static List<UUID> ironPatreons       = new ArrayList<>();
    public static Set<UUID> muteList           = new HashSet<>();

    public static void handlePlayerConnected( PlayerEvent.PlayerLoggedInEvent event )
    {
        EntityPlayer player = event.player;
        if( !player.world.isRemote )
        {
            UUID uuid = player.getUniqueID();
            boolean showWelcome = FConfig.showWelcome;
            boolean showPatreonWelcome = FConfig.showPatreonWelcome;

            PmmoSavedData.get().setName( player.getDisplayName().getUnformattedText(), uuid );
            migratePlayerDataToWorldSavedData( player );
            XP.syncPlayer( player );
            awardScheduledXp( uuid );

            if( !muteList.contains( uuid ) )
            {
                if( lapisPatreons.contains( uuid ) )
                {
                    player.getServer().getPlayerList().getPlayers().forEach( (thePlayer) ->
                    {
                        thePlayer.sendStatusMessage( new TextComponentTranslation( "pmmo.lapisPatreonWelcome", thePlayer.getDisplayName().getUnformattedText() ).setStyle( XP.textStyle.get( "cyan" ) ), false );
                    });
                }
                else if( showPatreonWelcome )
                {
                    if( dandelionPatreons.contains( uuid ) )
                        player.sendStatusMessage( new TextComponentTranslation( "pmmo.dandelionPatreonWelcome", player.getDisplayName().getUnformattedText() ).setStyle( XP.textStyle.get( "yellow" ) ), false );
                    else if( ironPatreons.contains( uuid ) )
                        player.sendStatusMessage( new TextComponentTranslation( "pmmo.ironPatreonWelcome", player.getDisplayName().getUnformattedText() ).setStyle( XP.textStyle.get( "grey" ) ), false );
                }

                if( showWelcome )
                    player.sendStatusMessage( new TextComponentTranslation( "pmmo.welcome" ), false );
            }
        }
    }

    private static void migratePlayerDataToWorldSavedData( EntityPlayer player )
    {
        if( player.getEntityData().hasKey( player.PERSISTED_NBT_TAG ) )
        {
            NBTTagCompound pmmoTag = player.getEntityData().getCompoundTag( player.PERSISTED_NBT_TAG );
            NBTTagCompound tag;
            Skill skill;
            UUID uuid = player.getUniqueID();
            Map<String, Double> map;

            LOGGER.info( "Migrating Player " + player.getDisplayName().getUnformattedText() + " Pmmo Data from PlayerData to WorldSavedData" );

            if( pmmoTag.hasKey( "skills" ) )
            {
                tag = pmmoTag.getCompoundTag( "skills" );
                for( String key : tag.getKeySet() )
                {
                    skill = Skill.getSkill( key );
                    if( skill != Skill.INVALID_SKILL )
                    {
                        skill.setXp( uuid, skill.getXp( uuid ) + tag.getDouble( key ) );
                        LOGGER.info( "Adding " + tag.getDouble( key ) + " xp in " + skill.toString() );
                    }
                }
            }

            LOGGER.info( "Migrated Player " + player.getDisplayName().getUnformattedText() + " Done" );
            PmmoSavedData.get().setDirty( true );
        }
    }

    private static void awardScheduledXp( UUID uuid )
    {
        Map<Skill, Double> scheduledXp = PmmoSavedData.get().getScheduledXpMap( uuid );

        if( scheduledXp.size() > 0 )
            LOGGER.info( "Awarding Scheduled Xp for: " + PmmoSavedData.get().getName( uuid ) );

        for( Map.Entry<Skill, Double> entry : scheduledXp.entrySet() )
        {
            entry.getKey().addXp( uuid, entry.getValue(), "scheduledXp", false, false );
            LOGGER.info( "+" + entry.getValue() + " in " + entry.getKey().toString() );
        }
        PmmoSavedData.get().removeScheduledXpUuid( uuid );
    }
}
