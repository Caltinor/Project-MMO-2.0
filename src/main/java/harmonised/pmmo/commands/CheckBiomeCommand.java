package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Map;

public class CheckBiomeCommand
{
    public static int execute(CommandContext<CommandSource> context) throws CommandException
    {
        PlayerEntity sender = (PlayerEntity) context.getSource().getEntity();
        String biomeKey = sender.world.getBiome( XP.vecToBlock( sender.getPositionVec() ) ).getRegistryName().toString();
        String transKey = sender.world.getBiome( XP.vecToBlock( sender.getPositionVec() ) ).getTranslationKey();
        Map<String, Object> theMap = JsonConfig.data.get( JType.BIOME_MOB_MULTIPLIER ).get( biomeKey );

        String damageBonus = "100";
        String hpBonus = "100";
        String speedBonus = "100";

        if( theMap != null )
        {
            if( theMap.containsKey( "damageBonus" ) )
                damageBonus = DP.dp( (double) theMap.get( "damageBonus" ) * 100 );
            if( theMap.containsKey( "hpBonus" ) )
                hpBonus = DP.dp( (double) theMap.get( "hpBonus" ) * 100 );
            if( theMap.containsKey( "damageBonus" ) )
                speedBonus = DP.dp( (double) theMap.get( "damageBonus" ) * 100 );
        }

        sender.sendStatusMessage( new TranslationTextComponent( "pmmo.mobDamageBoost", new TranslationTextComponent( damageBonus ).func_240703_c_( XP.textStyle.get( "grey" ) ), new TranslationTextComponent( transKey ).func_240703_c_( XP.textStyle.get( "grey" ) ) ), false );
        sender.sendStatusMessage( new TranslationTextComponent( "pmmo.mobHpBoost", new TranslationTextComponent( hpBonus ).func_240703_c_( XP.textStyle.get( "grey" ) ), new TranslationTextComponent( transKey ).func_240703_c_( XP.textStyle.get( "grey" ) ) ), false );
        sender.sendStatusMessage( new TranslationTextComponent( "pmmo.mobSpeedBoost", new TranslationTextComponent( speedBonus ).func_240703_c_( XP.textStyle.get( "grey" ) ), new TranslationTextComponent( transKey ).func_240703_c_( XP.textStyle.get( "grey" ) ) ), false );

        return 1;
    }
}
