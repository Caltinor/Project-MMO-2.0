package harmonised.pmmo.commands;


import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import net.minecraft.command.CommandException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.Map;

public class CheckBiomeCommand
{
    public static int execute(  ) throws CommandException
    {
        EntityPlayer sender = (EntityPlayer) ;
        String biomeKey = sender.world.getBiome( sender.getPosition() ).getRegistryName().toString();
        String transKey = sender.world.getBiome( sender.getPosition() ).getTranslationKey();
        Map<String, Double> theMap = JsonConfig.data.get( JType.BIOME_MOB_MULTIPLIER ).get( biomeKey );

        String damageBonus = "100";
        String hpBonus = "100";
        String speedBonus = "100";

        if( theMap != null )
        {
            if( theMap.containsKey( "damageBonus" ) )
                damageBonus = DP.dp( theMap.get( "damageBonus" ) * 100 );
            if( theMap.containsKey( "hpBonus" ) )
                hpBonus = DP.dp( theMap.get( "hpBonus" ) * 100 );
            if( theMap.containsKey( "damageBonus" ) )
                speedBonus = DP.dp( theMap.get( "damageBonus" ) * 100 );
        }

        sender.sendStatusMessage( new TextComponentTranslation( "pmmo.mobDamageBoost", new TextComponentTranslation( damageBonus ).setStyle( XP.textStyle.get( "grey" ) ), new TextComponentTranslation( transKey ).setStyle( XP.textStyle.get( "grey" ) ) ), false );
        sender.sendStatusMessage( new TextComponentTranslation( "pmmo.mobHpBoost", new TextComponentTranslation( hpBonus ).setStyle( XP.textStyle.get( "grey" ) ), new TextComponentTranslation( transKey ).setStyle( XP.textStyle.get( "grey" ) ) ), false );
        sender.sendStatusMessage( new TextComponentTranslation( "pmmo.mobSpeedBoost", new TextComponentTranslation( speedBonus ).setStyle( XP.textStyle.get( "grey" ) ), new TextComponentTranslation( transKey ).setStyle( XP.textStyle.get( "grey" ) ) ), false );

        return 1;
    }
}
