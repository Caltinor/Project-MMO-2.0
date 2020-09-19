package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.LogHandler;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerConnectedHandler
{
    public static Set<UUID> lapisPatreons = new HashSet<>();
    public static Set<UUID> dandelionPatreons = new HashSet<>();
    public static Set<UUID> ironPatreons = new HashSet<>();

    public static void handlePlayerConnected( PlayerEvent.PlayerLoggedInEvent event )
    {
        PlayerEntity player = event.getPlayer();
        if( !player.world.isRemote() )
        {
            UUID uuid = player.getUniqueID();
            boolean showWelcome = Config.forgeConfig.showWelcome.get();
            boolean showPatreonWelcome = Config.forgeConfig.showPatreonWelcome.get();

            PmmoSavedData.get( player ).setName( player.getDisplayName().getString(), uuid );
            migratePlayerDataToWorldSavedData( player );
            XP.syncPlayer( player );
            awardScheduledXp( uuid );

            if( lapisPatreons.contains( uuid ) )
            {
                player.getServer().getPlayerList().getPlayers().forEach( (thePlayer) ->
                {
                    thePlayer.sendStatusMessage( new TranslationTextComponent( "pmmo.lapisPatreonWelcome", thePlayer.getDisplayName().getString() ).setStyle( new Style().setColor( TextFormatting.BLUE ) ), false );
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
                player.sendStatusMessage( new TranslationTextComponent( "pmmo.welcome" ), false );
        }
    }

    private static void migratePlayerDataToWorldSavedData( PlayerEntity player )
    {
        if( player.getPersistentData().contains( "pmmo" ) )
        {
            CompoundNBT pmmoTag = player.getPersistentData().getCompound( "pmmo" );
            CompoundNBT tag;
            Skill skill;
            UUID uuid = player.getUniqueID();
            Map<String, Double> map;

            LogHandler.LOGGER.info( "Migrating Player " + player.getDisplayName().getString() + " Pmmo Data from PlayerData to WorldSavedData" );

            if( pmmoTag.contains( "skills" ) )
            {
                tag = pmmoTag.getCompound( "skills" );
                for( String key : tag.keySet() )
                {
                    skill = Skill.getSkill( key );
                    if( skill != Skill.INVALID_SKILL )
                    {
                        skill.setXp( uuid, skill.getXp( uuid ) + tag.getDouble( key ) );
                        LogHandler.LOGGER.info( "Adding " + tag.getDouble( key ) + " xp in " + skill.toString() );
                    }
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

            player.getPersistentData().remove( "pmmo" );
            LogHandler.LOGGER.info( "Migrated Player " + player.getDisplayName().getString() + " Done" );
            PmmoSavedData.get( player ).setDirty( true );
        }
    }

    private static void awardScheduledXp( UUID uuid )
    {
        Map<Skill, Double> scheduledXp = PmmoSavedData.get().getScheduledXpMap( uuid );

        if( scheduledXp.size() > 0 )
            LogHandler.LOGGER.info( "Awarding Scheduled Xp for: " + PmmoSavedData.get().getName( uuid ) );

        for( Map.Entry<Skill, Double> entry : scheduledXp.entrySet() )
        {
            entry.getKey().addXp( uuid, entry.getValue(), "scheduledXp", false, false );
            LogHandler.LOGGER.info( "+" + entry.getValue() + " in " + entry.getKey().toString() );
        }
        PmmoSavedData.get().removeScheduledXpUuid( uuid );
    }
}
