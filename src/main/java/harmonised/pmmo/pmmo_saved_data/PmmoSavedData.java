package harmonised.pmmo.pmmo_saved_data;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.events.PlayerConnectedHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.LogHandler;
import harmonised.pmmo.util.NBTHelper;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class PmmoSavedData extends WorldSavedData
{
    public static MinecraftServer server;
    private static String NAME = Reference.MOD_ID;
    private Map<UUID, Map<Skill, Double>> xp = new HashMap<>();
    private Map<UUID, Map<Skill, Double>> scheduledXp = new HashMap<>();
    private Map<UUID, Map<String, Double>> abilities = new HashMap<>();
    private Map<UUID, Map<String, Double>> preferences = new HashMap<>();
    private Map<UUID, String> name = new HashMap<>();
    public PmmoSavedData()
    {
        super( NAME );
    }

    @Override
    public void read( CompoundNBT inData )
    {
        CompoundNBT playersTag, playerTag;

        if( inData.contains( "players" ) )
        {
            playersTag = inData.getCompound( "players" );
            for( String playerUuidKey : playersTag.keySet() )
            {
                playerTag = playersTag.getCompound( playerUuidKey );
                if( playerTag.contains( "xp" ) )
                {
                    CompoundNBT xpTag = playerTag.getCompound( "xp" );
                    for( String tag : new HashSet<>( xpTag.keySet() ) )
                    {
                        if( Skill.getInt( tag ) == 0 )
                        {
                            if( Skill.getInt( tag.toLowerCase() ) != 0 )
                                xpTag.put( tag.toLowerCase(), xpTag.get(tag) );

                            if( tag.toLowerCase().equals( "repairing" ) )
                                xpTag.put( "smithing", xpTag.get(tag) );

                            LogHandler.LOGGER.info( "REMOVING INVALID SKILL " + tag + " FROM PLAYER " + playerUuidKey );
                            xpTag.remove( tag );
                        }
                    }
                }

                if( playerTag.contains( "name" ) )
                    name.put( UUID.fromString( playerUuidKey ), playerTag.getString( "name" ) );

                xp = NBTHelper.nbtToMapUuidSkill( NBTHelper.extractNbtPlayersIndividualTagsFromPlayersTag( playersTag, "xp" ) );
                scheduledXp = NBTHelper.nbtToMapUuidSkill( NBTHelper.extractNbtPlayersIndividualTagsFromPlayersTag( playersTag, "scheduledXp" ) );
                abilities = NBTHelper.nbtToMapUuidString( NBTHelper.extractNbtPlayersIndividualTagsFromPlayersTag( playersTag, "abilities" ) );
                preferences = NBTHelper.nbtToMapUuidString( NBTHelper.extractNbtPlayersIndividualTagsFromPlayersTag( playersTag, "preferences" ) );
            }
        }
    }

    @Override
    public CompoundNBT write( CompoundNBT outData )
    {
        CompoundNBT playersTag = new CompoundNBT();

        for( Map.Entry<UUID, Map<Skill, Double>> entry : xp.entrySet() )
        {
            Map<String, CompoundNBT> playerMap = new HashMap<>();

            playerMap.put( "xp", NBTHelper.mapSkillToNbt( xp.get( entry.getKey() ) ) );
            playerMap.put( "scheduledXp", NBTHelper.mapSkillToNbt( scheduledXp.get( entry.getKey() ) ) );
            playerMap.put( "abilities", NBTHelper.mapStringToNbt( abilities.get( entry.getKey() ) ) );
            playerMap.put( "preferences", NBTHelper.mapStringToNbt( preferences.get( entry.getKey() ) ) );

            CompoundNBT playerTag = NBTHelper.mapStringNbtToNbt( playerMap );
            playerTag.putString( "name", name.get( entry.getKey() ) );

            playersTag.put( entry.getKey().toString(), playerTag );
        }
        outData.put( "players", playersTag );

        return outData;
    }

    public Map<Skill, Double> getXpMap( UUID uuid )
    {
        if( !xp.containsKey( uuid ) )
            xp.put( uuid, new HashMap<>() );
        return xp.get( uuid );
    }

    public Map<Skill, Double> getScheduledXpMap( UUID uuid )
    {
        if( !scheduledXp.containsKey( uuid ) )
            scheduledXp.put( uuid, new HashMap<>() );
        return scheduledXp.get( uuid );
    }

    public Map<String, Double> getAbilitiesMap( UUID uuid )
    {
        if( !abilities.containsKey( uuid ) )
            abilities.put( uuid, new HashMap<>() );
        return abilities.get( uuid );
    }

    public Map<String, Double> getPreferencesMap( UUID uuid )
    {
        if( !preferences.containsKey( uuid ) )
            preferences.put( uuid, new HashMap<>() );
        return preferences.get( uuid );    }

    public double getXp( Skill skill, UUID uuid )
    {
        if( skill.equals( Skill.INVALID_SKILL ) )
        {
            LogHandler.LOGGER.error( "Invalid Skill at getXp" );
            return -1;
        }

        return xp.getOrDefault( uuid, new HashMap<>() ).getOrDefault( skill, 0D );
    }

    public int getLevel( Skill skill, UUID uuid )
    {
        return XP.levelAtXp( getXp( skill, uuid ) );
    }

    public double getLevelDecimal( Skill skill, UUID uuid )
    {
        return XP.levelAtXpDecimal( getXp( skill, uuid ) );
    }

    public boolean setXp( Skill skill, UUID uuid, double amount )
    {
        if( !skill.equals( Skill.INVALID_SKILL ) )
        {
            double maxXp = Config.getConfig( "maxXp" );

            if( amount > maxXp )
                amount = maxXp;

            if( amount < 0 )
                amount = 0;

            if( !xp.containsKey( uuid ) )
                xp.put( uuid, new HashMap<>() );
            xp.get( uuid ).put( skill, amount );
            setDirty( true );
            return true;
        }
        else
        {
            LogHandler.LOGGER.error( "Invalid Skill at method setXp, amount: " + amount );
            return false;
        }
    }

    public boolean addXp( Skill skill, UUID uuid, double amount )
    {
        if( !skill.equals( Skill.INVALID_SKILL ) )
        {
            setXp( skill, uuid, getXp( skill, uuid ) + amount );
            setDirty( true );
            return true;
        }
        else
        {
            LogHandler.LOGGER.error( "Invalid Skill at method addXp, amount: " + amount );
            return false;
        }
    }

    public void scheduleXp( Skill skill, UUID uuid, double amount, String sourceName )
    {
        if( !skill.equals( Skill.INVALID_SKILL ) )
        {
            Map<Skill, Double> scheduledXpMap = getScheduledXpMap( uuid );
            if( !scheduledXpMap.containsKey( skill ) )
                scheduledXpMap.put( skill, amount );
            else
                scheduledXpMap.put( skill, amount + scheduledXpMap.get( skill ) );
            LogHandler.LOGGER.debug( "Scheduled " + amount + "xp for: " + sourceName + ", to: " + getName( uuid ) );
            setDirty( true );
        }
        else
            LogHandler.LOGGER.error( "Invalid Skill at method addXp, amount: " + amount );
    }

    public void removeScheduledXpUuid( UUID uuid )
    {
        scheduledXp.remove( uuid );
    }

    public void setName( String name, UUID uuid )
    {
        this.name.put( uuid, name );
    }

    public String getName( UUID uuid )
    {
        return name.getOrDefault( uuid, "Nameless Warning" );
    }

    public static PmmoSavedData get()
    {
        return server.getWorld( DimensionType.OVERWORLD ).getSavedData().getOrCreate( PmmoSavedData::new, NAME );
    }

    public static PmmoSavedData get( PlayerEntity player )
    {
        if( player.getServer() == null )
            LogHandler.LOGGER.error( "FATAL PMMO ERROR: SERVER IS NULL. Could not get PmmoSavedData" );

        return player.getServer().getWorld( DimensionType.OVERWORLD ).getSavedData().getOrCreate( PmmoSavedData::new, NAME );
    }
}
