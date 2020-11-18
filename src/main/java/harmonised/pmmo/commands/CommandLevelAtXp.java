package harmonised.pmmo.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import harmonised.pmmo.util.DP;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class CommandLevelAtXp extends CommandBase
{

	@Override
	public String getName()
	{
		return "levelatxp";
	}
	
	@Override
	public int getRequiredPermissionLevel()
	{
		return 0;
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
		
		List<String> completions = new ArrayList<String>();
		
		for( String skill : skillsTag.getKeySet() )
		{
			completions.add( Float.toString( skillsTag.getFloat( skill ) ) );
		}
		
		return completions;
    }
	
	@Override
    public List<String> getAliases()
    {
		List<String> aliases = new ArrayList<String>();
		aliases.add( "level" );
		aliases.add( "levelat" );
        return aliases;
    }

	@Override
	public String getUsage(ICommandSender sender)
	{
		return null;
	}

	@Override
	public void execute( MinecraftServer server, ICommandSender sender, String[] args ) throws CommandException
	{
		EntityPlayer player = CommandBase.getCommandSenderAsPlayer(sender);
		
		double xp;
		if( args.length > 0 )
		{
			try
			{
				xp = Float.parseFloat( args[0].replace(',', '.'));
			}
			catch( NumberFormatException e )
			{
				player.sendStatusMessage( new TextComponentString( "\"" + args[0] + "\" is not a valid number!" ), false);
				return;
			}
			player.sendStatusMessage( new TextComponentString( DP.dp( xp ) + "xp is level " + DP.dp( XP.levelAtXpDecimal( xp ) ) ), false );
		}
		else
			player.sendStatusMessage( new TextComponentString( "You must specify a start level, optionally also a goal level!" ), false);
	}
}
