package harmonised.pmmo.skills;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.network.MessageXp;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    ENGINEERING( 20 ),
    BLOOD_MAGIC( 21 ),
    ASTRAL_MAGIC( 22 ),
    GOOD_MAGIC( 23 ),
    EVIL_MAGIC( 24 ),
    ARCANE_MAGIC( 25 ),
    ELEMENTAL( 26 ),
    EARTH( 27 ),
    WATER( 28 ),
    AIR( 29 ),
    FIRE( 30 ),
    LIGHTNING( 31 ),
    VOID( 32 ),
    THAUMATIC( 33 ),
    SUMMONING( 34 ),
    INVENTION( 35 ),
    RUNECRAFTING( 36 ),
    PRAYER( 37 ),
    COOKING( 38 ),
    FIREMAKING( 39 ),
    TRADING( 41 ),
    SAILING( 42 ),
    ALCHEMY( 43 ),
    CONSTRUCTION( 44 ),
    LEATHERWORKING( 45 );

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
        if( player.world.isRemote() )
            return XP.levelAtXp( XP.getOfflineXp( this, player.getUniqueID() ) );
        else
            return PmmoSavedData.get( player ).getLevel( this, player.getUniqueID() );
    }

    public int getLevel( UUID uuid )
    {
        return PmmoSavedData.get().getLevel( this, uuid );
    }

    public double getLevelDecimal( PlayerEntity player )
    {
        if( player.world.isRemote() )
            return XP.levelAtXpDecimal( XP.getOfflineXp( this, player.getUniqueID() ) );
        else
            return PmmoSavedData.get( player ).getLevelDecimal( this, player.getUniqueID() );
    }

    public double getLevelDecimal( UUID uuid )
    {
        return PmmoSavedData.get().getLevelDecimal( this, uuid );
    }

    public double getXp( PlayerEntity player )
    {
        if( player.world.isRemote() )
            return XP.getOfflineXp( this, player.getUniqueID() );
        else
            return PmmoSavedData.get( player ).getXp( this, player.getUniqueID() );
    }

    public double getXp( UUID uuid )
    {
        return PmmoSavedData.get().getXp( this, uuid );
    }

    public void setLevel( ServerPlayerEntity player, double amount )
    {
        this.setXp( player, XP.xpAtLevelDecimal( amount ) );
    }

    public void setXp( UUID uuid, double amount )
    {
        ServerPlayerEntity player = PmmoSavedData.server.getPlayerList().getPlayerByUUID( uuid );

        if( player == null )
            PmmoSavedData.get().setXp( this, uuid, amount );
        else
            setXp( player, amount );
    }

    public void setXp( ServerPlayerEntity player, double amount )
    {
        if( PmmoSavedData.get( player ).setXp( this, player.getUniqueID(), amount ) )
        {
            AttributeHandler.updateAll( player );
            XP.updateRecipes( player );

            NetworkHandler.sendToPlayer( new MessageXp( amount, this.getValue(), 0, false ), (ServerPlayerEntity) player );
        }
    }

    public void addLevel( UUID uuid, double amount, String sourceName, boolean skip, boolean ignoreBonuses )
    {
        double missingXp = XP.xpAtLevelDecimal( this.getLevelDecimal( uuid ) + amount ) - this.getXp( uuid );

        this.addXp( uuid, missingXp, sourceName, skip, ignoreBonuses );
    }

    public void addLevel( ServerPlayerEntity player, double amount, String sourceName, boolean skip, boolean ignoreBonuses )
    {
        double missingXp = XP.xpAtLevelDecimal( this.getLevelDecimal( player ) + amount ) - this.getXp( player );

        this.addXp( player, missingXp, sourceName, skip, ignoreBonuses );
    }

    public void addXp( UUID uuid, double amount, String sourceName, boolean skip, boolean ignoreBonuses )
    {
        ServerPlayerEntity player = PmmoSavedData.server.getPlayerList().getPlayerByUUID( uuid );

        if( player == null )
            PmmoSavedData.get().scheduleXp( this, uuid, amount, sourceName );
        else
            addXp( player, amount, sourceName, skip, ignoreBonuses );
    }

    public void addXp( ServerPlayerEntity player, double amount, String sourceName, boolean skip, boolean ignoreBonuses )
    {
        XP.awardXp( player, this, sourceName, amount, skip, ignoreBonuses );
    }
}