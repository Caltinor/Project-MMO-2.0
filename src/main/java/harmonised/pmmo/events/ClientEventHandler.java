package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.Requirements;
import harmonised.pmmo.gui.ScreenSkills;
import harmonised.pmmo.gui.XPOverlayGUI;
import harmonised.pmmo.network.MessageCrawling;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.proxy.ClientHandler;
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
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
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
                Minecraft.getInstance().displayGuiScreen( new ScreenSkills( new TranslationTextComponent( "pmmo.text.potato" ) ) );

                wasOpenMenu = ClientHandler.OPEN_MENU.isKeyDown();
//                NetworkHandler.sendToServer( new MessageCrawling( ClientHandler.CRAWL_KEY.isKeyDown() ) );
            }

            if( !(Minecraft.getInstance().player == null) && ClientHandler.TOGGLE_TOOLTIP.isKeyDown() && !tooltipKeyWasPressed )
            {
                tooltipOn = !tooltipOn;
                if( tooltipOn )
                    Minecraft.getInstance().player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.tooltipOn" ), true );
                else
                    Minecraft.getInstance().player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.tooltipOff" ), true );
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
                tooltip.add( new TranslationTextComponent( tKey ).setStyle( new Style().setColor( TextFormatting.GREEN ) ) );
            else
                tooltip.add( new TranslationTextComponent( tKey ).setStyle( new Style().setColor( TextFormatting.RED ) ) );

            for( String key : theMap.keySet() )
            {
                if(XPOverlayGUI.skills.containsKey( key ))
                    level = XP.levelAtXp( XPOverlayGUI.skills.get( key ).goalXp );
                else
                    level = 1;

                if( theMap.get( key ) instanceof Double )
                {
                    value = (int) Math.floor( (double) theMap.get( key ) );

                    if( level < value )
                        tooltip.add( new TranslationTextComponent( "pmmo.text.levelDisplay", " " + new TranslationTextComponent( "pmmo.text." + key ).getString(), value ).setStyle( new Style().setColor( TextFormatting.RED ) ) );
                    else
                        tooltip.add( new TranslationTextComponent( "pmmo.text.levelDisplay", " " + new TranslationTextComponent( "pmmo.text." + key ).getString(), value ).setStyle( new Style().setColor( TextFormatting.GREEN ) ) );
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

            Map<String, Object> wearReq = Requirements.data.get( "wearReq" ).get( regKey );
            Map<String, Object> toolReq = Requirements.data.get( "toolReq" ).get( regKey );
            Map<String, Object> weaponReq = Requirements.data.get( "weaponReq" ).get( regKey );
            Map<String, Object> useReq = Requirements.data.get( "useReq" ).get( regKey );
            Map<String, Object> placeReq = Requirements.data.get( "placeReq" ).get( regKey );
            Map<String, Object> breakReq = Requirements.data.get( "breakReq" ).get( regKey );
            Map<String, Object> xpValue = Requirements.data.get( "xpValue" ).get( regKey );
            Map<String, Object> xpValueCrafting = Requirements.data.get( "xpValueCrafting" ).get( regKey );
            Map<String, Object> salvageInfo = Requirements.data.get( "salvageInfo" ).get( regKey );
            Map<String, Object> salvagesFrom = Requirements.data.get( "salvagesFrom" ).get( regKey );

            if( xpValue != null && xpValue.size() > 0 )      //XP VALUE
            {
                tooltip.add( new TranslationTextComponent( "pmmo.text.xpValue" ) );

                for( String key : xpValue.keySet() )
                {
                    if( xpValue.get( key ) instanceof Double )
                    {
                        dValue = (double) xpValue.get( key );
                        tooltip.add( new TranslationTextComponent( "pmmo.text.levelDisplay", " " + new TranslationTextComponent( "pmmo.text." + key ).getString(), DP.dp( dValue ) ) );
                    }
                }
            }

            if( xpValueCrafting != null && xpValueCrafting.size() > 0 )      //XP VALUE
            {
                tooltip.add( new TranslationTextComponent( "pmmo.text.xpValueCrafting" ) );

                for( String key : xpValueCrafting.keySet() )
                {
                    if( xpValueCrafting.get( key ) instanceof Double )
                    {
                        dValue = (double) xpValueCrafting.get( key );
                        tooltip.add( new TranslationTextComponent( "pmmo.text.levelDisplay", " " + new TranslationTextComponent( "pmmo.text." + key ).getString(), DP.dp( dValue ) ) );
                    }
                }
            }

            if( item instanceof BlockItem )
            {
                material = ( (BlockItem) item).getBlock().getDefaultState().getMaterial();

                hardness = ((BlockItem) item).getBlock().getBlockHardness( ((BlockItem) item).getBlock().getDefaultState(), null, null );
                if( hardness > 0 )
                    tooltip.add( new TranslationTextComponent( "pmmo.text.levelDisplay", " " + new TranslationTextComponent( "pmmo.text.hardness", DP.dp( hardness ) ).getString() ) );
            }

            if( wearReq != null && wearReq.size() > 0 )
                addTooltipTextSkill( "pmmo.text.armor", "wear", wearReq, event );

            if( toolReq != null && toolReq.size() > 0 )
                addTooltipTextSkill( "pmmo.text.tool", "tool", toolReq, event );

            if( weaponReq != null && weaponReq.size() > 0 )
                addTooltipTextSkill( "pmmo.text.weapon", "weapon", weaponReq, event );

            if( useReq != null && useReq.size() > 0 )
                addTooltipTextSkill( "pmmo.text.use", "use", useReq, event );

//            if( wearReq != null && wearReq.size() > 0 )
//                addTooltipTextSkill( "pmmo.text.wear", "mob", mobReq, event );

            if( placeReq != null && placeReq.size() > 0 )
            {
                if( Requirements.data.get( "plantInfo" ).containsKey( item.getRegistryName().toString() ) || item instanceof IPlantable )
                    addTooltipTextSkill( "pmmo.text.plant", "place", placeReq, event );
                else
                    addTooltipTextSkill( "pmmo.text.placeDown", "place", placeReq, event );
            }

            if( breakReq != null && breakReq.size() > 0 )
            {
                if( XP.correctHarvestTool( material ).equals( "axe" ) )
                    addTooltipTextSkill( "pmmo.text.chop", "break", breakReq, event );
                else if( Requirements.data.get( "plantInfo" ).containsKey( item.getRegistryName().toString() ) || item instanceof IPlantable )
                    addTooltipTextSkill( "pmmo.text.harvest", "break", breakReq, event );
                else
                    addTooltipTextSkill( "pmmo.text.break", "break", breakReq, event );
            }

            if( Requirements.data.get( "oreInfo" ).containsKey( regKey ) && Requirements.data.get( "oreInfo" ).get( regKey ).containsKey( "extraChance" ) )
            {
                if( XP.getExtraChance( player, item.getRegistryName(), "ore" ) > 0 )  //ORE EXTRA CHANCE
                    tooltip.add( new TranslationTextComponent( "pmmo.text.oreExtraDrop", DP.dp( XP.getExtraChance( player, item.getRegistryName(), "ore" ) / 100 ) ).setStyle( new Style().setColor( TextFormatting.GREEN ) ) );
                else
                    tooltip.add( new TranslationTextComponent( "pmmo.text.oreExtraDrop", 0 ).setStyle( new Style().setColor( TextFormatting.RED ) ) );
            }

            if( Requirements.data.get( "logInfo" ).containsKey( regKey ) && Requirements.data.get( "logInfo" ).get( regKey ).containsKey( "extraChance" ) )
            {
                if( XP.getExtraChance( player, item.getRegistryName(), "log" ) > 0 )  //ORE EXTRA CHANCE
                    tooltip.add( new TranslationTextComponent( "pmmo.text.logExtraDrop", DP.dp( XP.getExtraChance( player, item.getRegistryName(), "log" ) / 100 ) ).setStyle( new Style().setColor( TextFormatting.GREEN ) ) );
                else
                    tooltip.add( new TranslationTextComponent( "pmmo.text.logExtraDrop", 0 ).setStyle( new Style().setColor( TextFormatting.RED ) ) );
            }

            if( Requirements.data.get( "plantInfo" ).containsKey( regKey ) && Requirements.data.get( "plantInfo" ).get( regKey ).containsKey( "extraChance" ) )
            {
                if( XP.getExtraChance( player, item.getRegistryName(), "plant" ) > 0 )  //ORE EXTRA CHANCE
                    tooltip.add( new TranslationTextComponent( "pmmo.text.plantExtraDrop", DP.dp( XP.getExtraChance( player, item.getRegistryName(), "plant" ) / 100 ) ).setStyle( new Style().setColor( TextFormatting.GREEN ) ) );
                else
                    tooltip.add( new TranslationTextComponent( "pmmo.text.plantExtraDrop", 0 ).setStyle( new Style().setColor( TextFormatting.RED ) ) );
            }

            if( salvageInfo != null && !XP.getItem( (String) salvageInfo.get( "salvageItem" ) ).equals( Items.AIR ) )
            {
                level = XP.getLevel( "smithing", player );
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
                    tooltip.add( new TranslationTextComponent( "pmmo.text.cannotSalvageLackLevel", reqLevel ).setStyle( new Style().setColor( TextFormatting.RED ) ) );
                }
                else
                {
                    if( potentialReturnAmount > 0 )
                        tooltip.add( new TranslationTextComponent( "pmmo.text.salvagesInto", potentialReturnAmount, new TranslationTextComponent( salvageItem.getTranslationKey() ) ).setStyle( new Style().setColor( TextFormatting.GREEN ) ) );
                    else
                        tooltip.add( new TranslationTextComponent( "pmmo.text.salvagesInto", potentialReturnAmount, new TranslationTextComponent( salvageItem.getTranslationKey() ) ).setStyle( new Style().setColor( TextFormatting.RED ) ) );
                    if( chance > 0 )
                        tooltip.add( new TranslationTextComponent( "pmmo.text.xpEachChanceEach", " " + DP.dp( xpPerItem ), DP.dp( chance ) ).setStyle( new Style().setColor( TextFormatting.GREEN ) ) );
                    else
                        tooltip.add( new TranslationTextComponent( "pmmo.text.xpEachChanceEach", " " + DP.dp( xpPerItem ), DP.dp( chance ) ).setStyle( new Style().setColor( TextFormatting.RED ) ) );
                }
            }

            if( salvagesFrom != null )
            {
                tooltip.add( new TranslationTextComponent( "pmmo.text.salvagesFrom" ).setStyle( new Style().setColor( TextFormatting.GREEN ) ) );
                level = XP.getLevel( "smithing", player );

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

                salvageInfo = Requirements.data.get( "salvageInfo" ).get( key );

                if( salvageInfo != null && (double) salvageInfo.get( "levelReq" ) <= level )
                    tooltip.add( new TranslationTextComponent( "pmmo.text.salvagesFromItem", " " + value, displayName ).setStyle( new Style().setColor( TextFormatting.GREEN ) ) );
                else
                    tooltip.add( new TranslationTextComponent( "pmmo.text.salvagesFromItem", " " + value, displayName ).setStyle( new Style().setColor( TextFormatting.RED ) ) );
            }
        }
    }
}