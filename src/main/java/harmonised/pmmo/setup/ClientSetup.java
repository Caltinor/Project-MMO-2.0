package harmonised.pmmo.setup;

import harmonised.pmmo.client.gui.IndicatorsOverlayGUI;
import harmonised.pmmo.client.gui.TutorialOverlayGUI;
import harmonised.pmmo.client.gui.XPOverlayGUI;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid=Reference.MOD_ID, value= Dist.CLIENT)
public class ClientSetup {

	public static KeyMapping.Category CATEGORY = new KeyMapping.Category(Reference.rl("pmmo"));
	public static final KeyMapping SHOW_VEIN = new KeyMapping(LangProvider.KEYBIND_SHOWVEIN.key(), GLFW.GLFW_KEY_TAB, CATEGORY);
	public static final KeyMapping ADD_VEIN = new KeyMapping(LangProvider.KEYBIND_ADDVEIN.key(), GLFW.GLFW_KEY_LEFT_BRACKET, CATEGORY);
	public static final KeyMapping SUB_VEIN = new KeyMapping(LangProvider.KEYBIND_SUBVEIN.key(), GLFW.GLFW_KEY_RIGHT_BRACKET, CATEGORY);
	public static final KeyMapping CYCLE_VEIN = new KeyMapping(LangProvider.KEYBIND_VEINCYCLE.key(), GLFW.GLFW_KEY_APOSTROPHE, CATEGORY);
    public static final KeyMapping SHOW_LIST = new KeyMapping(LangProvider.KEYBIND_SHOWLIST.key(), GLFW.GLFW_KEY_LEFT_ALT, CATEGORY);
    public static final KeyMapping VEIN_KEY = new KeyMapping(LangProvider.KEYBIND_VEIN.key(), GLFW.GLFW_KEY_GRAVE_ACCENT, CATEGORY);
    public static final KeyMapping OPEN_MENU = new KeyMapping(LangProvider.KEYBIND_OPENMENU.key(), GLFW.GLFW_KEY_P, CATEGORY);
	public static final KeyMapping TOGGLE_BRKSPD = new KeyMapping(LangProvider.KEYBIND_TOGGLE_BRKSPD.key(), GLFW.GLFW_KEY_MINUS, CATEGORY);

    @SubscribeEvent
    public static void init(RegisterKeyMappingsEvent event) {
    	event.register(SHOW_VEIN);
    	event.register(ADD_VEIN);
    	event.register(SUB_VEIN);
    	event.register(CYCLE_VEIN);
    	event.register(SHOW_LIST);
    	event.register(VEIN_KEY);
    	event.register(OPEN_MENU);
		event.register(TOGGLE_BRKSPD);
    }
    
    @SubscribeEvent
    public static void registerOverlay(RegisterGuiLayersEvent event) {
    	event.registerAboveAll(Reference.rl("stats_overlay"), new XPOverlayGUI());
    	event.registerAboveAll(Reference.rl("tutorial"), new TutorialOverlayGUI());
    	event.registerAbove(Reference.mc("crosshair"), Reference.rl("overlay_icons"), new IndicatorsOverlayGUI());
    }
	
}
