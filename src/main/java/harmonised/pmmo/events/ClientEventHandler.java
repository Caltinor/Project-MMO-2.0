package harmonised.pmmo.events;

import harmonised.pmmo.config.JType;
import harmonised.pmmo.gui.*;
import harmonised.pmmo.network.MessageKeypress;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.proxy.ClientHandler;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
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
        if( Minecraft.getInstance().player != null )
        {
            if( wasVeining != ClientHandler.VEIN_KEY.isKeyDown() )
            {
                wasVeining = ClientHandler.VEIN_KEY.isKeyDown();
                NetworkHandler.sendToServer( new MessageKeypress( ClientHandler.VEIN_KEY.isKeyDown(), 1 ) );
            }

            if( wasOpenMenu != ClientHandler.OPEN_MENU.isKeyDown() || wasOpenSettings != ClientHandler.OPEN_SETTINGS.isKeyDown() || wasOpenSkills != ClientHandler.OPEN_SKILLS.isKeyDown() || wasOpenGlossary != ClientHandler.OPEN_GLOSSARY.isKeyDown() )
            {
                UUID uuid = Minecraft.getInstance().player.getUniqueID();
                String name = Minecraft.getInstance().player.getDisplayName().getString();

                XP.playerNames.put( uuid, name );

                XPOverlayGUI.skills.forEach( (skill, aSkill) ->
                {
                    XP.getOfflineXpMap( uuid ).put( skill, aSkill.goalXp );
                });

                if( ClientHandler.OPEN_MENU.isKeyDown() )
                {
                    Minecraft.getInstance().displayGuiScreen( new MainScreen( uuid, new TranslationTextComponent( "pmmo.potato" ) ) );
                    wasOpenMenu = ClientHandler.OPEN_MENU.isKeyDown();
                }
                else if( ClientHandler.OPEN_SETTINGS.isKeyDown() )
                {
                    Minecraft.getInstance().displayGuiScreen( new PrefsChoiceScreen( new TranslationTextComponent( "pmmo.preferences" ) ) );
                    wasOpenSettings = ClientHandler.OPEN_SETTINGS.isKeyDown();
                }
                else if( ClientHandler.OPEN_SKILLS.isKeyDown() )
                {
                    Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid,  new TranslationTextComponent( "pmmo.stats" ), JType.STATS, Minecraft.getInstance().player ) );
                    wasOpenSkills = ClientHandler.OPEN_SKILLS.isKeyDown();
                }
                else if( ClientHandler.OPEN_GLOSSARY.isKeyDown() )
                {
                    Minecraft.getInstance().displayGuiScreen( new GlossaryScreen( uuid, new TranslationTextComponent( "pmmo.glossary" ) ) );
                    wasOpenGlossary = ClientHandler.OPEN_GLOSSARY.isKeyDown();
                }
            }

            if( !(Minecraft.getInstance().player == null) && ClientHandler.TOGGLE_TOOLTIP.isKeyDown() && !tooltipKeyWasPressed )
            {
                TooltipHandler.tooltipOn = !TooltipHandler.tooltipOn;
                if( TooltipHandler.tooltipOn )
                    Minecraft.getInstance().player.sendStatusMessage( new TranslationTextComponent( "pmmo.tooltipOn" ), true );
                else
                    Minecraft.getInstance().player.sendStatusMessage( new TranslationTextComponent( "pmmo.tooltipOff" ), true );
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