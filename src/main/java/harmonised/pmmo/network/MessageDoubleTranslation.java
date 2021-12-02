package harmonised.pmmo.network;

import harmonised.pmmo.util.XP;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.function.Supplier;

public class MessageDoubleTranslation
{
    private static String regKey = "banana";
    private static int lastAmount;
    private static long lastTime = System.nanoTime();

    private String tKey;
    private String fKey;
    private String sKey;
    private boolean bar;
    private int color;

    public MessageDoubleTranslation(String tKey, String fKey, String sKey, boolean bar, int color)
    {
        this.tKey = tKey;
        this.fKey = fKey;
        this.sKey = sKey;
        this.bar = bar;
        this.color = color;
    }

    public MessageDoubleTranslation(ResourceLocation tKey, ResourceLocation fKey, ResourceLocation sKey, boolean bar, int color)
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

    public static MessageDoubleTranslation decode(FriendlyByteBuf buf)
    {
        MessageDoubleTranslation packet = new MessageDoubleTranslation();
        packet.tKey = buf.readUtf();
        packet.fKey = buf.readUtf();
        packet.sKey = buf.readUtf();
        packet.bar = buf.readBoolean();
        packet.color = buf.readInt();

        return packet;
    }

    public static void encode(MessageDoubleTranslation packet, FriendlyByteBuf buf)
    {
        buf.writeUtf(packet.tKey);
        buf.writeUtf(packet.fKey);
        buf.writeUtf(packet.sKey);
        buf.writeBoolean(packet.bar);
        buf.writeInt(packet.color);
    }

    public static void handlePacket(MessageDoubleTranslation packet, Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() ->
        {
            switch(packet.color)
            {
                case 0: //white
                    Minecraft.getInstance().player.displayClientMessage(new TranslatableComponent(packet.tKey, new TranslatableComponent(packet.fKey), new TranslatableComponent(packet.sKey)), packet.bar);
                    break;

                case 1: //green
                    if(packet.tKey.equals("pmmo.extraDrop"))
                    {
                        if(!regKey.equals(packet.sKey)) //item type
                        {
                            regKey = packet.sKey;
                            lastAmount = Integer.parseInt(packet.fKey);
                        }

                        if(System.nanoTime() - lastTime < 3000000000L)
                        {
//                            System.out.println(lastAmount + " + " + Integer.parseInt(packet.fKey) + " = " + (lastAmount + Integer.parseInt(packet.fKey)));
                            lastAmount += Integer.parseInt(packet.fKey);
                        }
                        else
                            lastAmount = Integer.parseInt(packet.fKey);

                        lastTime = System.nanoTime();

                        Minecraft.getInstance().player.displayClientMessage(new TranslatableComponent(packet.tKey, new TranslatableComponent("" + lastAmount), new TranslatableComponent(packet.sKey)).setStyle(XP.textStyle.get("green")), packet.bar);
                    }
                    else
                        Minecraft.getInstance().player.displayClientMessage(new TranslatableComponent(packet.tKey, new TranslatableComponent("" + packet.fKey), new TranslatableComponent(packet.sKey)).setStyle(XP.textStyle.get("green")), packet.bar);
                    break;

                case 2: //red
                    Minecraft.getInstance().player.displayClientMessage(new TranslatableComponent(packet.tKey, new TranslatableComponent(packet.fKey), new TranslatableComponent(packet.sKey)).setStyle(XP.textStyle.get("red")), packet.bar);
                    break;

                case 3: //yellow
                    Minecraft.getInstance().player.displayClientMessage(new TranslatableComponent(packet.tKey, new TranslatableComponent(packet.fKey), new TranslatableComponent(packet.sKey)).setStyle(XP.textStyle.get("yellow")), packet.bar);
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
