package harmonised.pmmo.network;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.LogHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class MessageLevelUp
{
    private static final boolean broadcastMilestone = Config.forgeConfig.broadcastMilestone.get();
    private int skill, level;

    public MessageLevelUp( int skill, int level )
    {
        this.skill = skill;
        this.level = level;
    }

    public MessageLevelUp()
    {
    }

    public static MessageLevelUp decode( PacketBuffer buf )
    {
        MessageLevelUp packet = new MessageLevelUp();

        packet.skill = buf.readInt();
        packet.level = buf.readInt();

        return packet;
    }

    public static void encode( MessageLevelUp packet, PacketBuffer buf )
    {
        buf.writeInt( packet.skill );
        buf.writeInt( packet.level );
    }

    public static void handlePacket( MessageLevelUp packet, Supplier<NetworkEvent.Context> ctx )
    {
        ctx.get().enqueueWork(() ->
        {
            ServerPlayerEntity player = ctx.get().getSender();
            String skillName = Skill.getString( packet.skill );

            if( packet.level % 10 == 0 && broadcastMilestone )
            {
                LogHandler.LOGGER.info( player.getName().getString() + " " + packet.level + " " + skillName + " level up!" );

                player.server.getPlayerList().getPlayers().forEach( otherPlayer ->
                {
                    if( otherPlayer.getUniqueID() != player.getUniqueID() )
                        NetworkHandler.sendToPlayer( new MessageTripleTranslation( "pmmo.milestoneLevelUp", player.getDisplayName().getString(), "" + packet.level, "pmmo." + skillName, false, 3 ), otherPlayer );
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}