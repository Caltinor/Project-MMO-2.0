package harmonised.pmmo.events;

import harmonised.pmmo.config.JType;
import harmonised.pmmo.gui.*;
import harmonised.pmmo.network.MessageKeypress;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.proxy.ClientHandler;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

import net.minecraft.network.chat.TranslatableComponent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEventHandler
{
    private static boolean wasVeining = false, wasOpenMenu = false, wasOpenSettings = false, wasOpenSkills = false, wasOpenGlossary = false, tooltipKeyWasPressed = false;

    public static void subscribeClientEvents( IEventBus eventBus )
    {
        eventBus.register( harmonised.pmmo.events.ClientEventHandler.class );
    }

    @SuppressWarnings("resource")
	@SubscribeEvent
    public static void keyPressEvent( net.minecraftforge.client.event.InputEvent.KeyInputEvent event )
    {
        if( Minecraft.getInstance().player != null )
        {
            if( wasVeining != ClientHandler.VEIN_KEY.isDown() )
            {
                wasVeining = ClientHandler.VEIN_KEY.isDown();
                NetworkHandler.sendToServer( new MessageKeypress( ClientHandler.VEIN_KEY.isDown(), 1 ) );
            }

            if( wasOpenMenu != ClientHandler.OPEN_MENU.isDown() || wasOpenSettings != ClientHandler.OPEN_SETTINGS.isDown() || wasOpenSkills != ClientHandler.OPEN_SKILLS.isDown() || wasOpenGlossary != ClientHandler.OPEN_GLOSSARY.isDown() )
            {
                UUID uuid = Minecraft.getInstance().player.getUUID();
                String name = Minecraft.getInstance().player.getDisplayName().getString();

                XP.playerNames.put( uuid, name );
                XP.playerUUIDs.put( name, uuid );

                XPOverlayGUI.skills.forEach( (skill, aSkill) ->
                {
                    XP.getOfflineXpMap( uuid ).put( skill, aSkill.goalXp );
                });

                if( Minecraft.getInstance().screen == null )
                {
                    if( ClientHandler.OPEN_MENU.isDown() )
                    {
                        Minecraft.getInstance().setScreen( new MainScreen( uuid, new TranslatableComponent( "pmmo.potato" ) ) );
                        wasOpenMenu = ClientHandler.OPEN_MENU.isDown();
                    }
                    else if( ClientHandler.OPEN_SETTINGS.isDown() )
                    {
                        Minecraft.getInstance().setScreen( new PrefsChoiceScreen( new TranslatableComponent( "pmmo.preferences" ) ) );
                        wasOpenSettings = ClientHandler.OPEN_SETTINGS.isDown();
                    }
                    else if( ClientHandler.OPEN_SKILLS.isDown() )
                    {
                        Minecraft.getInstance().setScreen( new ListScreen( uuid,  new TranslatableComponent( "pmmo.skills" ), "", JType.SKILLS, Minecraft.getInstance().player ) );
                        wasOpenSkills = ClientHandler.OPEN_SKILLS.isDown();
                    }
                    else if( ClientHandler.OPEN_GLOSSARY.isDown() )
                    {
                        Minecraft.getInstance().setScreen( new GlossaryScreen( uuid, new TranslatableComponent( "pmmo.glossary" ), true ) );
                        wasOpenGlossary = ClientHandler.OPEN_GLOSSARY.isDown();
                    }
                }

            }

            if( !(Minecraft.getInstance().player == null) && ClientHandler.TOGGLE_TOOLTIP.isDown() && !tooltipKeyWasPressed )
            {
                TooltipHandler.tooltipOn = !TooltipHandler.tooltipOn;
                if( TooltipHandler.tooltipOn )
                    Minecraft.getInstance().player.displayClientMessage( new TranslatableComponent( "pmmo.tooltipOn" ), true );
                else
                    Minecraft.getInstance().player.displayClientMessage( new TranslatableComponent( "pmmo.tooltipOff" ), true );
            }

            tooltipKeyWasPressed = ClientHandler.TOGGLE_TOOLTIP.isDown();
        }
    }

    @SubscribeEvent
    public static void tooltipEvent( ItemTooltipEvent event )
    {
        TooltipHandler.handleTooltip( event );
    }
}