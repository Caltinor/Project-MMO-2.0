package harmonised.pmmo.proxy;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.gui.ListScreen;
import harmonised.pmmo.gui.XPOverlayGUI;
import harmonised.pmmo.network.MessageUpdatePlayerNBT;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.NBTHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class ClientHandler
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static final KeyBinding SHOW_BAR = new KeyBinding( "key.pmmo.showBar", GLFW.GLFW_KEY_TAB, "category.pmmo" );
    public static final KeyBinding SHOW_LIST = new KeyBinding( "key.pmmo.showList", GLFW.GLFW_KEY_LEFT_ALT, "category.pmmo" );
    public static final KeyBinding TOGGLE_TOOLTIP = new KeyBinding( "key.pmmo.toggleTooltip", GLFW.GLFW_KEY_F6, "category.pmmo" );
    public static final KeyBinding VEIN_KEY = new KeyBinding( "key.pmmo.vein", GLFW.GLFW_KEY_GRAVE_ACCENT, "category.pmmo" );
    public static final KeyBinding OPEN_MENU = new KeyBinding( "key.pmmo.openMenu", GLFW.GLFW_KEY_P, "category.pmmo" );
    public static final KeyBinding OPEN_SETTINGS = new KeyBinding( "key.pmmo.openSettings", GLFW.GLFW_KEY_UNKNOWN, "category.pmmo" );
    public static final KeyBinding OPEN_SKILLS = new KeyBinding( "key.pmmo.openSkills", GLFW.GLFW_KEY_UNKNOWN, "category.pmmo" );
    public static final KeyBinding OPEN_GLOSSARY = new KeyBinding( "key.pmmo.openGlossary", GLFW.GLFW_KEY_UNKNOWN, "category.pmmo" );

    public static void init()
    {
        MinecraftForge.EVENT_BUS.register( new XPOverlayGUI() );
        ClientRegistry.registerKeyBinding( SHOW_BAR );
        ClientRegistry.registerKeyBinding( SHOW_LIST );
        ClientRegistry.registerKeyBinding( TOGGLE_TOOLTIP );
        ClientRegistry.registerKeyBinding( VEIN_KEY );
        ClientRegistry.registerKeyBinding( OPEN_MENU );
        ClientRegistry.registerKeyBinding( OPEN_SETTINGS );
        ClientRegistry.registerKeyBinding( OPEN_SKILLS );
        ClientRegistry.registerKeyBinding( OPEN_GLOSSARY );
    }

    public static void updateNBTTag( MessageUpdatePlayerNBT packet )
    {
        PlayerEntity player = Minecraft.getInstance().player;
        CompoundNBT newPackage = packet.reqPackage;
        Set<String> keySet = newPackage.keySet();

        switch( packet.type )
        {
            case 0:
                Map<String, Double> prefsMap = Config.getPreferencesMap( player );
                for( String tag : keySet )
                {
                    prefsMap.put( tag, newPackage.getDouble( tag ) );
                }
                AttributeHandler.updateAll( player );
                XPOverlayGUI.doInit();
                break;

            case 1:
                Map<String, Double> abilitiesMap = Config.getAbilitiesMap( player );
                for( String tag : keySet )
                {
                    abilitiesMap.put( tag, newPackage.getDouble( tag ) );
                }
                break;

            case 6:
                Config.setPlayerXpBoostsMaps( player, NBTHelper.nbtToMapStringSkill( newPackage ) );
                break;

            default:
                LOGGER.error( "ERROR MessageUpdateNBT WRONG TYPE" );
                break;
        }
    }

    public static void openStats( UUID uuid )
    {
        Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid,  new TranslationTextComponent( "pmmo.skills" ), "", JType.SKILLS, Minecraft.getInstance().player ) );
    }

    public static void syncPrefsToServer()
    {
        NetworkHandler.sendToServer( new MessageUpdatePlayerNBT( NBTHelper.mapStringToNbt( Config.getPreferencesMap( Minecraft.getInstance().player ) ), 0 ) );
    }
}