package harmonised.pmmo.network;

import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.events.WorldTickHandler;
import harmonised.pmmo.proxy.ClientHandler;
import harmonised.pmmo.proxy.ServerHandler;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.util.NBTHelper;
import harmonised.pmmo.util.XP;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.UUID;

public class MessageUpdatePlayerNBT extends MessageBase<MessageUpdatePlayerNBT>
{
    public static final Logger LOGGER = LogManager.getLogger();

    public NBTTagCompound reqPackage;
    public int type;

    public MessageUpdatePlayerNBT( NBTTagCompound theNBT, int type )
    {
        this.reqPackage = theNBT;
        this.type = type;
    }

    public MessageUpdatePlayerNBT()
    {
    }

    @Override
    public void fromBytes( ByteBuf buf )
    {
        reqPackage = ByteBufUtils.readTag( buf );
        type = buf.readInt();
    }

    @Override
    public void toBytes( ByteBuf buf )
    {
        ByteBufUtils.writeTag( buf, reqPackage );
        buf.writeInt( type );
    }


    @Override
    public void handleClientSide( MessageUpdatePlayerNBT packet, EntityPlayer onlinePlayer )
    {
        switch( packet.type )
        {
            case 0: //prefs
            case 1: //abilities
                ClientHandler.updateNBTTag( packet );
                break;

            case 2: //config
                FConfig.setConfigMap( NBTHelper.nbtToMapString( packet.reqPackage ) );
                WorldTickHandler.refreshVein();
                AttributeHandler.init();
                break;

            case 3: //stats
                UUID uuid = UUID.fromString( packet.reqPackage.getString( "UUID" ) );
                packet.reqPackage.removeTag( "UUID" );

                String name = packet.reqPackage.getString( "name" );
                packet.reqPackage.removeTag( "name" );

                if( !XP.playerNames.containsKey( uuid ) )
                    XP.playerNames.put( uuid, name );

                XP.setOfflineXpMap( uuid, NBTHelper.nbtToMapSkill( packet.reqPackage ) );
//                ClientHandler.openStats( uuid );
                //COUT GUI
                break;

            case 4: //data
                if( packet.reqPackage.hasKey( "wipe" ) )
                {
                    JsonConfig.data = new HashMap<>();
                    JsonConfig.initMap( JsonConfig.data );
                }
                else
                    NBTHelper.addData3( JsonConfig.data, NBTHelper.nbtToData3( packet.reqPackage ) );
                break;

            case 5: //data2
                if( packet.reqPackage.hasKey( "wipe" ) )
                {
                    JsonConfig.data2 = new HashMap<>();
                    JsonConfig.initMap2( JsonConfig.data2 );
                }
                else
                    NBTHelper.addData4( JsonConfig.data2, NBTHelper.nbtToData4( packet.reqPackage ) );
                break;

            case 6:
                ClientHandler.updateNBTTag( packet );
                break;

            default:
                LOGGER.info( "WRONG SYNC ID AT NBT UPDATE PACKET CLIENT", packet );
                break;
        }
    }

    @Override
    public void handleServerSide( MessageUpdatePlayerNBT packet, EntityPlayer player )
    {
        switch( packet.type )
        {
            case 0: //prefs
            case 1: //abilities
                ServerHandler.updateNBTTag( packet, player );
                break;

            default:
                LOGGER.info( "WRONG SYNC ID AT NBT UPDATE PACKET SERVER", packet );
                break;

        }
    }
}