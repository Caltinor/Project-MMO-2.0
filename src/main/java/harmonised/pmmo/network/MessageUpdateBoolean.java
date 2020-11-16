package harmonised.pmmo.network;

import harmonised.pmmo.gui.GlossaryScreen;
import harmonised.pmmo.gui.XPOverlayGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageUpdateBoolean
{
    boolean value;
    int type;

    public MessageUpdateBoolean( boolean value, int type )
    {
        this.value = value;
        this.type = type;
    }

    public MessageUpdateBoolean()
    {
    }

    public static MessageUpdateBoolean decode( PacketBuffer buf )
    {
        MessageUpdateBoolean packet = new MessageUpdateBoolean();

        packet.value = buf.readBoolean();
        packet.type = buf.readInt();

        return packet;
    }

    public static void encode( MessageUpdateBoolean packet, PacketBuffer buf )
    {
        buf.writeBoolean( packet.value );
        buf.writeInt( packet.type );
    }

    public static void handlePacket( MessageUpdateBoolean packet, Supplier<NetworkEvent.Context> ctx )
    {
        ctx.get().enqueueWork(() ->
        {
            switch( packet.type )
            {
                case 0: //vein stuff
                    if( ctx.get().getDirection().getReceptionSide().equals( LogicalSide.CLIENT ) )
                    {
                        if( Minecraft.getMinecraft().player != null )
                            XPOverlayGUI.isVeining = packet.value;
                    }
                    break;

                case 1: //update Glossary
                    if( ctx.get().getDirection().getReceptionSide().equals( LogicalSide.CLIENT ) )
                        GlossaryScreen.initButtons();
                    break;
            }

        });
        ctx.get().setPacketHandled(true);
    }
}