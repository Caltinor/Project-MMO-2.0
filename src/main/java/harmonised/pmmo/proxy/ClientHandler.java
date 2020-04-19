package harmonised.pmmo.proxy;

import harmonised.pmmo.gui.XPOverlayGUI;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public class ClientHandler
{
    public static final KeyBinding SHOW_GUI = new KeyBinding( "key.pmmo.showGui", GLFW.GLFW_KEY_TAB, "category.pmmo" );
    public static final KeyBinding TOGGLE_TOOLTIP = new KeyBinding( "key.pmmo.toggleTooltip", GLFW.GLFW_KEY_F6, "category.pmmo" );
    public static final KeyBinding CRAWL_KEY = new KeyBinding( "key.pmmo.crawl", GLFW.GLFW_KEY_C, "category.pmmo" );

    public static void init()
    {
        MinecraftForge.EVENT_BUS.register( new XPOverlayGUI() );
        ClientRegistry.registerKeyBinding( SHOW_GUI );
        ClientRegistry.registerKeyBinding( TOGGLE_TOOLTIP );
        ClientRegistry.registerKeyBinding( CRAWL_KEY );
    }
}
