package harmonised.pmmo.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageGrow
{
    int slot, amount;

    public MessageGrow( int slot, int amount )
    {
        this.slot = slot;
        this.amount = amount;
    }

    public MessageGrow()
    {
    }

    public static MessageGrow decode( PacketBuffer buf )
    {
        MessageGrow packet = new MessageGrow();

        packet.slot = buf.readInt();
        packet.amount = buf.readInt();

        return packet;
    }

    public static void encode( MessageGrow packet, PacketBuffer buf )
    {
        buf.writeInt( packet.slot );
        buf.writeInt( packet.amount );
    }

    public static void handlePacket( MessageGrow packet, Supplier<NetworkEvent.Context> ctx )
    {
        ctx.get().enqueueWork(() ->
        {
            if( Minecraft.getInstance().player != null )
            {
                if( packet.slot == 0 )
                    Minecraft.getInstance().player.getHeldItemMainhand().setCount( packet.amount );
                else if( packet.slot == 1 )
                    Minecraft.getInstance().player.getHeldItemOffhand().setCount( packet.amount );
            }
        });
        ctx.get().setPacketHandled(true);
    }
}