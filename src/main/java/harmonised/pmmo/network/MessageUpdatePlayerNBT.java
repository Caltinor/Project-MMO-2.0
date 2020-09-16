package harmonised.pmmo.network;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.events.WorldTickHandler;
import harmonised.pmmo.gui.GlossaryScreen;
import harmonised.pmmo.proxy.ClientHandler;
import harmonised.pmmo.proxy.ServerHandler;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.LogHandler;
import harmonised.pmmo.util.NBTHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class MessageUpdatePlayerNBT
{
    public CompoundNBT reqPackage = new CompoundNBT();
    public int type;

    public MessageUpdatePlayerNBT(CompoundNBT theNBT, int type )
    {
        this.type = type;
        reqPackage = theNBT;
    }

    MessageUpdatePlayerNBT()
    {
    }

    public static MessageUpdatePlayerNBT decode(PacketBuffer buf )
    {
        MessageUpdatePlayerNBT packet = new MessageUpdatePlayerNBT();
        packet.reqPackage = buf.readCompoundTag();
        packet.type = buf.readInt();

        return packet;
    }

    public static void encode(MessageUpdatePlayerNBT packet, PacketBuffer buf )
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
                    }
                    else
                        LogHandler.LOGGER.error(  "TYPE " + packet.type + " UPDATE NBT PACKET HAS BEEN SENT TO SERVER", packet );
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
                        LogHandler.LOGGER.error(  "TYPE " + packet.type + " UPDATE NBT PACKET HAS BEEN SENT TO SERVER", packet );
                    break;

                case 4: //data
                    JsonConfig.data = NBTHelper.nbtToData3( packet.reqPackage );
                    GlossaryScreen.initButtons();
                    break;

                case 5: //data2
                    JsonConfig.data2 = NBTHelper.nbtToData4( packet.reqPackage );
                    GlossaryScreen.initButtons();
                    break;

                default:
                    LogHandler.LOGGER.error( "WRONG SYNC ID AT NBT UPDATE PACKET", packet );
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}