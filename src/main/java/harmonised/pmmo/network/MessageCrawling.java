package harmonised.pmmo.network;

import harmonised.pmmo.gui.XPOverlayGUI;
import harmonised.pmmo.skills.XP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageCrawling
{
    boolean crawling;

    public MessageCrawling( boolean crawling )
    {
        this.crawling = crawling;
    }

    MessageCrawling()
    {
    }

    public static MessageCrawling decode( PacketBuffer buf )
    {
        MessageCrawling packet = new MessageCrawling();
        packet.crawling = buf.readBoolean();

        return packet;
    }

    public static void encode( MessageCrawling packet, PacketBuffer buf )
    {
        buf.writeBoolean( packet.crawling );
    }

    public static void handlePacket( MessageCrawling packet, Supplier<NetworkEvent.Context> ctx )
    {
        ctx.get().enqueueWork(() ->
        {
            String name = ctx.get().getSender().getName().getFormattedText();
            if( packet.crawling )
                XP.isCrawling.add( name );
            else
                XP.isCrawling.remove( name );
        });
        ctx.get().setPacketHandled(true);
    }
}
