package harmonised.pmmo.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
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

public class AddCommand
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static int execute(CommandContext<CommandSourceStack> context) throws CommandRuntimeException
    {
        String[] args = context.getInput().split(" ");
        String skill = StringArgumentType.getString(context, "Skill").toLowerCase();
        String type = StringArgumentType.getString(context, "Level|Xp").toLowerCase();
        boolean ignoreBonuses = true;
        Player sender = null;

        try
        {
            ignoreBonuses = BoolArgumentType.getBool(context, "Ignore Bonuses");
        }
        catch(IllegalArgumentException e)
        {
            //no Ignore Bonuses specified, it's fine
        }

        try
        {
            sender = context.getSource().getPlayerOrException();
        }
        catch(CommandSyntaxException e)
        {
            //not player, it's fine
        }

        try
        {
            Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "target");

            for(ServerPlayer player : players)
            {
                String playerName = player.getDisplayName().getString();
                double newValue = DoubleArgumentType.getDouble(context, "Value To Add");

                if(type.equals("level"))
                    Skill.addLevel(skill, player, newValue, "add level Command", false, ignoreBonuses);
                else if(type.equals("xp"))
                    Skill.addXp(skill, player, newValue, "add xp Command", false, ignoreBonuses);
                else
                {
                    LOGGER.error("PMMO Command Add: Invalid 6th Element in command (level|xp) " + Arrays.toString(args));

                    if(sender != null)
                        sender.displayClientMessage(new TranslatableComponent("pmmo.invalidChoice", args[5]).setStyle(XP.textStyle.get("red")), false);
                }

                LOGGER.info("PMMO Command Add: " + playerName + " " + args[4] + " has had " + args[6] + " " + args[5] + " added");
            }
        }
        catch(CommandSyntaxException e)
        {
            LOGGER.error("PMMO Command Add: Add Command Failed to get Players [" + Arrays.toString(args) + "]", e);
        }

        return 1;
    }
}
