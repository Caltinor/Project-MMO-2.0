package harmonised.pmmo.network;

import harmonised.pmmo.gui.WorldText;

import harmonised.pmmo.util.XP;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageWorldText
{
    private ResourceLocation worldResLoc;
    private Vec3 startPos, endPos;
    private String text = "EMPTY";
    private float secondsLifespan = 1;
    private float maxOffset = 0;
    private byte preset = 0;
    private boolean hueColor = false;
    private int color = 0xffffff;
    private float startHue = 0;
    private float startSaturation = 1;
    private float startBrightness = 1;
    private float endHue = 360;
    private float endSaturation = 1;
    private float endBrightness = 1;
    private boolean showValue = false;
    private float startValue = 0;
    private float endValue = 0;
    private float valueDecaySpeed = 1;
    private boolean decayByValue = false;
    private float value = 0;
    private float startSize = 1;
    private float endSize = 0;
    private float startRot = 0;
    private float endRot = 0;


    public MessageWorldText( WorldText worldText )
    {
        this.worldResLoc = worldText.getWorldResLoc();
        this.startPos = worldText.getStartPos();
        this.endPos = worldText.getEndPos();
        this.text = worldText.getText();
        this.secondsLifespan = worldText.getSecondsLifespan();
        this.maxOffset = worldText.getMaxOffset();
        this.preset = worldText.getPreset();
        this.hueColor = worldText.isHueColor();
        this.color = worldText.getColor();
        this.startHue = worldText.getStartHue();
        this.startSaturation = worldText.getStartSaturation();
        this.startBrightness = worldText.getStartBrightness();
        this.endHue = worldText.getEndHue();
        this.endSaturation = worldText.getEndSaturation();
        this.endBrightness = worldText.getEndBrightness();
        this.showValue = worldText.isShowValue();
        this.startValue = worldText.getStartValue();
        this.endValue = worldText.getEndValue();
        this.valueDecaySpeed = worldText.getValueDecaySpeed();
        this.decayByValue = worldText.isDecayByValue();
        this.value = worldText.getValue();
        this.startSize = worldText.getStartSize();
        this.endSize = worldText.getEndSize();
        this.startRot = worldText.getStartRot();
        this.endRot = worldText.getEndRot();
    }

    public MessageWorldText()
    {

    }

    public static MessageWorldText decode( FriendlyByteBuf buf )
    {
        MessageWorldText packet = new MessageWorldText();
        packet.worldResLoc = new ResourceLocation( buf.readUtf() );
        packet.startPos = new Vec3( buf.readDouble(), buf.readDouble(), buf.readDouble() );
        packet.endPos = new Vec3( buf.readDouble(), buf.readDouble(), buf.readDouble() );
        packet.text = buf.readUtf();
        packet.secondsLifespan = buf.readFloat();
        packet.maxOffset = buf.readFloat();
        packet.preset = buf.readByte();
        packet.hueColor = buf.readBoolean();
        packet.color = buf.readInt();
        packet.startHue = buf.readFloat();
        packet.startSaturation = buf.readFloat();
        packet.startBrightness = buf.readFloat();
        packet.endHue = buf.readFloat();
        packet.endSaturation = buf.readFloat();
        packet.endBrightness = buf.readFloat();
        packet.showValue = buf.readBoolean();
        packet.startValue = buf.readFloat();
        packet.endValue = buf.readFloat();
        packet.valueDecaySpeed = buf.readFloat();
        packet.decayByValue = buf.readBoolean();
        packet.startSize = buf.readFloat();
        packet.endSize = buf.readFloat();
        packet.startRot = buf.readFloat();
        packet.endRot = buf.readFloat();

        return packet;
    }

    public static void encode( MessageWorldText packet, FriendlyByteBuf buf )
    {
        buf.writeUtf( packet.worldResLoc.toString() );

        buf.writeDouble( packet.startPos.x() );
        buf.writeDouble( packet.startPos.y() );
        buf.writeDouble( packet.startPos.z() );

        buf.writeDouble( packet.endPos.x() );
        buf.writeDouble( packet.endPos.y() );
        buf.writeDouble( packet.endPos.z() );

        buf.writeUtf     ( packet.text               );
        buf.writeFloat      ( packet.secondsLifespan    );
        buf.writeFloat      ( packet.maxOffset          );
        buf.writeByte       ( packet.preset             );
        buf.writeBoolean    ( packet.hueColor           );
        buf.writeInt        ( packet.color              );
        buf.writeFloat      ( packet.startHue           );
        buf.writeFloat      ( packet.startSaturation    );
        buf.writeFloat      ( packet.startBrightness    );
        buf.writeFloat      ( packet.endHue             );
        buf.writeFloat      ( packet.endSaturation      );
        buf.writeFloat      ( packet.endBrightness      );
        buf.writeBoolean    ( packet.showValue          );
        buf.writeFloat      ( packet.startValue         );
        buf.writeFloat      ( packet.endValue           );
        buf.writeFloat      ( packet.valueDecaySpeed    );
        buf.writeBoolean    ( packet.decayByValue       );
        buf.writeFloat      ( packet.startSize          );
        buf.writeFloat      ( packet.endSize            );
        buf.writeFloat      ( packet.startRot           );
        buf.writeFloat      ( packet.endRot             );
    }

    public static void handlePacket( MessageWorldText packet, Supplier<NetworkEvent.Context> ctx )
    {
        ctx.get().enqueueWork(() ->
        {
            WorldText worldText = WorldText.fromVector( packet.worldResLoc, packet.startPos, packet.endPos );
            worldText.setText               ( packet.text            );
            worldText.setSecondsLifespan    ( packet.secondsLifespan );
            worldText.setMaxOffset          ( packet.maxOffset       );
            worldText.setPreset             ( packet.preset          );
            worldText.setHueColor           ( packet.hueColor        );
            worldText.setColor              ( packet.color           );
            worldText.setStartHue           ( packet.startHue        );
            worldText.setStartSaturation    ( packet.startSaturation );
            worldText.setStartBrightness    ( packet.startBrightness );
            worldText.setEndHue             ( packet.endHue          );
            worldText.setEndSaturation      ( packet.endSaturation   );
            worldText.setEndBrightness      ( packet.endBrightness   );
            worldText.setShowValue          ( packet.showValue       );
            worldText.setStartValue         ( packet.startValue      );
            worldText.setEndValue           ( packet.endValue        );
            worldText.setValueDecaySpeed    ( packet.valueDecaySpeed );
            worldText.setDecayByValue       ( packet.decayByValue    );
            worldText.setStartSize          ( packet.startSize       );
            worldText.setEndSize            ( packet.endSize         );
            worldText.setStartRot           ( packet.startRot        );
            worldText.setEndRot             ( packet.endRot          );
            XP.addWorldTextOffline( worldText );
        });
        ctx.get().setPacketHandled( true );
    }
}