package harmonised.pmmo.pmmo_saved_data;

import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.party.Party;
import harmonised.pmmo.party.PartyMemberInfo;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.NBTHelper;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class PmmoSavedData extends WorldSavedData
{
    public static final Logger LOGGER = LogManager.getLogger();

    private static PmmoSavedData pmmoSavedData;
    private static MinecraftServer server;
    private static String ID = Reference.MOD_ID;
    private Map<UUID, Map<String, Double>> xp = new HashMap<>();
    private Map<UUID, Map<String, Double>> scheduledXp = new HashMap<>();
    private Map<UUID, Map<String, Double>> abilities = new HashMap<>();
    private Map<UUID, Map<String, Double>> preferences = new HashMap<>();
    private Map<UUID, Map<String, Map<String, Double>>> xpBoosts = new HashMap<>();    //playerUUID -> boostUUID -> boostMap
    private Set<Party> parties = new HashSet<>();
    private Map<UUID, String> name = new HashMap<>();

    public PmmoSavedData()
    {
        super(ID);
    }

    public PmmoSavedData( String name )
    {
        super( name );
    }

    @Override
    public void readFromNBT( NBTTagCompound inData )
    {
        NBTTagCompound playersTag, playerTag;

        if( inData.hasKey( "players" ) )
        {
            playersTag = inData.getCompoundTag( "players" );
            for( String playerUuidKey : playersTag.getKeySet() )
            {
                playerTag = playersTag.getCompoundTag( playerUuidKey );
                if( playerTag.hasKey( "xp" ) )
                {
                    NBTTagCompound xpTag = playerTag.getCompoundTag( "xp" );
                    for( String tag : new HashSet<>( xpTag.getKeySet() ) )
                    {
                        if( xpTag.getDouble( tag ) > 0 )
                            xpTag.setDouble( tag.toLowerCase(), xpTag.getDouble( tag ) );
                        else
                            xpTag.removeTag( tag.toLowerCase() );
                    }
                }

                if( playerTag.hasKey( "name" ) )
                    name.put( UUID.fromString( playerUuidKey ), playerTag.getString( "name" ) );

                xp = NBTHelper.nbtToMapUuidString( NBTHelper.extractNbtPlayersIndividualTagsFromPlayersTag( playersTag, "xp" ) );
                scheduledXp = NBTHelper.nbtToMapUuidString( NBTHelper.extractNbtPlayersIndividualTagsFromPlayersTag( playersTag, "scheduledXp" ) );
                abilities = NBTHelper.nbtToMapUuidString( NBTHelper.extractNbtPlayersIndividualTagsFromPlayersTag( playersTag, "abilities" ) );
                preferences = NBTHelper.nbtToMapUuidString( NBTHelper.extractNbtPlayersIndividualTagsFromPlayersTag( playersTag, "preferences" ) );
                xpBoosts = NBTHelper.nbtToMapStringMapUuidString( NBTHelper.extractNbtPlayersIndividualTagsFromPlayersTag( playersTag, "xpBoosts" ) );
            }
        }

        if( inData.hasKey( "parties" ) )
        {
            NBTTagCompound partiesTag = inData.getCompoundTag( "parties" );
            NBTTagCompound partyTag, membersTag, memberInfoTag;
            Set<PartyMemberInfo> membersInfo;
            PartyMemberInfo memberInfo;

            for( String key : partiesTag.getKeySet() )
            {
                partyTag = partiesTag.getCompoundTag( key );
                membersTag = partyTag.getCompoundTag( "members" );
                membersInfo = new HashSet<>();

                for( String id : membersTag.getKeySet() )
                {
                    memberInfoTag = membersTag.getCompoundTag( id );
                    memberInfo = new PartyMemberInfo( UUID.fromString( memberInfoTag.getString( "uuid" ) ), memberInfoTag.getLong( "joinDate" ), memberInfoTag.getDouble( "xpGained" ) );
                    membersInfo.add( memberInfo );
                }

                parties.add( new Party( partyTag.getLong( "creationDate" ), membersInfo ) );
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT( NBTTagCompound outData )
    {
        NBTTagCompound playersTag = new NBTTagCompound(), partiesTag = new NBTTagCompound(), partyTag, membersTag, memberInfoTag;
        Map<String, NBTTagCompound> playerMap;

        for( Map.Entry<UUID, Map<String, Double>> entry : xp.entrySet() )
        {
            playerMap = new HashMap<>();

            playerMap.put( "xp", NBTHelper.mapStringToNbt(                   xp.getOrDefault(            entry.getKey(), new HashMap<>() ) ) );
            playerMap.put( "scheduledXp", NBTHelper.mapStringToNbt(          scheduledXp.getOrDefault(   entry.getKey(), new HashMap<>() ) ) );
            playerMap.put( "abilities", NBTHelper.mapStringToNbt(            abilities.getOrDefault(     entry.getKey(), new HashMap<>() ) ) );
            playerMap.put( "preferences", NBTHelper.mapStringToNbt(          preferences.getOrDefault(   entry.getKey(), new HashMap<>() ) ) );
            playerMap.put( "xpBoosts", NBTHelper.mapStringMapStringToNbt(    xpBoosts.getOrDefault(      entry.getKey(), new HashMap<>() ) ) );

            NBTTagCompound playerTag = NBTHelper.mapStringNbtToNbt( playerMap );
            playerTag.setString( "name", name.get( entry.getKey() ) );

            playersTag.setTag( entry.getKey().toString(), playerTag );
        }
        outData.setTag( "players", playersTag );

        int i = 0, j;
        for( Party party : parties )
        {
            partyTag = new NBTTagCompound();
            membersTag = new NBTTagCompound();

            j = 0;
            for( PartyMemberInfo memberInfo : party.getAllMembersInfo() )
            {
                memberInfoTag = new NBTTagCompound();

                memberInfoTag.setString( "uuid", memberInfo.uuid.toString() );
                memberInfoTag.setLong( "joinDate", memberInfo.joinDate );
                memberInfoTag.setDouble( "xpGained", memberInfo.xpGained );

                membersTag.setTag( "" + j++, memberInfoTag );
            }
            partyTag.setLong( "creationDate", party.getCreationDate() );
            partyTag.setTag( "members", membersTag );

            partiesTag.setTag( "" + i, partyTag );

            i++;
        }
        outData.setTag( "parties", partiesTag );

        return outData;
    }

    public Map<String, Double> getXpMap( UUID uuid )
    {
        if( !xp.containsKey( uuid ) )
            xp.put( uuid, new HashMap<>() );
        return xp.get( uuid );
    }

    public Map<String, Double> getScheduledXpMap( UUID uuid )
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

    public double getXp( String skill, UUID uuid )
    {
        skill = skill.toLowerCase();
        return xp.getOrDefault( uuid, new HashMap<>() ).getOrDefault( skill, 0D );
    }

    public int getLevel( String skill, UUID uuid )
    {
        return XP.levelAtXp( getXp( skill, uuid ) );
    }

    public double getLevelDecimal( String skill, UUID uuid )
    {
        return XP.levelAtXpDecimal( getXp( skill, uuid ) );
    }

    public boolean setXp( String skill, UUID uuid, double amount )
    {
        skill = skill.toLowerCase();
        double maxXp = FConfig.getConfig( "maxXp" );

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

    public boolean addXp( String skill, UUID uuid, double amount )
    {
        setXp( skill, uuid, getXp( skill, uuid ) + amount );
        setDirty( true );
        return true;
    }

    public void scheduleXp( String skill, UUID uuid, double amount, String sourceName )
    {
        skill = skill.toLowerCase();
        Map<String, Double> scheduledXpMap = getScheduledXpMap( uuid );
        if( !scheduledXpMap.containsKey( skill ) )
            scheduledXpMap.put( skill, amount );
        else
            scheduledXpMap.put( skill, amount + scheduledXpMap.get( skill ) );
        LOGGER.info( "Scheduled " + amount + "xp for: " + sourceName + ", to: " + getName( uuid ) );
        setDirty( true );
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
//    public static PmmoSavedData get( EntityPlayer player )
//    {
//        if( player.getServer() == null )
//            LOGGER.info( "FATAL PMMO ERROR: SERVER IS NULL. Could not get PmmoSavedData" );
//
//        return player.getServer().getWorld( DimensionType.OVERWORLD ).getSavedData().getOrCreate( PmmoSavedData::new, NAME );
//    }

    public static void init( MinecraftServer server )
    {
        PmmoSavedData.server = server;
        World world = server.getWorld( DimensionType.OVERWORLD.getId() );
        MapStorage storage = world.getMapStorage();
        PmmoSavedData instance = (PmmoSavedData) storage.getOrLoadData( PmmoSavedData.class, ID );
        if (instance == null)
        {
            instance = new PmmoSavedData();
            storage.setData( ID, instance );
        }
        PmmoSavedData.pmmoSavedData = instance;
    }

    public static PmmoSavedData get()   //Only available on Server Side, after the Server has Started.
    {
        return PmmoSavedData.pmmoSavedData;
    }

    public static MinecraftServer getServer()
    {
        return PmmoSavedData.server;
    }

    public Map<String, Map<String, Double>> getPlayerXpBoostsMap( UUID playerUUID )
    {
        return xpBoosts.getOrDefault( playerUUID, new HashMap<>() );
    }

    public Map<String, Double> getPlayerXpBoostMap( UUID playerUUID, UUID xpBoostUUID )
    {
        return getPlayerXpBoostsMap( playerUUID ).getOrDefault( xpBoostUUID, new HashMap<>() );
    }

    public double getPlayerXpBoost( UUID playerUUID, String skill )
    {
        double xpBoost = 0;

        for( Map.Entry<String , Map<String, Double>> entry : getPlayerXpBoostsMap( playerUUID ).entrySet() )
        {
            xpBoost += entry.getValue().getOrDefault( skill, 0D );
        }

        return xpBoost;
    }

    public void setPlayerXpBoostsMaps( UUID playerUUID, Map<String, Map<String, Double>> newBoosts )
    {
        xpBoosts.put( playerUUID, newBoosts );
        setDirty( true );
    }


    public void setPlayerXpBoost( UUID playerUUID, String xpBoostKey, Map<String, Double> newXpBoosts )
    {
        for( Map.Entry<String, Double> entry : newXpBoosts.entrySet() )
        {
            setPlayerXpBoost( playerUUID, xpBoostKey, entry.getKey(), entry.getValue() );
        }
        EntityPlayerMP player = XP.getPlayerByUUID( playerUUID, server );
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

    public Map<UUID, Map<String, Double>> getAllXpMaps()
    {
        return xp;
    }

    private void setPlayerXpBoost( UUID playerUUID, String xpBoostKey, String skill, Double xpBoost )
    {
        if( !this.xpBoosts.containsKey( playerUUID ) )
            this.xpBoosts.put( playerUUID, new HashMap<>() );
        if( !this.xpBoosts.get( playerUUID ).containsKey( xpBoostKey ) )
            this.xpBoosts.get( playerUUID ).put( xpBoostKey, new HashMap<>() );

        this.xpBoosts.get( playerUUID ).get( xpBoostKey ).put( skill, xpBoost );
        setDirty( true );
    }
}