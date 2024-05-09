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

@EventBusSubscriber(modid=Reference.MOD_ID, bus=EventBusSubscriber.Bus.MOD, value= Dist.CLIENT)
public class ClientSetup {

	public static final KeyMapping SHOW_VEIN = new KeyMapping(LangProvider.KEYBIND_SHOWVEIN.key(), GLFW.GLFW_KEY_TAB, LangProvider.KEYBIND_CATEGORY.key());
	public static final KeyMapping ADD_VEIN = new KeyMapping(LangProvider.KEYBIND_ADDVEIN.key(), GLFW.GLFW_KEY_LEFT_BRACKET, LangProvider.KEYBIND_CATEGORY.key());
	public static final KeyMapping SUB_VEIN = new KeyMapping(LangProvider.KEYBIND_SUBVEIN.key(), GLFW.GLFW_KEY_RIGHT_BRACKET, LangProvider.KEYBIND_CATEGORY.key());
	public static final KeyMapping CYCLE_VEIN = new KeyMapping(LangProvider.KEYBIND_VEINCYCLE.key(), GLFW.GLFW_KEY_APOSTROPHE, LangProvider.KEYBIND_CATEGORY.key());
    public static final KeyMapping SHOW_LIST = new KeyMapping(LangProvider.KEYBIND_SHOWLIST.key(), GLFW.GLFW_KEY_LEFT_ALT, LangProvider.KEYBIND_CATEGORY.key());
    public static final KeyMapping VEIN_KEY = new KeyMapping(LangProvider.KEYBIND_VEIN.key(), GLFW.GLFW_KEY_GRAVE_ACCENT, LangProvider.KEYBIND_CATEGORY.key());
    public static final KeyMapping OPEN_MENU = new KeyMapping(LangProvider.KEYBIND_OPENMENU.key(), GLFW.GLFW_KEY_P, LangProvider.KEYBIND_CATEGORY.key());

    @SubscribeEvent
    public static void init(RegisterKeyMappingsEvent event) {
    	event.register(SHOW_VEIN);
    	event.register(ADD_VEIN);
    	event.register(SUB_VEIN);
    	event.register(CYCLE_VEIN);
    	event.register(SHOW_LIST);
    	event.register(VEIN_KEY);
    	event.register(OPEN_MENU);
    }
    
    @SubscribeEvent
    public static void registerOverlay(RegisterGuiLayersEvent event) {
    	event.registerAboveAll(new ResourceLocation(Reference.MOD_ID, "stats_overlay"), new XPOverlayGUI());
    	event.registerAboveAll(new ResourceLocation(Reference.MOD_ID, "tutorial"), new TutorialOverlayGUI());
    	event.registerAbove(new ResourceLocation("crosshair"), new ResourceLocation(Reference.MOD_ID, "overlay_icons"), new IndicatorsOverlayGUI());
    }
}
