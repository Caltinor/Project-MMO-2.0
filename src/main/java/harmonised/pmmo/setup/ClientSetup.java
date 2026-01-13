package harmonised.pmmo.setup;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.client.gui.IndicatorsOverlayGUI;
import harmonised.pmmo.client.gui.TutorialOverlayGUI;
import harmonised.pmmo.client.gui.XPOverlayGUI;
import harmonised.pmmo.client.gui.glossary.components.perks.AttributePanel;
import harmonised.pmmo.client.gui.glossary.components.perks.BreakSpeedPanel;
import harmonised.pmmo.client.gui.glossary.components.perks.BreathPanel;
import harmonised.pmmo.client.gui.glossary.components.perks.CommandPanel;
import harmonised.pmmo.client.gui.glossary.components.perks.DamageBoostPanel;
import harmonised.pmmo.client.gui.glossary.components.perks.DamageReducePanel;
import harmonised.pmmo.client.gui.glossary.components.perks.EffectsPanel;
import harmonised.pmmo.client.gui.glossary.components.perks.JumpBoostPanel;
import harmonised.pmmo.client.gui.glossary.components.perks.TamingPanel;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

import static harmonised.pmmo.util.Reference.rl;

@EventBusSubscriber(modid=Reference.MOD_ID, bus=EventBusSubscriber.Bus.MOD, value= Dist.CLIENT)
public class ClientSetup {

	public static final KeyMapping SHOW_VEIN = new KeyMapping(LangProvider.KEYBIND_SHOWVEIN.key(), GLFW.GLFW_KEY_TAB, LangProvider.KEYBIND_CATEGORY.key());
	public static final KeyMapping ADD_VEIN = new KeyMapping(LangProvider.KEYBIND_ADDVEIN.key(), GLFW.GLFW_KEY_LEFT_BRACKET, LangProvider.KEYBIND_CATEGORY.key());
	public static final KeyMapping SUB_VEIN = new KeyMapping(LangProvider.KEYBIND_SUBVEIN.key(), GLFW.GLFW_KEY_RIGHT_BRACKET, LangProvider.KEYBIND_CATEGORY.key());
	public static final KeyMapping CYCLE_VEIN = new KeyMapping(LangProvider.KEYBIND_VEINCYCLE.key(), GLFW.GLFW_KEY_APOSTROPHE, LangProvider.KEYBIND_CATEGORY.key());
    public static final KeyMapping SHOW_LIST = new KeyMapping(LangProvider.KEYBIND_SHOWLIST.key(), GLFW.GLFW_KEY_LEFT_ALT, LangProvider.KEYBIND_CATEGORY.key());
    public static final KeyMapping VEIN_KEY = new KeyMapping(LangProvider.KEYBIND_VEIN.key(), GLFW.GLFW_KEY_GRAVE_ACCENT, LangProvider.KEYBIND_CATEGORY.key());
    public static final KeyMapping OPEN_MENU = new KeyMapping(LangProvider.KEYBIND_OPENMENU.key(), GLFW.GLFW_KEY_P, LangProvider.KEYBIND_CATEGORY.key());
	public static final KeyMapping TOGGLE_BRKSPD = new KeyMapping(LangProvider.KEYBIND_TOGGLE_BRKSPD.key(), GLFW.GLFW_KEY_MINUS, LangProvider.KEYBIND_CATEGORY.key());

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

	@SubscribeEvent
	public static void registerPerkRenderers(FMLClientSetupEvent event) {
		APIUtils.registerPerkRenderer(rl("break_speed"), BreakSpeedPanel::new);
		APIUtils.registerPerkRenderer(rl("attribute"), AttributePanel::new);
		APIUtils.registerPerkRenderer(rl("temp_attribute"), AttributePanel::new);
		APIUtils.registerPerkRenderer(rl("jump_boost"), JumpBoostPanel::new);
		APIUtils.registerPerkRenderer(rl("jump_boost"), JumpBoostPanel::new);
		APIUtils.registerPerkRenderer(rl("breath"), BreathPanel::new);
		APIUtils.registerPerkRenderer(rl("damage_reduce"), DamageReducePanel::new);
		APIUtils.registerPerkRenderer(rl("damage_boost"), DamageBoostPanel::new);
		APIUtils.registerPerkRenderer(rl("command"), CommandPanel::new);
		APIUtils.registerPerkRenderer(rl("effect"), EffectsPanel::new);
		APIUtils.registerPerkRenderer(rl("tame_boost"), TamingPanel::new);
	}
}
