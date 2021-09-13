package harmonised.pmmo.network;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.gui.WorldText;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
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

    public static MessageLevelUp decode( FriendlyByteBuf buf )
    {
        MessageLevelUp packet = new MessageLevelUp();

        packet.skill = buf.readUtf( 64 );
        packet.level = buf.readInt();

        return packet;
    }

    public static void encode( MessageLevelUp packet, FriendlyByteBuf buf )
    {
        buf.writeUtf( packet.skill );
        buf.writeInt( packet.level );
    }

    public static void handlePacket( MessageLevelUp packet, Supplier<NetworkEvent.Context> ctx )
    {
        ctx.get().enqueueWork(() ->
        {
            try
            {
                ServerPlayer player = ctx.get().getSender();
                if( player == null )
                    return;
                String skill = packet.skill.toLowerCase();

                int realLevel = Skill.getLevel( skill, player );
                if( packet.level <= realLevel )
                {
                    Map<String, Double> prefsMap = Config.getPreferencesMap( player );
                    Vec3 playerPos = player.position();

                    WorldText explosionText = WorldText.fromVector( XP.getDimResLoc( player.getLevel() ), player.position() );
                    explosionText.setColor( Skill.getSkillColor( skill ) );
                    explosionText.setText( player.getDisplayName().getString() + " " + packet.level + " " + skill + " level up!" );
                    explosionText.setMaxOffset( 1 );
                    explosionText.setStartSize( 4 );
                    explosionText.setEndSize( 0 );
                    explosionText.setSecondsLifespan( 15.23f );

                    if( Config.forgeConfig.levelUpFirework.get() && !( prefsMap.containsKey( "spawnFireworksCausedByMe" ) && prefsMap.get( "spawnFireworksCausedByMe" ) == 0 ) )
                        XP.spawnRocket( player.level, player.position(), skill, explosionText );

                    LOGGER.info( player.getDisplayName().getString() + " has reached level " + packet.level + " in " + skill + "! [" + XP.getDimResLoc( player.level ).toString() + "|x:" + DP.dp( playerPos.x() ) + "|y:" + DP.dp( playerPos.y() ) + "|z:" + DP.dp( playerPos.z() ) + "]" );

                    if( Config.forgeConfig.broadcastMilestone.get() )
                    {
                        Map<String, Double> skillsMap = new HashMap<>( PmmoSavedData.get().getXpMap( player.getUUID() ) );
                        skillsMap.put( skill, XP.xpAtLevel( packet.level ) );
                        int totalLevel = XP.getTotalLevelFromMap( skillsMap );

                        boolean levelUpMilestone    =   ( packet.level % Config.forgeConfig.levelsPerMilestone.get() ) == 0;
                        boolean totalLevelMilestone =   ( totalLevel % Config.forgeConfig.levelsPerTotalLevelMilestone.get() ) == 0;

                        if( levelUpMilestone || totalLevelMilestone )
                        {
                            List<ServerPlayer> players = new ArrayList<>( player.server.getPlayerList().getPlayers() );
                            for( ServerPlayer otherPlayer : players )
                            {
                                if( otherPlayer.getUUID() != player.getUUID() )
                                {
                                    Map<String, Double> otherprefsMap = Config.getPreferencesMap( otherPlayer );
                                    if( levelUpMilestone )
                                    {
                                        otherPlayer.displayClientMessage( new TranslatableComponent( "pmmo.milestoneLevelUp", player.getDisplayName(), packet.level, new TranslatableComponent( "pmmo." + skill ) ).setStyle( Skill.getSkillStyle( skill ) ), false );
                                        if( Config.forgeConfig.milestoneLevelUpFirework.get() && !( otherprefsMap.containsKey( "spawnFireworksCausedByOthers" ) && otherprefsMap.get( "spawnFireworksCausedByOthers" ) == 0 ) )
                                            XP.spawnRocket( otherPlayer.level, otherPlayer.position(), skill, explosionText );
                                    }
                                    if( totalLevelMilestone )
                                        otherPlayer.displayClientMessage( new TranslatableComponent( "pmmo.milestoneTotalLevel", player.getDisplayName(), totalLevel ).setStyle( XP.getColorStyle( 0x00ff00 ) ), false );
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