package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import harmonised.pmmo.api.perks.PerkRegistry;
import harmonised.pmmo.api.perks.PerkTrigger;
import harmonised.pmmo.network.MessageXp;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.util.XP;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.logging.log4j.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class ClearCommand
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static int execute(CommandContext<CommandSourceStack> context) throws CommandRuntimeException
    {
        String[] args = context.getInput().split(" ");

        try
        {
            Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "target");

            for(ServerPlayer player : players)
            {
                String playerName = player.getDisplayName().getString();
                PerkRegistry.executePerk(PerkTrigger.SKILL_UP, (ServerPlayer)player, 1);
                XP.updateRecipes(player);

                Map<String, Double> xpMap = PmmoSavedData.get().getXpMap(player.getUUID());
                for(String skill : new HashSet<>(xpMap.keySet()))
                {
                    xpMap.remove(skill);
                }
                NetworkHandler.sendToPlayer(new MessageXp(0f, "42069", 0, true), player);
                player.displayClientMessage(new TranslatableComponent("pmmo.skillsCleared"), false);

                LOGGER.info("PMMO Command Clear: " + playerName + " has had their stats wiped!");
            }
        }
        catch(CommandSyntaxException e)
        {
            LOGGER.error("Clear Command Failed to get Players [" + Arrays.toString(args) + "]", e);
        }

        return 1;
    }
}
