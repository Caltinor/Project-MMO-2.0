package harmonised.pmmo.config;

import com.google.common.collect.Multimap;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AutoValues
{
    public static final Logger LOGGER = LogManager.getLogger();

    private static void addJsonConfigValue( String resLoc, JType jType, Map<String, Double> values, boolean fillIfExists )
    {
        double value;
        boolean hadEntry = JsonConfig.localData.get( jType ).containsKey( resLoc );
        if( !hadEntry )
            JsonConfig.localData.get( jType ).put( resLoc, new HashMap<>() );

        if( !hadEntry || fillIfExists )
        {
            for( Map.Entry<String, Double> entry : values.entrySet() )
            {
                value = entry.getValue();
                if( JsonConfig.levelJTypes.contains( jType ) && entry.getValue() > JsonConfig.maxLevel )
                    value = JsonConfig.maxLevel;
                if( !JsonConfig.localData.get( jType ).get( resLoc ).containsKey( entry.getKey() ) )
                    JsonConfig.localData.get( jType ).get( resLoc ).put( entry.getKey(), value );
            }
        }
    }

    private static Map<String, AttributeModifier> mergeMultimaps( Multimap<String, AttributeModifier> ... maps )
    {
        Map<String, AttributeModifier> output = new HashMap<>();

        for( Multimap<String, AttributeModifier> map : maps )
        {
            for( Map.Entry<String, AttributeModifier> entry : map.entries() )
            {
                output.put( entry.getKey(), entry.getValue() );
            }
        }

        return output;
    }

    public static void setAutoValues()
    {
        if( FConfig.autoGenerateValuesEnabled )
        {
            for( Item item : ForgeRegistries.ITEMS )
            {
                try
                {
                    ItemStack itemStack = new ItemStack( item );
                    String resLoc = item.getRegistryName().toString();

                    //Wear and Weapon Req
                    Multimap<String, AttributeModifier> mainHandAttributes = itemStack.getAttributeModifiers( EntityEquipmentSlot.MAINHAND );
                    Multimap<String, AttributeModifier> offHandAttributes = itemStack.getAttributeModifiers( EntityEquipmentSlot.OFFHAND );
                    Multimap<String, AttributeModifier> headHandAttributes = itemStack.getAttributeModifiers( EntityEquipmentSlot.HEAD );
                    Multimap<String, AttributeModifier> chestHandAttributes = itemStack.getAttributeModifiers( EntityEquipmentSlot.CHEST );
                    Multimap<String, AttributeModifier> legsHandAttributes = itemStack.getAttributeModifiers( EntityEquipmentSlot.LEGS );
                    Multimap<String, AttributeModifier> feetHandAttributes = itemStack.getAttributeModifiers( EntityEquipmentSlot.FEET );

                    Map<String, AttributeModifier> attributes = mergeMultimaps( mainHandAttributes, offHandAttributes, headHandAttributes, chestHandAttributes, legsHandAttributes, feetHandAttributes );

                    AttributeModifier armorAttribute = attributes.get( "generic.armor" );
                    AttributeModifier armorToughnessAttribute = attributes.get( "generic.armorToughness" );
                    AttributeModifier attackSpeedAttribute = attributes.get( "generic.attackSpeed" );
                    AttributeModifier attackDamageAttribute = attributes.get( "generic.attackDamage" );

                    double armor            = armorAttribute          == null ? 0D : armorAttribute.getAmount();
                    double armorToughness   = armorToughnessAttribute == null ? 0D : armorToughnessAttribute.getAmount();
                    double attackSpeed      = attackSpeedAttribute    == null ? 0D : attackSpeedAttribute.getAmount();
                    double attackDamage     = attackDamageAttribute   == null ? 0D : attackDamageAttribute.getAmount();
                    double enduranceReq = 0;
                    double combatReq = 0;
                    double toolReq = 0, highestToolReq = 0;

                    Map<String, Double> reqWear     = new HashMap<>();
                    Map<String, Double> reqWeapon   = new HashMap<>();
                    Map<String, Double> reqTool     = new HashMap<>();

                    if( attributes.size() > 0 )
                    {
                        enduranceReq = Math.ceil( armor * FConfig.armorReqScale + armorToughness * FConfig.armorToughnessReqScale );
                        combatReq = Math.ceil( (attackDamage) * FConfig.attackDamageReqScale * (4+attackSpeed) );

                        reqWear.put( Skill.ENDURANCE.toString(), Math.max( 1, enduranceReq ) );
                        reqWeapon.put( Skill.COMBAT.toString(),  Math.max( 1, combatReq ) );

                        if( FConfig.wearReqEnabled && FConfig.autoGenerateWearReqEnabled && reqWear.getOrDefault( Skill.ENDURANCE.toString(), 0D ) > 1 )
                            addJsonConfigValue( resLoc, JType.REQ_WEAR, reqWear, false );
                        if( FConfig.weaponReqEnabled && FConfig.autoGenerateWeaponReqEnabled && reqWeapon.getOrDefault( Skill.COMBAT.toString(), 0D ) > 1 )
                            addJsonConfigValue( resLoc, JType.REQ_WEAPON, reqWeapon, false );
                    }

                    //Tool Req
                    double speed;
                    if( item instanceof ItemTool)
                    {
                        ItemTool itemTool = (ItemTool) item;
                        Set<String> toolClasses = itemTool.getToolClasses( itemStack );
                        for( String toolClass : toolClasses )
                        {
                            if( toolClass.toLowerCase().equals( "axe" ) )
                            {
                                speed = item.getDestroySpeed( itemStack, Blocks.LOG.getDefaultState() );
                                toolReq = Math.max( 1, speed * FConfig.toolReqScaleLog );
                                if( highestToolReq < toolReq )
                                    highestToolReq = toolReq;
                                reqTool.put( Skill.WOODCUTTING.toString(), toolReq );
                            }
                            if( toolClass.toLowerCase().equals( "pickaxe" ) )
                            {
                                speed = item.getDestroySpeed( itemStack, Blocks.STONE.getDefaultState() );
                                toolReq = Math.max( 1, speed * FConfig.toolReqScaleOre );
                                if( highestToolReq < toolReq )
                                    highestToolReq = toolReq;
                                reqTool.put( Skill.MINING.toString(), toolReq );
                            }
                            if( toolClass.toLowerCase().equals( "shovel" ) )
                            {
                                speed = item.getDestroySpeed( itemStack, Blocks.DIRT.getDefaultState() );
                                toolReq = Math.max( 1, speed * FConfig.toolReqScaleDirt );
                                if( highestToolReq < toolReq )
                                    highestToolReq = toolReq;
                                reqTool.put( Skill.EXCAVATION.toString(), toolReq );
                            }
                        }
                    }
                    if( FConfig.toolReqEnabled && FConfig.autoGenerateToolReqEnabled )
                        addJsonConfigValue( resLoc, JType.REQ_TOOL, reqTool, true );

                    //Crafting Xp Value
                    if( FConfig.autoGenerateCraftingXpEnabled )
                    {
                        double craftingXp = 0;
                        double smithingXp = 0;

                        if( enduranceReq > 0 || combatReq > 0 || toolReq > 0 )
                        {
                            craftingXp = enduranceReq * 10D +                           Math.max( ( Math.max( combatReq - 10, 1 ) ) * 5D,  ( Math.max( toolReq - 10, 1 ) ) * 5D );
                            smithingXp = ( Math.max( enduranceReq - 10, 1 ) ) * 5D  +   Math.max( ( Math.max( combatReq - 10, 1 ) ) * 2D,  ( Math.max( toolReq - 10, 1 ) ) * 2D );

                            craftingXp *= FConfig.autoGeneratedCraftingXpValueMultiplierCrafting;
                            smithingXp *= FConfig.autoGeneratedCraftingXpValueMultiplierSmithing;
                        }

                        Map<String, Double> xpValueMap = new HashMap<>();
                        if( craftingXp > 0 )
                            xpValueMap.put( Skill.CRAFTING.toString(), craftingXp );
                        if( smithingXp > 0 )
                            xpValueMap.put( Skill.SMITHING.toString(), smithingXp );
                        addJsonConfigValue( resLoc, JType.XP_VALUE_CRAFT, xpValueMap, true );
                    }
                }
                catch( Exception e )
                {
                    LOGGER.debug( e );
                }
            }
            if( FConfig.autoGenerateExtraChanceEnabled )
            {
                String[] oreNames = OreDictionary.getOreNames();

                for( Block block : ForgeRegistries.BLOCKS )
                {
                    try
                    {
//                ItemStack itemStack = new ItemStack( block );
                        String resLoc = block.getRegistryName().toString();
                        Material material = block.getDefaultState().getMaterial();
                        Skill skill = XP.getSkill( material );
                        JType jType = JType.NONE;
                        Map<String, Double> infoMap = new HashMap<>();
                        double chance = 0;
                        int[] oreIDs = OreDictionary.getOreIDs( new ItemStack( block ) );

                        //Ore/Log/Plant Extra Chance
                        for( int id : oreIDs )
                        {
                            String tagName = oreNames[ id ];
                            if( tagName.startsWith( "ore" ) )
                            {
                                jType = JType.INFO_ORE;
                                chance = FConfig.defaultExtraChanceOre;
                            }
                            else if( tagName.startsWith( "log" ) )
                            {
                                jType = JType.INFO_PLANT;
                                chance = FConfig.defaultExtraChancePlant;
                            }
                            else if( tagName.startsWith( "crop" ) )
                            {
                                jType = JType.INFO_LOG;
                                chance = FConfig.defaultExtraChanceLog;
                            }
                        }
                        if( !jType.equals( JType.NONE ) )
                            infoMap.put( "extraChance", chance );

                        if( infoMap.size() > 0 && infoMap.getOrDefault( "extraChance", 0D ) > 0 )
                            addJsonConfigValue( resLoc, jType, infoMap, false );
                    }
                    catch( Exception e )
                    {
                        LOGGER.error( e );
                    }
                }
            }
            JsonConfig.data = JsonConfig.localData;
        }
    }
}