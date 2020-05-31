package harmonised.pmmo.network;

import com.mojang.authlib.GameProfile;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.gui.MainScreen;
import harmonised.pmmo.gui.ScrollScreen;
import harmonised.pmmo.gui.TileButton;
import harmonised.pmmo.proxy.ClientHandler;
import harmonised.pmmo.skills.XP;
import harmonised.pmmo.util.LogHandler;
import harmonised.pmmo.util.NBTHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
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
                case "abilities":
                    ClientHandler.updateNBTTag( packet );
                    break;

                case "config":
                    Config.config = NBTHelper.nbtToMap( packet.reqPackage );
                    break;

                case "stats":
                    UUID uuid = UUID.fromString( packet.reqPackage.getString( "UUID" ) );
                    packet.reqPackage.remove( "UUID" );

                    String name = packet.reqPackage.getString( "name" );
                    packet.reqPackage.remove( "name" );

                    if( !XP.playerNames.containsKey( uuid ) )
                        XP.playerNames.put( uuid, name );

                    XP.skills.put( uuid, NBTHelper.nbtToMap( packet.reqPackage ) );

                    if( name.equals( "Dev" ) || name.equals( "MareoftheStars" ) || name.equals( "Harmonised" ) )
                        ClientHandler.openStats( uuid );
                    break;

                default:
                    LogHandler.LOGGER.error( "WRONG SYNC NAME" );
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}