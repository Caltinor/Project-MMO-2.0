package harmonised.pmmo.skills;

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
    REPAIRING( 10 ),
    FLYING( 11 ),
    SWIMMING( 12 ),
    FISHING( 13 ),
    CRAFTING( 14 );

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

    public int getValue()
    {
        return this.value;
    }

}
