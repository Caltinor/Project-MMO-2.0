package harmonised.pmmo.network;

import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.util.LogHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MessageUpdateReq
{
    private CompoundNBT reqPackage = new CompoundNBT();
    private int type;

    public MessageUpdateReq( Map<String, Map<String, Map<String, Object>>> theMap, int type )
    {
        this.type = type;

        for( String reqKey : theMap.keySet() )
        {
            reqPackage.put( reqKey, new CompoundNBT() );
            for( String topKey : theMap.get( reqKey ).keySet() )
            {
                reqPackage.getCompound( reqKey ).put( topKey, new CompoundNBT() );
                for( String botKey : theMap.get( reqKey ).get( topKey ).keySet() )
                {
                    Object value = theMap.get( reqKey ).get( topKey ).get( botKey );
                    if( botKey.equals( "salvageItem" ) )
                        reqPackage.getCompound( reqKey ).getCompound( topKey ).putString( botKey, (String) value );
                    else
                        reqPackage.getCompound( reqKey ).getCompound( topKey ).putDouble( botKey, (double) value );
                }
            }
        }
    }

    MessageUpdateReq()
    {
    }

    public static MessageUpdateReq decode(PacketBuffer buf )
    {
        MessageUpdateReq packet = new MessageUpdateReq();
        packet.reqPackage = buf.readCompoundTag();
        packet.type = buf.readInt();

        return packet;
    }

    public static void encode(MessageUpdateReq packet, PacketBuffer buf )
    {
        buf.writeCompoundTag( packet.reqPackage );
        buf.writeInt( packet.type );
    }

    public static void handlePacket(MessageUpdateReq packet, Supplier<NetworkEvent.Context> ctx )
    {
        ctx.get().enqueueWork(() ->
        {
            Map<String, Map<String, Map<String, Object>>> newPackage = new HashMap<>();

            for( String reqKey : packet.reqPackage.keySet() )
            {
                newPackage.put( reqKey, new HashMap<>() );
                for( String topKey : packet.reqPackage.getCompound( reqKey ).keySet() )
                {
                    newPackage.get( reqKey ).put( topKey, new HashMap<>() );
                    for( String botKey : packet.reqPackage.getCompound( reqKey ).getCompound( topKey ).keySet() )
                    {
                        if( botKey.equals( "salvageItem" ) )
                            newPackage.get( reqKey ).get( topKey ).put( botKey, packet.reqPackage.getCompound( reqKey ).getCompound( topKey ).getString( botKey ) );
                        else
                            newPackage.get( reqKey ).get( topKey ).put( botKey, packet.reqPackage.getCompound( reqKey ).getCompound( topKey ).getDouble( botKey ) );
                    }
                }
            }

            switch( packet.type )
            {
                case 0: //json
                    JsonConfig.data = newPackage;
                    break;

                default:
                    LogHandler.LOGGER.error( "ERROR MessageUpdateReq WRONG TYPE" );
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}