package harmonised.pmmo.network;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.events.WorldTickHandler;
import harmonised.pmmo.proxy.ClientHandler;
import harmonised.pmmo.skills.XP;
import harmonised.pmmo.util.LogHandler;
import harmonised.pmmo.util.NBTHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class MessageUpdateNBT
{
    public CompoundNBT reqPackage = new CompoundNBT();
    public int type;

    public MessageUpdateNBT( CompoundNBT theNBT, int type )
    {
        this.type = type;
        reqPackage = theNBT;
    }

    MessageUpdateNBT()
    {
    }

    public static MessageUpdateNBT decode( PacketBuffer buf )
    {
        MessageUpdateNBT packet = new MessageUpdateNBT();
        packet.reqPackage = buf.readCompoundTag();
        packet.type = buf.readInt();

        return packet;
    }

    public static void encode( MessageUpdateNBT packet, PacketBuffer buf )
    {
        buf.writeCompoundTag( packet.reqPackage );
        buf.writeInt( packet.type );
    }

    public static void handlePacket( MessageUpdateNBT packet, Supplier<NetworkEvent.Context> ctx )
    {
        ctx.get().enqueueWork(() ->
        {
            switch( packet.type )
            {
                case 0: //abilities
                case 1: //prefs
                    ClientHandler.updateNBTTag( packet );
                    break;

                case 2: //config
                    Config.config = NBTHelper.nbtToMap( packet.reqPackage );
                    WorldTickHandler.refreshVein();
                    break;

                case 3: //stats
                    UUID uuid = UUID.fromString( packet.reqPackage.getString( "UUID" ) );
                    packet.reqPackage.remove( "UUID" );

                    String name = packet.reqPackage.getString( "name" );
                    packet.reqPackage.remove( "name" );

                    if( !XP.playerNames.containsKey( uuid ) )
                        XP.playerNames.put( uuid, name );

                    XP.skills.put( uuid, NBTHelper.nbtToMap( packet.reqPackage ) );

                    ClientHandler.openStats( uuid );
                    break;

                default:
                    LogHandler.LOGGER.error( "WRONG SYNC ID" );
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}