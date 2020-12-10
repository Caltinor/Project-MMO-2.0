package harmonised.pmmo.pmmo_saved_data;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.party.Party;
import harmonised.pmmo.party.PartyMemberInfo;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.NBTHelper;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.WorldSavedData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class PmmoSavedData extends WorldSavedData
{
    public static final Logger LOGGER = LogManager.getLogger();

    private static PmmoSavedData pmmoSavedData;
    private static MinecraftServer server;
    private static String NAME = Reference.MOD_ID;
    private Map<UUID, Map<Skill, Double>> xp = new HashMap<>();
    private Map<UUID, Map<Skill, Double>> scheduledXp = new HashMap<>();
    private Map<UUID, Map<String, Double>> abilities = new HashMap<>();
    private Map<UUID, Map<String, Double>> preferences = new HashMap<>();
    private Map<UUID, Map<String, Map<Skill, Double>>> xpBoosts = new HashMap<>();    //playerUUID -> boostUUID -> boostMap
    private Set<Party> parties = new HashSet<>();
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

                            LOGGER.info( "REMOVING INVALID SKILL " + tag + " FROM PLAYER " + playerUuidKey );
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
                xpBoosts = NBTHelper.nbtToMapStringMapUuidSkill( NBTHelper.extractNbtPlayersIndividualTagsFromPlayersTag( playersTag, "xpBoosts" ) );
            }
        }

        if( inData.contains( "parties" ) )
        {
            CompoundNBT partiesTag = inData.getCompound( "parties" );
            CompoundNBT partyTag, membersTag, memberInfoTag;
            Set<PartyMemberInfo> membersInfo;
            PartyMemberInfo memberInfo;

            for( String key : partiesTag.keySet() )
            {
                partyTag = partiesTag.getCompound( key );
                membersTag = partyTag.getCompound( "members" );
                membersInfo = new HashSet<>();

                for( String id : membersTag.keySet() )
                {
                    memberInfoTag = membersTag.getCompound( id );
                    memberInfo = new PartyMemberInfo( UUID.fromString( memberInfoTag.getString( "uuid" ) ), memberInfoTag.getLong( "joinDate" ), memberInfoTag.getDouble( "xpGained" ) );
                    membersInfo.add( memberInfo );
                }

                parties.add( new Party( partyTag.getLong( "creationDate" ), membersInfo ) );
            }
        }
    }

    @Override
    public CompoundNBT write( CompoundNBT outData )
    {
        CompoundNBT playersTag = new CompoundNBT(), partiesTag = new CompoundNBT(), partyTag, membersTag, memberInfoTag;
        Map<String, CompoundNBT> playerMap;

        for( Map.Entry<UUID, Map<Skill, Double>> entry : xp.entrySet() )
        {
            playerMap = new HashMap<>();

            playerMap.put( "xp", NBTHelper.mapSkillToNbt(                   xp.getOrDefault(            entry.getKey(), new HashMap<>() ) ) );
            playerMap.put( "scheduledXp", NBTHelper.mapSkillToNbt(          scheduledXp.getOrDefault(   entry.getKey(), new HashMap<>() ) ) );
            playerMap.put( "abilities", NBTHelper.mapStringToNbt(           abilities.getOrDefault(     entry.getKey(), new HashMap<>() ) ) );
            playerMap.put( "preferences", NBTHelper.mapStringToNbt(         preferences.getOrDefault(   entry.getKey(), new HashMap<>() ) ) );
            playerMap.put( "xpBoosts", NBTHelper.mapStringMapSkillToNbt(    xpBoosts.getOrDefault(      entry.getKey(), new HashMap<>() ) ) );

            CompoundNBT playerTag = NBTHelper.mapStringNbtToNbt( playerMap );
            playerTag.putString( "name", name.get( entry.getKey() ) );

            playersTag.put( entry.getKey().toString(), playerTag );
        }
        outData.put( "players", playersTag );

        int i = 0, j;
        for( Party party : parties )
        {
            partyTag = new CompoundNBT();
            membersTag = new CompoundNBT();

            j = 0;
            for( PartyMemberInfo memberInfo : party.getAllMembersInfo() )
            {
                memberInfoTag = new CompoundNBT();

                memberInfoTag.putString( "uuid", memberInfo.uuid.toString() );
                memberInfoTag.putLong( "joinDate", memberInfo.joinDate );
                memberInfoTag.putDouble( "xpGained", memberInfo.xpGained );

                membersTag.put( "" + j++, memberInfoTag );
            }
            partyTag.putLong( "creationDate", party.getCreationDate() );
            partyTag.put( "members", membersTag );

            partiesTag.put( "" + i, partyTag );

            i++;
        }
        outData.put( "parties", partiesTag );

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
        return preferences.get( uuid );
    }

    public double getXp( Skill skill, UUID uuid )
    {
        if( skill.equals( Skill.INVALID_SKILL ) )
        {
            LOGGER.debug( "Invalid Skill at getXp" );
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
            if( amount > 0 )
                xp.get( uuid ).put( skill, amount );
            else
                xp.get( uuid ).remove( skill );
            setDirty( true );
            return true;
        }
        else
        {
            LOGGER.error( "Invalid Skill at method setXp, amount: " + amount );
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
            LOGGER.error( "Invalid Skill at method addXp, amount: " + amount );
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
            LOGGER.debug( "Scheduled " + amount + "xp for: " + sourceName + ", to: " + getName( uuid ) );
            setDirty( true );
        }
        else
            LOGGER.error( "Invalid Skill at method addXp, amount: " + amount );
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

    public Party getParty( UUID uuid )
    {
        Party party = null;
        for( Party thisParty : parties )
        {
            if( thisParty.getMemberInfo( uuid ) != null )
                party = thisParty;
        }
        return party;
    }

    public boolean makeParty( UUID uuid )
    {
        if( getParty( uuid ) == null )
        {
            Party party = new Party( System.currentTimeMillis(), new HashSet<>() );
            party.addMember( uuid );
            parties.add( party );
            return true;
        }
        else
            return false;
    }

    public int addToParty( UUID ownerUuid, UUID newMemberUuid )
    {
        Party ownerParty = getParty( ownerUuid );
        Party newMemberParty = getParty( newMemberUuid );
        if( ownerParty == null )
            return -1;  //-1 = owner does not have a party
        else if( newMemberParty != null )
            return -2;  //-2 = new member is already in a party
        else if( ownerParty.getMembersCount() + 1 > Party.getMaxPartyMembers() )
            return -4;  //-4 = the party is full
        else
        {
            ownerParty.addMember( newMemberUuid );
            this.markDirty();
            return 0;   //0 = member has been added
        }
    }

    public int removeFromParty( UUID uuid )
    {
        Party party = getParty( uuid );
        if( party == null )
            return -1;  //-1 = not in a party
        else
        {
            party.removeMember( uuid );
            if( party.getPartySize() == 0 )
            {
                parties.remove( party );
                this.markDirty();
                return 1;   //1 = The party became empty, and got deleted
            }
            else
                return 0;   //0 = the member was removed
        }
    }

//    public static PmmoSavedData get()
//    {
//        return server.getWorld( DimensionType.OVERWORLD ).getSavedData().getOrCreate( PmmoSavedData::new, NAME );
//    }
//
//    public static PmmoSavedData get( PlayerEntity player )
//    {
//        if( player.getServer() == null )
//            LOGGER.error( "FATAL PMMO ERROR: SERVER IS NULL. Could not get PmmoSavedData" );
//
//        return player.getServer().getWorld( DimensionType.OVERWORLD ).getSavedData().getOrCreate( PmmoSavedData::new, NAME );
//    }

    public static void init( MinecraftServer server )
    {
        PmmoSavedData.server = server;
        PmmoSavedData.pmmoSavedData = server.getWorld( DimensionType.OVERWORLD ).getSavedData().getOrCreate( PmmoSavedData::new, NAME );
    }

    public static PmmoSavedData get()   //Only available on Server Side, after the Server has Started.
    {
        return PmmoSavedData.pmmoSavedData;
    }

    public static MinecraftServer getServer()
    {
        return PmmoSavedData.server;
    }

    public Map<String, Map<Skill, Double>> getPlayerXpBoostsMap( UUID playerUUID )
    {
        return xpBoosts.getOrDefault( playerUUID, new HashMap<>() );
    }

    public Map<Skill, Double> getPlayerXpBoostMap( UUID playerUUID, UUID xpBoostUUID )
    {
        return getPlayerXpBoostsMap( playerUUID ).getOrDefault( xpBoostUUID, new HashMap<>() );
    }

    public double getPlayerXpBoost( UUID playerUUID, Skill skill )
    {
        double xpBoost = 0;

        for( Map.Entry<String , Map<Skill, Double>> entry : getPlayerXpBoostsMap( playerUUID ).entrySet() )
        {
            xpBoost += entry.getValue().getOrDefault( skill, 0D );
        }

        return xpBoost;
    }

    public void setPlayerXpBoostsMaps( UUID playerUUID, Map<String, Map<Skill, Double>> newBoosts )
    {
        xpBoosts.put( playerUUID, newBoosts );
        setDirty( true );
    }


    public void setPlayerXpBoost( UUID playerUUID, String xpBoostKey, Map<Skill, Double> newXpBoosts )
    {
        for( Map.Entry<Skill, Double> entry : newXpBoosts.entrySet() )
        {
            setPlayerXpBoost( playerUUID, xpBoostKey, entry.getKey(), entry.getValue() );
        }
        ServerPlayerEntity player = XP.getPlayerByUUID( playerUUID, server );
        if( player != null )
            XP.syncPlayerXpBoost( player );
        setDirty( true );

    }

    public void removePlayerXpBoost( UUID playerUUID, String xpBoostKey )
    {
        getPlayerXpBoostsMap( playerUUID ).remove( xpBoostKey );
        setDirty( true );
    }

    public void removeAllPlayerXpBoosts( UUID playerUUID )
    {
        xpBoosts.remove( playerUUID );
        setDirty( true );
    }

    public Map<UUID, Map<Skill, Double>> getAllXpMaps()
    {
        return xp;
    }

    private void setPlayerXpBoost( UUID playerUUID, String xpBoostKey, Skill skill, Double xpBoost )
    {
        if( !this.xpBoosts.containsKey( playerUUID ) )
            this.xpBoosts.put( playerUUID, new HashMap<>() );
        if( !this.xpBoosts.get( playerUUID ).containsKey( xpBoostKey ) )
            this.xpBoosts.get( playerUUID ).put( xpBoostKey, new HashMap<>() );

        this.xpBoosts.get( playerUUID ).get( xpBoostKey ).put( skill, xpBoost );
        setDirty( true );
    }
}