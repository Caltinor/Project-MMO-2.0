package harmonised.pmmo.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.logging.log4j.*;

import java.util.Arrays;
import java.util.Collection;

public class SetCommand
{
    private static final Logger LOGGER = LogManager.getLogger();

    @SuppressWarnings("deprecation")
	public static int execute(CommandContext<CommandSourceStack> context) throws CommandRuntimeException
    {
        String[] args = context.getInput().split(" ");
        String skill = StringArgumentType.getString(context, "Skill").toLowerCase();
        String type = StringArgumentType.getString(context, "Level|Xp").toLowerCase();
        Player sender = null;

        try
        {
            sender = context.getSource().getPlayerOrException();
        }
        catch(CommandSyntaxException e)
        {
            //not player, it's fine
        }

        if(skill.equals("power"))
        {
            sender.displayClientMessage(new TranslatableComponent("pmmo.invalidChoice", skill), false);
            return 1;
        }

        try
        {
            Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "target");

            for(ServerPlayer player : players)
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
                        sender.displayClientMessage(new TranslatableComponent("pmmo.invalidChoice", args[5]).setStyle(XP.textStyle.get("red")), false);
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
