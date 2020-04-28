package harmonised.pmmo.proxy;

import harmonised.pmmo.gui.XPOverlayGUI;
import harmonised.pmmo.network.MessageUpdateNBT;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.skills.XP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

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

    public static void updatePrefsTag( MessageUpdateNBT packet )
    {
        PlayerEntity player = Minecraft.getInstance().player;
        CompoundNBT newPackage = packet.reqPackage;
        Set<String> keySet = new HashSet<>( newPackage.keySet() );

        switch( packet.outputName.toLowerCase() )
        {
            case "prefs":
                CompoundNBT prefsTag = XP.getPreferencesTag( player );
                for( String tag : keySet )
                {
                    prefsTag.putDouble( tag, newPackage.getDouble( tag ) );
                }
                AttributeHandler.updateAll( player );

                XPOverlayGUI.doInit();
                break;

            default:
                System.out.println( "WRONG NBT UPDATE NAME" );
                break;
        }
    }
}
