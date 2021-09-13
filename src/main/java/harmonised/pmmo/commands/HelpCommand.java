package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.network.MessageTrigger;
import harmonised.pmmo.network.NetworkHandler;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class HelpCommand
{
    public static int execute( CommandContext<CommandSourceStack> context ) throws CommandRuntimeException
    {
        NetworkHandler.sendToPlayer( new MessageTrigger( 1 ), (ServerPlayer) context.getSource().getEntity() );
        return 1;
    }
}
