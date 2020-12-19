package harmonised.pmmo.skills;

import harmonised.pmmo.commands.PmmoCommand;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.network.MessageXp;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

import java.util.*;

public enum Skill
{
    INVALID_SKILL( 0xffffff ),
    MINING( 0x00ffff ),
    BUILDING( 0x00ffff ),
    EXCAVATION( 0xe69900 ),
    WOODCUTTING( 0xffa31a ),
    FARMING( 0x00e600 ),
    AGILITY( 0x66cc66 ),
    ENDURANCE( 0xcc0000 ),
    COMBAT( 0xff3300 ),
    ARCHERY( 0xffff00 ),
    SMITHING( 0xf0f0f0 ),
    FLYING( 0xccccff ),
    SWIMMING( 0x3366ff ),
    FISHING( 0x00ccff ),
    CRAFTING( 0xff9900 ),
    MAGIC( 0x0000ff ),
    SLAYER( 0xffffff ),
    HUNTER( 0xcf7815 ),
    TAMING( 0xffffff ),
    COOKING( 0xe69900 ),
    ALCHEMY( 0xe69900 );

    private static final Map<String, Integer> validSkills = new HashMap<>();
    private static final Map<String, Style> skillStyle = new HashMap<>();
    public final String name;
    public final int color;

    Skill( int color )
    {
        this.name = name();
        this.color = color;
    }

    static
    {
        for( Skill skill : values() )
        {
            setSkill( skill.toString(), skill.color );
        }
    }

    public static void setSkill( String skill, int color )
    {
        skill = skill.toLowerCase();
        if( !skill.equals( INVALID_SKILL.toString() ) )
        {
            validSkills.put( skill, color );
            if( color == 0xff0000 )
                skillStyle.put( skill, new Style().setColor( TextFormatting.RED ) );
            else if( color == 0x00ff00 )
                skillStyle.put( skill, new Style().setColor( TextFormatting.GREEN ) );
            else if( color == 0x0000ff )
                skillStyle.put( skill, new Style().setColor( TextFormatting.BLUE ) );
            else if( color == 0xffff00 )
                skillStyle.put( skill, new Style().setColor( TextFormatting.YELLOW ) );
            else if( color == 0x00ffff )
                skillStyle.put( skill, new Style().setColor( TextFormatting.AQUA ) );
            else if( color == 0xff00ff )
                skillStyle.put( skill, new Style().setColor( TextFormatting.LIGHT_PURPLE ) );
            else
                skillStyle.put( skill, new Style() );
        }
    }

    public static int getSkillColor( String skill )
    {
        return validSkills.getOrDefault( skill, 0xffffff );
    }

    public static Style getSkillStyle( String skill )
    {
        return skillStyle.getOrDefault( skill, new Style() );
    }

    public static void updateSkills()
    {
        int color;
        Map test = JsonConfig.data;
        for( Map.Entry<String, Map<String, Double>> entry : JsonConfig.data.getOrDefault( JType.SKILLS, new HashMap<>() ).entrySet() )
        {
            color = 0xffffff;
            if( entry.getValue().containsKey( "color" ) )
                color = (int) Math.floor( entry.getValue().get( "color" ) );
            setSkill( entry.getKey(), color );
        }
        PmmoCommand.init();
    }

    public static void setSkillStyle( String skill, Style style )
    {
        skillStyle.put( skill.toLowerCase(), style );
    }

    @Override
    public String toString()
    {
        return this.name().toLowerCase();
    }

    public boolean equals( String string )
    {
        return this.toString().equals( string.toLowerCase() );
    }

    public static Map<String, Integer> getSkills()
    {
        return new HashMap<>( validSkills );
    }

    public static int getLevel( String skill, PlayerEntity player)
    {
        if( player.world.isRemote )
            return XP.levelAtXp( XP.getOfflineXp( skill, player.getUniqueID() ) );
        else
            return PmmoSavedData.get().getLevel( skill, player.getUniqueID() );
    }

    public static int getLevel( String skill, UUID uuid )
    {
        return PmmoSavedData.get().getLevel( skill, uuid );
    }

    public static double getLevelDecimal( String skill, PlayerEntity player )
    {
        if( player.world.isRemote )
            return XP.levelAtXpDecimal( XP.getOfflineXp( skill, player.getUniqueID() ) );
        else
            return PmmoSavedData.get().getLevelDecimal( skill, player.getUniqueID() );
    }

    public static double getLevelDecimal( String skill, UUID uuid )
    {
        return PmmoSavedData.get().getLevelDecimal( skill, uuid );
    }

    public static double getXp( String skill, PlayerEntity player )
    {
        if( player.world.isRemote )
            return XP.getOfflineXp( skill, player.getUniqueID() );
        else
            return PmmoSavedData.get().getXp( skill, player.getUniqueID() );
    }

    public static double getXp( String skill, UUID uuid )
    {
        return PmmoSavedData.get().getXp( skill, uuid );
    }

    public static void setLevel( String skill, ServerPlayerEntity player, double amount )
    {
        setXp( skill, player, XP.xpAtLevelDecimal( amount ) );
    }

    public static void setXp( String skill, UUID uuid, double amount )
    {
        ServerPlayerEntity player = PmmoSavedData.getServer().getPlayerList().getPlayerByUUID( uuid );

        if( player == null )
            PmmoSavedData.get().setXp( skill, uuid, amount );
        else
            setXp( skill, player, amount );
    }

    public static void setXp( String skill, ServerPlayerEntity player, double amount )
    {
        if( PmmoSavedData.get().setXp( skill, player.getUniqueID(), amount ) )
        {
            AttributeHandler.updateAll( player );
            XP.updateRecipes( player );

            NetworkHandler.sendToPlayer( new MessageXp( amount, skill, 0, false ), (ServerPlayerEntity) player );
        }
    }

    public static void addLevel( String skill, UUID uuid, double amount, String sourceName, boolean skip, boolean ignoreBonuses )
    {
        double missingXp = XP.xpAtLevelDecimal( getLevelDecimal( skill, uuid ) + amount ) - getXp( skill, uuid );

        addXp( skill, uuid, missingXp, sourceName, skip, ignoreBonuses );
    }

    public static void addLevel( String skill, ServerPlayerEntity player, double amount, String sourceName, boolean skip, boolean ignoreBonuses )
    {
        double missingXp = XP.xpAtLevelDecimal( getLevelDecimal( skill, player ) + amount ) - getXp( skill, player );

        addXp( skill, player, missingXp, sourceName, skip, ignoreBonuses );
    }

    public static void addXp( String skill, UUID uuid, double amount, String sourceName, boolean skip, boolean ignoreBonuses )
    {
        ServerPlayerEntity player = PmmoSavedData.getServer().getPlayerList().getPlayerByUUID( uuid );

        if( player == null )
            PmmoSavedData.get().scheduleXp( skill, uuid, amount, sourceName );
        else
            addXp( skill, player, amount, sourceName, skip, ignoreBonuses );
    }

    public static void addXp( String skill, ServerPlayerEntity player, double amount, String sourceName, boolean skip, boolean ignoreBonuses )
    {
        XP.awardXp( player, skill, sourceName, amount, skip, ignoreBonuses, false );
    }
}