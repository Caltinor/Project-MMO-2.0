package harmonised.pmmo.features.party;

import java.util.List;

import net.minecraft.server.level.ServerPlayer;

public class PartyUtils {

	public static List<ServerPlayer> getPartyMembersInRange(ServerPlayer player) {
		return List.of(player);
	}
}
