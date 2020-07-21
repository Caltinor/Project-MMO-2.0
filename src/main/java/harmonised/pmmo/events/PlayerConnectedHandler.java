package harmonised.pmmo.events;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerConnectedHandler
{
    public static Set<UUID> lapisPatreons = new HashSet<>();
    public static Set<UUID> dandelionPatreons = new HashSet<>();
    public static Set<UUID> ironPatreons = new HashSet<>();

    public static void handlePlayerConnected( PlayerEvent.PlayerLoggedInEvent event )
    {
        PlayerEntity player = event.getPlayer();
        if( !player.world.isRemote() )
        {
            boolean showWelcome = Config.forgeConfig.showWelcome.get();
            boolean showPatreonWelcome = Config.forgeConfig.showPatreonWelcome.get();
            migrateTags( player );
            XP.syncPlayer( player );

            if( lapisPatreons.contains( player.getUniqueID() ) )
            {
                player.getServer().getPlayerList().getPlayers().forEach( (thePlayer) ->
                {
                    thePlayer.sendStatusMessage( new TranslationTextComponent( "pmmo.lapisPatreonWelcome", thePlayer.getDisplayName().getString() ).func_240703_c_( Style.EMPTY.setFormatting( TextFormatting.BLUE ) ), false );
                });
            }
            else if( showPatreonWelcome )
            {
                if( dandelionPatreons.contains( player.getUniqueID() ) )
                    player.sendStatusMessage( new TranslationTextComponent( "pmmo.dandelionPatreonWelcome", player.getDisplayName().getString() ).func_240703_c_( XP.textStyle.get( "yellow" ) ), false );
                else if( ironPatreons.contains( player.getUniqueID() ) )
                    player.sendStatusMessage( new TranslationTextComponent( "pmmo.ironPatreonWelcome", player.getDisplayName().getString() ).func_240703_c_( XP.textStyle.get( "grey" ) ), false );
            }

            if( showWelcome )
                player.sendStatusMessage( new TranslationTextComponent( "pmmo.welcome" ), false );
        }
    }

    private static void migrateTags( PlayerEntity player )
    {

    }
}
