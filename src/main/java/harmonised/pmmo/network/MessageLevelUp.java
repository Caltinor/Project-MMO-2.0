package harmonised.pmmo.network;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.LogHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.Vec3d;
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
            CompoundNBT prefsTag = XP.getPreferencesTag( player );
            Skill skill = Skill.getSkill( packet.skill );
            String skillName = skill.name().toLowerCase();
            Vec3d playerPos = player.getPositionVec();

            if( levelUpFirework && !( prefsTag.contains( "spawnFireworksCausedByMe" ) && prefsTag.getDouble( "spawnFireworksCausedByMe" ) == 0 ) )
                XP.spawnRocket( player.world, player.getPosition(), skill );

            LogHandler.LOGGER.info( player.getDisplayName().getString() + " has reached level " + packet.level + " in " + skillName + "! [" + player.dimension.getRegistryName().toString() + "|x:" + DP.dp( playerPos.getX() ) + "|y:" + DP.dp( playerPos.getY() ) + "|z:" + DP.dp( playerPos.getZ() ) + "]" );

            if( packet.level % levelsPerMilestone == 0 && broadcastMilestone )
            {
                player.server.getPlayerList().getPlayers().forEach( otherPlayer ->
                {
                    if( otherPlayer.getUniqueID() != player.getUniqueID() )
                    {
                        CompoundNBT otherPrefsTag = XP.getPreferencesTag( otherPlayer );
                        otherPlayer.sendStatusMessage( new TranslationTextComponent( "pmmo.milestoneLevelUp", player.getDisplayName(), packet.level, new TranslationTextComponent( "pmmo." + skillName ) ).setStyle( XP.skillStyle.get( skill ) ), false );
                        if( milestoneLevelUpFirework )
                        {
                            if( !( otherPrefsTag.contains( "spawnFireworksCausedByOthers" ) && otherPrefsTag.getDouble( "spawnFireworksCausedByOthers" ) == 0 ) )
                                XP.spawnRocket( otherPlayer.world, otherPlayer.getPosition(), skill );
                        }
                    }
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}