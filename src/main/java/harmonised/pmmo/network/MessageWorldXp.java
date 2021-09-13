package harmonised.pmmo.network;

import harmonised.pmmo.gui.WorldXpDrop;

import harmonised.pmmo.util.XP;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageWorldXp
{
    private ResourceLocation worldResLoc;
    private Vec3 pos;
    private String skill;
    private float startXp;
    private float rotation;
    private float size = 1;
    private float decaySpeed = 1;
    
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

    public static MessageWorldXp decode( FriendlyByteBuf buf )
    {
        MessageWorldXp packet = new MessageWorldXp();

        packet.worldResLoc = XP.getResLoc( buf.readUtf() );
        packet.pos = new Vec3( buf.readDouble(), buf.readDouble(), buf.readDouble() );
        packet.skill = buf.readUtf();
        packet.startXp = buf.readFloat();
        packet.decaySpeed = buf.readFloat();
        packet.rotation = buf.readFloat();
        packet.size = buf.readFloat();

        return packet;
    }

    public static void encode( MessageWorldXp packet, FriendlyByteBuf buf )
    {
        buf.writeUtf( packet.worldResLoc.toString() );

        buf.writeDouble( packet.pos.x() );
        buf.writeDouble( packet.pos.y() );
        buf.writeDouble( packet.pos.z() );

        buf.writeUtf( packet.skill );
        buf.writeFloat( packet.startXp );
        buf.writeFloat( packet.decaySpeed );
        buf.writeFloat( packet.rotation );
        buf.writeFloat( packet.size );

    }

    public static void handlePacket( MessageWorldXp packet, Supplier<NetworkEvent.Context> ctx )
    {
        ctx.get().enqueueWork(() ->
        {
            WorldXpDrop xpDrop = WorldXpDrop.fromVector( packet.worldResLoc, packet.pos, 0, packet.startXp, packet.skill );
            xpDrop.setSize( packet.size );
            xpDrop.setDecaySpeed( packet.decaySpeed );
            xpDrop.setRotation( packet.rotation );
            XP.addWorldXpDropOffline( xpDrop );
        });
        ctx.get().setPacketHandled( true );
    }
}