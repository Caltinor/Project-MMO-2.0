package harmonised.pmmo.network.clientpackets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import harmonised.pmmo.client.utils.DataMirror;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class CP_UpdateLevelCache {
	List<Long> levelCache = new ArrayList<>();
	
	public CP_UpdateLevelCache(List<Long> levelCache) {this.levelCache = levelCache;}
	public CP_UpdateLevelCache(FriendlyByteBuf buf) {
		int size = buf.readInt();
		for (int i = 0; i < size; i++) {
			levelCache.add(buf.readLong());
		}
	}
	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(levelCache.size());
		for (int i = 0; i < levelCache.size(); i++) {
			buf.writeLong(levelCache.get(i));
		}
	}
	
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		DataMirror.setLevelCache(levelCache);
	}
}
