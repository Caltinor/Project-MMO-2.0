package harmonised.pmmo.network;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.proxy.ClientHandler;
import harmonised.pmmo.skills.XP;
import harmonised.pmmo.util.NBTHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

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
            switch( packet.outputName )
            {
                case "prefs":
                    ClientHandler.updatePrefsTag( packet );
                    break;

                case "config":
                    Config.config = NBTHelper.nbtToMap( packet.reqPackage );
                    break;

                default:
                    System.out.println( "WRONG SYNC NAME" );
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}