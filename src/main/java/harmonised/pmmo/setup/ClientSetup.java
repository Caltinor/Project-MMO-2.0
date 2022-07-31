package harmonised.pmmo.setup;

import org.lwjgl.glfw.GLFW;

import harmonised.pmmo.client.gui.XPOverlayGUI;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE, value=Dist.CLIENT)
public class ClientSetup {

	public static final KeyMapping SHOW_VEIN = new KeyMapping("key.pmmo.showVein", GLFW.GLFW_KEY_TAB, "category.pmmo");
	public static final KeyMapping ADD_VEIN = new KeyMapping("key.pmmo.addvein", GLFW.GLFW_KEY_LEFT_BRACKET, "category.pmmo");
	public static final KeyMapping SUB_VEIN = new KeyMapping("key.pmmo.subvein", GLFW.GLFW_KEY_RIGHT_BRACKET, "category.pmmo");
    public static final KeyMapping SHOW_LIST = new KeyMapping("key.pmmo.showList", GLFW.GLFW_KEY_LEFT_ALT, "category.pmmo");
    //public static final KeyMapping TOGGLE_TOOLTIP = new KeyMapping("key.pmmo.toggleTooltip", GLFW.GLFW_KEY_F6, "category.pmmo");
    public static final KeyMapping VEIN_KEY = new KeyMapping("key.pmmo.vein", GLFW.GLFW_KEY_GRAVE_ACCENT, "category.pmmo");
    public static final KeyMapping OPEN_MENU = new KeyMapping("key.pmmo.openMenu", GLFW.GLFW_KEY_P, "category.pmmo");
    //public static final KeyMapping OPEN_SETTINGS = new KeyMapping("key.pmmo.openSettings", GLFW.GLFW_KEY_UNKNOWN, "category.pmmo");
    //public static final KeyMapping OPEN_SKILLS = new KeyMapping("key.pmmo.openSkills", GLFW.GLFW_KEY_UNKNOWN, "category.pmmo");
    //public static final KeyMapping OPEN_GLOSSARY = new KeyMapping("key.pmmo.openGlossary", GLFW.GLFW_KEY_UNKNOWN, "category.pmmo");
    
    public static final IIngameOverlay STATS_OVERLAY = OverlayRegistry.registerOverlayTop("stats_overlay", new XPOverlayGUI());
    
    public static void init(FMLClientSetupEvent event) {
    	ClientRegistry.registerKeyBinding(SHOW_VEIN);
		ClientRegistry.registerKeyBinding(ADD_VEIN);
		ClientRegistry.registerKeyBinding(SUB_VEIN);
    	ClientRegistry.registerKeyBinding(SHOW_LIST);
    	ClientRegistry.registerKeyBinding(VEIN_KEY);
    	ClientRegistry.registerKeyBinding(OPEN_MENU);
    }
}
