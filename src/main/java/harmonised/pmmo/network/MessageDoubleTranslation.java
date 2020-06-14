package harmonised.pmmo.network;

import harmonised.pmmo.skills.XP;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

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
                    if( packet.tKey.equals( "pmmo.extraDrop" ) )
                    {
                        if( !regKey.equals( packet.sKey ) ) //item type
                        {
                            regKey = packet.sKey;
                            lastAmount = 0;
                        }

                        if( System.nanoTime() - lastTime < 3000000000L )
                        {
//                            System.out.println( lastAmount + " + " + Integer.parseInt( packet.fKey ) + " = " + (lastAmount + Integer.parseInt( packet.fKey )) );
                            lastAmount += Integer.parseInt( packet.fKey );
                        }
                        else
                            lastAmount = 0;

                        lastTime = System.nanoTime();

                        Minecraft.getInstance().player.sendStatusMessage( new TranslationTextComponent( packet.tKey, new TranslationTextComponent( "" + ( Integer.parseInt( packet.fKey ) + lastAmount ) ), new TranslationTextComponent( packet.sKey ) ).setStyle( XP.textStyle.get( "green" ) ), packet.bar );
                    }
                    else
                        Minecraft.getInstance().player.sendStatusMessage( new TranslationTextComponent( packet.tKey, new TranslationTextComponent( "" + packet.fKey ), new TranslationTextComponent( packet.sKey ) ).setStyle( XP.textStyle.get( "green" ) ), packet.bar );
                    break;

                case 2: //red
                    Minecraft.getInstance().player.sendStatusMessage( new TranslationTextComponent( packet.tKey, new TranslationTextComponent( packet.fKey ), new TranslationTextComponent( packet.sKey ) ).setStyle( XP.textStyle.get( "red" ) ), packet.bar );
                    break;

                case 3: //yellow
                    Minecraft.getInstance().player.sendStatusMessage( new TranslationTextComponent( packet.tKey, new TranslationTextComponent( packet.fKey ), new TranslationTextComponent( packet.sKey ) ).setStyle( XP.textStyle.get( "yellow" ) ), packet.bar );
                    break;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
