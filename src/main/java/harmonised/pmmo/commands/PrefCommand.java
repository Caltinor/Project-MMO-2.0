package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.network.MessageUpdatePlayerNBT;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.util.NBTHelper;
import harmonised.pmmo.util.XP;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Map;

public class PrefCommand
{
    public static int execute(CommandContext<CommandSourceStack> context) throws CommandRuntimeException
    {
        Player player = (Player) context.getSource().getEntity();
        String[] args = context.getInput().split(" ");
        Map<String, Double> prefsMap = Config.getPreferencesMap(player);
        Double value = null;
        if(args.length > 3)
        {
            value = Double.parseDouble(args[3]);
            if(value < 0)
                value = 0D;
        }


        boolean matched = false;
        String match = "ERROR";

//        for(String element : PmmoCommand.suggestPref)
//        {
//            if(args[2].toLowerCase().equals(element.toLowerCase()))
//            {
//                match = element;
//                matched = true;
//            }
//        }

        if(matched)
        {
            if(value != null)
            {
                prefsMap.put(match, value);

                NetworkHandler.sendToPlayer(new MessageUpdatePlayerNBT(NBTHelper.mapStringToNbt(prefsMap), 0), (ServerPlayer) player);
                AttributeHandler.updateAll(player);

                player.displayClientMessage(new TranslatableComponent("pmmo.hasBeenSet", match, args[3]), false);
            }
            else if(prefsMap.containsKey(match))
                player.displayClientMessage(new TranslatableComponent("pmmo.hasTheValue", "" + match, "" + prefsMap.get(match)), false);
            else
                player.displayClientMessage(new TranslatableComponent("pmmo.hasUnsetValue", "" + match), false);
        }
        else
            player.displayClientMessage(new TranslatableComponent("pmmo.invalidChoice", args[2]).setStyle(XP.textStyle.get("red")), false);

        return 1;
    }
}
