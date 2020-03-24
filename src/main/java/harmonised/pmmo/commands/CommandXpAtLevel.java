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

public class CommandXpAtLevel
{
    public static int execute(CommandContext<CommandSource> context) throws CommandException
	{
		System.out.println( "happened" );
        PlayerEntity player = (PlayerEntity) context.getSource().getEntity();
        String[] args = context.getInput().split(" ");

		float startLevel;
		float goalLevel;
		if( args.length > 0 )
		{
			try
			{
				startLevel = Float.parseFloat( args[0].replace(',', '.'));
			}
			catch( NumberFormatException e )
			{
				player.sendStatusMessage( new StringTextComponent( "\"" + args[0] + "\" is not a valid number!" ), false);
				return 1;
			}
			if( args.length > 1 )
			{
				try
				{

					goalLevel = Float.parseFloat( args[1].replace(',', '.'));
				}
				catch( NumberFormatException e )
				{
					player.sendStatusMessage( new StringTextComponent( "\"" + args[1] + "\" is not a valid number!" ), false);
					return 1;
				}

				if( startLevel > goalLevel )
				{
					float temp = startLevel;
					startLevel = goalLevel;
					goalLevel = temp;
				}

				if( goalLevel >= 999 ) goalLevel = 999.99f;
				if( goalLevel < 1 ) goalLevel = 1;
				if( startLevel >= 999 ) startLevel = 999.99f;
				if( startLevel < 1 ) startLevel = 1;

				player.sendStatusMessage( new StringTextComponent( "level " + startLevel + " -> " + goalLevel + " is " + DP.dp( XP.xpAtLevelDecimal( goalLevel ) - XP.xpAtLevelDecimal( startLevel ) ) + "xp" ), false);
			}
			else
				player.sendStatusMessage( new StringTextComponent( "level " + startLevel + " is " + DP.dp( XP.xpAtLevelDecimal( startLevel ) ) + "xp" ), false);
		}
		else
			player.sendStatusMessage( new StringTextComponent( "You must specify a start level, optionally also a goal level!" ), false);

		return 1;
	}
}
