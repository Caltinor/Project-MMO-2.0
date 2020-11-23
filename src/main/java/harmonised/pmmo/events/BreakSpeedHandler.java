package harmonised.pmmo.events;

import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;

public class BreakSpeedHandler
{
    public static void handleBreakSpeed( net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed event )
    {
        EntityPlayer player = event.getEntityPlayer();

        Skill skill = XP.getSkill( event.getState().getMaterial() );
        if( skill.equals( Skill.INVALID_SKILL ) )
            return;
        String skillName = skill.toString();
        double speedBonus;
        int toolGap = XP.getSkillReqGap( player, player.getHeldItemMainhand().getItem().getRegistryName(), JType.REQ_TOOL );
        boolean reqMet = XP.checkReq( player, event.getState().getBlock().getRegistryName(), JType.REQ_BREAK );

        if( !reqMet )
        {
            player.sendStatusMessage( new TextComponentTranslation( "pmmo.notSkilledEnoughToBreak", new TextComponentTranslation( event.getState().getBlock().getUnlocalizedName() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
            event.setCanceled( true );
            return;
        }
        else if( toolGap > 0 )
        {
            player.sendStatusMessage( new TextComponentTranslation( "pmmo.notSkilledEnoughToUseAsTool", new TextComponentTranslation( player.getHeldItemMainhand().getUnlocalizedName() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
            if( FConfig.getConfig( "strictReqTool" ) == 1 )
            {
                event.setCanceled( true );
                return;
            }
        }

        int startLevel = Skill.getSkill( skillName ).getLevel( player );

        switch ( XP.correctHarvestTool( event.getState().getMaterial() ) )
        {
            case "pickaxe":
                double height = event.getPos().getY();
                if (height < 0)
                    height = -height;

                double blocksToUnbreakableY = FConfig.blocksToUnbreakableY;
                double heightMultiplier = 1 - ( height / blocksToUnbreakableY );

                if ( heightMultiplier < FConfig.minBreakSpeed )
                    heightMultiplier = FConfig.minBreakSpeed;

                speedBonus = FConfig.miningBonusSpeed / 100;
                event.setNewSpeed( event.getOriginalSpeed() * ( 1 + (startLevel - toolGap) * (float) speedBonus ) * ( (float) heightMultiplier ) );
                break;

            case "axe":
                speedBonus = FConfig.woodcuttingBonusSpeed / 100;
                event.setNewSpeed( event.getOriginalSpeed() * ( 1 + (startLevel - toolGap) * (float) speedBonus ) );
                break;

            case "shovel":
                speedBonus = FConfig.excavationBonusSpeed / 100;
                event.setNewSpeed( event.getOriginalSpeed() * ( 1 + (startLevel - toolGap) * (float) speedBonus ) );
                break;

            case "hoe":
                speedBonus = FConfig.farmingBonusSpeed / 100;
                event.setNewSpeed( event.getOriginalSpeed() * ( 1 + (startLevel - toolGap) * (float) speedBonus ) );
                break;

            default:
                event.setNewSpeed( event.getOriginalSpeed() );
                break;
        }

        event.setNewSpeed( event.getNewSpeed() / (toolGap + 1) );
    }
}