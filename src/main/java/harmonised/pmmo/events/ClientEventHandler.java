package harmonised.pmmo.events;

import harmonised.pmmo.config.JType;
import harmonised.pmmo.gui.*;
import harmonised.pmmo.network.MessageKeypress;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.proxy.ClientHandler;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.*;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.UUID;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandler
{
    private static boolean wasVeining = false, wasOpenMenu = false, wasOpenSettings = false, wasOpenSkills = false, wasOpenGlossary = false, tooltipKeyWasPressed = false;

    public static void subscribeClientEvents( IEventBus eventBus )
    {
        eventBus.register( harmonised.pmmo.events.ClientEventHandler.class );
    }

    @SubscribeEvent
    public static void keyPressEvent( net.minecraftforge.client.event.InputEvent.KeyInputEvent event )
    {
        if( Minecraft.getMinecraft().player != null )
        {
            if( wasVeining != ClientHandler.VEIN_KEY.isKeyDown() )
            {
                wasVeining = ClientHandler.VEIN_KEY.isKeyDown();
                NetworkHandler.sendToServer( new MessageKeypress( ClientHandler.VEIN_KEY.isKeyDown(), 1 ) );
            }

            if( wasOpenMenu != ClientHandler.OPEN_MENU.isKeyDown() || wasOpenSettings != ClientHandler.OPEN_SETTINGS.isKeyDown() || wasOpenSkills != ClientHandler.OPEN_SKILLS.isKeyDown() || wasOpenGlossary != ClientHandler.OPEN_GLOSSARY.isKeyDown() )
            {
                UUID uuid = Minecraft.getMinecraft().player.getUniqueID();
                String name = Minecraft.getMinecraft().player.getDisplayName().getUnformattedText();

                XP.playerNames.put( uuid, name );

                XPOverlayGUI.skills.forEach( (skill, aSkill) ->
                {
                    XP.getOfflineXpMap( uuid ).put( skill, aSkill.goalXp );
                });

                if( Minecraft.getMinecraft().currentScreen == null )
                {
                    if( ClientHandler.OPEN_MENU.isKeyDown() )
                    {
                        Minecraft.getMinecraft().displayGuiScreen( new MainScreen( uuid, new TextComponentTranslation( "pmmo.potato" ) ) );
                        wasOpenMenu = ClientHandler.OPEN_MENU.isKeyDown();
                    }
                    else if( ClientHandler.OPEN_SETTINGS.isKeyDown() )
                    {
                        Minecraft.getMinecraft().displayGuiScreen( new PrefsChoiceScreen( new TextComponentTranslation( "pmmo.preferences" ) ) );
                        wasOpenSettings = ClientHandler.OPEN_SETTINGS.isKeyDown();
                    }
                    else if( ClientHandler.OPEN_SKILLS.isKeyDown() )
                    {
                        Minecraft.getMinecraft().displayGuiScreen( new ListScreen( uuid,  new TextComponentTranslation( "pmmo.stats" ), "", JType.STATS, Minecraft.getMinecraft().player ) );
                        wasOpenSkills = ClientHandler.OPEN_SKILLS.isKeyDown();
                    }
                    else if( ClientHandler.OPEN_GLOSSARY.isKeyDown() )
                    {
                        Minecraft.getMinecraft().displayGuiScreen( new GlossaryScreen( uuid, new TextComponentTranslation( "pmmo.glossary" ), true ) );
                        wasOpenGlossary = ClientHandler.OPEN_GLOSSARY.isKeyDown();
                    }
                }

            }

            if( !(Minecraft.getMinecraft().player == null) && ClientHandler.TOGGLE_TOOLTIP.isKeyDown() && !tooltipKeyWasPressed )
            {
                TooltipHandler.tooltipOn = !TooltipHandler.tooltipOn;
                if( TooltipHandler.tooltipOn )
                    Minecraft.getMinecraft().player.sendStatusMessage( new TextComponentTranslation( "pmmo.tooltipOn" ), true );
                else
                    Minecraft.getMinecraft().player.sendStatusMessage( new TextComponentTranslation( "pmmo.tooltipOff" ), true );
            }

            tooltipKeyWasPressed = ClientHandler.TOGGLE_TOOLTIP.isKeyDown();
        }
    }

    @SubscribeEvent
    public static void tooltipEvent( ItemTooltipEvent event )
    {
        TooltipHandler.handleTooltip( event );
    }
}