package harmonised.pmmo.events;

import harmonised.pmmo.config.AutoValues;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.Map;

public class BreakSpeedHandler
{
    public static void handleBreakSpeed( PlayerEvent.BreakSpeed event )
    {
        PlayerEntity player = event.getPlayer();

        String skill = XP.getSkill( event.getState() );
        double speedBonus;
        ItemStack itemStack = player.getHeldItemMainhand();
        ResourceLocation resLoc = itemStack.getItem().getRegistryName();
        if( resLoc == null )
            return;
        Map<String, Double> toolReq = XP.getJsonMap( resLoc, JType.REQ_TOOL );
        if( Config.getConfig( "autoGenerateValuesEnabled" ) != 0 && Config.getConfig( "autoGenerateToolReqDynamicallyEnabled" ) != 0 )
        {
            Map<String, Double> dynToolReq = AutoValues.getToolReqFromStack( itemStack );
            for( Map.Entry<String, Double> entry : dynToolReq.entrySet() )
            {
                if( !toolReq.containsKey( entry.getKey() ) )
                    toolReq.put( entry.getKey(), Math.max( 1, entry.getValue() ) );
            }
        }
        int toolGap = XP.getSkillReqGap( player, toolReq );
        int enchantGap = XP.getSkillReqGap( player, XP.getEnchantsUseReq( player.getHeldItemMainhand() ) );
        int gap = Math.max( toolGap, enchantGap );
        boolean reqMet = XP.checkReq( player, event.getState().getBlock().getRegistryName(), JType.REQ_BREAK );

        if( !reqMet )
        {
            player.sendStatusMessage( new TranslationTextComponent( "pmmo.notSkilledEnoughToBreak", new TranslationTextComponent( event.getState().getBlock().getTranslationKey() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
            event.setCanceled( true );
            return;
        }
        else if( gap > 0 )
        {
            if( enchantGap < gap )
                player.sendStatusMessage( new TranslationTextComponent( "pmmo.notSkilledEnoughToUseAsTool", new TranslationTextComponent( player.getHeldItemMainhand().getTranslationKey() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
            if( Config.getConfig( "strictReqTool" ) == 1 )
            {
                event.setCanceled( true );
                return;
            }
        }

        int startLevel = Skill.getLevel( skill, player );

        switch ( XP.getHarvestTool( event.getState() ) )
        {
            case "pickaxe":
                float height = event.getPos().getY();
                if (height < 0)
                    height = -height;

                double blocksToUnbreakableY = Config.forgeConfig.blocksToUnbreakableY.get();
                double heightMultiplier = 1 - ( height / blocksToUnbreakableY );

                if ( heightMultiplier < Config.forgeConfig.minBreakSpeed.get() )
                    heightMultiplier = Config.forgeConfig.minBreakSpeed.get();

                speedBonus = Config.forgeConfig.miningBonusSpeed.get() / 100;
                event.setNewSpeed( event.getOriginalSpeed() * ( 1 + (startLevel - toolGap) * (float) speedBonus ) * ( (float) heightMultiplier ) );
                break;

            case "axe":
                speedBonus = Config.forgeConfig.woodcuttingBonusSpeed.get() / 100;
                event.setNewSpeed( event.getOriginalSpeed() * ( 1 + (startLevel - toolGap) * (float) speedBonus ) );
                break;

            case "shovel":
                speedBonus = Config.forgeConfig.excavationBonusSpeed.get() / 100;
                event.setNewSpeed( event.getOriginalSpeed() * ( 1 + (startLevel - toolGap) * (float) speedBonus ) );
                break;

            case "hoe":
                speedBonus = Config.forgeConfig.farmingBonusSpeed.get() / 100;
                event.setNewSpeed( event.getOriginalSpeed() * ( 1 + (startLevel - toolGap) * (float) speedBonus ) );
                break;

            default:
                event.setNewSpeed( event.getOriginalSpeed() );
                break;
        }

        event.setNewSpeed( event.getNewSpeed() / (toolGap + 1) );
    }
}