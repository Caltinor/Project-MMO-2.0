package harmonised.pmmo.features.party;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PartyUtils {
	private static final Map<UUID, Invite> invites = new HashMap<>();
	
	private static record Invite(PlayerTeam team, UUID player) {}

	public static List<ServerPlayer> getPartyMembersInRange(ServerPlayer player) {
		List<ServerPlayer> inRange = new ArrayList<>();
		for (ServerPlayer member : getPartyMembers(player)) {
			if (Config.server().party().range() == -2
					|| (Config.server().party().range() == -1 && player.level().equals(member.level()))
					|| player.position().distanceTo(member.position()) <= Config.server().party().range())
				inRange.add(member);
		}
		return inRange;
	}
	
	public static List<ServerPlayer> getPartyMembers(ServerPlayer player) {
		if (player.getTeam() == null)
			return List.of(player);
		else
			return player.getTeam().getPlayers().stream()
				.map(str -> player.getServer().getPlayerList().getPlayerByName(str))
				.filter(Objects::nonNull).toList();
	}
	
	public static void inviteToParty(ServerPlayer member, Player invitee) {
		UUID requestID = UUID.randomUUID();
		Style acceptStyle = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pmmo party accept "+requestID.toString())).withBold(true).withColor(ChatFormatting.GREEN).withUnderlined(true);
		MutableComponent accept = LangProvider.PARTY_ACCEPT.asComponent().withStyle(acceptStyle);
		Style declineStyle = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pmmo party decline "+requestID.toString())).withBold(true).withColor(ChatFormatting.RED).withUnderlined(true);
		MutableComponent decline = LangProvider.PARTY_DECLINE_INVITE.asComponent().withStyle(declineStyle);
		invitee.sendSystemMessage(LangProvider.PARTY_PLAYER_INVITED.asComponent(member.getDisplayName(), accept, decline));
		
		invites.put(requestID, new Invite(member.getTeam(), invitee.getUUID()));
	}
	
	public static void uninviteToParty(ServerPlayer member, Player invitee) {
		PlayerTeam memberParty = member.getTeam();
		if (memberParty == null) {
			member.sendSystemMessage(LangProvider.PARTY_NOT_IN.asComponent());
			return;
		}
		List<UUID> inviteToRemove = invites.entrySet().stream()
				.filter(entry -> entry.getValue().team() == memberParty
					&& entry.getValue().player().equals(invitee.getUUID()))
				.map(Map.Entry::getKey).toList();
		inviteToRemove.forEach(invites::remove);
		member.sendSystemMessage(LangProvider.PARTY_RESCIND_INVITE.asComponent(invitee.getDisplayName()));
	}
	
	public static void acceptInvite(ServerPlayer invitee, UUID requestID) {
		if (invites.get(requestID) == null)
			invitee.sendSystemMessage(LangProvider.PARTY_NO_INVITES.asComponent());
		Invite invite = invites.get(requestID);
		if (!invite.player().equals(invitee.getUUID()))
			return;
		else {
			invitee.getScoreboard().addPlayerToTeam(invitee.getScoreboardName(), invite.team());
			invites.remove(requestID);
		}
		invitee.sendSystemMessage(LangProvider.PARTY_JOINED.asComponent());
	}
	
	public static boolean declineInvite(UUID requestID) {
		return !(invites.remove(requestID) == null);
	}
	
	public static boolean isInParty(ServerPlayer player) {
		return MsLoggy.DEBUG.logAndReturn(player.getTeam() != null, LOG_CODE.FEATURE, "Is In Party: {}");
	}
}
