package harmonised.pmmo.network;

import harmonised.pmmo.commands.PmmoCommand;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.events.WorldTickHandler;
import harmonised.pmmo.party.PartyPendingSystem;
import harmonised.pmmo.proxy.ClientHandler;
import harmonised.pmmo.proxy.ServerHandler;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Supplier;

public class MessageUpdatePlayerNBT
{
    public static final Logger LOGGER = LogManager.getLogger();

    public CompoundTag reqPackage = new CompoundTag();
    public int type;

    public MessageUpdatePlayerNBT( CompoundTag theNBT, int type )
    {
        this.reqPackage = theNBT;
        this.type = type;
    }

    MessageUpdatePlayerNBT()
    {
    }

    public static MessageUpdatePlayerNBT decode( FriendlyByteBuf buf )
    {
        MessageUpdatePlayerNBT packet = new MessageUpdatePlayerNBT();
        packet.reqPackage = buf.readNbt();
        packet.type = buf.readInt();

        return packet;
    }

    public static void encode( MessageUpdatePlayerNBT packet, FriendlyByteBuf buf )
    {
        buf.writeNbt( packet.reqPackage );
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
                        PmmoCommand.init();
                    }
                    else
                        LOGGER.error(  "TYPE " + packet.type + " UPDATE NBT PACKET HAS BEEN SENT TO SERVER", packet );
                    break;

                case 3: //stats
                    if( ctx.get().getDirection().getReceptionSide().equals( LogicalSide.CLIENT ) )
                    {
                        for( String uuidKey : packet.reqPackage.getAllKeys() )
                        {
                            UUID uuid = UUID.fromString( uuidKey );
                            String name = packet.reqPackage.getCompound( uuidKey ).getString( "name" );
                            packet.reqPackage.getCompound( uuidKey ).remove( "name" );

                            if( !XP.playerNames.containsKey( uuid ) )
                            {
                                XP.playerNames.put( uuid, name );
                                XP.playerUUIDs.put( name, uuid );
                            }
                        }

                        XP.setOfflineXpMaps( NBTHelper.nbtToXpMaps( packet.reqPackage ) );
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

                case 6:
                    if( ctx.get().getDirection().getReceptionSide().equals( LogicalSide.CLIENT ) )
                        ClientHandler.updateNBTTag( packet );
                    else
                        LOGGER.error( "XP BOOST PACKET SENT TO SERVER" );
                    break;

                case 7:
                    PartyPendingSystem.offlineData = packet.reqPackage;
                    break;

                default:
                    LOGGER.error( "WRONG SYNC ID AT NBT UPDATE PACKET", packet );
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}