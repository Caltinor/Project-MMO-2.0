package harmonised.pmmo.network;

import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.XP;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageLevelUp extends MessageBase<MessageLevelUp>
{
    public static final Logger LOGGER = LogManager.getLogger();
    private int level;
    private String skill;

    public MessageLevelUp( String skill, int level )
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
        this.skill = ByteBufUtils.readUTF8String( buf );
        this.level = buf.readInt();
    }

    @Override
    public void toBytes( ByteBuf buf )
    {
        ByteBufUtils.writeUTF8String( buf, skill );
        buf.writeInt( level );
    }


    @Override
    public void handleClientSide( MessageLevelUp packet, EntityPlayer onlinePlayer )
    {

    }

    @Override
    public void handleServerSide( MessageLevelUp packet, EntityPlayer player )
    {
        try
        {
            String skill = packet.skill;
            int realLevel = Skill.getLevel( skill, player );
            if( packet.level <= realLevel )
            {
                Map<String, Double> prefsMap = FConfig.getPreferencesMap( player );
                Vec3d playerPos = player.getPositionVector();

                if( FConfig.levelUpFirework && !( prefsMap.containsKey( "spawnFireworksCausedByMe" ) && prefsMap.get( "spawnFireworksCausedByMe" ) == 0 ) )
                    XP.spawnRocket( player.world, player.getPosition(), skill );

                LOGGER.info( player.getDisplayName().getUnformattedText() + " has reached level " + packet.level + " in " + skill + "! [" + player.dimension + "|x:" + DP.dp( playerPos.x ) + "|y:" + DP.dp( playerPos.y ) + "|z:" + DP.dp( playerPos.z ) + "]" );

                if( FConfig.broadcastMilestone )
                {
                    Map<String, Double> skillsMap = PmmoSavedData.get().getXpMap( player.getUniqueID() );
                    skillsMap.put( skill, XP.xpAtLevel( packet.level ) );
                    int totalLevel = XP.getTotalLevelFromMap( skillsMap );

                    boolean levelUpMilestone    =   ( packet.level % FConfig.levelsPerMilestone ) == 0;
                    boolean totalLevelMilestone =   ( totalLevel % FConfig.levelsPerTotalLevelMilestone ) == 0;

                    if( levelUpMilestone || totalLevelMilestone )
                    {
                        List<EntityPlayerMP> players = new ArrayList<>( player.getServer().getPlayerList().getPlayers() );
                        for( EntityPlayerMP otherPlayer : players )
                        {
                            if( otherPlayer.getUniqueID() != player.getUniqueID() )
                            {
                                Map<String, Double> otherprefsMap = FConfig.getPreferencesMap( otherPlayer );
                                if( levelUpMilestone )
                                {
                                    otherPlayer.sendStatusMessage( new TextComponentTranslation( "pmmo.milestoneLevelUp", player.getDisplayName(), packet.level, new TextComponentTranslation( "pmmo." + skill ) ).setStyle( Skill.getSkillStyle( skill ) ), false );
                                    if( FConfig.milestoneLevelUpFirework && !( otherprefsMap.containsKey( "spawnFireworksCausedByOthers" ) && otherprefsMap.get( "spawnFireworksCausedByOthers" ) == 0 ) )
                                        XP.spawnRocket( otherPlayer.world, otherPlayer.getPosition(), skill );
                                }
                                if( totalLevelMilestone )
                                    otherPlayer.sendStatusMessage( new TextComponentTranslation( "pmmo.milestoneTotalLevel", player.getDisplayName(), totalLevel ).setStyle( XP.textStyle.get( "green" ) ), false );
                            }
                        };
                    }
                }
            }
            else
                NetworkHandler.sendToPlayer( new MessageXp( Skill.getXp( skill, player ), skill, 0, true ), (EntityPlayerMP) player );
        }
        catch( Exception e )
        {
            LOGGER.debug( e );
        }

    }
}