package harmonised.pmmo.network;

import harmonised.pmmo.util.Reference;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkHandler
{
	private static SimpleNetworkWrapper INSTANCE;
	public static void init()
	{
		INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel( Reference.MOD_ID );

		INSTANCE.registerMessage( MessageXp.class, MessageXp.class, 0, Side.SERVER );
		INSTANCE.registerMessage( MessageXp.class, MessageXp.class, 0, Side.CLIENT );
		INSTANCE.registerMessage( MessageKeypress.class, MessageKeypress.class, 0, Side.CLIENT );
		INSTANCE.registerMessage( MessageDoubleTranslation.class, MessageDoubleTranslation.class, 0, Side.SERVER );
		INSTANCE.registerMessage( MessageTripleTranslation.class, MessageTripleTranslation.class, 0, Side.SERVER );
		INSTANCE.registerMessage( MessageUpdatePlayerNBT.class, MessageUpdatePlayerNBT.class, 0, Side.SERVER );
		INSTANCE.registerMessage( MessageUpdatePlayerNBT.class, MessageUpdatePlayerNBT.class, 0, Side.CLIENT );
		INSTANCE.registerMessage( MessageGrow.class, MessageGrow.class, 0, Side.SERVER );
		INSTANCE.registerMessage( MessageLevelUp.class, MessageLevelUp.class, 0, Side.CLIENT );
		INSTANCE.registerMessage( MessageUpdateBoolean.class, MessageUpdateBoolean.class, 0, Side.CLIENT );
	}

	public static void sendToServer( IMessage message )
	{
		INSTANCE.sendToServer( message );
	}

	public static void sendToPlayer( IMessage message, EntityPlayerMP player )
	{
		INSTANCE.sendTo( message, player );
	}
}