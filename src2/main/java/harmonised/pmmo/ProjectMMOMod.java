package harmonised.pmmo;

import harmonised.pmmo.commands.CommandLevelAtXp;
import harmonised.pmmo.commands.CommandXpAtLevel;
import harmonised.pmmo.commands.PmmoCommand;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.proxy.CommonProxy;
import harmonised.pmmo.skills.XP;
import harmonised.pmmo.util.Reference;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Reference.MOD_ID, version = Reference.VERSION)
public class ProjectMMOMod
{	
	@Instance
	public static ProjectMMOMod instance;
	
	@SidedProxy( clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.COMMON_PROXY_CLASS )
	public static CommonProxy proxy;
	
	@EventHandler
	public static void serverStart( FMLServerStartingEvent event )
	{
		event.registerServerCommand( new PmmoCommand() );
		event.registerServerCommand( new CommandXpAtLevel() );
		event.registerServerCommand( new CommandLevelAtXp() );
	}
	
	@EventHandler
	public static void preInit( FMLPreInitializationEvent event )
	{
		NetworkHandler.init();
	}
	
	@EventHandler
	public static void init( FMLInitializationEvent event )
	{
		MinecraftForge.EVENT_BUS.register( harmonised.pmmo.events.EventHandler.class );
		MinecraftForge.EVENT_BUS.register( harmonised.pmmo.skills.AttributeHandler.class );
		
		XP.initValues();
	}
	
	@EventHandler
	public static void postInit( FMLPostInitializationEvent event )
	{
		proxy.postInit( event );
	}
}
