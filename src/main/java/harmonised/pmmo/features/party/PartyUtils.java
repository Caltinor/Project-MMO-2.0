package harmonised.pmmo.features.party;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class PartyUtils {
	private static Map<UUID, Integer> playerToPartyMap = new HashMap<>();

	public static List<ServerPlayer> getPartyMembersInRange(ServerPlayer player) {
		int partyID = playerToPartyMap.getOrDefault(player.getUUID(), -1);
		if (partyID == -1)
			return List.of(player);
		else {
			List<ServerPlayer> outList = new ArrayList<>();
			for (Map.Entry<UUID, Integer> member : playerToPartyMap.entrySet()) {
				if (member.getValue() == partyID) {
					ServerPlayer memberPlayer = player.getServer().getPlayerList().getPlayer(member.getKey());
					if (memberPlayer != null)
						outList.add(memberPlayer);
				}
			}
			return outList;
		}
	}
	
	public static void removeFromParty(Player player) {
		playerToPartyMap.remove(player.getUUID());
	}
}
