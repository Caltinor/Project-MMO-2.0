package harmonised.pmmo.skills;

import harmonised.pmmo.api.perks.PerkRegistry;
import harmonised.pmmo.api.perks.PerkTrigger;
import harmonised.pmmo.commands.PmmoCommand;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.network.MessageXp;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.Style;
import java.util.*;

public enum Skill
{
    INVALID_SKILL(0xffffff),
    MINING(0x00ffff),
    BUILDING(0x00ffff),
    EXCAVATION(0xe69900),
    WOODCUTTING(0xffa31a),
    FARMING(0x00e600),
    AGILITY(0x66cc66),
    ENDURANCE(0xcc0000),
    COMBAT(0xff3300),
    GUNSLINGING(0xd3c1a3),
    ARCHERY(0xffff00),
    SMITHING(0xf0f0f0),
    FLYING(0xccccff),
    SWIMMING(0x3366ff),
    SAILING(0x99b3ff),
    FISHING(0x00ccff),
    CRAFTING(0xff9900),
    MAGIC(0x0000ff),
    SLAYER(0xffffff),
    HUNTER(0xcf7815),
    TAMING(0xffffff),
    COOKING(0xe69900),
    ALCHEMY(0xe69900);

    private static final Map<String, Integer> validSkills = new HashMap<>();
    private static final Map<String, Style> skillStyle = new HashMap<>();
    public final String name;
    public final int color;

    Skill(int color)
    {
        this.name = name();
        this.color = color;
    }

    static
    {
        for(Skill skill : values())
        {
            setSkill(skill.name, skill.color);
        }
    }

    public static void setSkill(String skill, int color)
    {
        skill = skill.toLowerCase();
        if(!skill.equals(INVALID_SKILL.toString()))
        {
            validSkills.put(skill, color);
            skillStyle.put(skill, Style.EMPTY.withColor(TextColor.fromRgb(color)));
        }
    }

    public static int getSkillColor(String skill)
    {
        return validSkills.getOrDefault(skill, 0xffffff);
    }

    public static Style getSkillStyle(String skill)
    {
        return skillStyle.getOrDefault(skill, Style.EMPTY);
    }

    public static void updateSkills()
    {
    	for (Map.Entry<JType, Map<String, Map<String, Double>>> map : JsonConfig.data.entrySet()) 
    	{
	        for(Map.Entry<String, Map<String, Double>> entry : JsonConfig.data.getOrDefault(map.getKey(), new HashMap<>()).entrySet())
	        {
	        	setSkill(entry.getKey(), 
	        			JsonConfig.data.getOrDefault(JType.SKILLS, new HashMap<>()).containsKey("color") ?
	        			(int) Math.floor(entry.getValue().get("color")) :
	        			16777215);
	        }
    	}
        PmmoCommand.init();
    }

    public static void setSkillStyle(String skill, Style style)
    {
        skillStyle.put(skill.toLowerCase(), style);
    }

    @Override
    public String toString()
    {
        return this.name().toLowerCase();
    }

    public boolean equals(String string)
    {
        return this.toString().equals(string.toLowerCase());
    }

    public static Map<String, Integer> getSkills()
    {
        return new HashMap<>(validSkills);
    }

    /**
     * @deprecated This method is only for internal use now.  please use {@link harmonised.pmmo.api.APIUtils#getLevel(String, PlayerEntity) APIUtils.getLevel}
     */
    @Deprecated
    public static int getLevel(String skill, Player player)
    {
        if(player.level.isClientSide)
            return XP.getOfflineLevel(skill, player.getUUID());
        else
            return PmmoSavedData.get().getLevel(skill, player.getUUID());
    }

    public static int getLevel(String skill, UUID uuid)
    {
        return PmmoSavedData.get().getLevel(skill, uuid);
    }

    public static double getLevelDecimal(String skill, Player player)
    {
        if(player.level.isClientSide)
            return XP.getOfflineLevelDecimal(skill, player.getUUID());
        else
            return PmmoSavedData.get().getLevelDecimal(skill, player.getUUID());
    }

    public static double getLevelDecimal(String skill, UUID uuid)
    {
        return PmmoSavedData.get().getLevelDecimal(skill, uuid);
    }

    /**
     * @deprecated This method is only for internal use now.  please use {@link harmonised.pmmo.api.APIUtils#getXp(String, PlayerEntity) APIUtils.getXp}
     */
    @Deprecated
    public static double getXp(String skill, Player player)
    {
        if(player.level.isClientSide)
            return XP.getOfflineXp(skill, player.getUUID());
        else
            return PmmoSavedData.get().getXp(skill, player.getUUID());
    }

    public static double getXp(String skill, UUID uuid)
    {
        return PmmoSavedData.get().getXp(skill, uuid);
    }

    /**
     * @deprecated This method is only for internal use now.  please use {@link harmonised.pmmo.api.APIUtils#setLevel(String, ServerPlayerEntity, double) APIUtils.setLevel}
     */
    @Deprecated
    public static void setLevel(String skill, ServerPlayer player, double amount)
    {
        setXp(skill, player, XP.xpAtLevelDecimal(amount));
    }

    public static void setXp(String skill, UUID uuid, double amount)
    {
        ServerPlayer player = PmmoSavedData.getServer().getPlayerList().getPlayer(uuid);

        if(player == null)
            PmmoSavedData.get().setXp(skill, uuid, amount);
        else
            setXp(skill, player, amount);
    }

    /**
     * @deprecated This method is only for internal use now.  please use {@link harmonised.pmmo.api.APIUtils#setXp(String, ServerPlayerEntity, double) APIUtils.setXp}
     */
    @Deprecated
    public static void setXp(String skill, ServerPlayer player, double amount)
    {
        if(PmmoSavedData.get().setXp(skill, player.getUUID(), amount))
        {
            PerkRegistry.executePerk(PerkTrigger.SKILL_UP, player, XP.levelAtXp(amount));
            XP.updateRecipes(player);

            NetworkHandler.sendToPlayer(new MessageXp(amount, skill, 0, false), player);
        }
    }

    /**
     * @deprecated This method is only for internal use now.  please use {@link harmonised.pmmo.api.APIUtils#addLevel(String, UUID, double, String, boolean, boolean) APIUtils.addLevel}
     */
    @Deprecated
    public static void addLevel(String skill, UUID uuid, double amount, String sourceName, boolean skip, boolean ignoreBonuses)
    {
        double missingXp = XP.xpAtLevelDecimal(getLevelDecimal(skill, uuid) + amount) - getXp(skill, uuid);

        addXp(skill, uuid, missingXp, sourceName, skip, ignoreBonuses);
    }

    public static void addLevel(String skill, ServerPlayer player, double amount, String sourceName, boolean skip, boolean ignoreBonuses)
    {
        double missingXp = XP.xpAtLevelDecimal(getLevelDecimal(skill, player) + amount) - getXp(skill, player);

        addXp(skill, player, missingXp, sourceName, skip, ignoreBonuses);
    }

    /**
     * @deprecated This method is only for internal use now.  please use {@link harmonised.pmmo.api.APIUtils#addXp(String, UUID, double, String, boolean, boolean) APIUtils.addXp}
     */
    @Deprecated
    public static void addXp(String skill, UUID uuid, double amount, String sourceName, boolean skip, boolean ignoreBonuses)
    {
        ServerPlayer player = PmmoSavedData.getServer().getPlayerList().getPlayer(uuid);

        if(player == null)
            PmmoSavedData.get().scheduleXp(skill, uuid, amount, sourceName);
        else
            addXp(skill, player, amount, sourceName, skip, ignoreBonuses);
    }

    public static void addXp(String skill, ServerPlayer player, double amount, String sourceName, boolean skip, boolean ignoreBonuses)
    {
        XP.awardXp(player, skill, sourceName, amount, skip, ignoreBonuses, false);
    }
}