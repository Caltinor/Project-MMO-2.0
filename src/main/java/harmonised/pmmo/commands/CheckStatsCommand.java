package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.network.MessageUpdatePlayerNBT;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.util.NBTHelper;
import harmonised.pmmo.util.XP;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CheckStatsCommand
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static int execute( CommandContext<CommandSourceStack> context ) throws CommandRuntimeException
    {
        Player sender = (Player) context.getSource().getEntity();
        String[] args = context.getInput().split(" ");

        if( sender == null )
        {
            LOGGER.error( "Error: Pmmo checkstats sent by non-player" );
            return -1;
        }

        try
        {
            Player target = EntityArgument.getPlayer( context, "player name" );

            CompoundTag packetxpMap = NBTHelper.mapStringToNbt(Config.getXpMap( target ) );

            packetxpMap.putString( "UUID", target.getUUID().toString() );
            packetxpMap.putString( "name", target.getName().getString() );

            NetworkHandler.sendToPlayer( new MessageUpdatePlayerNBT( packetxpMap, 3 ), (ServerPlayer) sender );
        }
        catch( CommandSyntaxException e )
        {
            LOGGER.error( "Error: Invalid Player requested at CheckStats Command \"" + args[2] + "\"", e );

            sender.displayClientMessage(  new TranslatableComponent( "pmmo.invalidPlayer", args[2] ).setStyle( XP.textStyle.get( "red" ) ), false );
            return -1;
        }

        return 1;
    }
}
