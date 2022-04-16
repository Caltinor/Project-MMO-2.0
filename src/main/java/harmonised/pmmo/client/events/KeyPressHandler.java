package harmonised.pmmo.client.events;

import harmonised.pmmo.client.utils.VeinTracker;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.network.serverpackets.SP_UpdateVeinTarget;
import harmonised.pmmo.setup.ClientSetup;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE, value=Dist.CLIENT)
public class KeyPressHandler {
	private static boolean wasOpenMenu = false, wasOpenSettings = false, wasOpenSkills = false, wasOpenGlossary = false, tooltipKeyWasPressed = false;

	@SuppressWarnings("resource")
	@SubscribeEvent
    public static void keyPressEvent(net.minecraftforge.client.event.InputEvent.KeyInputEvent event)
    {
		Minecraft mc = Minecraft.getInstance();
        if(mc.player != null)
        {
            if(ClientSetup.VEIN_KEY.isDown() && mc.hitResult != null && mc.hitResult instanceof BlockHitResult) {
            	BlockHitResult bhr = (BlockHitResult) mc.hitResult;
            	VeinTracker.setTarget(bhr.getBlockPos());
            	Networking.sendToServer(new SP_UpdateVeinTarget(bhr.getBlockPos()));
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
