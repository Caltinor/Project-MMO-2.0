package harmonised.pmmo.network;

import harmonised.pmmo.util.XP;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.UUID;
import java.util.function.Supplier;

public class MessageKeypress
{
    int key;
    boolean keyState;

    public MessageKeypress(boolean keyState, int key)
    {
        this.keyState = keyState;
        this.key = key;
    }

    MessageKeypress()
    {
    }

    public static MessageKeypress decode(FriendlyByteBuf buf)
    {
        MessageKeypress packet = new MessageKeypress();
        packet.keyState = buf.readBoolean();
        packet.key = buf.readInt();

        return packet;
    }

    public static void encode(MessageKeypress packet, FriendlyByteBuf buf)
    {
        buf.writeBoolean(packet.keyState);
        buf.writeInt(packet.key);
    }

    public static void handlePacket(MessageKeypress packet, Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() ->
        {
            UUID playerUUID = ctx.get().getSender().getUUID();

            if(packet.key == 1)
            {
                if(packet.keyState)
                    XP.isVeining.add(playerUUID);
                else
                    XP.isVeining.remove(playerUUID);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
