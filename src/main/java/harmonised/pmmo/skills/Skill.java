package harmonised.pmmo.skills;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.network.MessageXp;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.LogHandler;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public enum Skill
{
    INVALID_SKILL( 0 ),
    MINING( 1 ),
    BUILDING(2 ),
    EXCAVATION( 3 ),
    WOODCUTTING( 4 ),
    FARMING( 5 ),
    AGILITY( 6 ),
    ENDURANCE( 7 ),
    COMBAT( 8 ),
    ARCHERY( 9 ),
    SMITHING( 10 ),
    FLYING( 11 ),
    SWIMMING( 12 ),
    FISHING( 13 ),
    CRAFTING( 14 ),
    MAGIC( 15 ),
    SLAYER( 16 ),
    HUNTER( 17 ),
    FLETCHING( 18 ),
    TAMING( 19 ),
    ENGINEERING( 20 );

    public static final Map<Skill, Integer > skillMap = new HashMap<>();
    public static final Map< Integer, Skill > intMap = new HashMap<>();
    public static final Map< String, Skill > stringMap = new HashMap<>();
    //    Skill[] VALUES = values();

    private final int value;

    static
    {
        for( Skill skill : Skill.values() )
        {
            skillMap.put( skill, skill.value );
            intMap.put( skill.value, skill );
            stringMap.put( skill.toString(), skill );
        }
    }

    @Override
    public String toString()
    {
        return this.name().toLowerCase();
    }

    Skill( int i )
    {
        this.value = i;
    }

//    public static

    public static int getInt( String i )
    {
        if( stringMap.get( i.toLowerCase() ) != null )
            return stringMap.get( i.toLowerCase() ).getValue();
        else
            return 0;
    }

    public static String getString( int i )
    {
        for( Skill theEnum : Skill.values() )
        {
            if( theEnum.value == i )
                return theEnum.name().toLowerCase();
        }
        return "none";
    }

    public static Skill getSkill( String input )
    {
        for( Skill theEnum : Skill.values() )
        {
            if( theEnum.name().toLowerCase().equals( input.toLowerCase() ) )
                return theEnum;
        }
        return Skill.INVALID_SKILL;
    }

    public static Skill getSkill( int input )
    {

        for( Skill theEnum : Skill.values() )
        {
            if( theEnum.value == input )
                return theEnum;
        }
        return Skill.INVALID_SKILL;
    }

    public int getValue()
    {
        return this.value;
    }

    public int getLevel( PlayerEntity player )
    {
        return XP.getLevel( this, player );
    }

    public double getLevelDecimal( PlayerEntity player )
    {
        return XP.getLevelDecimal( this, player );
    }

    public double getXp( PlayerEntity player )
    {
        return XP.getXp( this, player );
    }

    public void setLevel( ServerPlayerEntity player, double setAmount )
    {
        this.setXp( player, XP.xpAtLevelDecimal( setAmount ) );
    }

    public void setXp( ServerPlayerEntity player, double setAmount )
    {
        if( this != Skill.INVALID_SKILL )
        {
            CompoundNBT skillsTag = XP.getSkillsTag( player );
            double maxXp = Config.getConfig( "maxXp" );

            if( setAmount > maxXp )
                setAmount = maxXp;

            if( setAmount < 0 )
                setAmount = 0;

            skillsTag.putDouble( this.name().toLowerCase(), setAmount );
            AttributeHandler.updateAll( player );
            XP.updateRecipes( player );

            NetworkHandler.sendToPlayer( new MessageXp( setAmount, this.getValue(), 0, false ), (ServerPlayerEntity) player );
        }
        else
            LogHandler.LOGGER.error( "Invalid skill at method setXp" );
    }

    public void addLevel( ServerPlayerEntity player, double addAmount, boolean ignoreBonuses )
    {
        double missingXp = XP.xpAtLevelDecimal( this.getLevelDecimal( player ) + addAmount ) - this.getXp( player );

        this.addXp( player, missingXp, ignoreBonuses );
    }

    public void addXp( ServerPlayerEntity player, double addAmount, boolean ignoreBonuses )
    {
        CompoundNBT skillsTag = XP.getSkillsTag( player );
        double playerXp = this.getXp( player );
        double maxXp = Config.getConfig( "maxXp" );
        double newLevelXp;
        String skillName = this.name().toLowerCase();

        if( !ignoreBonuses )
            addAmount *= XP.getMultiplier( player, this );

        newLevelXp = addAmount + playerXp;

        if( newLevelXp > maxXp )
            newLevelXp = maxXp;

        if( newLevelXp < 0 )
            newLevelXp = 0;

        if( newLevelXp > playerXp )
            XP.awardXp( player, this, "commandAdd", addAmount, false, true );
        else
            NetworkHandler.sendToPlayer( new MessageXp( newLevelXp, this.getValue(), 0, true ), player );
//        player.sendStatusMessage( new TranslationTextComponent( "pmmo.addXp", new TranslationTextComponent( "pmmo." + skillName ).getString(), DP.dp(addAmount) ), false );
        skillsTag.putDouble( skillName, newLevelXp );
        AttributeHandler.updateAll( player );
        XP.updateRecipes( player );
    }
}
