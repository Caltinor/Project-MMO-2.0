package harmonised.pmmo.network;

import harmonised.pmmo.util.XP;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

import java.util.UUID;

public class MessageKeypress extends MessageBase<MessageKeypress>
{
    private int key;
    private boolean keyState;

    public MessageKeypress( boolean keyState, int key )
    {
        this.keyState = keyState;
        this.key = key;
    }

    public MessageKeypress()
    {
    }

    @Override
    public void fromBytes( ByteBuf buf )
    {
        keyState = buf.readBoolean();
        key = buf.readInt();
    }

    @Override
    public void toBytes( ByteBuf buf )
    {
        buf.writeBoolean( this.keyState );
        buf.writeInt( this.key );
    }


    @Override
    public void handleClientSide( MessageKeypress packet, EntityPlayer player )
    {

    }

    @Override
    public void handleServerSide( MessageKeypress packet, EntityPlayer player )
    {
        UUID playerUUID = player.getUniqueID();

        if( packet.key == 1 )
        {
            if( packet.keyState )
                XP.isVeining.add( playerUUID );
            else
                XP.isVeining.remove( playerUUID );
        }
    }
}