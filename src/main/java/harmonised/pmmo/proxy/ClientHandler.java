package harmonised.pmmo.proxy;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.gui.InfoScreen;
import harmonised.pmmo.gui.ListScreen;
import harmonised.pmmo.gui.WorldRenderHandler;
import harmonised.pmmo.gui.XPOverlayGUI;
import harmonised.pmmo.network.MessageUpdatePlayerNBT;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.util.NBTHelper;
import harmonised.pmmo.util.XP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fmlclient.registry.ClientRegistry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class ClientHandler
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static final KeyMapping SHOW_BAR = new KeyMapping( "key.pmmo.showBar", GLFW.GLFW_KEY_TAB, "category.pmmo" );
    public static final KeyMapping SHOW_LIST = new KeyMapping( "key.pmmo.showList", GLFW.GLFW_KEY_LEFT_ALT, "category.pmmo" );
    public static final KeyMapping TOGGLE_TOOLTIP = new KeyMapping( "key.pmmo.toggleTooltip", GLFW.GLFW_KEY_F6, "category.pmmo" );
    public static final KeyMapping VEIN_KEY = new KeyMapping( "key.pmmo.vein", GLFW.GLFW_KEY_GRAVE_ACCENT, "category.pmmo" );
    public static final KeyMapping OPEN_MENU = new KeyMapping( "key.pmmo.openMenu", GLFW.GLFW_KEY_P, "category.pmmo" );
    public static final KeyMapping OPEN_SETTINGS = new KeyMapping( "key.pmmo.openSettings", GLFW.GLFW_KEY_UNKNOWN, "category.pmmo" );
    public static final KeyMapping OPEN_SKILLS = new KeyMapping( "key.pmmo.openSkills", GLFW.GLFW_KEY_UNKNOWN, "category.pmmo" );
    public static final KeyMapping OPEN_GLOSSARY = new KeyMapping( "key.pmmo.openGlossary", GLFW.GLFW_KEY_UNKNOWN, "category.pmmo" );
    //Map<String, Map<playerName, Map<String, Double>>>
    public static Map<String, Map<String, Map<String, Double>>> hiscoreMap = new HashMap<>();

    public static void init()
    {
        MinecraftForge.EVENT_BUS.register( new XPOverlayGUI() );
        MinecraftForge.EVENT_BUS.register( new WorldRenderHandler() );
        ClientRegistry.registerKeyBinding( SHOW_BAR );
        ClientRegistry.registerKeyBinding( SHOW_LIST );
        ClientRegistry.registerKeyBinding( TOGGLE_TOOLTIP );
        ClientRegistry.registerKeyBinding( VEIN_KEY );
        ClientRegistry.registerKeyBinding( OPEN_MENU );
        ClientRegistry.registerKeyBinding( OPEN_SETTINGS );
        ClientRegistry.registerKeyBinding( OPEN_SKILLS );
        ClientRegistry.registerKeyBinding( OPEN_GLOSSARY );
    }

    @SuppressWarnings("resource")
	public static void updateNBTTag( MessageUpdatePlayerNBT packet )
    {
        Player player = Minecraft.getInstance().player;
        CompoundTag newPackage = packet.reqPackage;
        Set<String> keySet = newPackage.getAllKeys();

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
                if( XP.isPlayerSurvival( player ) )
                    XPOverlayGUI.updateVein();
                break;

            case 6:
                APIUtils.setPlayerXpBoostsMaps( player, NBTHelper.nbtToMapStringString( newPackage ) );
                break;

            case 7:

                break;

            default:
                LOGGER.error( "ERROR MessageUpdateNBT WRONG TYPE" );
                break;
        }
    }

    public static void openStats( UUID uuid )
    {
        Minecraft.getInstance().setScreen( new ListScreen( uuid,  new TranslatableComponent( "pmmo.skills" ), "", JType.SKILLS, Minecraft.getInstance().player ) );
    }

    public static void syncPrefsToServer()
    {
        NetworkHandler.sendToServer( new MessageUpdatePlayerNBT( NBTHelper.mapStringToNbt( Config.getPreferencesMap( Minecraft.getInstance().player ) ), 0 ) );
    }

    public static void openInfoMenu()
    {
        Minecraft.getInstance().setScreen( new InfoScreen( Minecraft.getInstance().player.getUUID(),  new TranslatableComponent( "pmmo.info" ) ) );
    }
}