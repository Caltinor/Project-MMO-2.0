package harmonised.pmmo.proxy;

import harmonised.pmmo.gui.XPOverlayGUI;
import harmonised.pmmo.network.MessageUpdateNBT;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.util.LogHandler;
import harmonised.pmmo.util.XP;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;

import java.util.HashSet;
import java.util.Set;

public class ServerHandler
{
    public static void updateNBTTag( MessageUpdateNBT packet, PlayerEntity player )
    {
        CompoundNBT newPackage = packet.reqPackage;
        Set<String> keySet = new HashSet<>( newPackage.keySet() );

        switch( packet.type )
        {
            case 0:
                CompoundNBT prefsTag = XP.getPreferencesTag( player );
                for( String tag : keySet )
                {
                    prefsTag.putDouble( tag, newPackage.getDouble( tag ) );
                }
                AttributeHandler.updateAll( player );
                break;

            default:
                LogHandler.LOGGER.error( "ERROR MessageUpdateNBT WRONG TYPE", packet );
                break;
        }
    }
}
