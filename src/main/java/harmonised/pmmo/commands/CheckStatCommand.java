package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import harmonised.pmmo.events.FishedHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.TranslatableComponent;

public class CheckStatCommand
{
    public static int execute(CommandContext<CommandSourceStack> context) throws CommandRuntimeException
    {
        Player sender = (Player) context.getSource().getEntity();
        String[] args = context.getInput().split(" ");
        String skill = args[3].toLowerCase();

        try
        {
            double level = 1;

            ServerPlayer target = EntityArgument.getPlayer(context, "player name");
            if(args[3].toLowerCase().equals("power"))
            {
                level = XP.getPowerLevel(target.getUUID());
                sender.displayClientMessage(new TranslatableComponent("pmmo.playerLevelDisplay", target.getDisplayName().getString(), (level % 1 == 0 ? (int) Math.floor(level) : DP.dp(level)), new TranslatableComponent("pmmo.power").setStyle(XP.textStyle.get("cyan"))), false);
            }
            else
            {
                level = Skill.getLevelDecimal(skill, target.getUUID());
                sender.displayClientMessage(new TranslatableComponent("pmmo.playerLevelDisplay", target.getDisplayName().getString(), (level % 1 == 0 ? (int) Math.floor(level) : DP.dp(level)), new TranslatableComponent("pmmo." + skill).setStyle(Skill.getSkillStyle(skill))), false);
            }

            //EXTRA INFO
            switch(skill)
            {
                case "fishing":
                    sender.displayClientMessage(new TranslatableComponent("pmmo.fishPoolChance", DP.dpSoft(FishedHandler.getFishPoolChance(sender)) ).setStyle(Skill.getSkillStyle(skill)), false);
                    break;
            }
        }
        catch(CommandSyntaxException e)
        {
            sender.displayClientMessage( new TranslatableComponent("pmmo.invalidPlayer", args[2]).setStyle(XP.textStyle.get("red")), false);
        }

        return 1;
    }
}
