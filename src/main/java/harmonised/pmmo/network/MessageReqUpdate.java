package harmonised.pmmo.network;

import harmonised.pmmo.skills.XP;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public class MessageReqUpdate
{
    private CompoundNBT reqPackage = new CompoundNBT();
    private String outputName;

    public MessageReqUpdate( Map<String, Map<String, Double>> theMap, String outputName )
    {
        this.outputName = outputName;
        for( String keyTop : theMap.keySet() )
        {
            reqPackage.put( keyTop, new CompoundNBT() );
            for( String keyBot : theMap.get( keyTop ).keySet() )
            {
                reqPackage.getCompound( keyTop ).putDouble( keyBot, theMap.get( keyTop ).get( keyBot ) );
            }
        }
    }

    MessageReqUpdate()
    {
    }

    public static MessageReqUpdate decode( PacketBuffer buf )
    {
        MessageReqUpdate packet = new MessageReqUpdate();
        packet.reqPackage = buf.readCompoundTag();
        packet.outputName = buf.readString();

        return packet;
    }

    public static void encode( MessageReqUpdate packet, PacketBuffer buf )
    {
        buf.writeCompoundTag( packet.reqPackage );
        buf.writeString( packet.outputName );
    }

    public static void handlePacket( MessageReqUpdate packet, Supplier<NetworkEvent.Context> ctx )
    {
        ctx.get().enqueueWork(() ->
        {

        });
        ctx.get().setPacketHandled(true);
    }
}