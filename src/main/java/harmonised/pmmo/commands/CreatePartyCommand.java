package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.party.Party;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.util.XP;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.UUID;

public class CreatePartyCommand
{
    public static int execute( CommandContext<CommandSource> context ) throws CommandException
    {
        EntityPlayer player = (EntityPlayer) context.getSource().getEntity();
        UUID uuid = player.getUniqueID();
        PmmoSavedData pmmoSavedData = PmmoSavedData.get();
        boolean result = pmmoSavedData.makeParty( uuid );

        if( result )
            player.sendStatusMessage( new TextComponentTranslation( "pmmo.partyCreated" ).setStyle(XP.textStyle.get( "green" ) ), false );
        else
            player.sendStatusMessage( new TextComponentTranslation( "pmmo.youAreAlreadyInAParty" ).setStyle(XP.textStyle.get( "red" ) ), false );

        return 1;
    }
}
