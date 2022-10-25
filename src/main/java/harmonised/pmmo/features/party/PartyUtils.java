package harmonised.pmmo.features.party;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

public class PartyUtils {
	private static final Map<UUID, Integer> playerToPartyMap = new HashMap<>();
	private static final Map<UUID, Invite> invites = new HashMap<>();
	
	private static record Invite(int partyID, UUID player) {}

	public static List<ServerPlayer> getPartyMembersInRange(ServerPlayer player) {
		List<ServerPlayer> inRange = new ArrayList<>();
		for (ServerPlayer member : getPartyMembers(player)) {
			if (player.position().distanceTo(member.position()) <= Config.PARTY_RANGE.get())
				inRange.add(member);
		}
		return inRange;
	}
	
	public static List<ServerPlayer> getPartyMembers(ServerPlayer player) {
		int partyID = playerToPartyMap.getOrDefault(player.getUUID(), -1);
		if (partyID == -1)
			return List.of(player);
		else {
			List<ServerPlayer> outList = new ArrayList<>();
			outList.add(player); //source player should always be at index zero
			for (Map.Entry<UUID, Integer> member : playerToPartyMap.entrySet()) {
				if (member.getKey().equals(player.getUUID())) continue; //don't add source player again to the list
				if (member.getValue() == partyID) {
					ServerPlayer memberPlayer = player.getServer().getPlayerList().getPlayer(member.getKey());
					if (memberPlayer != null)
						outList.add(memberPlayer);
				}
			}
			return outList;
		}
	}
	
	public static void inviteToParty(Player member, Player invitee) {
		UUID requestID = UUID.randomUUID();
		Style acceptStyle = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pmmo party accept "+requestID.toString())).withBold(true).withColor(ChatFormatting.GREEN).withUnderlined(true);
		MutableComponent accept = LangProvider.PARTY_ACCEPT.asComponent().withStyle(acceptStyle);
		Style declineStyle = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pmmo party decline "+requestID.toString())).withBold(true).withColor(ChatFormatting.RED).withUnderlined(true);
		MutableComponent decline = LangProvider.PARTY_DECLINE_INVITE.asComponent().withStyle(declineStyle);
		invitee.sendMessage(LangProvider.PARTY_PLAYER_INVITED.asComponent(member.getDisplayName(), accept, decline), invitee.getUUID());
		
		invites.put(requestID, new Invite(playerToPartyMap.get(member.getUUID()), invitee.getUUID()));
	}
	
	public static void uninviteToParty(Player member, Player invitee) {
		int memberPartyID = playerToPartyMap.getOrDefault(member.getUUID(), -1);
		if (memberPartyID == -1) {
			member.sendMessage(LangProvider.PARTY_NOT_IN.asComponent(), member.getUUID());
			return;
		}
		UUID inviteToRemove = null;
		for (Map.Entry<UUID, Invite> invite : invites.entrySet()) {
			Invite i = invite.getValue();
			if (i.partyID() == memberPartyID && i.player().equals(invitee.getUUID())) {
				inviteToRemove = invite.getKey();
				break;
			}				
		}
		if (inviteToRemove != null) {
			invites.remove(inviteToRemove);
			member.sendMessage(LangProvider.PARTY_RESCIND_INVITE.asComponent(invitee.getDisplayName()), member.getUUID());
		}
	}
	
	public static void acceptInvite(Player invitee, UUID requestID) {
		if (invites.get(requestID) == null)
			invitee.sendMessage(LangProvider.PARTY_NO_INVITES.asComponent(), invitee.getUUID());
		Invite invite = invites.get(requestID);
		if (!invite.player().equals(invitee.getUUID()))
			return;
		else {
			playerToPartyMap.put(invitee.getUUID(), invite.partyID());
			invites.remove(requestID);
		}
		invitee.sendMessage(LangProvider.PARTY_JOINED.asComponent(), invitee.getUUID());
	}
	
	public static boolean declineInvite(UUID requestID) {
		return !(invites.remove(requestID) == null);
	}
	
	public static void removeFromParty(Player player) {
		playerToPartyMap.remove(player.getUUID());
	}
	
	public static void createParty(Player player) {
		playerToPartyMap.put(player.getUUID(), getFreePartyID());
		MsLoggy.DEBUG.log(LOG_CODE.FEATURE, MsLoggy.mapToString(playerToPartyMap));
	}
	
	public static boolean isInParty(Player player) {
		return MsLoggy.DEBUG.logAndReturn(playerToPartyMap.containsKey(player.getUUID()), LOG_CODE.FEATURE, "Is In Party: {}");
	}
	
	private static int getFreePartyID() {
		int id = 0;
		while (id < Integer.MAX_VALUE) {
			if (playerToPartyMap.values().contains(id)) {
				id++;
				continue;
			}
			return id;
		}
		return -1;
	}
}
