package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;

import harmonised.pmmo.network.MessageXp;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.AttributeHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.XP;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Arrays;
import java.util.Set;

public class CommandClear
{
    public static int execute( CommandContext<CommandSource> context ) throws CommandException
	{
		PlayerEntity player = (PlayerEntity) context.getSource().getEntity();
		CompoundNBT persistTag = player.getPersistentData();
		String[] args = context.getInput().split( " " );

		System.out.println( Arrays.toString( args ) );

		if( args[2].equals( "iagreetothetermsandconditions" ) )
		{
			NetworkHandler.sendToPlayer( new MessageXp( 0f, 42069, 0, true ), (ServerPlayerEntity) player );
			persistTag.getCompound( "pmmo" ).put( "skills", new CompoundNBT() );

			player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.skillsCleared" ), false);
		}
		else
		{
			CompoundNBT skillsTag = XP.getSkillsTag( player );
			Set<String> keySet = skillsTag.keySet();

			NetworkHandler.sendToPlayer( new MessageXp( 0f, 42069, 0f, true ), (ServerPlayerEntity) player );
			for( String tag : keySet )
			{
				NetworkHandler.sendToPlayer( new MessageXp( skillsTag.getDouble( tag ), Skill.getInt( tag ), 0, true ), (ServerPlayerEntity) player );
			}

			AttributeHandler.updateDamage( player );
			AttributeHandler.updateHP( player );
			AttributeHandler.updateReach( player );
			player.sendStatusMessage( new TranslationTextComponent( "pmmo.text.skillsResynced" ), false);
		}

		return 1;
	}
}
