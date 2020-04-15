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
import net.minecraft.entity.PMMOPoseSetter;
import net.minecraft.entity.Pose;
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

            if( ClientHandler.CRAWL_KEY.isKeyDown() )
                XP.isCrawling.add( Minecraft.getInstance().player.getUniqueID() );
            else
                XP.isCrawling.remove( Minecraft.getInstance().player.getUniqueID() );
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
                    tooltip.add( new TranslationTextComponent( "pmmo.text.levelDisplay", " " + new TranslationTextComponent( "pmmo.text." + key ).getString(), value ).setStyle( new Style().setColor( TextFormatting.RED ) ) );
                else
                    tooltip.add( new TranslationTextComponent( "pmmo.text.levelDisplay", " " + new TranslationTextComponent( "pmmo.text." + key ).getString(), value ).setStyle( new Style().setColor( TextFormatting.GREEN ) ) );
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

                switch( skill )
                {
                    case "mining":
                        break;

                    case "woodcutting":
                        tooltip.add( new TranslationTextComponent( "pmmo.text.logExtraChance", DP.dp( theMap.get( "extraChance" ) * level ) ).setStyle( new Style().setColor( TextFormatting.GREEN ) ) );
                        break;

                    case "farming":
                        tooltip.add( new TranslationTextComponent( "pmmo.text.plantExtraChance", DP.dp( theMap.get( "extraChance" ) * level ) ).setStyle( new Style().setColor( TextFormatting.GREEN ) ) );
                        break;
                }
            }
        }
    }

    @SubscribeEvent
    public static void tooltipEvent( ItemTooltipEvent event )
    {
        PlayerEntity player = event.getPlayer();

        if( player != null )
        {
            Item item = event.getItemStack().getItem();
            List<ITextComponent> tooltip = event.getToolTip();
            int level;
            String regKey = item.getRegistryName().toString();
            float hardness;
            double dValue;

            Map<String, Double> wearReq = Requirements.wearReq.get( regKey );
            Map<String, Double> toolReq = Requirements.toolReq.get( regKey );
            Map<String, Double> weaponReq = Requirements.weaponReq.get( regKey );
            Map<String, Double> placeReq = Requirements.placeReq.get( regKey );
            Map<String, Double> breakReq = Requirements.breakReq.get( regKey );
            Map<String, Double> xpValue = Requirements.xpValue.get( regKey );

            if( xpValue != null && xpValue.size() > 0 )      //XP VALUE
            {
                tooltip.add( new TranslationTextComponent( "pmmo.text.xpValue" ) );

                for( String key : xpValue.keySet() )
                {
                    dValue = xpValue.get( key );

                    tooltip.add( new TranslationTextComponent( "pmmo.text.levelDisplay", " " + new TranslationTextComponent( "pmmo.text." + key ).getString(), DP.dp( dValue ) ) );
                }
            }

            if( item instanceof BlockItem )
            {
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

//            if( wearReq != null && wearReq.size() > 0 )
//                addTooltipTextSkill( "pmmo.text.wear", "mob", mobReq, event );

            if( placeReq != null && placeReq.size() > 0 )
                addTooltipTextSkill( "pmmo.text.placeDown", "place", placeReq, event );

            if( breakReq != null && breakReq.size() > 0 )
                addTooltipTextSkill( "pmmo.text.break", "break", breakReq, event );

            if( XP.getExtraChance( regKey, "ore", XP.getBreakReqGap( regKey, player, "mining" ) ) > 0 )  //ORE EXTRA CHANCE
                tooltip.add( new TranslationTextComponent( "pmmo.text.oreExtraChance", DP.dp( XP.getExtraChance( regKey, "ore", XP.getBreakReqGap( regKey, player, "mining" ) ) ) ).setStyle( new Style().setColor( TextFormatting.GREEN ) ) );
            else if( Requirements.oreInfo.containsKey( regKey ) && Requirements.oreInfo.get( regKey ).containsKey( "extraChance" ) )
                tooltip.add( new TranslationTextComponent( "pmmo.text.oreExtraChance", 0 ).setStyle( new Style().setColor( TextFormatting.RED ) ) );

            if( XP.getExtraChance( regKey, "log", XP.getBreakReqGap( regKey, player, "woodcutting" ) ) > 0 )  //LOG EXTRA CHANCE
                tooltip.add( new TranslationTextComponent( "pmmo.text.logExtraChance", DP.dp( XP.getExtraChance( regKey, "log", XP.getBreakReqGap( regKey, player, "woodcutting" ) ) ) ).setStyle( new Style().setColor( TextFormatting.GREEN ) ) );
            else if( Requirements.logInfo.containsKey( regKey ) && Requirements.logInfo.get( regKey ).containsKey( "extraChance" ) )
                tooltip.add( new TranslationTextComponent( "pmmo.text.logExtraChance", 0 ).setStyle( new Style().setColor( TextFormatting.RED ) ) );

            if( XP.getExtraChance( regKey, "plant", XP.getBreakReqGap( regKey, player, "farming" ) ) > 0 )  //PLANT EXTRA CHANCE
                tooltip.add( new TranslationTextComponent( "pmmo.text.plantExtraChance", DP.dp( XP.getExtraChance( regKey, "plant", XP.getBreakReqGap( regKey, player, "farming" ) ) ) ).setStyle( new Style().setColor( TextFormatting.GREEN ) ) );
            else if( Requirements.plantInfo.containsKey( regKey ) && Requirements.plantInfo.get( regKey ).containsKey( "extraChance" ) )
                tooltip.add( new TranslationTextComponent( "pmmo.text.plantExtraChance", 0 ).setStyle( new Style().setColor( TextFormatting.RED ) ) );

        }
    }
}