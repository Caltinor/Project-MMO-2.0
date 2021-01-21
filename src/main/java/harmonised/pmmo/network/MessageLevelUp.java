package harmonised.pmmo.network;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class MessageLevelUp
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

    public static MessageLevelUp decode( PacketBuffer buf )
    {
        MessageLevelUp packet = new MessageLevelUp();

        packet.skill = buf.readString( 64 );
        packet.level = buf.readInt();

        return packet;
    }

    public static void encode( MessageLevelUp packet, PacketBuffer buf )
    {
        buf.writeString( packet.skill );
        buf.writeInt( packet.level );
    }

    public static void handlePacket( MessageLevelUp packet, Supplier<NetworkEvent.Context> ctx )
    {
        ctx.get().enqueueWork(() ->
        {
            try
            {
                ServerPlayerEntity player = ctx.get().getSender();
                if( player == null )
                    return;
                String skill = packet.skill.toLowerCase();

                int realLevel = Skill.getLevel( skill, player );
                if( packet.level <= realLevel )
                {
                    Map<String, Double> prefsMap = Config.getPreferencesMap( player );
                    Vector3d playerPos = player.getPositionVec();

                    if( Config.forgeConfig.levelUpFirework.get() && !( prefsMap.containsKey( "spawnFireworksCausedByMe" ) && prefsMap.get( "spawnFireworksCausedByMe" ) == 0 ) )
                        XP.spawnRocket( player.world, player.getPositionVec(), skill );

                    LOGGER.info( player.getDisplayName().getString() + " has reached level " + packet.level + " in " + skill + "! [" + XP.getDimensionResLoc( player.world ).toString() + "|x:" + DP.dp( playerPos.getX() ) + "|y:" + DP.dp( playerPos.getY() ) + "|z:" + DP.dp( playerPos.getZ() ) + "]" );

                    if( Config.forgeConfig.broadcastMilestone.get() )
                    {
                        Map<String, Double> skillsMap = PmmoSavedData.get().getXpMap( player.getUniqueID() );
                        skillsMap.put( skill, XP.xpAtLevel( packet.level ) );
                        int totalLevel = XP.getTotalLevelFromMap( skillsMap );

                        boolean levelUpMilestone    =   ( packet.level % Config.forgeConfig.levelsPerMilestone.get() ) == 0;
                        boolean totalLevelMilestone =   ( totalLevel % Config.forgeConfig.levelsPerTotalLevelMilestone.get() ) == 0;

                        if( levelUpMilestone || totalLevelMilestone )
                        {
                            List<ServerPlayerEntity> players = new ArrayList<>( player.server.getPlayerList().getPlayers() );
                            for( ServerPlayerEntity otherPlayer : players )
                            {
                                if( otherPlayer.getUniqueID() != player.getUniqueID() )
                                {
                                    Map<String, Double> otherprefsMap = Config.getPreferencesMap( otherPlayer );
                                    if( levelUpMilestone )
                                    {
                                        otherPlayer.sendStatusMessage( new TranslationTextComponent( "pmmo.milestoneLevelUp", player.getDisplayName(), packet.level, new TranslationTextComponent( "pmmo." + skill ) ).setStyle( Skill.getSkillStyle( skill ) ), false );
                                        if( Config.forgeConfig.milestoneLevelUpFirework.get() && !( otherprefsMap.containsKey( "spawnFireworksCausedByOthers" ) && otherprefsMap.get( "spawnFireworksCausedByOthers" ) == 0 ) )
                                            XP.spawnRocket( otherPlayer.world, otherPlayer.getPositionVec(), skill );
                                    }
                                    if( totalLevelMilestone )
                                        otherPlayer.sendStatusMessage( new TranslationTextComponent( "pmmo.milestoneTotalLevel", player.getDisplayName(), totalLevel ).setStyle( XP.getColorStyle( 0x00ff00 ) ), false );
                                }
                            };
                        }
                    }
                }
                else
                    NetworkHandler.sendToPlayer( new MessageXp( Skill.getXp( skill, player ), skill, 0, true ), player );
            }
            catch( Exception e )
            {
                LOGGER.debug( e );
            }
        });
        ctx.get().setPacketHandled(true);
    }
}