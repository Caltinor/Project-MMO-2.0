package harmonised.pmmo.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.network.MessageXp;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.XP;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

public class CommandSet
{
    public static int execute(CommandContext<CommandSource> context) throws CommandException
    {
        PlayerEntity player = (PlayerEntity) context.getSource().getEntity();
        String[] args = context.getInput().split(" ");

        System.out.println(Arrays.toString(args));


        if (Skill.getInt( args[2] ) != 0)
        {
            boolean setLevel = false;
            float newXp = -1;
            try
            {
                if ( args[3].toLowerCase().charAt( args[3].length() - 1) == 'l')
                {
                    args[3] = args[3].toLowerCase().replaceFirst("l", "");
                    setLevel = true;
                }
                newXp = Float.parseFloat( args[3].replace(',', '.') );
            }
            catch (NumberFormatException e)
            {
                player.sendStatusMessage(new StringTextComponent("\"" + args[3] + "\" is not a valid number!"), false);
                return 1;
            }

            if ( setLevel )
            {
                if (newXp >= 999)
                    newXp = 999.99f;
                XP.setXp( player, args[2], XP.xpAtLevelDecimal(newXp) );
                player.sendStatusMessage(new StringTextComponent(args[2] + " has been set to level: " + newXp), false);
            }
            else if (newXp >= 0 && newXp <= 2000000000)
            {
                XP.setXp( player, args[2], newXp );
                player.sendStatusMessage(new StringTextComponent(args[2] + " has been set to: " + args[3] + "xp"), false);
            }
            else
                player.sendStatusMessage(new StringTextComponent("New XP must be no less than 0, and no more than 2b!"), false);
        }
        else
            player.sendStatusMessage(new StringTextComponent("\"" + args[2] + "\" is not a valid skill!"), false);
    return 1;
    }
}
