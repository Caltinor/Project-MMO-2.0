package harmonised.pmmo.skills;

import harmonised.pmmo.network.MessageXp;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.util.DP;
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
    BUILDING(2),
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
    FLETCHING( 17 );

    private static final Logger LOGGER = LogManager.getLogger();
    private static Map< String, Integer > map = new HashMap<>();

    private int value;

    static
    {
        for( Skill skill : Skill.values() )
        {
            map.put( skill.name().toLowerCase(), skill.value );
        }
    }

    Skill( int i )
    {
        this.value = i;
    }

    public static int getInt( String i )
    {
        if( map.get( i.toLowerCase() ) != null )
            return map.get( i.toLowerCase() );
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
            double maxXp = XP.getConfig( "maxXp" );

            if( setAmount > maxXp )
                setAmount = maxXp;

            if( setAmount < 0 )
                setAmount = 0;

            skillsTag.putDouble( this.name().toLowerCase(), setAmount );
            AttributeHandler.updateAll( player );

            NetworkHandler.sendToPlayer( new MessageXp( setAmount, this.getValue(), 0, false ), (ServerPlayerEntity) player );
        }
        else
            LOGGER.error( "Invalid skill at method setXp" );
    }

    public void addLevel( ServerPlayerEntity player, double addAmount )
    {
        double missingXp = XP.xpAtLevelDecimal( this.getLevel( player ) + addAmount ) - this.getXp( player );

        this.addXp( player, missingXp );
    }

    public void addXp( ServerPlayerEntity player, double addAmount )
    {
        CompoundNBT skillsTag = XP.getSkillsTag( player );
        double playerXp = this.getXp( player );
        double maxXp = XP.getConfig( "maxXp" );
        double newLevelXp;
        String skillName = this.name().toLowerCase();

        newLevelXp = addAmount + playerXp;

        if( newLevelXp > maxXp )
            newLevelXp = maxXp;

        if( newLevelXp < 0 )
            newLevelXp = 0;

        if( newLevelXp > playerXp )
            XP.awardXp( player, this, "commandAdd", addAmount, false );
        else
            NetworkHandler.sendToPlayer( new MessageXp( newLevelXp, this.getValue(), 0, true ), player );
        skillsTag.putDouble( skillName, newLevelXp );

        player.sendStatusMessage( new TranslationTextComponent( "pmmo.addXp", skillName, DP.dp(addAmount) ), false );

        AttributeHandler.updateAll( player );
    }
}
