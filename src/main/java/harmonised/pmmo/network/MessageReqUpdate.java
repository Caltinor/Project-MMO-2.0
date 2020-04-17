package harmonised.pmmo.network;

import harmonised.pmmo.config.Requirements;
import harmonised.pmmo.skills.XP;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MessageReqUpdate
{
    private CompoundNBT reqPackage = new CompoundNBT();
    private String outputName;

    public MessageReqUpdate( Map<String, Map<String, Object>> theMap, String outputName )
    {
        this.outputName = outputName;
        for( String keyTop : theMap.keySet() )
        {
            reqPackage.put( keyTop, new CompoundNBT() );
            for( String keyBot : theMap.get( keyTop ).keySet() )
            {
                Object value = theMap.get( keyTop ).get( keyBot );
                if( keyBot.equals( "salvageItem" ) )
                    reqPackage.getCompound( keyTop ).putString( keyBot, (String) value );
                else
                    reqPackage.getCompound( keyTop ).putDouble( keyBot, (double) value );
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
            Map<String, Map<String, Object>> newPackage = new HashMap<>();

            if( !packet.outputName.toLowerCase().equals( "wipe" ) )
            {
                for( String topKey : packet.reqPackage.keySet() )
                {
                    newPackage.put( topKey, new HashMap<>() );
                    for( String botKey : packet.reqPackage.getCompound( topKey ).keySet() )
                    {
                        if( botKey.equals( "salvageItem" ) )
                            newPackage.get( topKey ).put( botKey, packet.reqPackage.getCompound( topKey ).getString( botKey ) );
                        else
                            newPackage.get( topKey ).put( botKey, packet.reqPackage.getCompound( topKey ).getDouble( botKey ) );
                    }
                }
            }

            switch( packet.outputName.toLowerCase() )
            {
                case "wipe":
                    Requirements.wearReq = new HashMap<>();
                    Requirements.toolReq = new HashMap<>();
                    Requirements.weaponReq = new HashMap<>();
                    Requirements.mobReq = new HashMap<>();
                    Requirements.useReq = new HashMap<>();
                    Requirements.placeReq = new HashMap<>();
                    Requirements.breakReq = new HashMap<>();
                    Requirements.xpValue = new HashMap<>();
                    Requirements.oreInfo = new HashMap<>();
                    Requirements.logInfo = new HashMap<>();
                    Requirements.plantInfo = new HashMap<>();
                    break;

                case "wearreq":
                    Requirements.wearReq = newPackage;
                    break;

                case "toolreq":
                    Requirements.toolReq = newPackage;
                    break;

                case "weaponreq":
                    Requirements.weaponReq = newPackage;
                    break;

                case "mobreq":
                    Requirements.mobReq = newPackage;
                    break;

                case "usereq":
                    Requirements.useReq = newPackage;
                    break;

                case "placereq":
                    Requirements.placeReq = newPackage;
                    break;

                case "breakreq":
                    Requirements.breakReq = newPackage;
                    break;

                case "xpvalue":
                    Requirements.xpValue = newPackage;
                    break;

                case "oreinfo":
                    Requirements.oreInfo = newPackage;
                    break;

                case "loginfo":
                    Requirements.logInfo = newPackage;
                    break;

                case "plantinfo":
                    Requirements.plantInfo = newPackage;
                    break;

                case "salvageInfo":

                    break;

                default:
                    System.out.println( "WRONG SYNC NAME" );
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}