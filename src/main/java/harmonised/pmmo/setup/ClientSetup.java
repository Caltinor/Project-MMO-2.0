package harmonised.pmmo.setup;

import org.lwjgl.glfw.GLFW;

import harmonised.pmmo.client.gui.XPOverlayGUI;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID, bus=Mod.EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
public class ClientSetup {

	public static final KeyMapping SHOW_VEIN = new KeyMapping(LangProvider.KEYBIND_SHOWVEIN.key(), GLFW.GLFW_KEY_TAB, LangProvider.KEYBIND_CATEGORY.key());
	public static final KeyMapping ADD_VEIN = new KeyMapping(LangProvider.KEYBIND_ADDVEIN.key(), GLFW.GLFW_KEY_LEFT_BRACKET, LangProvider.KEYBIND_CATEGORY.key());
	public static final KeyMapping SUB_VEIN = new KeyMapping(LangProvider.KEYBIND_SUBVEIN.key(), GLFW.GLFW_KEY_RIGHT_BRACKET, LangProvider.KEYBIND_CATEGORY.key());
    public static final KeyMapping SHOW_LIST = new KeyMapping(LangProvider.KEYBIND_SHOWLIST.key(), GLFW.GLFW_KEY_LEFT_ALT, LangProvider.KEYBIND_CATEGORY.key());
    //public static final KeyMapping TOGGLE_TOOLTIP = new KeyMapping("key.pmmo.toggleTooltip", GLFW.GLFW_KEY_F6, LangProvider.KEYBIND_CATEGORY.key());
    public static final KeyMapping VEIN_KEY = new KeyMapping(LangProvider.KEYBIND_VEIN.key(), GLFW.GLFW_KEY_GRAVE_ACCENT, LangProvider.KEYBIND_CATEGORY.key());
    public static final KeyMapping OPEN_MENU = new KeyMapping(LangProvider.KEYBIND_OPENMENU.key(), GLFW.GLFW_KEY_P, LangProvider.KEYBIND_CATEGORY.key());
    //public static final KeyMapping OPEN_SETTINGS = new KeyMapping("key.pmmo.openSettings", GLFW.GLFW_KEY_UNKNOWN, LangProvider.KEYBIND_CATEGORY.key());
    //public static final KeyMapping OPEN_SKILLS = new KeyMapping("key.pmmo.openSkills", GLFW.GLFW_KEY_UNKNOWN, LangProvider.KEYBIND_CATEGORY.key());
    //public static final KeyMapping OPEN_GLOSSARY = new KeyMapping("key.pmmo.openGlossary", GLFW.GLFW_KEY_UNKNOWN, LangProvider.KEYBIND_CATEGORY.key());
    
    @SubscribeEvent
    public static void init(RegisterKeyMappingsEvent event) {
    	event.register(SHOW_VEIN);
    	event.register(ADD_VEIN);
    	event.register(SUB_VEIN);
    	event.register(SHOW_LIST);
    	event.register(VEIN_KEY);
    	event.register(OPEN_MENU);
    }
    
    @SubscribeEvent
    public static void registerOverlay(RegisterGuiOverlaysEvent event) {
    	event.registerAboveAll("stats_overlay", new XPOverlayGUI());
    }
}
