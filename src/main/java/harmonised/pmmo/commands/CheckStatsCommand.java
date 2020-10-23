package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.network.MessageUpdatePlayerNBT;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.util.NBTHelper;
import harmonised.pmmo.util.XP;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CheckStatsCommand
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static int execute( CommandContext<CommandSource> context ) throws CommandException
    {
        PlayerEntity sender = (PlayerEntity) context.getSource().getEntity();
        String[] args = context.getInput().split(" ");

        if( sender == null )
        {
            LOGGER.error( "Error: Pmmo checkstats sent by non-player" );
            return -1;
        }

        try
        {
            PlayerEntity target = EntityArgument.getPlayer( context, "player name" );

            CompoundNBT packetxpMap = NBTHelper.mapSkillToNbt(Config.getXpMap( target ) );

            packetxpMap.putString( "UUID", target.getUniqueID().toString() );
            packetxpMap.putString( "name", target.getName().getString() );

            NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( packetxpMap, 3 ), (ServerPlayerEntity) sender );
        }
        catch( CommandSyntaxException e )
        {
            LOGGER.error( "Error: Invalid Player requested at CheckStats Command \"" + args[2] + "\"", e );

            sender.sendStatusMessage(  new TranslationTextComponent( "pmmo.invalidPlayer", args[2] ).setStyle( XP.textStyle.get( "red" ) ), false );
            return -1;
        }

        return 1;
    }
}
