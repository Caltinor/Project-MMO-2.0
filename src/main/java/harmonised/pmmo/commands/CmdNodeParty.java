package harmonised.pmmo.commands;

import java.util.List;
import java.util.UUID;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import harmonised.pmmo.features.party.PartyUtils;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.MsLoggy;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

public class CmdNodeParty {
	//CREATE, LEAVE, INVITE, ACCEPT, DECLINE
	private static final String REQUEST_ID = "requestID";
	
	public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher) {
		return Commands.literal("party")
				.then(Commands.literal("create")
						.then(Commands.argument("name", StringArgumentType.word())
						.executes(CmdNodeParty::partyCreate)))
				.then(Commands.literal("leave")
						.executes(CmdNodeParty::partyLeave))
				.then(Commands.literal("invite")
						.then(Commands.argument("player", EntityArgument.player())
								.executes(CmdNodeParty::partyInvite)))
				.then(Commands.literal("uninvite")
						.then(Commands.argument("player", EntityArgument.player())
								.executes(CmdNodeParty::partyUninvite)))
				.then(Commands.literal("list")
						.executes(CmdNodeParty::listParty))
				.then(Commands.literal("accept")
						.then(Commands.argument(REQUEST_ID, UuidArgument.uuid())
								.executes(CmdNodeParty::partyAccept)))
				.then(Commands.literal("decline")
						.then(Commands.argument(REQUEST_ID, UuidArgument.uuid())
								.executes(CmdNodeParty::partyDecline)))
				.executes(c -> {System.out.println(PartyUtils.isInParty(c.getSource().getPlayerOrException())); return 0;});
	}
	
	public static int partyCreate(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		String name = StringArgumentType.getString(ctx, "name");
		Scoreboard board = player.getScoreboard();
		board.addPlayerTeam(name);
		board.addPlayerToTeam(player.getScoreboardName(), board.getPlayerTeam(name));
		ctx.getSource().sendSuccess(LangProvider.PARTY_CREATED::asComponent, false);
		return 0;
	}
	
	public static int partyLeave(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		PlayerTeam team = ctx.getSource().getScoreboard().getPlayersTeam(ctx.getSource().getPlayerOrException().getScoreboardName());
		ctx.getSource().getScoreboard().removePlayerFromTeam(ctx.getSource().getPlayerOrException().getScoreboardName());
		if (team != null && team.getPlayers().isEmpty())
			ctx.getSource().getScoreboard().removePlayerTeam(team);
		ctx.getSource().sendSuccess(LangProvider.PARTY_LEFT::asComponent, false);
		return 0;
	}
	
	public static int partyInvite(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
		if (!PartyUtils.isInParty(ctx.getSource().getPlayerOrException())) {
			ctx.getSource().sendFailure(LangProvider.PARTY_NOT_IN.asComponent());
			return 1;
		}
		PartyUtils.inviteToParty(ctx.getSource().getPlayerOrException(), player);
		ctx.getSource().sendSuccess(() -> LangProvider.PARTY_INVITE.asComponent(player.getDisplayName()), false);
		return 0;
	}
	
	public static int partyUninvite(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
		PartyUtils.uninviteToParty(ctx.getSource().getPlayerOrException(), player);
		return 0;
	}
	
	public static int listParty(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		if (!PartyUtils.isInParty(ctx.getSource().getPlayerOrException())) {
			ctx.getSource().sendFailure(LangProvider.PARTY_NOT_IN.asComponent());
			return 1;
		}
		List<String> memberNames = PartyUtils.getPartyMembers(ctx.getSource().getPlayerOrException())
				.stream().map(s -> s.getName().getString()).toList();
		ctx.getSource().sendSuccess(() -> LangProvider.PARTY_MEMBER_TOTAL.asComponent(memberNames.size()), false);
		ctx.getSource().sendSuccess(() -> LangProvider.PARTY_MEMBER_LIST.asComponent(MsLoggy.listToString(memberNames)), false);
		return 0;
	}
	
	public static int partyAccept(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		UUID requestID = UuidArgument.getUuid(ctx, REQUEST_ID);
		PartyUtils.acceptInvite(ctx.getSource().getPlayerOrException(), requestID);
		return 0;
	}
	
	public static int partyDecline(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		UUID requestID = UuidArgument.getUuid(ctx, REQUEST_ID);
		if (PartyUtils.declineInvite(requestID)) {
			ctx.getSource().sendSuccess(LangProvider.PARTY_DECLINE::asComponent, false);
			return 1;
		}
		else {
			ctx.getSource().sendSuccess(LangProvider.PARTY_NO_INVITES::asComponent, false);
			return 0;
		}
	}
}
