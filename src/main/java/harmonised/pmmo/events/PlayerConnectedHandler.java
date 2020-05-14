package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.skills.XP;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerConnectedHandler
{
    private static boolean showWelcome = Config.config.showWelcome.get();
    private static boolean showDonatorWelcome = Config.config.showDonatorWelcome.get();
    public static Set<UUID> lapisDonators = new HashSet<>();
    public static Set<UUID> dandelionDonators = new HashSet<>();
    public static Set<UUID> ironDonators = new HashSet<>();

    public static void handlePlayerConnected( PlayerEvent.PlayerLoggedInEvent event )
    {
        PlayerEntity player = event.getPlayer();
        if( !player.world.isRemote() )
        {
            XP.syncPlayer( player );

            if( lapisDonators.contains( player.getUniqueID() ) )
            {
                player.getServer().getPlayerList().getPlayers().forEach( (thePlayer) ->
                {
                    thePlayer.sendStatusMessage( new TranslationTextComponent( "pmmo.lapisDonatorWelcome", thePlayer.getDisplayName().getString() ).setStyle( new Style().setColor( TextFormatting.BLUE ) ), false );
                });
            }
            else if( showDonatorWelcome )
            {
                if( dandelionDonators.contains( player.getUniqueID() ) )
                    player.sendStatusMessage( new TranslationTextComponent( "pmmo.dandelionDonatorWelcome", player.getDisplayName().getString() ).setStyle( XP.textStyle.get( "yellow" ) ), false );
                else if( ironDonators.contains( player.getUniqueID() ) )
                    player.sendStatusMessage( new TranslationTextComponent( "pmmo.ironDonatorWelcome", player.getDisplayName().getString() ).setStyle( XP.textStyle.get( "grey" ) ), false );
            }

            if( showWelcome )
                player.sendStatusMessage( new TranslationTextComponent( "pmmo.welcome" ), false );
        }
    }
}
