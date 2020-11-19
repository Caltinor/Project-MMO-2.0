package harmonised.pmmo.proxy;

import org.lwjgl.input.Keyboard;

import harmonised.pmmo.gui.XPOverlayGUI;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy extends CommonProxy
{	
	public static final KeyBinding SHOW_GUI = new KeyBinding( "key.showgui", Keyboard.KEY_TAB, "category.pmmo" );
	
//	public void registerItemRenderer(Item item, int meta, String id)
//	{
//		ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName(), id ) );
//	}

	public void postInit( FMLPostInitializationEvent event )
	{
		super.postInit( event );
		MinecraftForge.EVENT_BUS.register( new XPOverlayGUI() );
		ClientRegistry.registerKeyBinding( SHOW_GUI );
		ClientHandler.init();
	}
}
