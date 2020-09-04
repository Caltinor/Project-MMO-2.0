package harmonised.pmmo.events;

import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import java.util.List;
import java.util.Map;

public class TooltipHandler
{
    public static boolean tooltipOn = true;
    private static String lastKey = "";
    private static int salvageArrayPos = 0, salvageArrayLength;
    private static long lastTime = System.nanoTime();
    private static Object[] salvageArray;

    public static void handleTooltip( ItemTooltipEvent event )
    {
        {
            if( !tooltipOn )
                return;

            PlayerEntity player = event.getPlayer();

            if( player != null )
            {
                ItemStack itemStack = event.getItemStack();
                Item item = itemStack.getItem();
                List<ITextComponent> tooltip = event.getToolTip();
                int level;

                if( item.getRegistryName() == null )
                    return;

                String regKey = item.getRegistryName().toString();
                float hardness;
                double dValue;
                Material material = null;

                Map<String, Object> craftReq = JsonConfig.data.get( JType.REQ_CRAFT ).get( regKey );
                Map<String, Object> wearReq = JsonConfig.data.get( JType.REQ_WEAR ).get( regKey );
                Map<String, Object> toolReq = JsonConfig.data.get( JType.REQ_TOOL ).get( regKey );
                Map<String, Object> weaponReq = JsonConfig.data.get( JType.REQ_WEAPON ).get( regKey );
                Map<String, Object> useReq = JsonConfig.data.get( JType.REQ_USE ).get( regKey );
                Map<String, Object> placeReq = JsonConfig.data.get( JType.REQ_PLACE ).get( regKey );
                Map<String, Object> breakReq = JsonConfig.data.get( JType.REQ_BREAK ).get( regKey );
                Map<String, Object> xpValueGeneral = JsonConfig.data.get( JType.XP_VALUE_GENERAL ).get( regKey );
                Map<String, Object> xpValueBreaking = JsonConfig.data.get( JType.XP_VALUE_BREAK ).get( regKey );
                Map<String, Object> xpValueCrafting = JsonConfig.data.get( JType.XP_VALUE_CRAFT ).get( regKey );
                Map<String, Object> xpValueSmelting = JsonConfig.data.get( JType.XP_VALUE_SMELT ).get( regKey );
                Map<String, Object> xpValueCooking = JsonConfig.data.get( JType.XP_VALUE_COOK ).get( regKey );
                Map<String, Object> xpValueBrewing = JsonConfig.data.get( JType.XP_VALUE_BREW ).get( regKey );
                Map<String, Object> xpValueGrowing = JsonConfig.data.get( JType.XP_VALUE_GROW ).get( regKey );
                Map<String, Object> salvageInfo = JsonConfig.data.get( JType.SALVAGE_TO ).get( regKey );
                Map<String, Object> salvagesFrom = JsonConfig.data.get( JType.SALVAGE_FROM ).get( regKey );
                Map<String, Object> heldItemXpBoost = JsonConfig.data.get( JType.XP_BONUS_HELD ).get( regKey );
                Map<String, Object> wornItemXpBoost = JsonConfig.data.get( JType.XP_BONUS_WORN ).get( regKey );

                if( item instanceof BlockItem)
                {
                    material = ( (BlockItem) item).getBlock().getDefaultState().getMaterial();
                    hardness = ((BlockItem) item).getBlock().getDefaultState().getBlockHardness( null, null );
                    if( hardness > 0 )
                        tooltip.add( new TranslationTextComponent( "pmmo.levelDisplay", " " + new TranslationTextComponent( "pmmo.hardness", DP.dp( hardness ) ).getString() ) );
//                    tooltip.add( new StringTextComponent( XP.checkMaterial( material ) + " " + XP.getSkill( material ) ) );
                }

                if( xpValueGeneral != null && xpValueGeneral.size() > 0 )      //XP GENERAL
                {
                    tooltip.add( new TranslationTextComponent( "pmmo.xpValueBreak" ) );

                    for( String key : xpValueGeneral.keySet() )
                    {
                        if( xpValueGeneral.get( key ) instanceof Double )
                        {
                            dValue = (double) xpValueGeneral.get( key );
                            tooltip.add( new TranslationTextComponent( "pmmo.levelDisplay", " " + new TranslationTextComponent( "pmmo." + key ).getString(), DP.dp( dValue ) ).setStyle( XP.getSkillStyle( Skill.getSkill( key ) ) ) );
                        }
                    }
                }

                if( xpValueBreaking != null && xpValueBreaking.size() > 0 )      //XP BREAK
                {
                    tooltip.add( new TranslationTextComponent( "pmmo.xpValueBreak" ) );

                    for( String key : xpValueBreaking.keySet() )
                    {
                        if( xpValueBreaking.get( key ) instanceof Double )
                        {
                            dValue = (double) xpValueBreaking.get( key );
                            tooltip.add( new TranslationTextComponent( "pmmo.levelDisplay", " " + new TranslationTextComponent( "pmmo." + key ).getString(), DP.dp( dValue ) ).setStyle( XP.getSkillStyle( Skill.getSkill( key ) ) ) );
                        }
                    }
                }

                if( xpValueCrafting != null && xpValueCrafting.size() > 0 )      //XP CRAFT
                {
                    tooltip.add( new TranslationTextComponent( "pmmo.xpValueCraft" ) );

                    for( String key : xpValueCrafting.keySet() )
                    {
                        if( xpValueCrafting.get( key ) instanceof Double )
                        {
                            dValue = (double) xpValueCrafting.get( key );
                            tooltip.add( new TranslationTextComponent( "pmmo.levelDisplay", " " + new TranslationTextComponent( "pmmo." + key ).getString(), DP.dp( dValue ) ).setStyle( XP.getSkillStyle( Skill.getSkill( key ) ) ) );
                        }
                    }
                }

                if( xpValueSmelting != null && xpValueSmelting.size() > 0 )      //XP SMELT
                {
                    tooltip.add( new TranslationTextComponent( "pmmo.xpValueSmelt" ) );

                    for( String key : xpValueSmelting.keySet() )
                    {
                        if( xpValueSmelting.get( key ) instanceof Double )
                        {
                            dValue = (double) xpValueSmelting.get( key );
                            tooltip.add( new TranslationTextComponent( "pmmo.levelDisplay", " " + new TranslationTextComponent( "pmmo." + key ).getString(), DP.dp( dValue ) ).setStyle( XP.getSkillStyle( Skill.getSkill( key ) ) ) );
                        }
                    }
                }

                if( xpValueCooking != null && xpValueCooking.size() > 0 )      //XP COOK
                {
                    tooltip.add( new TranslationTextComponent( "pmmo.xpValueCook" ) );

                    for( String key : xpValueCooking.keySet() )
                    {
                        if( xpValueCooking.get( key ) instanceof Double )
                        {
                            dValue = (double) xpValueCooking.get( key );
                            tooltip.add( new TranslationTextComponent( "pmmo.levelDisplay", " " + new TranslationTextComponent( "pmmo." + key ).getString(), DP.dp( dValue ) ).setStyle( XP.getSkillStyle( Skill.getSkill( key ) ) ) );
                        }
                    }
                }

                if( xpValueBrewing != null && xpValueBrewing.size() > 0 )      //XP BREW
                {
                    tooltip.add( new TranslationTextComponent( "pmmo.xpValueBrew" ) );

                    for( String key : xpValueBrewing.keySet() )
                    {
                        if( xpValueBrewing.get( key ) instanceof Double )
                        {
                            dValue = (double) xpValueBrewing.get( key );
                            tooltip.add( new TranslationTextComponent( "pmmo.levelDisplay", " " + new TranslationTextComponent( "pmmo." + key ).getString(), DP.dp( dValue ) ).setStyle( XP.getSkillStyle( Skill.getSkill( key ) ) ) );
                        }
                    }
                }
                if( xpValueGrowing != null && xpValueGrowing.size() > 0 )      //XP GROW
                {
                    tooltip.add( new TranslationTextComponent( "pmmo.xpValueGrow" ) );

                    for( String key : xpValueGrowing.keySet() )
                    {
                        if( xpValueGrowing.get( key ) instanceof Double )
                        {
                            dValue = (double) xpValueGrowing.get( key );
                            tooltip.add( new TranslationTextComponent( "pmmo.levelDisplay", " " + new TranslationTextComponent( "pmmo." + key ).getString(), DP.dp( dValue ) ).setStyle( XP.getSkillStyle( Skill.getSkill( key ) ) ) );
                        }
                    }
                }

                if( craftReq != null && craftReq.size() > 0 )
                    addTooltipTextSkill( "pmmo.toCraft", JType.REQ_CRAFT, craftReq, event );

                if( wearReq != null && wearReq.size() > 0 )
                    addTooltipTextSkill( "pmmo.toWear", JType.REQ_WEAR, wearReq, event );

                if( wornItemXpBoost != null && wornItemXpBoost.size() > 0 )
                    addTooltipTextSkillPercentage( "pmmo.itemXpBoostWorn", wornItemXpBoost, event );

                if( toolReq != null && toolReq.size() > 0 )
                    addTooltipTextSkill( "pmmo.tool", JType.REQ_TOOL, toolReq, event );
                if( weaponReq != null && weaponReq.size() > 0 )
                    addTooltipTextSkill( "pmmo.weapon", JType.REQ_WEAPON, weaponReq, event );

                if( heldItemXpBoost != null && heldItemXpBoost.size() > 0 )
                    addTooltipTextSkillPercentage( "pmmo.itemXpBoostHeld", heldItemXpBoost, event );

                if( useReq != null && useReq.size() > 0 )
                    addTooltipTextSkill( "pmmo.use", JType.REQ_USE, useReq, event );

                if( placeReq != null && placeReq.size() > 0 )
                {
                    if( JsonConfig.data.get( JType.INFO_PLANT ).containsKey( item.getRegistryName().toString() ) || item instanceof IPlantable)
                        addTooltipTextSkill( "pmmo.plant", JType.REQ_PLACE, placeReq, event );
                    else
                        addTooltipTextSkill( "pmmo.place", JType.REQ_PLACE, placeReq, event );
                }

                if( breakReq != null && breakReq.size() > 0 )
                {
                    if( XP.correctHarvestTool( material ).equals( "axe" ) )
                        addTooltipTextSkill( "pmmo.chop", JType.REQ_BREAK, breakReq, event );
                    else if( JsonConfig.data.get( JType.INFO_PLANT ).containsKey( item.getRegistryName().toString() ) || item instanceof IPlantable )
                        addTooltipTextSkill( "pmmo.harvest", JType.REQ_BREAK, breakReq, event );
                    else
                        addTooltipTextSkill( "pmmo.break", JType.REQ_BREAK, breakReq, event );
                }

                if( JsonConfig.data.get( JType.INFO_ORE ).containsKey( regKey ) && JsonConfig.data.get( JType.INFO_ORE ).get( regKey ).containsKey( "extraChance" ) )
                {
                    if( XP.getExtraChance( player, item.getRegistryName(), JType.INFO_ORE ) > 0 )  //ORE EXTRA CHANCE
                        tooltip.add( new TranslationTextComponent( "pmmo.oreExtraDrop", DP.dp( XP.getExtraChance( player, item.getRegistryName(), JType.INFO_ORE ) / 100 ) ).setStyle( XP.textStyle.get( "green" ) ) );
                    else
                        tooltip.add( new TranslationTextComponent( "pmmo.oreExtraDrop", 0 ).setStyle( XP.textStyle.get( "red" ) ) );
                }

                if( JsonConfig.data.get( JType.INFO_LOG ).containsKey( regKey ) && JsonConfig.data.get( JType.INFO_LOG ).get( regKey ).containsKey( "extraChance" ) )
                {
                    if( XP.getExtraChance( player, item.getRegistryName(), JType.INFO_LOG ) > 0 )  //ORE EXTRA CHANCE
                        tooltip.add( new TranslationTextComponent( "pmmo.logExtraDrop", DP.dp( XP.getExtraChance( player, item.getRegistryName(), JType.INFO_LOG ) / 100 ) ).setStyle( XP.textStyle.get( "green" ) ) );
                    else
                        tooltip.add( new TranslationTextComponent( "pmmo.logExtraDrop", 0 ).setStyle( XP.textStyle.get( "red" ) ) );
                }

                if( JsonConfig.data.get( JType.INFO_PLANT ).containsKey( regKey ) && JsonConfig.data.get( JType.INFO_PLANT ).get( regKey ).containsKey( "extraChance" ) )
                {
                    if( XP.getExtraChance( player, item.getRegistryName(), JType.INFO_PLANT ) > 0 )  //ORE EXTRA CHANCE
                        tooltip.add( new TranslationTextComponent( "pmmo.plantExtraDrop", DP.dp( XP.getExtraChance( player, item.getRegistryName(), JType.INFO_PLANT ) / 100 ) ).setStyle( XP.textStyle.get( "green" ) ) );
                    else
                        tooltip.add( new TranslationTextComponent( "pmmo.plantExtraDrop", 0 ).setStyle( XP.textStyle.get( "red" ) ) );
                }

                if( JsonConfig.data.get( JType.INFO_SMELT ).containsKey( regKey ) && JsonConfig.data.get( JType.INFO_SMELT ).get( regKey ).containsKey( "extraChance" ) )
                {
                    if( XP.getExtraChance( player, item.getRegistryName(), JType.INFO_SMELT ) > 0 )  //SMELT EXTRA CHANCE
                        tooltip.add( new TranslationTextComponent( "pmmo.smeltExtraDrop", DP.dp( XP.getExtraChance( player, item.getRegistryName(), JType.INFO_SMELT ) / 100 ) ).setStyle( XP.textStyle.get( "green" ) ) );
                    else
                        tooltip.add( new TranslationTextComponent( "pmmo.smeltExtraDrop", 0 ).setStyle( XP.textStyle.get( "red" ) ) );
                }

                if( JsonConfig.data.get( JType.INFO_COOK ).containsKey( regKey ) && JsonConfig.data.get( JType.INFO_COOK ).get( regKey ).containsKey( "extraChance" ) )
                {
                    if( XP.getExtraChance( player, item.getRegistryName(), JType.INFO_COOK ) > 0 )  //COOK EXTRA CHANCE
                        tooltip.add( new TranslationTextComponent( "pmmo.cookExtraDrop", DP.dp( XP.getExtraChance( player, item.getRegistryName(), JType.INFO_COOK ) / 100 ) ).setStyle( XP.textStyle.get( "green" ) ) );
                    else
                        tooltip.add( new TranslationTextComponent( "pmmo.cookExtraDrop", 0 ).setStyle( XP.textStyle.get( "red" ) ) );
                }

                if( JsonConfig.data.get( JType.INFO_BREW ).containsKey( regKey ) && JsonConfig.data.get( JType.INFO_BREW ).get( regKey ).containsKey( "extraChance" ) )
                {
                    if( XP.getExtraChance( player, item.getRegistryName(), JType.INFO_BREW ) > 0 )  //BREW EXTRA CHANCE
                        tooltip.add( new TranslationTextComponent( "pmmo.brewExtraDrop", DP.dp( XP.getExtraChance( player, item.getRegistryName(), JType.INFO_BREW ) / 100 ) ).setStyle( XP.textStyle.get( "green" ) ) );
                    else
                        tooltip.add( new TranslationTextComponent( "pmmo.brewExtraDrop", 0 ).setStyle( XP.textStyle.get( "red" ) ) );
                }

                if( salvageInfo != null && !XP.getItem( (String) salvageInfo.get( "salvageItem" ) ).equals( Items.AIR ) )
                {
                    level = XP.getLevel(Skill.SMITHING, player );
                    int reqLevel = (int) Math.floor( (double) salvageInfo.get( "levelReq" ) );
                    int finalLevel = level - reqLevel;

                    double baseChance = (double) salvageInfo.get( "baseChance" );
                    double xpPerItem = (double) salvageInfo.get( "xpPerItem" );
                    double chancePerLevel = (double) salvageInfo.get( "chancePerLevel" );
                    double maxSalvageMaterialChance = (double) salvageInfo.get( "maxChance" );
                    double chance = baseChance + ( chancePerLevel * finalLevel );

                    if( chance > maxSalvageMaterialChance )
                        chance = maxSalvageMaterialChance;

                    int salvageMax = (int) Math.floor( (double) salvageInfo.get( "salvageMax" ) );
                    double durabilityPercent = ( 1.00f - ( (double) itemStack.getDamage() / (double) itemStack.getMaxDamage() ) );

                    if( Double.isNaN( durabilityPercent ) )
                        durabilityPercent = 1;

                    int potentialReturnAmount = (int) Math.floor( salvageMax * durabilityPercent );
                    Item salvageItem = XP.getItem( (String) salvageInfo.get( "salvageItem" ) );

                    if( finalLevel < 0 )
                    {
                        tooltip.add( new TranslationTextComponent( "pmmo.cannotSalvageLackLevel", reqLevel ).setStyle( XP.textStyle.get( "red" ) ) );
                    }
                    else
                    {
                        if( potentialReturnAmount > 0 )
                            tooltip.add( new TranslationTextComponent( "pmmo.salvagesInto", potentialReturnAmount, new TranslationTextComponent( salvageItem.getTranslationKey() ) ).setStyle( XP.textStyle.get( "green" ) ) );
                        else
                            tooltip.add( new TranslationTextComponent( "pmmo.salvagesInto", potentialReturnAmount, new TranslationTextComponent( salvageItem.getTranslationKey() ) ).setStyle( XP.textStyle.get( "red" ) ) );
                        if( chance > 0 )
                            tooltip.add( new TranslationTextComponent( "pmmo.xpEachChanceEach", " " + DP.dp( xpPerItem ), DP.dp( chance ) ).setStyle( XP.textStyle.get( "green" ) ) );
                        else
                            tooltip.add( new TranslationTextComponent( "pmmo.xpEachChanceEach", " " + DP.dp( xpPerItem ), DP.dp( chance ) ).setStyle( XP.textStyle.get( "red" ) ) );
                    }
                }

                if( salvagesFrom != null )
                {
                    tooltip.add( new TranslationTextComponent( "pmmo.salvagesFrom" ).setStyle( XP.textStyle.get( "green" ) ) );
                    level = XP.getLevel( Skill.SMITHING, player );

                    if( !lastKey.equals( regKey ) )
                    {
                        salvageArray = salvagesFrom.keySet().toArray();
                        salvageArrayLength = salvageArray.length;
                    }

                    if( System.nanoTime() - lastTime > 750000000 )
                    {
                        lastTime = System.nanoTime();
                        salvageArrayPos++;
                    }
                    if( salvageArrayPos > salvageArrayLength - 1 )
                        salvageArrayPos = 0;

                    String key = (String) salvageArray[salvageArrayPos];
                    String displayName = new TranslationTextComponent( XP.getItem( (String) salvageArray[salvageArrayPos] ).getTranslationKey() ).getString();
                    int value = (int) Math.floor( (double) salvagesFrom.get( key ) );

                    salvageInfo = JsonConfig.data.get( JType.SALVAGE_TO ).get( key );

                    if( salvageInfo != null && (double) salvageInfo.get( "levelReq" ) <= level )
                        tooltip.add( new TranslationTextComponent( "pmmo.valueFromValue", " " + value, displayName ).setStyle( XP.textStyle.get( "green" ) ) );
                    else
                        tooltip.add( new TranslationTextComponent( "pmmo.valueFromValue", " " + value, displayName ).setStyle( XP.textStyle.get( "red" ) ) );
                }
            }
        }
    }

    private static void addTooltipTextSkill( String tKey, JType jType, Map<String, Object> theMap, ItemTooltipEvent event )
    {
        PlayerEntity player = event.getPlayer();
        List<ITextComponent> tooltip = event.getToolTip();
        Item item = event.getItemStack().getItem();
        double level, value;

        if( theMap.size() > 0 )
        {
            tooltip.add( new TranslationTextComponent( tKey ).setStyle( XP.textStyle.get( XP.checkReq( player, item.getRegistryName(), jType ) ? "green" : "red" ) ) );

            for( String key : theMap.keySet() )
            {
                level = XP.getLevelDecimal( Skill.getSkill( key ), player );

                if( theMap.get( key ) instanceof Double )
                {
                    value = (double) theMap.get( key );
                    tooltip.add( new TranslationTextComponent( "pmmo.levelDisplay", " " + new TranslationTextComponent( "pmmo." + key ).getString(), DP.dpSoft( value ) ).setStyle( XP.textStyle.get( level < value ? "red" : "green" ) ) );
                }
            }
        }
    }

    private static void addTooltipTextSkillPercentage( String tKey, Map<String, Object> theMap, ItemTooltipEvent event )
    {
        List<ITextComponent> tooltip = event.getToolTip();
        double value;

        if( theMap.size() > 0 )
        {
            tooltip.add( new TranslationTextComponent( tKey ).setStyle( XP.textStyle.get( "green" ) ) );

            for( String key : theMap.keySet() )
            {
                if( theMap.get( key ) instanceof Double )
                {
                    value = (double) theMap.get( key );
                    if( value < 0 )
                        tooltip.add( new TranslationTextComponent( "pmmo.levelDisplayPercentage", " " + DP.dp( value ), new TranslationTextComponent( "pmmo." + key ).getString() ).setStyle( XP.textStyle.get( "red" ) ) );
                    else
                        tooltip.add( new TranslationTextComponent( "pmmo.levelDisplayPercentage", " +" + DP.dp( value ), new TranslationTextComponent( "pmmo." + key ).getString() ).setStyle( XP.getSkillStyle( Skill.getSkill( key ) ) ) );
                }
            }
        }
    }
}
