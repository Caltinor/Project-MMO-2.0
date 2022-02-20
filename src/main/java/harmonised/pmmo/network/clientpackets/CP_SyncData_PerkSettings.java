package harmonised.pmmo.network.clientpackets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.LinkedListMultimap;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.core.Core;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class CP_SyncData_PerkSettings {	
	Map<EventType, LinkedListMultimap<String, CompoundTag>> perkSettings;
	
	public CP_SyncData_PerkSettings(Map<EventType, LinkedListMultimap<String, CompoundTag>> perkSettings) {this.perkSettings = perkSettings;}
	public static CP_SyncData_PerkSettings decode(FriendlyByteBuf buf) {
		Map<EventType, LinkedListMultimap<String, CompoundTag>> outSettings = new HashMap<>();
		int size = buf.readInt();
		for (int i = 0; i < size; i++) {
			EventType type = EventType.values()[buf.readInt()];
			LinkedListMultimap<String, CompoundTag> valueMap = LinkedListMultimap.create();
			int mapSize = buf.readInt();
			for (int j = 0; j < mapSize; j++) {
				String key = buf.readUtf();
				List<CompoundTag> values = new ArrayList<>();
				int listSize = buf.readInt();
				for (int k = 0; k < listSize; k++) {
					values.add(buf.readNbt());
				}
				valueMap.putAll(key, values);
			}
			outSettings.put(type, valueMap);
		}
		return new CP_SyncData_PerkSettings(outSettings);
	}
	public void encode(FriendlyByteBuf buf) {
		buf.writeInt(perkSettings.size());
		for (Map.Entry<EventType, LinkedListMultimap<String, CompoundTag>> entry : perkSettings.entrySet()) {
			buf.writeInt(entry.getKey().ordinal());
			buf.writeInt(entry.getValue().size());
			for (Map.Entry<String, Collection<CompoundTag>> linkedEntry : entry.getValue().asMap().entrySet()) {
				buf.writeUtf(linkedEntry.getKey());
				buf.writeInt(linkedEntry.getValue().size());
				for (CompoundTag tag : linkedEntry.getValue()) {
					buf.writeNbt(tag);
				}
			}
		}
	}
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Core.get(LogicalSide.CLIENT).getPerkRegistry().setSettings(perkSettings);
		});
		ctx.get().setPacketHandled(true);
	}
}
