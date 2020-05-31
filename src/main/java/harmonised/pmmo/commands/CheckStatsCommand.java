package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.network.MessageUpdateNBT;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.XP;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.LogHandler;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Set;

public class CheckStatsCommand
{
    public static int execute(CommandContext<CommandSource> context) throws CommandException
    {
        PlayerEntity sender = (PlayerEntity) context.getSource().getEntity();
        String[] args = context.getInput().split(" ");

        if( sender == null )
        {
            LogHandler.LOGGER.error( "Error: Pmmo checkstats sent by non-player" );
            return -1;
        }

        try
        {
            PlayerEntity target = EntityArgument.getPlayer( context, "player name" );

            CompoundNBT skillsTag = XP.getSkillsTag( target );
            CompoundNBT packetSkillsTag = new CompoundNBT();
            Set<String> keys = skillsTag.keySet();

            keys.forEach( key ->
            {
                packetSkillsTag.putDouble( key, skillsTag.getDouble( key ) );
            });

            packetSkillsTag.putString( "UUID", target.getUniqueID().toString() );
            packetSkillsTag.putString( "name", target.getName().getString() );

            NetworkHandler.sendToPlayer( new MessageUpdateNBT( packetSkillsTag, 3 ), (ServerPlayerEntity) sender );
        }
        catch( CommandSyntaxException e )
        {
            LogHandler.LOGGER.error( "Error: Invalid Player requested at CheckStats Command \"" + args[2] + "\"", e );

            sender.sendStatusMessage(  new TranslationTextComponent( "pmmo.invalidPlayer", args[2] ).setStyle( XP.textStyle.get( "red" ) ), false );
            return -1;
        }

        return 1;
    }
}
