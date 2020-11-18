package harmonised.pmmo.network;

import harmonised.pmmo.util.XP;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class MessageGrow extends MessageBase<MessageGrow>
{
    private int slot, amount;

    public MessageGrow( int slot, int amount )
    {
        this.slot = slot;
        this.amount = amount;
    }

    public MessageGrow()
    {
    }

    @Override
    public void fromBytes( ByteBuf buf )
    {
        slot = buf.readInt();
        amount = buf.readInt();
    }

    @Override
    public void toBytes( ByteBuf buf )
    {
        buf.writeInt( this.slot );
        buf.writeInt( this.amount );
    }


    @Override
    public void handleClientSide( MessageGrow packet, EntityPlayer onlinePlayer )
    {
        if( packet.slot == 0 )
            Minecraft.getMinecraft().player.getHeldItemMainhand().setCount( packet.amount );
        else if( packet.slot == 1 )
            Minecraft.getMinecraft().player.getHeldItemOffhand().setCount( packet.amount );
    }

    @Override
    public void handleServerSide( MessageGrow message, EntityPlayer player )
    {

    }
}