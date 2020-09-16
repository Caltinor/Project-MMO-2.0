package harmonised.pmmo.network;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.gui.XPOverlayGUI;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class packetHandler
{
    public static void handleXpPacket( MessageXp packet, Supplier<NetworkEvent.Context> ctx )
    {
        ctx.get().enqueueWork(() ->
        {
            UUID uuid = Minecraft.getInstance().player.getUniqueID();
            String name = Minecraft.getInstance().player.getName().getString();

            if( packet.skill == 42069 )
            {
                XP.removeOfflineXpUuid( uuid );
                XPOverlayGUI.clearXP();
            }
            else
            {
                if(  !XP.playerNames.containsKey( uuid ) )
                    XP.playerNames.put( uuid, name );

                if( Config.getXpMap( Minecraft.getInstance().player ).size() == 0 )
                    XPOverlayGUI.listOn = true;

                XP.getOfflineXpMap( uuid ).put( Skill.getSkill( packet.skill ), packet.xp + packet.gainedXp );

                XPOverlayGUI.makeXpDrop( packet.xp, Skill.getSkill( packet.skill ), 10000, packet.gainedXp, packet.skip );
            }
        });
        ctx.get().setPacketHandled(true);
    }
}