package harmonised.pmmo.events;

import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.gui.MainScreen;
import harmonised.pmmo.gui.XPOverlayGUI;
import harmonised.pmmo.network.MessageCrawling;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.proxy.ClientHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.XP;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.Reference;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandler
{
    private static boolean wasCrawling = false, wasOpenMenu = false, tooltipOn = true, tooltipKeyWasPressed = false;
    private static String lastKey = "";
    private static int salvageArrayPos = 0, salvageArrayLength;
    private static long lastTime = System.currentTimeMillis();
    private static Object[] salvageArray;
    private static final Logger LOGGER = LogManager.getLogger();

    public static void subscribeClientEvents( IEventBus eventBus )
    {
        eventBus.register( harmonised.pmmo.events.ClientEventHandler.class );
    }

    @SubscribeEvent
    public static void keyPressEvent( net.minecraftforge.client.event.InputEvent.KeyInputEvent event )
    {
        if( Minecraft.getInstance().player != null )
        {
            if( wasCrawling != ClientHandler.CRAWL_KEY.isKeyDown() )
            {
                wasCrawling = ClientHandler.CRAWL_KEY.isKeyDown();
                NetworkHandler.sendToServer( new MessageCrawling( ClientHandler.CRAWL_KEY.isKeyDown() ) );
            }

            if( wasOpenMenu != ClientHandler.OPEN_MENU.isKeyDown() )
            {
                if( Minecraft.getInstance().player.getDisplayName().getString().equals( "Dev" ) )
                    Minecraft.getInstance().displayGuiScreen( new MainScreen( new TranslationTextComponent( "pmmo.potato" ) ) );

                wasOpenMenu = ClientHandler.OPEN_MENU.isKeyDown();
//                NetworkHandler.sendToServer( new MessageCrawling( ClientHandler.CRAWL_KEY.isKeyDown() ) );
            }

            if( !(Minecraft.getInstance().player == null) && ClientHandler.TOGGLE_TOOLTIP.isKeyDown() && !tooltipKeyWasPressed )
            {
                tooltipOn = !tooltipOn;
                if( tooltipOn )
                    Minecraft.getInstance().player.sendStatusMessage( new TranslationTextComponent( "pmmo.tooltipOn" ), true );
                else
                    Minecraft.getInstance().player.sendStatusMessage( new TranslationTextComponent( "pmmo.tooltipOff" ), true );
            }

            tooltipKeyWasPressed = ClientHandler.TOGGLE_TOOLTIP.isKeyDown();

            if( ClientHandler.CRAWL_KEY.isKeyDown() )
                XP.isCrawling.add( Minecraft.getInstance().player.getUniqueID() );
            else
                XP.isCrawling.remove( Minecraft.getInstance().player.getUniqueID() );
        }
    }

    private static void addTooltipTextSkill( String tKey, String type, Map<String, Object> theMap, ItemTooltipEvent event )
    {
        PlayerEntity player = event.getPlayer();
        List<ITextComponent> tooltip = event.getToolTip();
        Item item = event.getItemStack().getItem();
        int level, value;

        if( theMap.size() > 0 )
        {
            if( XP.checkReq( player, item.getRegistryName(), type ) )
                tooltip.add( new TranslationTextComponent( tKey ).setStyle( XP.textStyle.get( "green" ) ) );
            else
                tooltip.add( new TranslationTextComponent( tKey ).setStyle( XP.textStyle.get( "red" ) ) );

            for( String key : theMap.keySet() )
            {
                level = XP.getLevel( Skill.getSkill( key ), player );

                if( theMap.get( key ) instanceof Double )
                {
                    value = (int) Math.floor( (double) theMap.get( key ) );

                    if( level < value )
                        tooltip.add( new TranslationTextComponent( "pmmo.levelDisplay", " " + new TranslationTextComponent( "pmmo." + key ).getString(), value ).setStyle( XP.textStyle.get( "red" ) ) );
                    else
                        tooltip.add( new TranslationTextComponent( "pmmo.levelDisplay", " " + new TranslationTextComponent( "pmmo." + key ).getString(), value ).setStyle( XP.textStyle.get( "green" ) ) );
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
            tooltip.add( new TranslationTextComponent( tKey ) );

            for( String key : theMap.keySet() )
            {
                if( theMap.get( key ) instanceof Double )
                {
                    value = (double) theMap.get( key );
                    if( value < 0 )
                        tooltip.add( new TranslationTextComponent( "pmmo.levelDisplayPercentage", " " + DP.dp( value ), new TranslationTextComponent( "pmmo." + key ).getString() ).setStyle( XP.textStyle.get( "red" ) ) );
                    else
                        tooltip.add( new TranslationTextComponent( "pmmo.levelDisplayPercentage", " +" + DP.dp( value ), new TranslationTextComponent( "pmmo." + key ).getString() ).setStyle( XP.textStyle.get( "green" ) ) );
                }
            }
        }
    }

    @SubscribeEvent
    public static void tooltipEvent( ItemTooltipEvent event )
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
            String regKey = item.getRegistryName().toString();
            float hardness;
            double dValue;
            Material material = null;

            Map<String, Object> wearReq = JsonConfig.data.get( "wearReq" ).get( regKey );
            Map<String, Object> toolReq = JsonConfig.data.get( "toolReq" ).get( regKey );
            Map<String, Object> weaponReq = JsonConfig.data.get( "weaponReq" ).get( regKey );
            Map<String, Object> useReq = JsonConfig.data.get( "useReq" ).get( regKey );
            Map<String, Object> placeReq = JsonConfig.data.get( "placeReq" ).get( regKey );
            Map<String, Object> breakReq = JsonConfig.data.get( "breakReq" ).get( regKey );
            Map<String, Object> xpValue = JsonConfig.data.get( "xpValue" ).get( regKey );
            Map<String, Object> xpValueCrafting = JsonConfig.data.get( "xpValueCrafting" ).get( regKey );
            Map<String, Object> salvageInfo = JsonConfig.data.get( "salvageInfo" ).get( regKey );
            Map<String, Object> salvagesFrom = JsonConfig.data.get( "salvagesFrom" ).get( regKey );
            Map<String, Object> heldItemXpBoost = JsonConfig.data.get( "heldItemXpBoost" ).get( regKey );
            Map<String, Object> wornItemXpBoost = JsonConfig.data.get( "wornItemXpBoost" ).get( regKey );

            if( xpValue != null && xpValue.size() > 0 )      //XP VALUE
            {
                tooltip.add( new TranslationTextComponent( "pmmo.xpValue" ) );

                for( String key : xpValue.keySet() )
                {
                    if( xpValue.get( key ) instanceof Double )
                    {
                        dValue = (double) xpValue.get( key );
                        tooltip.add( new TranslationTextComponent( "pmmo.levelDisplay", " " + new TranslationTextComponent( "pmmo." + key ).getString(), DP.dp( dValue ) ) );
                    }
                }
            }

            if( heldItemXpBoost != null && heldItemXpBoost.size() > 0 )      //XP VALUE
            {
                tooltip.add( new TranslationTextComponent( "pmmo.xpValue" ) );

                for( String key : heldItemXpBoost.keySet() )
                {
                    if( heldItemXpBoost.get( key ) instanceof Double )
                    {
                        dValue = (double) heldItemXpBoost.get( key );
                        tooltip.add( new TranslationTextComponent( "pmmo.levelDisplay", " " + new TranslationTextComponent( "pmmo." + key ).getString(), DP.dp( dValue ) ) );
                    }
                }
            }

            if( xpValueCrafting != null && xpValueCrafting.size() > 0 )      //XP VALUE
            {
                tooltip.add( new TranslationTextComponent( "pmmo.xpValueCrafting" ) );

                for( String key : xpValueCrafting.keySet() )
                {
                    if( xpValueCrafting.get( key ) instanceof Double )
                    {
                        dValue = (double) xpValueCrafting.get( key );
                        tooltip.add( new TranslationTextComponent( "pmmo.levelDisplay", " " + new TranslationTextComponent( "pmmo." + key ).getString(), DP.dp( dValue ) ) );
                    }
                }
            }

            if( item instanceof BlockItem )
            {
                material = ( (BlockItem) item).getBlock().getDefaultState().getMaterial();

                hardness = ((BlockItem) item).getBlock().getBlockHardness( ((BlockItem) item).getBlock().getDefaultState(), null, null );
                if( hardness > 0 )
                    tooltip.add( new TranslationTextComponent( "pmmo.levelDisplay", " " + new TranslationTextComponent( "pmmo.hardness", DP.dp( hardness ) ).getString() ) );
            }

            if( wearReq != null && wearReq.size() > 0 )
                addTooltipTextSkill( "pmmo.armor", "wear", wearReq, event );
            if( wornItemXpBoost != null && wornItemXpBoost.size() > 0 )
                addTooltipTextSkillPercentage( "pmmo.itemXpBoostWorn", wornItemXpBoost, event );

            if( toolReq != null && toolReq.size() > 0 )
                addTooltipTextSkill( "pmmo.tool", "tool", toolReq, event );
            if( weaponReq != null && weaponReq.size() > 0 )
                addTooltipTextSkill( "pmmo.weapon", "weapon", weaponReq, event );
            if( heldItemXpBoost != null && heldItemXpBoost.size() > 0 )
                addTooltipTextSkillPercentage( "pmmo.itemXpBoostHeld", heldItemXpBoost, event );


            if( useReq != null && useReq.size() > 0 )
                addTooltipTextSkill( "pmmo.use", "use", useReq, event );

//            if( wearReq != null && wearReq.size() > 0 )
//                addTooltipTextSkill( "pmmo.wear", "mob", mobReq, event );

            if( placeReq != null && placeReq.size() > 0 )
            {
                if( JsonConfig.data.get( "plantInfo" ).containsKey( item.getRegistryName().toString() ) || item instanceof IPlantable )
                    addTooltipTextSkill( "pmmo.plant", "place", placeReq, event );
                else
                    addTooltipTextSkill( "pmmo.placeDown", "place", placeReq, event );
            }

            if( breakReq != null && breakReq.size() > 0 )
            {
                if( XP.correctHarvestTool( material ).equals( "axe" ) )
                    addTooltipTextSkill( "pmmo.chop", "break", breakReq, event );
                else if( JsonConfig.data.get( "plantInfo" ).containsKey( item.getRegistryName().toString() ) || item instanceof IPlantable )
                    addTooltipTextSkill( "pmmo.harvest", "break", breakReq, event );
                else
                    addTooltipTextSkill( "pmmo.break", "break", breakReq, event );
            }

            if( JsonConfig.data.get( "oreInfo" ).containsKey( regKey ) && JsonConfig.data.get( "oreInfo" ).get( regKey ).containsKey( "extraChance" ) )
            {
                if( XP.getExtraChance( player, item.getRegistryName(), "ore" ) > 0 )  //ORE EXTRA CHANCE
                    tooltip.add( new TranslationTextComponent( "pmmo.oreExtraDrop", DP.dp( XP.getExtraChance( player, item.getRegistryName(), "ore" ) / 100 ) ).setStyle( XP.textStyle.get( "green" ) ) );
                else
                    tooltip.add( new TranslationTextComponent( "pmmo.oreExtraDrop", 0 ).setStyle( XP.textStyle.get( "red" ) ) );
            }

            if( JsonConfig.data.get( "logInfo" ).containsKey( regKey ) && JsonConfig.data.get( "logInfo" ).get( regKey ).containsKey( "extraChance" ) )
            {
                if( XP.getExtraChance( player, item.getRegistryName(), "log" ) > 0 )  //ORE EXTRA CHANCE
                    tooltip.add( new TranslationTextComponent( "pmmo.logExtraDrop", DP.dp( XP.getExtraChance( player, item.getRegistryName(), "log" ) / 100 ) ).setStyle( XP.textStyle.get( "green" ) ) );
                else
                    tooltip.add( new TranslationTextComponent( "pmmo.logExtraDrop", 0 ).setStyle( XP.textStyle.get( "red" ) ) );
            }

            if( JsonConfig.data.get( "plantInfo" ).containsKey( regKey ) && JsonConfig.data.get( "plantInfo" ).get( regKey ).containsKey( "extraChance" ) )
            {
                if( XP.getExtraChance( player, item.getRegistryName(), "plant" ) > 0 )  //ORE EXTRA CHANCE
                    tooltip.add( new TranslationTextComponent( "pmmo.plantExtraDrop", DP.dp( XP.getExtraChance( player, item.getRegistryName(), "plant" ) / 100 ) ).setStyle( XP.textStyle.get( "green" ) ) );
                else
                    tooltip.add( new TranslationTextComponent( "pmmo.plantExtraDrop", 0 ).setStyle( XP.textStyle.get( "red" ) ) );
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

                if( System.currentTimeMillis() - lastTime > 1000 )
                {
                    lastTime = System.currentTimeMillis();
                    salvageArrayPos++;
                }
                if( salvageArrayPos > salvageArrayLength - 1 )
                    salvageArrayPos = 0;

                String key = (String) salvageArray[salvageArrayPos];
                String displayName = new TranslationTextComponent( XP.getItem( (String) salvageArray[salvageArrayPos] ).getTranslationKey() ).getString();
                int value = (int) Math.floor( (double) salvagesFrom.get( key ) );

                salvageInfo = JsonConfig.data.get( "salvageInfo" ).get( key );

                if( salvageInfo != null && (double) salvageInfo.get( "levelReq" ) <= level )
                    tooltip.add( new TranslationTextComponent( "pmmo.salvagesFromItem", " " + value, displayName ).setStyle( XP.textStyle.get( "green" ) ) );
                else
                    tooltip.add( new TranslationTextComponent( "pmmo.salvagesFromItem", " " + value, displayName ).setStyle( XP.textStyle.get( "red" ) ) );
            }
        }
    }
}