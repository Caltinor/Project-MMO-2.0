package harmonised.pmmo.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import javax.annotation.Resource;
import java.util.function.Supplier;

public class MessageTripleTranslation
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

    public MessageTripleTranslation(ResourceLocation tKey, ResourceLocation fKey, ResourceLocation sKey, ResourceLocation rdKey, boolean bar, int color )
    {
        this.tKey = tKey.toString();
        this.fKey = fKey.toString();
        this.sKey = sKey.toString();
        this.rdKey = rdKey.toString();
        this.bar = bar;
        this.color = color;
    }

    MessageTripleTranslation()
    {
    }

    public static MessageTripleTranslation decode( PacketBuffer buf )
    {
        MessageTripleTranslation packet = new MessageTripleTranslation();
        packet.tKey = buf.readString();
        packet.fKey = buf.readString();
        packet.sKey = buf.readString();
        packet.rdKey = buf.readString();
        packet.bar = buf.readBoolean();
        packet.color = buf.readInt();

        return packet;
    }

    public static void encode( MessageTripleTranslation packet, PacketBuffer buf )
    {
        buf.writeString( packet.tKey );
        buf.writeString( packet.fKey );
        buf.writeString( packet.sKey );
        buf.writeString( packet.rdKey );
        buf.writeBoolean( packet.bar );
        buf.writeInt( packet.color );
    }

    public static void handlePacket( MessageTripleTranslation packet, Supplier<NetworkEvent.Context> ctx )
    {
        ctx.get().enqueueWork(() ->
        {
            switch( packet.color )
            {
                case 0: //white
                    Minecraft.getInstance().player.sendStatusMessage( new TranslationTextComponent( packet.tKey, new TranslationTextComponent( packet.fKey ), new TranslationTextComponent( packet.sKey ), new TranslationTextComponent( packet.rdKey ) ), packet.bar );
                    break;

                case 1: //green
                    Minecraft.getInstance().player.sendStatusMessage( new TranslationTextComponent( packet.tKey, new TranslationTextComponent( packet.fKey ), new TranslationTextComponent( packet.sKey ), new TranslationTextComponent( packet.rdKey ) ).setStyle( new Style().setColor( TextFormatting.GREEN ) ), packet.bar );
                    break;

                case 2: //red
                    Minecraft.getInstance().player.sendStatusMessage( new TranslationTextComponent( packet.tKey, new TranslationTextComponent( packet.fKey ), new TranslationTextComponent( packet.sKey ), new TranslationTextComponent( packet.rdKey ) ).setStyle( new Style().setColor( TextFormatting.RED ) ), packet.bar );
                    break;

                case 3: //yellow
                    Minecraft.getInstance().player.sendStatusMessage( new TranslationTextComponent( packet.tKey, new TranslationTextComponent( packet.fKey ), new TranslationTextComponent( packet.sKey ), new TranslationTextComponent( packet.rdKey ) ).setStyle( new Style().setColor( TextFormatting.YELLOW ) ), packet.bar );
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
