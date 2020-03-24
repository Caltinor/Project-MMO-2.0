package harmonised.pmmo.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.skills.XP;
import harmonised.pmmo.util.DP;

import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

public class CommandLevelAtXp
{
    public static int execute(CommandContext<CommandSource> context) throws CommandException
	{
        PlayerEntity player = (PlayerEntity) context.getSource().getEntity();
        String[] args = context.getInput().split(" ");

		float xp;
		if( args.length > 0 )
		{
			try
			{
				xp = Float.parseFloat( args[0].replace(',', '.'));
			}
			catch( NumberFormatException e )
			{
				player.sendStatusMessage( new StringTextComponent( "\"" + args[0] + "\" is not a valid number!" ), false);
				return 1;
			}
			player.sendStatusMessage( new StringTextComponent( DP.dp( xp ) + "xp is level " + DP.dp( XP.levelAtXpDecimal( xp ) ) ), false );
		}
		else
			player.sendStatusMessage( new StringTextComponent( "You must specify a start level, optionally also a goal level!" ), false);

		return 1;
	}
}
