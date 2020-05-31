package harmonised.pmmo.events;

import harmonised.pmmo.gui.MainScreen;
import harmonised.pmmo.gui.ScrollScreen;
import harmonised.pmmo.gui.XPOverlayGUI;
import harmonised.pmmo.network.MessageKeypress;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.proxy.ClientHandler;
import harmonised.pmmo.skills.XP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.UUID;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandler
{
    private static boolean wasCrawling = false, wasVeining = false, wasOpenMenu = false, tooltipKeyWasPressed = false;
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
                NetworkHandler.sendToServer( new MessageKeypress( ClientHandler.CRAWL_KEY.isKeyDown(), 0 ) );
            }

            if( wasVeining != ClientHandler.VEIN_KEY.isKeyDown() )
            {
                wasVeining = ClientHandler.VEIN_KEY.isKeyDown();
                NetworkHandler.sendToServer( new MessageKeypress( ClientHandler.VEIN_KEY.isKeyDown(), 1 ) );
            }

            if( wasOpenMenu != ClientHandler.OPEN_MENU.isKeyDown() )
            {
                UUID uuid = Minecraft.getInstance().player.getUniqueID();
                String name = Minecraft.getInstance().player.getDisplayName().getString();

                XP.playerNames.put( uuid, name );

                if( !XP.skills.containsKey( uuid ) )
                    XP.skills.put( uuid, new HashMap<>() );

                XPOverlayGUI.skills.forEach( (skill, aSkill) ->
                {
                    XP.skills.get( uuid ).put( skill.name().toLowerCase(), aSkill.goalXp );
                });

                if( name.equals( "Dev" ) || name.equals( "MareoftheStars" ) || name.equals( "Harmonised" ) )
                    Minecraft.getInstance().displayGuiScreen( new MainScreen( uuid, new TranslationTextComponent( "pmmo.potato" ) ) );

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