package harmonised.pmmo.network;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.XP;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class MessageLevelUp extends MessageBase<MessageLevelUp>
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

    @Override
    public void fromBytes( ByteBuf buf )
    {
        this.skill = buf.readInt();
        this.level = buf.readInt();
    }

    @Override
    public void toBytes( ByteBuf buf )
    {
        buf.writeInt( skill );
        buf.writeInt( level );
    }


    @Override
    public void handleClientSide( MessageLevelUp packet, EntityPlayer onlinePlayer )
    {

    }

    @Override
    public void handleServerSide( MessageLevelUp packet, EntityPlayer player )
    {
        Skill skill = Skill.getSkill( packet.skill );

        if( packet.level <= skill.getLevel( player ) )
        {
            Map<String, Double> prefsMap = Config.getPreferencesMap( player );
            String skillName = skill.name().toLowerCase();
            Vec3d playerPos = player.getPositionVector();

            if( Config.forgeConfig.levelUpFirework.get() && !( prefsMap.containsKey( "spawnFireworksCausedByMe" ) && prefsMap.get( "spawnFireworksCausedByMe" ) == 0 ) )
                XP.spawnRocket( player.world, player.getPosition(), skill );

            LOGGER.info( player.getDisplayName().getUnformattedText() + " has reached level " + packet.level + " in " + skillName + "! [" + player.dimension + "|x:" + DP.dp( playerPos.x ) + "|y:" + DP.dp( playerPos.y ) + "|z:" + DP.dp( playerPos.z ) + "]" );

            if( packet.level % Config.forgeConfig.levelsPerMilestone.get() == 0 && Config.forgeConfig.broadcastMilestone.get() )
            {
                player.getServer().getPlayerList().getPlayers().forEach( otherPlayer ->
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
            NetworkHandler.sendToPlayer( new MessageXp( skill.getXp( player ), skill.getValue(), 0, true ), (EntityPlayerMP) player );

    }
}