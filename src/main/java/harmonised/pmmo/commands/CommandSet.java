package harmonised.pmmo.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class CommandSet extends CommandBase
{

	@Override
	public String getName()
	{
		return "set";
	}
	
	@Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
		EntityPlayer player;
		try {
			player = CommandBase.getCommandSenderAsPlayer( sender );
		}
			catch (PlayerNotFoundException e)
		{
				return Collections.<String>emptyList();
		}
		NBTTagCompound persistTag = XP.getPersistTag( player );
		NBTTagCompound skillsTag = XP.getSkillsTag( persistTag );
		List<String> completions = new ArrayList<String>( skillsTag.getKeySet() );
		
        return completions;
    }

	@Override
	public String getUsage(ICommandSender sender)
	{
		return null;
	}

	@Override
	public void execute( MinecraftServer server, ICommandSender sender, String[] args ) throws CommandException
	{
		EntityPlayer player = CommandBase.getCommandSenderAsPlayer( sender );
		if( args.length > 0 )
		{
			if( XP.getSkillColor( args[0] ) != 0xffffff )
			{
				if( args.length > 1 )
				{
					boolean setLevel = false;
					double newXp = -1;
					try
					{
						if( args[1].charAt( args[1].length() - 1 ) == 'l' )
						{
							args[1] = args[1].replaceFirst( "l", "" );
							setLevel = true;
						}
						
						newXp = Float.parseFloat( args[1].replace(',', '.'));
					}
					catch (NumberFormatException e)
					{
						player.sendStatusMessage( new TextComponentString( "\"" + args[1] + "\" is not a valid number!" ), false);
						return;
					}
					
					if( setLevel )
					{
						if( newXp >= 999 )
							newXp = 999.99f;
						XP.setXp( player, args[0], XP.xpAtLevelDecimal( newXp ) );
						player.sendStatusMessage( new TextComponentString( args[0] + " has been set to level: " + newXp ), false );
					}
					else if( newXp >= 0 && newXp <= 2000000000 )
					{
						XP.setXp( player, args[0], newXp );
						player.sendStatusMessage( new TextComponentString( args[0] + " has been set to: " + args[1] + "xp" ), false );
					}
					else
						player.sendStatusMessage( new TextComponentString( "New XP must be no less than 0, and no more than 2b!" ), false );
					
				}
				else
					player.sendStatusMessage( new TextComponentString( "You must specify an amount!" ), false);
			}
			else
				player.sendStatusMessage( new TextComponentString( "\"" + args[0] + "\" is not a valid skill!" ), false);
		}
		else
			player.sendStatusMessage( new TextComponentString( "You must specify a skill!" ), false);
	}
}
