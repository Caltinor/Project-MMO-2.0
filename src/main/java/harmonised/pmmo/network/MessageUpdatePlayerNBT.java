package harmonised.pmmo.network;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.events.WorldTickHandler;
import harmonised.pmmo.gui.GlossaryScreen;
import harmonised.pmmo.proxy.ClientHandler;
import harmonised.pmmo.proxy.ServerHandler;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.NBTHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Supplier;

public class MessageUpdatePlayerNBT
{
    public static final Logger LOGGER = LogManager.getLogger();

    public CompoundNBT reqPackage = new CompoundNBT();
    public int type;

    public MessageUpdatePlayerNBT( CompoundNBT theNBT, int type )
    {
        this.reqPackage = theNBT;
        this.type = type;
    }

    MessageUpdatePlayerNBT()
    {
    }

    public static MessageUpdatePlayerNBT decode( PacketBuffer buf )
    {
        MessageUpdatePlayerNBT packet = new MessageUpdatePlayerNBT();
        packet.reqPackage = buf.readCompoundTag();
        packet.type = buf.readInt();

        return packet;
    }

    public static void encode( MessageUpdatePlayerNBT packet, PacketBuffer buf )
    {
        buf.writeCompoundTag( packet.reqPackage );
        buf.writeInt( packet.type );
    }

    public static void handlePacket( MessageUpdatePlayerNBT packet, Supplier<NetworkEvent.Context> ctx )
    {
        ctx.get().enqueueWork(() ->
        {
            switch( packet.type )
            {
                case 0: //prefs
                case 1: //abilities
                    if( ctx.get().getDirection().getReceptionSide().equals( LogicalSide.CLIENT ) )
                        ClientHandler.updateNBTTag( packet );
                    else
                        ServerHandler.updateNBTTag( packet, ctx.get().getSender() );
                    break;

                case 2: //config
                    if( ctx.get().getDirection().getReceptionSide().equals( LogicalSide.CLIENT ) )
                    {
                        Config.setConfigMap( NBTHelper.nbtToMapString( packet.reqPackage ) );
                        WorldTickHandler.refreshVein();
                        AttributeHandler.init();
                    }
                    else
                        LOGGER.error(  "TYPE " + packet.type + " UPDATE NBT PACKET HAS BEEN SENT TO SERVER", packet );
                    break;

                case 3: //stats
                    if( ctx.get().getDirection().getReceptionSide().equals( LogicalSide.CLIENT ) )
                    {
                        UUID uuid = UUID.fromString( packet.reqPackage.getString( "UUID" ) );
                        packet.reqPackage.remove( "UUID" );

                        String name = packet.reqPackage.getString( "name" );
                        packet.reqPackage.remove( "name" );

                        if( !XP.playerNames.containsKey( uuid ) )
                            XP.playerNames.put( uuid, name );

                        XP.setOfflineXpMap( uuid, NBTHelper.nbtToMapSkill( packet.reqPackage ) );
                        ClientHandler.openStats( uuid );
                    }
                    else
                        LOGGER.error(  "TYPE " + packet.type + " UPDATE NBT PACKET HAS BEEN SENT TO SERVER", packet );
                    break;

                case 4: //data
                    if( packet.reqPackage.contains( "wipe" ) )
                    {
                        JsonConfig.data = new HashMap<>();
                        JsonConfig.initMap( JsonConfig.data );
                    }
                    else
                        NBTHelper.addData3( JsonConfig.data, NBTHelper.nbtToData3( packet.reqPackage ) );
                    break;

                case 5: //data2
                    if( packet.reqPackage.contains( "wipe" ) )
                    {
                        JsonConfig.data2 = new HashMap<>();
                        JsonConfig.initMap2( JsonConfig.data2 );
                    }
                    else
                        NBTHelper.addData4( JsonConfig.data2, NBTHelper.nbtToData4( packet.reqPackage ) );
                    break;

                default:
                    LOGGER.error( "WRONG SYNC ID AT NBT UPDATE PACKET", packet );
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}