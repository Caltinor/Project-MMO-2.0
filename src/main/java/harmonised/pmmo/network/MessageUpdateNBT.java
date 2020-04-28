package harmonised.pmmo.network;

import harmonised.pmmo.config.Requirements;
import harmonised.pmmo.proxy.ClientHandler;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.skills.XP;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class MessageUpdateNBT
{
    public CompoundNBT reqPackage = new CompoundNBT();
    public String outputName;

    public MessageUpdateNBT( CompoundNBT theNBT, String outputName )
    {
        this.outputName = outputName;
        reqPackage = theNBT;
    }

    MessageUpdateNBT()
    {
    }

    public static MessageUpdateNBT decode( PacketBuffer buf )
    {
        MessageUpdateNBT packet = new MessageUpdateNBT();
        packet.reqPackage = buf.readCompoundTag();
        packet.outputName = buf.readString();

        return packet;
    }

    public static void encode( MessageUpdateNBT packet, PacketBuffer buf )
    {
        buf.writeCompoundTag( packet.reqPackage );
        buf.writeString( packet.outputName );
    }

    public static void handlePacket( MessageUpdateNBT packet, Supplier<NetworkEvent.Context> ctx )
    {
        ctx.get().enqueueWork(() ->
        {
            ClientHandler.updatePrefsTag( packet );
        });
        ctx.get().setPacketHandled(true);
    }
}