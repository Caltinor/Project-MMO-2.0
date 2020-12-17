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
                if( JsonConfig.levelJTypes.contains( jType ) )
                    value = Math.max( 1, Math.min( FConfig.maxLevel, value ) );
                if( !JsonConfig.localData.get( jType ).get( resLoc ).containsKey( entry.getKey() ) )
                    JsonConfig.localData.get( jType ).get( resLoc ).put( entry.getKey(), value );
            }
            if( JsonConfig.localData.get( jType ).get( resLoc ).size() == 0 )
                JsonConfig.localData.get( jType ).remove( resLoc );
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

    public static double getWearReqFromStack( ItemStack itemStack )
    {
        Multimap<String, AttributeModifier> headHandAttributes = itemStack.getAttributeModifiers( EntityEquipmentSlot.HEAD );
        Multimap<String, AttributeModifier> chestHandAttributes = itemStack.getAttributeModifiers( EntityEquipmentSlot.CHEST );
        Multimap<String, AttributeModifier> legsHandAttributes = itemStack.getAttributeModifiers( EntityEquipmentSlot.LEGS );
        Multimap<String, AttributeModifier> feetHandAttributes = itemStack.getAttributeModifiers( EntityEquipmentSlot.FEET );

        Map<String, AttributeModifier> attributes = mergeMultimaps( headHandAttributes, chestHandAttributes, legsHandAttributes, feetHandAttributes );

        AttributeModifier armorAttribute = attributes.get( "generic.armor" );
        AttributeModifier armorToughnessAttribute = attributes.get( "generic.armorToughness" );

        double armor            = armorAttribute          == null ? 0D : armorAttribute.getAmount();
        double armorToughness   = armorToughnessAttribute == null ? 0D : armorToughnessAttribute.getAmount();

        ResourceLocation regKey = itemStack.getItem().getRegistryName();
        double wearReqOffset = regKey == null ? 0D : XP.getJsonMap( regKey.toString(), JType.ITEM_SPECIFIC ).getOrDefault( "autoValueOffsetWear", 0D );
        double wearReq = Math.min( FConfig.maxLevel, Math.ceil( armor * FConfig.armorReqScale + armorToughness * FConfig.armorToughnessReqScale + wearReqOffset ) );

        if( FConfig.autoGenerateRoundedValuesOnly )
            wearReq = Math.ceil( wearReq );

        return wearReq;
    }

    public static double getWeaponReqFromStack( ItemStack itemStack )
    {
        Multimap<String, AttributeModifier> mainHandAttributes = itemStack.getAttributeModifiers( EntityEquipmentSlot.MAINHAND );
        Multimap<String, AttributeModifier> offHandAttributes = itemStack.getAttributeModifiers( EntityEquipmentSlot.OFFHAND );

        Map<String, AttributeModifier> attributes = mergeMultimaps( mainHandAttributes, offHandAttributes );

        AttributeModifier attackSpeedAttribute = attributes.get( "generic.attackSpeed" );
        AttributeModifier attackDamageAttribute = attributes.get( "generic.attackDamage" );

        double attackSpeed      = attackSpeedAttribute    == null ? 0D : attackSpeedAttribute.getAmount();
        double attackDamage     = attackDamageAttribute   == null ? 0D : attackDamageAttribute.getAmount();

        ResourceLocation regKey = itemStack.getItem().getRegistryName();
        double weaponReqOffset = regKey == null ? 0D : XP.getJsonMap( regKey.toString(), JType.ITEM_SPECIFIC ).getOrDefault( "autoValueOffsetWeapon", 0D );
        double weaponReq = Math.min( FConfig.maxLevel, Math.ceil( (attackDamage) * FConfig.attackDamageReqScale * (4+attackSpeed) + weaponReqOffset ) );

        if( FConfig.autoGenerateRoundedValuesOnly )
            weaponReq = Math.ceil( weaponReq );

        return weaponReq;
    }

    public static Map<String, Double> getToolReqFromStack( ItemStack itemStack )
    {
        Map<String, Double> reqTool = new HashMap<>();
        Item item = itemStack.getItem();
        double speed, toolReq;

        ResourceLocation regKey = itemStack.getItem().getRegistryName();
        double toolReqOffset = regKey == null ? 0D : XP.getJsonMap( regKey.toString(), JType.ITEM_SPECIFIC ).getOrDefault( "autoValueOffsetTool", 0D );

        //Woodcutting
        speed = item.getDestroySpeed( itemStack, Blocks.LOG.getDefaultState() );
        toolReq = Math.max( 1, Math.min( FConfig.maxLevel, speed * FConfig.toolReqScaleLog + toolReqOffset ) );
        if( toolReq > 5 )
            reqTool.put( Skill.WOODCUTTING.toString(), toolReq );

        //Mining
        speed = item.getDestroySpeed( itemStack, Blocks.STONE.getDefaultState() );
        toolReq = Math.max( 1, Math.min( FConfig.maxLevel, speed * FConfig.toolReqScaleOre + toolReqOffset ) );
        if( toolReq > 5 )
            reqTool.put( Skill.MINING.toString(), toolReq );

        //Excavation
        speed = item.getDestroySpeed( itemStack, Blocks.DIRT.getDefaultState() );
        toolReq = Math.max( 1, Math.min( FConfig.maxLevel, speed * FConfig.toolReqScaleDirt + toolReqOffset ) );
        if( toolReq > 5 )
            reqTool.put( Skill.EXCAVATION.toString(), toolReq );

        if( FConfig.autoGenerateRoundedValuesOnly )
            XP.ceilMapAnyDouble( reqTool );

        return reqTool;
    }

    public static String getItemSpecificSkill( String resLoc )
    {
        Map<String, Double> itemSpecificMap = JsonConfig.data.get( JType.ITEM_SPECIFIC ).getOrDefault( resLoc.toString(), new HashMap<>() );
        String skill;

        if( itemSpecificMap.getOrDefault( "archeryWeapon", 0D ) != 0 )
            skill = Skill.ARCHERY.toString();
        else if( itemSpecificMap.getOrDefault( "magicWeapon", 0D ) != 0 )
            skill = Skill.MAGIC.toString();
        else
            skill = Skill.COMBAT.toString();

        return skill;
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

                    double enduranceReq = getWearReqFromStack( itemStack ) + XP.getJsonMap( resLoc, JType.ITEM_SPECIFIC ).getOrDefault( "autoValueOffsetWear", 0D );
                    double combatReq = getWeaponReqFromStack( itemStack ) + XP.getJsonMap( resLoc, JType.ITEM_SPECIFIC ).getOrDefault( "autoValueOffsetWeapon", 0D );
                    Map<String, Double> reqTool = getToolReqFromStack( itemStack );

                    //Wear Req
                    if( enduranceReq > 1 && FConfig.wearReqEnabled && FConfig.autoGenerateWearReqEnabled )
                    {
                        Map<String, Double> reqWear     = new HashMap<>();
                        reqWear.put( Skill.ENDURANCE.toString(), Math.max( 1, enduranceReq ) );
                        addJsonConfigValue( resLoc, JType.REQ_WEAR, reqWear, false );
                    }

                    //Weapon Req
                    if( combatReq > 1 && FConfig.weaponReqEnabled && FConfig.autoGenerateWeaponReqEnabled )
                    {
                        Map<String, Double> reqWeapon   = new HashMap<>();
                        reqWeapon.put( getItemSpecificSkill( item.getRegistryName().toString() ).toString(),  Math.max( 1, combatReq ) );
                        addJsonConfigValue( resLoc, JType.REQ_WEAPON, reqWeapon, false );
                    }

                    //Tool Req
                    if( reqTool.size() > 0 && FConfig.toolReqEnabled && FConfig.autoGenerateToolReqEnabled )
                    {
                        addJsonConfigValue( resLoc, JType.REQ_TOOL, reqTool, true );
                    }

                    //Crafting Xp Value
                    if( FConfig.autoGenerateCraftingXpEnabled )
                    {
                        double highestToolReq = reqTool.values().stream().reduce( Math::max ).orElse( 0D );

                        double craftingXp = 0;
                        double smithingXp = 0;

                        if( enduranceReq > 0 || combatReq > 0 || highestToolReq > 0 )
                        {
                            craftingXp = enduranceReq * 10D +                           Math.max( ( Math.max( combatReq - 10, 1 ) ) * 5D,  ( Math.max( highestToolReq - 10, 1 ) ) * 5D );
                            smithingXp = ( Math.max( enduranceReq - 10, 1 ) ) * 5D  +   Math.max( ( Math.max( combatReq - 10, 1 ) ) * 2D,  ( Math.max( highestToolReq - 10, 1 ) ) * 2D );

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
                        String resLoc = block.getRegistryName().toString();
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