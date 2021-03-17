package harmonised.pmmo.network;

import harmonised.pmmo.gui.WorldXpDrop;

import harmonised.pmmo.util.XP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageWorldXp
{
    private ResourceLocation worldResLoc;
    private Vector3d pos;
    private String skill;
    private double startXp;
    private float rotation;
    private float size = 1;
    private double decaySpeed = 1;
    
    public MessageWorldXp( WorldXpDrop xpDrop )
    {
        this.worldResLoc = xpDrop.getWorldResLoc();
        this.pos = xpDrop.getPos();
        this.skill = xpDrop.getSkill();
        this.startXp = xpDrop.getStartXp();
        this.decaySpeed = xpDrop.getDecaySpeed();
        this.rotation = xpDrop.getRotation();
        this.size = xpDrop.getSize();
    }

    public MessageWorldXp()
    {

    }

    public static MessageWorldXp decode( PacketBuffer buf )
    {
        MessageWorldXp packet = new MessageWorldXp();
        packet.worldResLoc = XP.getResLoc( buf.readString() );
        packet.pos = new Vector3d( buf.readDouble(), buf.readDouble(), buf.readDouble() );
        packet.skill = buf.readString();
        packet.startXp = buf.readDouble();
        packet.decaySpeed = buf.readDouble();
        packet.rotation = buf.readFloat();
        packet.size = buf.readFloat();

        return packet;
    }

    public static void encode( MessageWorldXp packet, PacketBuffer buf )
    {
        buf.writeString( packet.worldResLoc.toString() );

        buf.writeDouble( packet.pos.getX() );
        buf.writeDouble( packet.pos.getY() );
        buf.writeDouble( packet.pos.getZ() );

        buf.writeString( packet.skill );
        buf.writeDouble( packet.startXp );
        buf.writeDouble( packet.decaySpeed );
        buf.writeFloat( packet.rotation );
        buf.writeFloat( packet.size );

    }

    public static void handlePacket( MessageWorldXp packet, Supplier<NetworkEvent.Context> ctx )
    {
        ctx.get().enqueueWork(() ->
        {
            WorldXpDrop xpDrop = new WorldXpDrop( packet.worldResLoc, packet.pos, packet.startXp, packet.skill );
            xpDrop.setSize( packet.size );
            xpDrop.setDecaySpeed( packet.decaySpeed );
            xpDrop.setRotation( packet.rotation );
            XP.addWorldXpDropOffline( xpDrop );
        });
        ctx.get().setPacketHandled( true );
    }
}