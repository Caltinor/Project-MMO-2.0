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
import harmonised.pmmo.util.MsLoggy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.Tag;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class CP_SyncData_PerkSettings {	
	Map<EventType, LinkedListMultimap<String, CompoundTag>> perkSettings;
	
	public CP_SyncData_PerkSettings(Map<EventType, LinkedListMultimap<String, CompoundTag>> perkSettings) {this.perkSettings = perkSettings;}
	public static CP_SyncData_PerkSettings decode(FriendlyByteBuf buf) {
		Map<EventType, LinkedListMultimap<String, CompoundTag>> outSettings = new HashMap<>();
		CompoundTag nbt = buf.readNbt();
		for (String eventKey : nbt.getAllKeys()) {
			EventType type = EventType.valueOf(eventKey);
			LinkedListMultimap<String, CompoundTag> valueMap = LinkedListMultimap.create();
			for (String skillKey : nbt.getCompound(eventKey).getAllKeys()) {
				List<CompoundTag> values = new ArrayList<>();
				ListTag list = nbt.getCompound(eventKey).getList(skillKey, Tag.TAG_COMPOUND);
				for (int i = 0; i < list.size(); i++) {
					values.add(list.getCompound(i));
				}
				valueMap.putAll(skillKey, values);
			}
			outSettings.put(type, valueMap);
		}	
		
		return new CP_SyncData_PerkSettings(outSettings);
	}
	public void encode(FriendlyByteBuf buf) {
		CompoundTag settings = new CompoundTag();
		for (Map.Entry<EventType, LinkedListMultimap<String, CompoundTag>> entry : perkSettings.entrySet()) {
			String eventKey = entry.getKey().name();
			CompoundTag eventValues = new CompoundTag();
			for (Map.Entry<String, Collection<CompoundTag>> linkedEntry : entry.getValue().asMap().entrySet()) {
				String skillKey = linkedEntry.getKey();
				ListTag perkList = new ListTag();
				for (CompoundTag tag : linkedEntry.getValue()) {
					perkList.add(tag);
				}
				eventValues.put(skillKey, perkList);
			}
			settings.put(eventKey, eventValues);
		}
		buf.writeNbt(settings);
	}
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Core.get(LogicalSide.CLIENT).getPerkRegistry().setSettings(perkSettings);
			MsLoggy.info("Client Side Perk Settings Copied:");
			for (Map.Entry<EventType, LinkedListMultimap<String, CompoundTag>> perks : perkSettings.entrySet()) {
				for (Map.Entry<String, Collection<CompoundTag>> map : perks.getValue().asMap().entrySet()) {
					for (CompoundTag tag : map.getValue()) {
						MsLoggy.info(perks.getKey().name()+":"+map.getKey()+":"+tag.toString());
					}
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
