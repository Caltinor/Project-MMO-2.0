package harmonised.pmmo.events;

import harmonised.pmmo.network.MessageCrawling;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.proxy.ClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ClientEventHandler
{
    public static void subscribeClientEvents( IEventBus eventBus )
    {
        eventBus.register( harmonised.pmmo.events.ClientEventHandler.class );
    }

    @SubscribeEvent
    public static void keyPress( net.minecraftforge.client.event.InputEvent.KeyInputEvent event )
    {
        if( Minecraft.getInstance().player != null )
            NetworkHandler.sendToServer( new MessageCrawling( ClientHandler.CRAWL_KEY.isKeyDown() ) );
    }
}