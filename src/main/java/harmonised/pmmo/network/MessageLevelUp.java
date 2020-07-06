package harmonised.pmmo.network;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.LogHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageLevelUp
{
    private static final boolean broadcastMilestone = Config.forgeConfig.broadcastMilestone.get();
    private static final boolean levelUpFirework = Config.forgeConfig.levelUpFirework.get();
    private static final boolean milestoneLevelUpFirework = Config.forgeConfig.milestoneLevelUpFirework.get();
    private static final int levelsPerMilestone = Config.forgeConfig.levelsPerMilestone.get();
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
            Skill skill = Skill.getSkill( packet.skill );
            String skillName = skill.name().toLowerCase();
            Vector3d playerPos = player.getPositionVec();

            if( levelUpFirework )
                XP.spawnRocket( player.world, player.getPositionVec(), skill );

            LogHandler.LOGGER.info( player.getDisplayName().getString() + " has reached level " + packet.level + " in " + skillName + "! [" + player.dimension.getRegistryName().toString() + "|x:" + DP.dp( playerPos.getX() ) + "|y:" + DP.dp( playerPos.getY() ) + "|z:" + DP.dp( playerPos.getZ() ) + "]" );

            if( packet.level % levelsPerMilestone == 0 && broadcastMilestone )
            {
                player.server.getPlayerList().getPlayers().forEach( otherPlayer ->
                {
                    if( otherPlayer.getUniqueID() != player.getUniqueID() )
                    {
                        otherPlayer.sendStatusMessage( new TranslationTextComponent( "pmmo.milestoneLevelUp", player.getDisplayName(), packet.level, new TranslationTextComponent( "pmmo." + skillName ) ).func_240703_c_( XP.skillStyle.get( skill ) ), false );
//                        NetworkHandler.sendToPlayer( new MessageTripleTranslation( "pmmo.milestoneLevelUp", player.getDisplayName().getString(), "" + packet.level, "pmmo." + skillName, false, 3 ), otherPlayer );

                        if( milestoneLevelUpFirework )
                            XP.spawnRocket( otherPlayer.world, otherPlayer.getPositionVec(), skill );
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}