package harmonised.pmmo.proxy;

import harmonised.pmmo.config.JType;
import harmonised.pmmo.gui.ListScreen;
import harmonised.pmmo.gui.XPOverlayGUI;
import harmonised.pmmo.network.MessageUpdateNBT;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.LogHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

import java.util.func_235901_b_hSet;
import java.util.Set;
import java.util.UUID;

public class ClientHandler
{
    public static final KeyBinding SHOW_GUI = new KeyBinding( "key.pmmo.showGui", GLFW.GLFW_KEY_TAB, "category.pmmo" );
    public static final KeyBinding TOGGLE_TOOLTIP = new KeyBinding( "key.pmmo.toggleTooltip", GLFW.GLFW_KEY_F6, "category.pmmo" );
    public static final KeyBinding VEIN_KEY = new KeyBinding( "key.pmmo.vein", GLFW.GLFW_KEY_GRAVE_ACCENT, "category.pmmo" );
    public static final KeyBinding OPEN_MENU = new KeyBinding( "key.pmmo.openMenu", GLFW.GLFW_KEY_P, "category.pmmo" );
    public static final KeyBinding OPEN_SETTINGS = new KeyBinding( "key.pmmo.openSettings", GLFW.GLFW_KEY_UNKNOWN, "category.pmmo" );
    public static final KeyBinding OPEN_SKILLS = new KeyBinding( "key.pmmo.openSkills", GLFW.GLFW_KEY_UNKNOWN, "category.pmmo" );
    public static final KeyBinding OPEN_GLOSSARY = new KeyBinding( "key.pmmo.openGlossary", GLFW.GLFW_KEY_UNKNOWN, "category.pmmo" );

    public static void init()
    {
        MinecraftForge.EVENT_BUS.register( new XPOverlayGUI() );
        ClientRegistry.registerKeyBinding( SHOW_GUI );
        ClientRegistry.registerKeyBinding( TOGGLE_TOOLTIP );
        ClientRegistry.registerKeyBinding( VEIN_KEY );
        ClientRegistry.registerKeyBinding( OPEN_MENU );
        ClientRegistry.registerKeyBinding( OPEN_SETTINGS );
        ClientRegistry.registerKeyBinding( OPEN_SKILLS );
        ClientRegistry.registerKeyBinding( OPEN_GLOSSARY );
    }

    public static void updateNBTTag( MessageUpdateNBT packet )
    {
        PlayerEntity player = Minecraft.getInstance().player;
        CompoundNBT newPackage = packet.reqPackage;
        Set<String> keySet = new HashSet<>( newPackage.keySet() );

        switch( packet.type )
        {
            case 0:
                CompoundNBT prefsTag = XP.getPreferencesTag( player );
                for( String tag : keySet )
                {
                    prefsTag.putDouble( tag, newPackage.getDouble( tag ) );
                }
                AttributeHandler.updateAll( player );

                XPOverlayGUI.doInit();
                break;

            case 1:
                CompoundNBT abilitiesTag = XP.getAbilitiesTag( player );
                for( String tag : keySet )
                {
                    abilitiesTag.putDouble( tag, newPackage.getDouble( tag ) );
                }
                break;

            default:
                LogHandler.LOGGER.error( "ERROR MessageUpdateNBT WRONG TYPE" );
                break;
        }
    }

    public static void openStats( UUID uuid )
    {
        Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid,  new TranslationTextComponent( "pmmo.stats" ), JType.STATS, Minecraft.getInstance().player ) );
    }

    public static void syncPrefsToServer()
    {
        NetworkHandler.sendToServer( new MessageUpdateNBT( XP.getPreferencesTag(Minecraft.getInstance().player ), 0 ) );
    }
}
