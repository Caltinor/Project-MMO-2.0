package harmonised.pmmo.network;

import harmonised.pmmo.gui.GlossaryScreen;
import harmonised.pmmo.gui.XPOverlayGUI;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class MessageUpdateBoolean extends MessageBase<MessageUpdateBoolean>
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

    @Override
    public void fromBytes( ByteBuf buf )
    {
        value = buf.readBoolean();
        type = buf.readInt();
    }

    @Override
    public void toBytes( ByteBuf buf )
    {
        buf.writeBoolean( value );
        buf.writeInt( type );
    }


    @Override
    public void handleClientSide( MessageUpdateBoolean packet, EntityPlayer onlinePlayer )
    {
        switch( packet.type )
        {
            case 0: //vein stuff
                if( Minecraft.getMinecraft().player != null )
                        XPOverlayGUI.isVeining = packet.value;
                break;

            case 1: //update Glossary
                GlossaryScreen.initButtons();
                break;
        }
    }

    @Override
    public void handleServerSide( MessageUpdateBoolean message, EntityPlayer player )
    {

    }
}