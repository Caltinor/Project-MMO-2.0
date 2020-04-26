package harmonised.pmmo.network;

import harmonised.pmmo.config.Requirements;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.skills.XP;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class MessageUpdateNBT
{
    private CompoundNBT reqPackage = new CompoundNBT();
    private String outputName;

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
            PlayerEntity player = Minecraft.getInstance().player;
            CompoundNBT newPackage = packet.reqPackage;
            Set<String> keySet = new HashSet<>( newPackage.keySet() );

            switch( packet.outputName.toLowerCase() )
            {
                case "prefs":
                    CompoundNBT prefsTag = XP.getPreferencesTag( player );
                    for( String tag : keySet )
                    {
                        prefsTag.putDouble( tag, newPackage.getDouble( tag ) );
                    }
                    AttributeHandler.updateAll( player );
                    break;

                default:
                    System.out.println( "WRONG NBT UPDATE NAME" );
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}