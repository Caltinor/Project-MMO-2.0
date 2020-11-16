package harmonised.pmmo.network;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.function.Supplier;

public class MessageLevelUp
{
    public static final Logger LOGGER = LogManager.getLogger();
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
            EntityPlayerMP player = ctx.get().getSender();
            Skill skill = Skill.getSkill( packet.skill );

            if( packet.level <= skill.getLevel( player ) )
            {
                Map<String, Double> prefsMap = Config.getPreferencesMap( player );
                String skillName = skill.name().toLowerCase();
                Vec3d playerPos = player.getPositionVector();

                if( Config.forgeConfig.levelUpFirework.get() && !( prefsMap.containsKey( "spawnFireworksCausedByMe" ) && prefsMap.get( "spawnFireworksCausedByMe" ) == 0 ) )
                    XP.spawnRocket( player.world, player.getPosition(), skill );

                LOGGER.info( player.getDisplayName().getUnformattedText() + " has reached level " + packet.level + " in " + skillName + "! [" + player.dimension.getRegistryName().toString() + "|x:" + DP.dp( playerPos.x ) + "|y:" + DP.dp( playerPos.y ) + "|z:" + DP.dp( playerPos.z ) + "]" );

                if( packet.level % Config.forgeConfig.levelsPerMilestone.get() == 0 && Config.forgeConfig.broadcastMilestone.get() )
                {
                    player.server.getPlayerList().getPlayers().forEach( otherPlayer ->
                    {
                        if( otherPlayer.getUniqueID() != player.getUniqueID() )
                        {
                            Map<String, Double> otherprefsMap = Config.getPreferencesMap( otherPlayer );
                            otherPlayer.sendStatusMessage( new TextComponentTranslation( "pmmo.milestoneLevelUp", player.getDisplayName(), packet.level, new TextComponentTranslation( "pmmo." + skillName ) ).setStyle( XP.getSkillStyle( skill ) ), false );
                            if( Config.forgeConfig.milestoneLevelUpFirework.get() )
                            {
                                if( !( otherprefsMap.containsKey( "spawnFireworksCausedByOthers" ) && otherprefsMap.get( "spawnFireworksCausedByOthers" ) == 0 ) )
                                    XP.spawnRocket( otherPlayer.world, otherPlayer.getPosition(), skill );
                            }
                        }
                    });
                }
            }
            else
                NetworkHandler.sendToPlayer( new MessageXp( skill.getXp( player ), skill.getValue(), 0, true ), player );
        });
        ctx.get().setPacketHandled(true);
    }
}