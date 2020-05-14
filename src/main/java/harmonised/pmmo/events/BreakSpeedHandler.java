package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.XP;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class BreakSpeedHandler
{
    public static void handleBreakSpeed( PlayerEvent.BreakSpeed event )
    {
        PlayerEntity player = event.getPlayer();

        String skill = XP.getSkill( event.getState().getMaterial() ).name().toLowerCase();
        double speedBonus = 0;

        int toolGap = XP.getSkillReqGap( player, player.getHeldItemMainhand().getItem().getRegistryName(), "tool" );

        if( toolGap > 0 )
            player.sendStatusMessage( new TranslationTextComponent( "pmmo.toUseAsTool", new TranslationTextComponent( player.getHeldItemMainhand().getTranslationKey() ) ).setStyle( XP.textStyle.get( "red" ) ), true );

        int startLevel = XP.getLevel( Skill.getSkill( skill ), player );

        switch ( XP.correctHarvestTool( event.getState().getMaterial() ) )
        {
            case "pickaxe":
                float height = event.getPos().getY();
                if (height < 0)
                    height = -height;

                double blocksToUnbreakableY = Config.config.blocksToUnbreakableY.get();
                double heightMultiplier = 1 - ( height / blocksToUnbreakableY );

                if ( heightMultiplier < Config.config.minBreakSpeed.get() )
                    heightMultiplier = Config.config.minBreakSpeed.get();

                speedBonus = Config.config.miningBonusSpeed.get() / 100;
                event.setNewSpeed( event.getOriginalSpeed() * ( 1 + (startLevel - toolGap) * (float) speedBonus ) * ( (float) heightMultiplier ) );
                break;

            case "axe":
                speedBonus = Config.config.woodcuttingBonusSpeed.get() / 100;
                event.setNewSpeed( event.getOriginalSpeed() * ( 1 + (startLevel - toolGap) * (float) speedBonus ) );
                break;

            case "shovel":
                speedBonus = Config.config.excavationBonusSpeed.get() / 100;
                event.setNewSpeed( event.getOriginalSpeed() * ( 1 + (startLevel - toolGap) * (float) speedBonus ) );
                break;

            case "hoe":
                speedBonus = Config.config.farmingBonusSpeed.get() / 100;
                event.setNewSpeed( event.getOriginalSpeed() * ( 1 + (startLevel - toolGap) * (float) speedBonus ) );
                break;

            default:
                event.setNewSpeed( event.getOriginalSpeed() );
                break;
        }

        event.setNewSpeed( event.getNewSpeed() / (toolGap + 1) );
    }
}
