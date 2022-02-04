package harmonised.pmmo.client.events;

import harmonised.pmmo.setup.ClientSetup;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE, value=Dist.CLIENT)
public class KeyPressHandler {
	private static boolean wasVeining = false, wasOpenMenu = false, wasOpenSettings = false, wasOpenSkills = false, wasOpenGlossary = false, tooltipKeyWasPressed = false;

	@SuppressWarnings("resource")
	@SubscribeEvent
    public static void keyPressEvent(net.minecraftforge.client.event.InputEvent.KeyInputEvent event)
    {
        if(Minecraft.getInstance().player != null)
        {
            if(wasVeining != ClientSetup.VEIN_KEY.isDown())
            {
                wasVeining = ClientSetup.VEIN_KEY.isDown();
                //NetworkHandler.sendToServer(new MessageKeypress(ClientSetup.VEIN_KEY.isDown(), 1));
            }

            if(wasOpenMenu != ClientSetup.OPEN_MENU.isDown() || wasOpenSettings != ClientSetup.OPEN_SETTINGS.isDown() || wasOpenSkills != ClientSetup.OPEN_SKILLS.isDown() || wasOpenGlossary != ClientSetup.OPEN_GLOSSARY.isDown())
            {
                if(Minecraft.getInstance().screen == null)
                {
                    if(ClientSetup.OPEN_MENU.isDown())
                    {
                        //Minecraft.getInstance().setScreen(new MainScreen(uuid, new TranslatableComponent("pmmo.potato")));
                        wasOpenMenu = ClientSetup.OPEN_MENU.isDown();
                    }
                    else if(ClientSetup.OPEN_SETTINGS.isDown())
                    {
                        //Minecraft.getInstance().setScreen(new PrefsChoiceScreen(new TranslatableComponent("pmmo.preferences")));
                        wasOpenSettings = ClientSetup.OPEN_SETTINGS.isDown();
                    }
                    else if(ClientSetup.OPEN_SKILLS.isDown())
                    {
                        //Minecraft.getInstance().setScreen(new ListScreen(uuid,  new TranslatableComponent("pmmo.skills"), "", JType.SKILLS, Minecraft.getInstance().player));
                        wasOpenSkills = ClientSetup.OPEN_SKILLS.isDown();
                    }
                    else if(ClientSetup.OPEN_GLOSSARY.isDown())
                    {
                        //Minecraft.getInstance().setScreen(new GlossaryScreen(uuid, new TranslatableComponent("pmmo.glossary"), true));
                        wasOpenGlossary = ClientSetup.OPEN_GLOSSARY.isDown();
                    }
                }

            }

            if(!(Minecraft.getInstance().player == null) && ClientSetup.TOGGLE_TOOLTIP.isDown() && !tooltipKeyWasPressed)
            {
                TooltipHandler.tooltipOn = !TooltipHandler.tooltipOn;
                if(TooltipHandler.tooltipOn)
                    Minecraft.getInstance().player.displayClientMessage(new TranslatableComponent("pmmo.tooltipOn"), true);
                else
                    Minecraft.getInstance().player.displayClientMessage(new TranslatableComponent("pmmo.tooltipOff"), true);
            }

            tooltipKeyWasPressed = ClientSetup.TOGGLE_TOOLTIP.isDown();
        }
    }

}
