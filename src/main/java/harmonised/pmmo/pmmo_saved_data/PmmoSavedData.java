package harmonised.pmmo.pmmo_saved_data;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.party.Party;
import harmonised.pmmo.party.PartyMemberInfo;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.NBTHelper;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.XP;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.apache.logging.log4j.*;

import java.util.*;

public class PmmoSavedData extends SavedData
{
    public static final Logger LOGGER = LogManager.getLogger();

    private static PmmoSavedData pmmoSavedData;
    private static MinecraftServer server;
    private static String NAME = Reference.MOD_ID;
    private Map<UUID, Map<String, Double>> xp = new HashMap<>();
    private Map<UUID, Map<String, Double>> scheduledXp = new HashMap<>();
    private Map<UUID, Map<String, Double>> abilities = new HashMap<>();
    private Map<UUID, Map<String, Double>> preferences = new HashMap<>();
    private Map<UUID, Map<String, Map<String, Double>>> xpBoosts = new HashMap<>();    //playerUUID -> boostUUID -> boostMap
    private Set<Party> parties = new HashSet<>();
    private Map<UUID, String> name = new HashMap<>();
    public PmmoSavedData()
    {
        super();
    }

    public PmmoSavedData load(CompoundTag inData)
    {
        CompoundTag playersTag, playerTag;

        if(inData.contains("players"))
        {
            playersTag = inData.getCompound("players");
            for(String playerUuidKey : playersTag.getAllKeys())
            {
                playerTag = playersTag.getCompound(playerUuidKey);
                if(playerTag.contains("xp"))
                {
                    CompoundTag xpTag = playerTag.getCompound("xp");
                    for(String tag : new HashSet<>(xpTag.getAllKeys()))
                    {
                        if(xpTag.getDouble(tag) <= 0)
                            xpTag.remove(tag.toLowerCase());
                    }
                }

                if(playerTag.contains("name"))
                    name.put(UUID.fromString(playerUuidKey), playerTag.getString("name"));

                xp = NBTHelper.nbtToMapUuidString(NBTHelper.extractNbtPlayersIndividualTagsFromPlayersTag(playersTag, "xp"));
                scheduledXp = NBTHelper.nbtToMapUuidString(NBTHelper.extractNbtPlayersIndividualTagsFromPlayersTag(playersTag, "scheduledXp"));
                abilities = NBTHelper.nbtToMapUuidString(NBTHelper.extractNbtPlayersIndividualTagsFromPlayersTag(playersTag, "abilities"));
                preferences = NBTHelper.nbtToMapUuidString(NBTHelper.extractNbtPlayersIndividualTagsFromPlayersTag(playersTag, "preferences"));
                xpBoosts = NBTHelper.nbtToMapStringMapUuidString(NBTHelper.extractNbtPlayersIndividualTagsFromPlayersTag(playersTag, "xpBoosts"));
            }
        }

        if(inData.contains("parties"))
        {
            CompoundTag partiesTag = inData.getCompound("parties");
            CompoundTag partyTag, membersTag, memberInfoTag;
            Set<PartyMemberInfo> membersInfo;
            PartyMemberInfo memberInfo;

            for(String key : partiesTag.getAllKeys())
            {
                partyTag = partiesTag.getCompound(key);
                membersTag = partyTag.getCompound("members");
                membersInfo = new HashSet<>();

                for(String id : membersTag.getAllKeys())
                {
                    memberInfoTag = membersTag.getCompound(id);
                    memberInfo = new PartyMemberInfo(UUID.fromString(memberInfoTag.getString("uuid")), memberInfoTag.getLong("joinDate"), memberInfoTag.getDouble("xpGained"));
                    membersInfo.add(memberInfo);
                }

                parties.add(new Party(partyTag.getLong("creationDate"), membersInfo));
            }
        }
        return this;
    }

    @Override
    public CompoundTag save(CompoundTag outData)
    {
        CompoundTag playersTag = new CompoundTag(), partiesTag = new CompoundTag(), partyTag, membersTag, memberInfoTag;
        Map<String, CompoundTag> playerMap;

        for(Map.Entry<UUID, Map<String, Double>> entry : xp.entrySet())
        {
            playerMap = new HashMap<>();

            playerMap.put("xp", NBTHelper.mapStringToNbt(                 xp.getOrDefault(           entry.getKey(), Collections.emptyMap())));
            playerMap.put("scheduledXp", NBTHelper.mapStringToNbt(        scheduledXp.getOrDefault(  entry.getKey(), Collections.emptyMap())));
            playerMap.put("abilities", NBTHelper.mapStringToNbt(          abilities.getOrDefault(    entry.getKey(), Collections.emptyMap())));
            playerMap.put("preferences", NBTHelper.mapStringToNbt(        preferences.getOrDefault(  entry.getKey(), Collections.emptyMap())));
            playerMap.put("xpBoosts", NBTHelper.mapStringMapStringToNbt(  xpBoosts.getOrDefault(     entry.getKey(), Collections.emptyMap())));

            CompoundTag playerTag = NBTHelper.mapStringNbtToNbt(playerMap);
            playerTag.putString("name", name.get(entry.getKey()));

            playersTag.put(entry.getKey().toString(), playerTag);
        }
        outData.put("players", playersTag);

        int i = 0, j;
        for(Party party : parties)
        {
            partyTag = new CompoundTag();
            membersTag = new CompoundTag();

            j = 0;
            for(PartyMemberInfo memberInfo : party.getAllMembersInfo())
            {
                memberInfoTag = new CompoundTag();

                memberInfoTag.putString("uuid", memberInfo.uuid.toString());
                memberInfoTag.putLong("joinDate", memberInfo.joinDate);
                memberInfoTag.putDouble("xpGained", memberInfo.xpGained);

                membersTag.put("" + j++, memberInfoTag);
            }
            partyTag.putLong("creationDate", party.getCreationDate());
            partyTag.put("members", membersTag);

            partiesTag.put("" + i, partyTag);

            i++;
        }
        outData.put("parties", partiesTag);

        return outData;
    }

    public Map<String, Double> getXpMap(UUID uuid)
    {
        if(!xp.containsKey(uuid))
            xp.put(uuid, new HashMap<>());
        return xp.get(uuid);
    }

    public Map<String, Double> getScheduledXpMap(UUID uuid)
    {
        if(!scheduledXp.containsKey(uuid))
            scheduledXp.put(uuid, new HashMap<>());
        return scheduledXp.get(uuid);
    }

    public Map<String, Double> getAbilitiesMap(UUID uuid)
    {
        if(!abilities.containsKey(uuid))
            abilities.put(uuid, new HashMap<>());
        return abilities.get(uuid);
    }

    public Map<String, Double> getPreferencesMap(UUID uuid)
    {
        if(!preferences.containsKey(uuid))
            preferences.put(uuid, new HashMap<>());
        return preferences.get(uuid);
    }

    public double getXp(String skill, UUID uuid)
    {
        return xp.getOrDefault(uuid, new HashMap<>()).getOrDefault(skill, 0D);
    }

    public int getLevel(String skill, UUID uuid)
    {
        if(skill.equals("totalLevel"))
            return XP.getTotalLevelFromMap(Config.getXpMap(uuid));
        else
            return XP.levelAtXp(getXp(skill, uuid));
    }

    public double getLevelDecimal(String skill, UUID uuid)
    {
        if(skill.equals("totalLevel"))
            return getLevel(skill, uuid);
        else
            return XP.levelAtXpDecimal(getXp(skill, uuid));
    }

    public boolean setXp(String skill, UUID uuid, double amount)
    {
        double maxXp = Config.getConfig("maxXp");

        if(amount > maxXp)
            amount = maxXp;

        if(amount < 0)
            amount = 0;

        if(!xp.containsKey(uuid))
            xp.put(uuid, new HashMap<>());
        if(amount > 0)
            xp.get(uuid).put(skill, amount);
        else
            xp.get(uuid).remove(skill);
        setDirty(true);
        return true;
    }

    public boolean addXp(String skill, UUID uuid, double amount)
    {
        setXp(skill, uuid, getXp(skill, uuid) + amount);
        setDirty(true);
        return true;
    }

    public void scheduleXp(String skill, UUID uuid, double amount, String sourceName)
    {
        Map<String, Double> scheduledXpMap = getScheduledXpMap(uuid);
        if(!scheduledXpMap.containsKey(skill))
            scheduledXpMap.put(skill, amount);
        else
            scheduledXpMap.put(skill, amount + scheduledXpMap.get(skill));
        LOGGER.debug("Scheduled " + amount + "xp for: " + sourceName + ", to: " + getName(uuid));
        setDirty(true);
    }

    public void removeScheduledXpUuid(UUID uuid)
    {
        scheduledXp.remove(uuid);
    }

    public void setName(String name, UUID uuid)
    {
        this.name.put(uuid, name);
    }

    public String getName(UUID uuid)
    {
        return name.getOrDefault(uuid, "Nameless Warning");
    }

    public Party getParty(UUID uuid)
    {
        Party party = null;
        for(Party thisParty : parties)
        {
            if(thisParty.getMemberInfo(uuid) != null)
                party = thisParty;
        }
        return party;
    }

    public boolean makeParty(UUID uuid)
    {
        if(getParty(uuid) == null)
        {
            Party party = new Party(System.currentTimeMillis(), new HashSet<>());
            party.addMember(uuid);
            parties.add(party);
            return true;
        }
        else
            return false;
    }

    public int addToParty(UUID ownerUuid, UUID newMemberUuid)
    {
        Party ownerParty = getParty(ownerUuid);
        Party newMemberParty = getParty(newMemberUuid);
        if(ownerParty == null)
            return -1;  //-1 = owner does not have a party
        else if(newMemberParty != null)
            return -2;  //-2 = new member is already in a party
        else if(ownerParty.getMembersCount() + 1 > Party.getMaxPartyMembers())
            return -4;  //-4 = the party is full
        else
        {
            ownerParty.addMember(newMemberUuid);
            this.setDirty();
            return 0;   //0 = member has been added
        }
    }

    public int removeFromParty(UUID uuid)
    {
        Party party = getParty(uuid);
        if(party == null)
            return -1;  //-1 = not in a party
        else
        {
            party.removeMember(uuid);
            if(party.getPartySize() == 0)
            {
                parties.remove(party);
                this.setDirty();
                return 1;   //1 = The party became empty, and got deleted
            }
            else
                return 0;   //0 = the member was removed
        }
    }

//    public static PmmoSavedData get()
//    {
//        return server.getWorld(DimensionType.OVERWORLD).getSavedData().getOrCreate(PmmoSavedData::new, NAME);
//    }
//
//    public static PmmoSavedData get(PlayerEntity player)
//    {
//        if(player.getServer() == null)
//            LOGGER.error("FATAL PMMO ERROR: SERVER IS NULL. Could not get PmmoSavedData");
//
//        return player.getServer().getWorld(DimensionType.OVERWORLD).getSavedData().getOrCreate(PmmoSavedData::new, NAME);
//    }

    public static void init(MinecraftServer server)
    {
        PmmoSavedData.server = server;
        PmmoSavedData.pmmoSavedData = server.getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(nbt -> new PmmoSavedData().load(nbt), PmmoSavedData::new, NAME);
    }

    public static PmmoSavedData get()   //Only available on Server Side, after the Server has Started.
    {
        return PmmoSavedData.pmmoSavedData;
    }

    public static MinecraftServer getServer()
    {
        return PmmoSavedData.server;
    }

    public Map<String, Map<String, Double>> getPlayerXpBoostsMap(UUID playerUUID)
    {
        return xpBoosts.getOrDefault(playerUUID, new HashMap<>());
    }

    public Map<String, Double> getPlayerXpBoostMap(UUID playerUUID, String xpBoostKey)
    {
        return getPlayerXpBoostsMap(playerUUID).getOrDefault(xpBoostKey, new HashMap<>());
    }

    public double getPlayerXpBoost(UUID playerUUID, String skill)
    {
        double xpBoost = 0;

        for(Map.Entry<String , Map<String, Double>> entry : getPlayerXpBoostsMap(playerUUID).entrySet())
        {
            xpBoost += entry.getValue().getOrDefault(skill, 0D);
        }

        return xpBoost;
    }

    public void setPlayerXpBoostsMaps(UUID playerUUID, Map<String, Map<String, Double>> newBoosts)
    {
        Map<String, Map<String, Double>> sanitizedBoosts = new HashMap<>();
        for(Map.Entry<String, Map<String, Double>> boostMapEntry : newBoosts.entrySet())
        {
            sanitizedBoosts.put(boostMapEntry.getKey(), NBTHelper.stringMapToLowerCase(boostMapEntry.getValue()));
        }
        xpBoosts.put(playerUUID, sanitizedBoosts);
        setDirty(true);
    }

    public void setPlayerXpBoost(UUID playerUUID, String xpBoostKey, Map<String, Double> newXpBoosts)
    {
        for(Map.Entry<String, Double> entry : newXpBoosts.entrySet())
        {
            setPlayerXpBoost(playerUUID, xpBoostKey, entry.getKey(), entry.getValue());
        }
        XP.syncPlayerXpBoost(playerUUID);
        setDirty(true);
    }

    private void setPlayerXpBoost(UUID playerUUID, String xpBoostKey, String skill, Double xpBoost)
    {
        if(!this.xpBoosts.containsKey(playerUUID))
            this.xpBoosts.put(playerUUID, new HashMap<>());
        if(!this.xpBoosts.get(playerUUID).containsKey(xpBoostKey))
            this.xpBoosts.get(playerUUID).put(xpBoostKey, new HashMap<>());

        this.xpBoosts.get(playerUUID).get(xpBoostKey).put(skill.toLowerCase(), xpBoost);
        setDirty(true);
    }

    public void removePlayerXpBoost(UUID playerUUID, String xpBoostKey)
    {
        getPlayerXpBoostsMap(playerUUID).remove(xpBoostKey);
        XP.syncPlayerXpBoost(playerUUID);
        setDirty(true);
    }

    public void removeAllPlayerXpBoosts(UUID playerUUID)
    {
        xpBoosts.remove(playerUUID);
        XP.syncPlayerXpBoost(playerUUID);
        setDirty(true);
    }

    public Map<UUID, Map<String, Double>> getAllXpMaps()
    {
        return xp;
    }
}