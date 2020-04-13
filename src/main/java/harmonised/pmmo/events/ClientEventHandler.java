package harmonised.pmmo.events;

import harmonised.pmmo.config.Requirements;
import harmonised.pmmo.gui.XPOverlayGUI;
import harmonised.pmmo.network.MessageCrawling;
import harmonised.pmmo.network.MessageDoubleTranslation;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.proxy.ClientHandler;
import harmonised.pmmo.skills.XP;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.Reference;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandler
{
    private static boolean wasCrawling = false;

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
        }
    }

    private static void addTooltipTextSkill( String tKey, String type, Map<String, Double> theMap, ItemTooltipEvent event )
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

                value = (int) Math.floor( theMap.get( key ) );

                if( level < value )
                    tooltip.add( new TranslationTextComponent( "pmmo.text.levelDisplay", " " + key, value ).setStyle( new Style().setColor( TextFormatting.RED ) ) );
                else
                    tooltip.add( new TranslationTextComponent( "pmmo.text.levelDisplay", " " + key, value ).setStyle( new Style().setColor( TextFormatting.GREEN ) ) );
            }
        }
    }

    private static void addTooltipTextInfo( Map<String, Double> theMap, String skill, ItemTooltipEvent event )
    {
        int level;
        List<ITextComponent> tooltip = event.getToolTip();

        if( theMap != null && theMap.size() > 0 )
        {
            if( theMap.get( "extraChance" ) != null )
            {
                if(XPOverlayGUI.skills.containsKey( skill ) )
                    level = XP.levelAtXp( XPOverlayGUI.skills.get( skill ).goalXp );
                else
                    level = 1;

                tooltip.add( new TranslationTextComponent( "pmmo.text.oreExtraChance", DP.dp( theMap.get( "extraChance" ) * level ) ).setStyle( new Style().setColor( TextFormatting.GREEN ) ) );
            }
        }
    }

    @SubscribeEvent
    public static void tooltipEvent( ItemTooltipEvent event )
    {
        if( event.getPlayer() != null )
        {
            Item item = event.getItemStack().getItem();
            List<ITextComponent> tooltip = event.getToolTip();
            int level;
            double dValue;

            Map<String, Double> wearReq = Requirements.wearReq.get( item.getRegistryName().toString() );
            Map<String, Double> toolReq = Requirements.toolReq.get( item.getRegistryName().toString() );
            Map<String, Double> weaponReq = Requirements.weaponReq.get( item.getRegistryName().toString() );
            Map<String, Double> placeReq = Requirements.placeReq.get( item.getRegistryName().toString() );
            Map<String, Double> breakReq = Requirements.breakReq.get( item.getRegistryName().toString() );
            Map<String, Double> xpValue = Requirements.xpValue.get( item.getRegistryName().toString() );
            Map<String, Double> oreInfo = Requirements.oreInfo.get( item.getRegistryName().toString() );
            Map<String, Double> logInfo = Requirements.logInfo.get( item.getRegistryName().toString() );
            Map<String, Double> plantInfo = Requirements.plantInfo.get( item.getRegistryName().toString() );

            if( xpValue != null && xpValue.size() > 0 )      //XP VALUE
            {
                tooltip.add( new TranslationTextComponent( "pmmo.text.xpValue" ) );

                for( String key : xpValue.keySet() )
                {
                    dValue = xpValue.get( key );

                    tooltip.add( new TranslationTextComponent( "pmmo.text.levelDisplay", " " + key, DP.dp( dValue ) ) );
                }
            }

            if( wearReq != null && wearReq.size() > 0 )
                addTooltipTextSkill( "pmmo.text.armor", "wear", wearReq, event );

            if( toolReq != null && toolReq.size() > 0 )
                addTooltipTextSkill( "pmmo.text.tool", "tool", toolReq, event );

            if( weaponReq != null && weaponReq.size() > 0 )
                addTooltipTextSkill( "pmmo.text.weapon", "weapon", weaponReq, event );

//            if( wearReq != null && wearReq.size() > 0 )
//                addTooltipTextSkill( "pmmo.text.wear", "mob", mobReq, event );

            if( placeReq != null && placeReq.size() > 0 )
                addTooltipTextSkill( "pmmo.text.placeDown", "place", placeReq, event );

            if( breakReq != null && breakReq.size() > 0 )
                addTooltipTextSkill( "pmmo.text.break", "break", breakReq, event );

            if( oreInfo != null && oreInfo.size() > 0 )      //ORE INFO
                addTooltipTextInfo( oreInfo, "mining", event );

            if( logInfo != null && logInfo.size() > 0 )      //LOG INFO
                addTooltipTextInfo( logInfo, "woodcutting", event );

            if( plantInfo != null && plantInfo.size() > 0 )  //PLANT INFO
                addTooltipTextInfo( plantInfo, "farming", event );
        }
    }
}