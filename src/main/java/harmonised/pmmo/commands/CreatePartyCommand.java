package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.util.XP;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.UUID;

public class CreatePartyCommand
{
    public static int execute( CommandContext<CommandSourceStack> context ) throws CommandRuntimeException
    {
        Player player = (Player) context.getSource().getEntity();
        UUID uuid = player.getUUID();
        PmmoSavedData pmmoSavedData = PmmoSavedData.get();
        boolean result = pmmoSavedData.makeParty( uuid );

        if( result )
            player.displayClientMessage( new TranslatableComponent( "pmmo.partyCreated" ).setStyle(XP.textStyle.get( "green" ) ), false );
        else
            player.displayClientMessage( new TranslatableComponent( "pmmo.youAreAlreadyInAParty" ).setStyle(XP.textStyle.get( "red" ) ), false );

        return 1;
    }
}
