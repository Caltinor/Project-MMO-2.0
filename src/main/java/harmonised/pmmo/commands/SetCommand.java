package harmonised.pmmo.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collection;

public class SetCommand
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static int execute(CommandContext<CommandSource> context) throws CommandException
    {
        String[] args = context.getInput().split(" ");
        String skill = StringArgumentType.getString(context, "Skill").toLowerCase();
        String type = StringArgumentType.getString(context, "Level|Xp").toLowerCase();
        PlayerEntity sender = null;

        try
        {
            sender = context.getSource().asPlayer();
        }
        catch(CommandSyntaxException e)
        {
            //not player, it's fine
        }

        if(skill.equals("power"))
        {
            sender.sendStatusMessage(new TranslationTextComponent("pmmo.invalidChoice", skill), false);
            return 1;
        }

        try
        {
            Collection<ServerPlayerEntity> players = EntityArgument.getPlayers(context, "target");

            for(ServerPlayerEntity player : players)
            {
                String playerName = player.getDisplayName().getString();
                double newValue = DoubleArgumentType.getDouble(context, "New Value");

                if(type.equals("level"))
                    Skill.setLevel(skill, player, newValue);
                else if(type.equals("xp"))
                    Skill.setXp(skill, player, newValue);
                else
                {
                    LOGGER.error("PMMO Command Set: Invalid 6th Element in command (level|xp) " + Arrays.toString(args));

                    if(sender != null)
                        sender.sendStatusMessage(new TranslationTextComponent("pmmo.invalidChoice", args[5]).setStyle(XP.textStyle.get("red")), false);
                }

                LOGGER.info("PMMO Command Set: " + playerName + " " + args[4] + " has been set to " + args[5] + " " + args[6]);
            }
        }
        catch(CommandSyntaxException e)
        {
            LOGGER.error("PMMO Command Set: Failed to get Players [" + Arrays.toString(args) + "]", e);
        }

        return 1;
    }
}
