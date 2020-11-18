package harmonised.pmmo.network;

import harmonised.pmmo.util.XP;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class MessageDoubleTranslation extends MessageBase<MessageDoubleTranslation>
{
    private static String regKey = "banana";
    private static int lastAmount;
    private static long lastTime = System.nanoTime();

    private String tKey;
    private String fKey;
    private String sKey;
    private boolean bar;
    private int color;

    public MessageDoubleTranslation( String tKey, String fKey, String sKey, boolean bar, int color )
    {
        this.tKey = tKey;
        this.fKey = fKey;
        this.sKey = sKey;
        this.bar = bar;
        this.color = color;
    }

    public MessageDoubleTranslation()
    {

    }

    @Override
    public void fromBytes( ByteBuf buf )
    {

        tKey = ByteBufUtils.readUTF8String( buf );
        fKey = ByteBufUtils.readUTF8String( buf );
        sKey = ByteBufUtils.readUTF8String( buf );
        bar = buf.readBoolean();
        color = buf.readInt();
    }

    @Override
    public void toBytes( ByteBuf buf )
    {
        ByteBufUtils.writeUTF8String( buf, this.tKey );
        ByteBufUtils.writeUTF8String( buf, this.fKey );
        ByteBufUtils.writeUTF8String( buf, this.sKey );
        buf.writeBoolean( this.bar );
        buf.writeInt( this.color );
    }

    @Override
    public void handleClientSide( MessageDoubleTranslation packet, EntityPlayer onlinePlayer )
    {
        switch( packet.color )
        {
            case 0: //white
                Minecraft.getMinecraft().player.sendStatusMessage( new TextComponentTranslation( packet.tKey, new TextComponentTranslation( packet.fKey ), new TextComponentTranslation( packet.sKey ) ), packet.bar );
                break;

            case 1: //green
                if( packet.tKey.equals( "pmmo.extraDrop" ) )
                {
                    if( !regKey.equals( packet.sKey ) ) //item type
                    {
                        regKey = packet.sKey;
                        lastAmount = Integer.parseInt( packet.fKey );
                    }

                    if( System.nanoTime() - lastTime < 3000000000L )
                    {
//                            System.out.println( lastAmount + " + " + Integer.parseInt( packet.fKey ) + " = " + (lastAmount + Integer.parseInt( packet.fKey )) );
                        lastAmount += Integer.parseInt( packet.fKey );
                    }
                    else
                        lastAmount = Integer.parseInt( packet.fKey );

                    lastTime = System.nanoTime();

                    Minecraft.getMinecraft().player.sendStatusMessage( new TextComponentTranslation( packet.tKey, new TextComponentTranslation( "" + lastAmount ), new TextComponentTranslation( packet.sKey ) ).setStyle( XP.textStyle.get( "green" ) ), packet.bar );
                }
                else
                    Minecraft.getMinecraft().player.sendStatusMessage( new TextComponentTranslation( packet.tKey, new TextComponentTranslation( "" + packet.fKey ), new TextComponentTranslation( packet.sKey ) ).setStyle( XP.textStyle.get( "green" ) ), packet.bar );
                break;

            case 2: //red
                Minecraft.getMinecraft().player.sendStatusMessage( new TextComponentTranslation( packet.tKey, new TextComponentTranslation( packet.fKey ), new TextComponentTranslation( packet.sKey ) ).setStyle( XP.textStyle.get( "red" ) ), packet.bar );
                break;

            case 3: //yellow
                Minecraft.getMinecraft().player.sendStatusMessage( new TextComponentTranslation( packet.tKey, new TextComponentTranslation( packet.fKey ), new TextComponentTranslation( packet.sKey ) ).setStyle( XP.textStyle.get( "yellow" ) ), packet.bar );
                break;
        }
    }

    @Override
    public void handleServerSide( MessageDoubleTranslation message, EntityPlayer player )
    {

    }
}