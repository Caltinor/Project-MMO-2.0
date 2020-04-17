package harmonised.pmmo.network;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageDoubleTranslation
{
    private static String regKey = "banana";
    private static int lastAmount;
    private static long lastTime = System.currentTimeMillis();

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

    public MessageDoubleTranslation( ResourceLocation tKey, ResourceLocation fKey, ResourceLocation sKey, boolean bar, int color )
    {
        this.tKey = tKey.toString();
        this.fKey = fKey.toString();
        this.sKey = sKey.toString();
        this.bar = bar;
        this.color = color;
    }

    MessageDoubleTranslation()
    {
    }

    public static MessageDoubleTranslation decode( PacketBuffer buf )
    {
        MessageDoubleTranslation packet = new MessageDoubleTranslation();
        packet.tKey = buf.readString();
        packet.fKey = buf.readString();
        packet.sKey = buf.readString();
        packet.bar = buf.readBoolean();
        packet.color = buf.readInt();

        return packet;
    }

    public static void encode( MessageDoubleTranslation packet, PacketBuffer buf )
    {
        buf.writeString( packet.tKey );
        buf.writeString( packet.fKey );
        buf.writeString( packet.sKey );
        buf.writeBoolean( packet.bar );
        buf.writeInt( packet.color );
    }

    public static void handlePacket( MessageDoubleTranslation packet, Supplier<NetworkEvent.Context> ctx )
    {
        ctx.get().enqueueWork(() ->
        {
            switch( packet.color )
            {
                case 0: //white
                    Minecraft.getInstance().player.sendStatusMessage( new TranslationTextComponent( packet.tKey, new TranslationTextComponent( packet.fKey ), new TranslationTextComponent( packet.sKey ) ), packet.bar );
                    break;

                case 1: //green
                    if( packet.tKey.equals( "pmmo.text.extraDrop" ) )
                    {
                        if( !regKey.equals( packet.sKey ) )
                        {
                            regKey = packet.sKey;
                            lastAmount = 0;
                        }

                        if( System.currentTimeMillis() - lastTime < 3000 )
                        {
//                            System.out.println( lastAmount + " + " + Integer.parseInt( packet.fKey ) + " = " + (lastAmount + Integer.parseInt( packet.fKey )) );
                            lastAmount += Integer.parseInt( packet.fKey );
                        }
                        else
                            lastAmount = 0;

                        Minecraft.getInstance().player.sendStatusMessage( new TranslationTextComponent( packet.tKey, new TranslationTextComponent( "" + ( Integer.parseInt( packet.fKey ) + lastAmount ) ), new TranslationTextComponent( packet.sKey ) ).setStyle( new Style().setColor( TextFormatting.GREEN ) ), packet.bar );
                    }
                    lastTime = System.currentTimeMillis();
                    break;

                case 2: //red
                    Minecraft.getInstance().player.sendStatusMessage( new TranslationTextComponent( packet.tKey, new TranslationTextComponent( packet.fKey ), new TranslationTextComponent( packet.sKey ) ).setStyle( new Style().setColor( TextFormatting.RED ) ), packet.bar );
                    break;

                case 3: //yellow
                    Minecraft.getInstance().player.sendStatusMessage( new TranslationTextComponent( packet.tKey, new TranslationTextComponent( packet.fKey ), new TranslationTextComponent( packet.sKey ) ).setStyle( new Style().setColor( TextFormatting.YELLOW ) ), packet.bar );
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
