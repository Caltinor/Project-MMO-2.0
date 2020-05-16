package harmonised.pmmo.events;

import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.gui.MainScreen;
import harmonised.pmmo.gui.ScreenshotHandler;
import harmonised.pmmo.gui.XPOverlayGUI;
import harmonised.pmmo.network.MessageCrawling;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.proxy.ClientHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.XP;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.Reference;
import javafx.stage.Screen;
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
    private static boolean wasCrawling = false, wasOpenMenu = false, tooltipKeyWasPressed = false;
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
                TooltipHandler.tooltipOn = !TooltipHandler.tooltipOn;
                if( TooltipHandler.tooltipOn )
                    Minecraft.getInstance().player.sendStatusMessage( new TranslationTextComponent( "pmmo.TooltipHandler.tooltipOn" ), true );
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

    @SubscribeEvent
    public static void tooltipEvent( ItemTooltipEvent event )
    {
        TooltipHandler.handleTooltip( event );
    }
}