package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import harmonised.pmmo.party.Party;
import harmonised.pmmo.party.PartyPendingSystem;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.util.XP;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

public class InvitePartyCommand
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static int execute(CommandContext<CommandSource> context) throws CommandException
    {
        PlayerEntity player = (PlayerEntity) context.getSource().getEntity();
        ServerPlayerEntity targetPlayer;
        UUID uuid = player.getUniqueID();

        try
        {
            targetPlayer = EntityArgument.getPlayer(context, "target");
        }
        catch(CommandSyntaxException err)
        {
            LOGGER.error("PMMO Invite Party Command Error: Target player does not exist. How..?");
            return 1;
        }

        int result = PartyPendingSystem.createInvitation(targetPlayer, uuid);
        switch(result)
        {
            case -4:
                player.sendStatusMessage(new TranslationTextComponent("pmmo.yourPartyIsFull").setStyle(XP.textStyle.get("red")), false);
                break;

            case -3:
                player.sendStatusMessage(new TranslationTextComponent("pmmo.youAlreadyInvitedPlayerToYourParty", targetPlayer.getDisplayName()).setStyle(XP.textStyle.get("red")), false);
                break;

            case -2:
                player.sendStatusMessage(new TranslationTextComponent("pmmo.playerAlreadyInAParty").setStyle(XP.textStyle.get("red")), false);
                break;

            case -1:
                player.sendStatusMessage(new TranslationTextComponent("pmmo.youAreNotInAParty").setStyle(XP.textStyle.get("red")), false);
                break;

            case 0:
                player.sendStatusMessage(new TranslationTextComponent("pmmo.youHaveInvitedAPlayerToYourParty", targetPlayer.getDisplayName()).setStyle(XP.textStyle.get("yellow")), false);
                targetPlayer.sendStatusMessage(new TranslationTextComponent("pmmo.playerInvitedYouToAParty", player.getDisplayName()).setStyle(XP.textStyle.get("yellow")), false);
                break;
        }

        return 1;
    }
}