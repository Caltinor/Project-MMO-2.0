package harmonised.pmmo.events;

import harmonised.pmmo.api.TooltipSupplier;
import harmonised.pmmo.config.AutoValues;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.Map;

public class BreakSpeedHandler
{
    public static void handleBreakSpeed( PlayerEvent.BreakSpeed event )
    {
        Player player = event.getPlayer();
        if( player instanceof FakePlayer)
            return;
        String skill = XP.getSkill( event.getState() ).toLowerCase();
        double speedBonus;
        ItemStack itemStack = player.getMainHandItem();

        ResourceLocation resLoc = itemStack.getItem().getRegistryName();
        if( resLoc == null )
            return;
        Map<String, Double> toolReq = TooltipSupplier.getTooltipData(resLoc, JType.REQ_TOOL, itemStack);
        if( Config.getConfig( "toolReqEnabled" ) != 0 && Config.getConfig( "autoGenerateValuesEnabled" ) != 0 && Config.getConfig( "autoGenerateToolReqDynamicallyEnabled" ) != 0 )
        {
            Map<String, Double> dynToolReq = AutoValues.getToolReqFromStack( itemStack );
            for( Map.Entry<String, Double> entry : dynToolReq.entrySet() )
            {
                if( !toolReq.containsKey( entry.getKey() ) )
                    toolReq.put( entry.getKey(), Math.max( 1, entry.getValue() ) );
            }
        }

        int toolGap = XP.getSkillReqGap( player, toolReq );
        int enchantGap = XP.getSkillReqGap( player, XP.getEnchantsUseReq( player.getMainHandItem() ) );
        int gap = Math.max( toolGap, enchantGap );
        boolean breakReqMet = event.getState().hasBlockEntity()
        		? XP.checkReq( player, event.getEntity().getCommandSenderWorld().getBlockEntity(event.getPos()), JType.REQ_BREAK)
        		: XP.checkReq( player, event.getState().getBlock().getRegistryName(), JType.REQ_BREAK );

        if( !breakReqMet )
        {
            player.displayClientMessage( new TranslatableComponent( "pmmo.notSkilledEnoughToBreak", new TranslatableComponent( event.getState().getBlock().getDescriptionId() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
            event.setCanceled( true );
            return;
        }
        else if( gap > 0 )
        {
            if( enchantGap < gap )
                player.displayClientMessage( new TranslatableComponent( "pmmo.notSkilledEnoughToUseAsTool", new TranslatableComponent( player.getMainHandItem().getDescriptionId() ) ).setStyle( XP.textStyle.get( "red" ) ), true );

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