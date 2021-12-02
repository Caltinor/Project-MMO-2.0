package harmonised.pmmo.network;

import harmonised.pmmo.util.XP;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.network.chat.TranslatableComponent;
import java.util.function.Supplier;

public class MessageTripleTranslation
{
    private String tKey;
    private String fKey;
    private String sKey;
    private String rdKey;
    private boolean bar;
    private int color;

    public MessageTripleTranslation(String tKey, String fKey, String sKey, String rdKey, boolean bar, int color)
    {
        this.tKey = tKey;
        this.fKey = fKey;
        this.sKey = sKey;
        this.rdKey = rdKey;
        this.bar = bar;
        this.color = color;
    }

    public MessageTripleTranslation(ResourceLocation tKey, ResourceLocation fKey, ResourceLocation sKey, ResourceLocation rdKey, boolean bar, int color)
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

    public static MessageTripleTranslation decode(FriendlyByteBuf buf)
    {
        MessageTripleTranslation packet = new MessageTripleTranslation();
        packet.tKey = buf.readUtf();
        packet.fKey = buf.readUtf();
        packet.sKey = buf.readUtf();
        packet.rdKey = buf.readUtf();
        packet.bar = buf.readBoolean();
        packet.color = buf.readInt();

        return packet;
    }

    public static void encode(MessageTripleTranslation packet, FriendlyByteBuf buf)
    {
        buf.writeUtf(packet.tKey);
        buf.writeUtf(packet.fKey);
        buf.writeUtf(packet.sKey);
        buf.writeUtf(packet.rdKey);
        buf.writeBoolean(packet.bar);
        buf.writeInt(packet.color);
    }

    public static void handlePacket(MessageTripleTranslation packet, Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() ->
        {
            switch(packet.color)
            {
                case 0: //white
                    Minecraft.getInstance().player.displayClientMessage(new TranslatableComponent(packet.tKey, new TranslatableComponent(packet.fKey), new TranslatableComponent(packet.sKey), new TranslatableComponent(packet.rdKey)), packet.bar);
                    break;

                case 1: //green
                    Minecraft.getInstance().player.displayClientMessage(new TranslatableComponent(packet.tKey, new TranslatableComponent(packet.fKey), new TranslatableComponent(packet.sKey), new TranslatableComponent(packet.rdKey)).setStyle(XP.textStyle.get("green")), packet.bar);
                    break;

                case 2: //red
                    Minecraft.getInstance().player.displayClientMessage(new TranslatableComponent(packet.tKey, new TranslatableComponent(packet.fKey), new TranslatableComponent(packet.sKey), new TranslatableComponent(packet.rdKey)).setStyle(XP.textStyle.get("red")), packet.bar);
                    break;

                case 3: //yellow
                    Minecraft.getInstance().player.displayClientMessage(new TranslatableComponent(packet.tKey, new TranslatableComponent(packet.fKey), new TranslatableComponent(packet.sKey), new TranslatableComponent(packet.rdKey)).setStyle(XP.textStyle.get("yellow")), packet.bar);
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
