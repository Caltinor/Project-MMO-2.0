package harmonised.pmmo.setup;

import org.lwjgl.glfw.GLFW;

import harmonised.pmmo.client.gui.XPOverlayGUI;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID, bus=Mod.EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
public class ClientSetup {

	public static final KeyMapping SHOW_VEIN = new KeyMapping(LangProvider.KEYBIND_SHOWVEIN.key(), GLFW.GLFW_KEY_TAB, LangProvider.KEYBIND_CATEGORY.key());
	public static final KeyMapping ADD_VEIN = new KeyMapping(LangProvider.KEYBIND_ADDVEIN.key(), GLFW.GLFW_KEY_LEFT_BRACKET, LangProvider.KEYBIND_CATEGORY.key());
	public static final KeyMapping SUB_VEIN = new KeyMapping(LangProvider.KEYBIND_SUBVEIN.key(), GLFW.GLFW_KEY_RIGHT_BRACKET, LangProvider.KEYBIND_CATEGORY.key());
	public static final KeyMapping CYCLE_VEIN = new KeyMapping(LangProvider.KEYBIND_VEINCYCLE.key(), GLFW.GLFW_KEY_APOSTROPHE, LangProvider.KEYBIND_CATEGORY.key());
    public static final KeyMapping SHOW_LIST = new KeyMapping(LangProvider.KEYBIND_SHOWLIST.key(), GLFW.GLFW_KEY_LEFT_ALT, LangProvider.KEYBIND_CATEGORY.key());
    //public static final KeyMapping TOGGLE_TOOLTIP = new KeyMapping("key.pmmo.toggleTooltip", GLFW.GLFW_KEY_F6, LangProvider.KEYBIND_CATEGORY.key());
    public static final KeyMapping VEIN_KEY = new KeyMapping(LangProvider.KEYBIND_VEIN.key(), GLFW.GLFW_KEY_GRAVE_ACCENT, LangProvider.KEYBIND_CATEGORY.key());
    public static final KeyMapping OPEN_MENU = new KeyMapping(LangProvider.KEYBIND_OPENMENU.key(), GLFW.GLFW_KEY_P, LangProvider.KEYBIND_CATEGORY.key());
    //public static final KeyMapping OPEN_SETTINGS = new KeyMapping("key.pmmo.openSettings", GLFW.GLFW_KEY_UNKNOWN, LangProvider.KEYBIND_CATEGORY.key());
    //public static final KeyMapping OPEN_SKILLS = new KeyMapping("key.pmmo.openSkills", GLFW.GLFW_KEY_UNKNOWN, LangProvider.KEYBIND_CATEGORY.key());
    //public static final KeyMapping OPEN_GLOSSARY = new KeyMapping("key.pmmo.openGlossary", GLFW.GLFW_KEY_UNKNOWN, LangProvider.KEYBIND_CATEGORY.key());
    
    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
    	ClientRegistry.registerKeyBinding(SHOW_VEIN);
    	ClientRegistry.registerKeyBinding(ADD_VEIN);
    	ClientRegistry.registerKeyBinding(SUB_VEIN);
    	ClientRegistry.registerKeyBinding(CYCLE_VEIN);
    	ClientRegistry.registerKeyBinding(SHOW_LIST);
    	ClientRegistry.registerKeyBinding(VEIN_KEY);
    	ClientRegistry.registerKeyBinding(OPEN_MENU);
    	OverlayRegistry.registerOverlayTop("stats_overlay", new XPOverlayGUI());
    }
}
