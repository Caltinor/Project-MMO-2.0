package harmonised.pmmo.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.sun.istack.internal.Nullable;
import harmonised.pmmo.network.MessageXp;
import harmonised.pmmo.network.NetworkHandler;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class CommandClear extends CommandBase
{

	@Override
	public String getName()
	{
		return "clear";
	}
	
	@Override
	public int getRequiredPermissionLevel()
	{
		return 0;
	}
	
	@Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
		List<String> completions = new ArrayList<String>();
		completions.add( "iagreetothetermsandconditions" );
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
		NBTTagCompound persistTag = XP.getPersistTag( player );
		
		if( args.length > 0 && args[0].equals( "iagreetothetermsandconditions" ) )
		{
			NetworkHandler.sendToPlayer( new MessageXp( 0f, "CLEAR", 0, true ), (EntityPlayerMP) player );
			persistTag.setTag( "skills", new NBTTagCompound() );
			
			player.sendStatusMessage( new TextComponentString( "Your stats have been reset!" ), false);
		}
		else
		{
			NBTTagCompound skillsTag = XP.getSkillsTag( persistTag );
			Set<String> keySet = skillsTag.getKeySet();
			
			NetworkHandler.sendToPlayer( new MessageXp( 0f, "CLEAR", 0f, true ), (EntityPlayerMP) player );
			for( String tag : keySet )
			{
				NetworkHandler.sendToPlayer( new MessageXp( skillsTag.getFloat( tag ), tag, 0, true ), (EntityPlayerMP) player );
			}
			
			player.sendStatusMessage( new TextComponentString( "Your stats have been resynced. \"iagreetothetermsandconditions\" to clear your stats!" ), false);
		}
	}
}
