package harmonised.pmmo.events;

import harmonised.pmmo.ProjectMMOMod;
import harmonised.pmmo.config.AutoValues;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.Map;

public class BreakSpeedHandler
{
    private static long lastWarning = 0;

    public static void handleBreakSpeed( PlayerEvent.BreakSpeed event )
    {
        PlayerEntity player = event.getPlayer();
        if( player instanceof FakePlayer)
            return;
        String skill = XP.getSkill( event.getState() ).toLowerCase();
        double speedBonus;
        ItemStack itemStack = player.getHeldItemMainhand();

        ResourceLocation resLoc = itemStack.getItem().getRegistryName();
        if( resLoc == null )
            return;
        Map<String, Double> toolReq = XP.getJsonMap( resLoc, JType.REQ_TOOL );
        if( Config.getConfig( "toolReqEnabled" ) != 0 && Config.getConfig( "autoGenerateValuesEnabled" ) != 0 && Config.getConfig( "autoGenerateToolReqDynamicallyEnabled" ) != 0 )
        {
            Map<String, Double> dynToolReq = AutoValues.getToolReqFromStack( itemStack );
            for( Map.Entry<String, Double> entry : dynToolReq.entrySet() )
            {
                if( !toolReq.containsKey( entry.getKey() ) )
                    toolReq.put( entry.getKey(), Math.max( 1, entry.getValue() ) );
            }
        }

        //TINKERS
        int tinkersMaterialsReqGap = 0;
        if( ProjectMMOMod.tinkersLoaded )
        {
            ListNBT tinkerTags = (ListNBT) itemStack.getOrCreateTag().get( "tic_materials" );
            if( tinkerTags != null )
            {
                for( INBT iNbtTag : tinkerTags )
                {
                    String tag = iNbtTag.getString();
                    Map<String, Double> tinkersMaterialsReqMap = XP.getJsonMap( tag, JType.REQ_TINKERS_MATERIALS );
                    boolean materialReqMet = XP.checkReq( player, tinkersMaterialsReqMap );
                    tinkersMaterialsReqGap = Math.max( tinkersMaterialsReqGap, XP.getSkillReqGap( player, tinkersMaterialsReqMap ) );

                    if( !materialReqMet && System.currentTimeMillis() - lastWarning > 3251 )
                    {
                        lastWarning = System.currentTimeMillis();
                        player.sendStatusMessage( new TranslationTextComponent( "pmmo.notSkilledEnoughToUseTinkersMaterial", tag ).setStyle( XP.textStyle.get( "red" ) ), true );
                        player.sendStatusMessage( new TranslationTextComponent( "pmmo.notSkilledEnoughToUseTinkersMaterial", tag ).setStyle( XP.textStyle.get( "red" ) ), false );

                        for( Map.Entry<String, Double> entry : tinkersMaterialsReqMap.entrySet() )
                        {
                            if( Skill.getLevel( entry.getKey(), player ) < entry.getValue() )
                                player.sendStatusMessage( new TranslationTextComponent( "pmmo.levelDisplay", new TranslationTextComponent( "pmmo." + entry.getKey() ), "" + (int) Math.floor( entry.getValue() ) ).setStyle( XP.textStyle.get( "red" ) ), false );
                            else
                                player.sendStatusMessage( new TranslationTextComponent( "pmmo.levelDisplay", new TranslationTextComponent( "pmmo." + entry.getKey() ), "" + (int) Math.floor( entry.getValue() ) ).setStyle( XP.textStyle.get( "green" ) ), false );
                        }
                    }
                }
            }
        }
        //END OF TINKERS

        int toolGap = XP.getSkillReqGap( player, toolReq );
        int enchantGap = XP.getSkillReqGap( player, XP.getEnchantsUseReq( player.getHeldItemMainhand() ) );
        int gap = Math.max( Math.max( toolGap, enchantGap ), tinkersMaterialsReqGap );
        boolean breakReqMet = XP.checkReq( player, event.getState().getBlock().getRegistryName(), JType.REQ_BREAK );

        if( !breakReqMet )
        {
            player.sendStatusMessage( new TranslationTextComponent( "pmmo.notSkilledEnoughToBreak", new TranslationTextComponent( event.getState().getBlock().getTranslationKey() ) ).setStyle( XP.textStyle.get( "red" ) ), true );
            event.setCanceled( true );
            return;
        }
        else if( gap > 0 )
        {
            if( enchantGap < gap && tinkersMaterialsReqGap < gap )
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