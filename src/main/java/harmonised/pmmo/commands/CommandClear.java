package harmonised.pmmo.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.ServerPlayerEntity;

public class CommandClear
{
	public static void register( CommandDispatcher<CommandSource> dispatch )
	{
		String[] suggestCommand = new String[4];
		suggestCommand[0] = "Set";
		suggestCommand[1] = "Clear";
		suggestCommand[2] = "LevelAtXp";
		suggestCommand[3] = "XpAtLevel";

//		LiteralArgumentBuilder<CommandSource> argBuilder = Commands.literal("pmmo")
//				        .requires( src -> src.hasPermissionLevel(0) )
//				        .requires( src -> src.getEntity() instanceof ServerPlayerEntity )
//                        .then( Commands.argument("command", StringArgumentType.word() )
//						.suggests( (ctx, builder) -> ISuggestionProvider.suggest( suggestCommand, builder ) )
//                        .then( Commands.argument("value", BoolArgumentType.bool() ) )
//                        .executes( CommandClear::execute ) );

		LiteralArgumentBuilder<CommandSource> argBuilder = Commands.literal("pmmo")
				.requires( src -> src.hasPermissionLevel(0) )
				.requires( src -> src.getEntity() instanceof ServerPlayerEntity )
				.then( Commands.argument("command", StringArgumentType.word() )
				.suggests( (ctx, builder) -> ISuggestionProvider.suggest( suggestCommand, builder ) )
				.then( Commands.argument("value", BoolArgumentType.bool() ) )
				.executes( CommandClear::execute ) );

		dispatch.register(argBuilder);
	}

    private static int execute( CommandContext<CommandSource> context ) throws CommandException
	{

	    System.out.println( context.getInput() );
	    return 1;
//		PlayerEntity player = commandSourceCommandContext.getArgument();
//		CompoundNBT persistTag = XP.player.getPersistentData();
//
//		if( args.length > 0 && args[0].equals( "iagreetothetermsandconditions" ) )
//		{
//			NetworkHandler.sendToPlayer( new MessageXp( 0f, 42069, 0, true ), (ServerPlayerEntity) player );
//			persistTag.setTag( "skills", new CompoundNBT() );
//
//			player.sendStatusMessage( new StringTextComponent( "Your stats have been reset!" ), false);
//		}
//		else
//		{
//			CompoundNBT skillsTag = XP.getSkillsTag( persistTag );
//			Set<String> keySet = skillsTag.keySet();
//
//			NetworkHandler.sendToPlayer( new MessageXp( 0f, 42069, 0f, true ), (ServerPlayerEntity) player );
//			for( String tag : keySet )
//			{
//				NetworkHandler.sendToPlayer( new MessageXp( skillsTag.getFloat( tag ), tag, 0, true ), (ServerPlayerEntity) player );
//			}
//
//			player.sendStatusMessage( new StringTextComponent( "Your stats have been resynced. \"iagreetothetermsandconditions\" to clear your stats!" ), false);
//		}
	}

    //    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
//    {
//		List<String> completions = new ArrayList<String>();
//		completions.add( "iagreetothetermsandconditions" );
//		return completions;
//    }
}
