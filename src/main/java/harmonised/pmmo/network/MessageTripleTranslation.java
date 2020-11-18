package harmonised.pmmo.network;

import harmonised.pmmo.util.XP;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class MessageTripleTranslation extends MessageBase<MessageTripleTranslation>
{
    private String tKey;
    private String fKey;
    private String sKey;
    private String rdKey;
    private boolean bar;
    private int color;

    public MessageTripleTranslation( String tKey, String fKey, String sKey, String rdKey, boolean bar, int color )
    {
        this.tKey = tKey;
        this.fKey = fKey;
        this.sKey = sKey;
        this.rdKey = rdKey;
        this.bar = bar;
        this.color = color;
    }

    MessageTripleTranslation()
    {
    }


    @Override
    public void fromBytes( ByteBuf buf )
    {
        tKey = ByteBufUtils.readUTF8String( buf );
        fKey = ByteBufUtils.readUTF8String( buf );
        sKey = ByteBufUtils.readUTF8String( buf );
        rdKey = ByteBufUtils.readUTF8String( buf );
        bar = buf.readBoolean();
        color = buf.readInt();
    }

    @Override
    public void toBytes( ByteBuf buf )
    {
        ByteBufUtils.writeUTF8String( buf, this.tKey );
        ByteBufUtils.writeUTF8String( buf, this.fKey );
        ByteBufUtils.writeUTF8String( buf, this.sKey );
        ByteBufUtils.writeUTF8String( buf, this.rdKey );
        buf.writeBoolean( this.bar );
        buf.writeInt( this.color );
    }

    @Override
    public void handleClientSide( MessageTripleTranslation packet, EntityPlayer onlinePlayer )
    {
        switch( packet.color )
        {
            case 0: //white
                Minecraft.getMinecraft().player.sendStatusMessage( new TextComponentTranslation( packet.tKey, new TextComponentTranslation( packet.fKey ), new TextComponentTranslation( packet.sKey ), new TextComponentTranslation( packet.rdKey ) ), packet.bar );
                break;

            case 1: //green
                Minecraft.getMinecraft().player.sendStatusMessage( new TextComponentTranslation( packet.tKey, new TextComponentTranslation( packet.fKey ), new TextComponentTranslation( packet.sKey ), new TextComponentTranslation( packet.rdKey ) ).setStyle( XP.textStyle.get( "green" ) ), packet.bar );
                break;

            case 2: //red
                Minecraft.getMinecraft().player.sendStatusMessage( new TextComponentTranslation( packet.tKey, new TextComponentTranslation( packet.fKey ), new TextComponentTranslation( packet.sKey ), new TextComponentTranslation( packet.rdKey ) ).setStyle( XP.textStyle.get( "red" ) ), packet.bar );
                break;

            case 3: //yellow
                Minecraft.getMinecraft().player.sendStatusMessage( new TextComponentTranslation( packet.tKey, new TextComponentTranslation( packet.fKey ), new TextComponentTranslation( packet.sKey ), new TextComponentTranslation( packet.rdKey ) ).setStyle( XP.textStyle.get( "yellow" ) ), packet.bar );
                break;
        }
    }

    @Override
    public void handleServerSide( MessageTripleTranslation message, EntityPlayer player )
    {

    }
}
