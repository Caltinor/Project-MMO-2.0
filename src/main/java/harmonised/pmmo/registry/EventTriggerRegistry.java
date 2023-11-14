package harmonised.pmmo.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import com.mojang.datafixers.util.Pair;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.TagBuilder;
import harmonised.pmmo.util.TagUtils;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;

public class EventTriggerRegistry {
	public EventTriggerRegistry() {}
	
	private LinkedListMultimap<EventType, Pair<ResourceLocation, BiFunction<? super Event, CompoundTag, CompoundTag>>> eventListeners = LinkedListMultimap.create();
	
	public void registerListener(@NonNull ResourceLocation listenerID, @NonNull EventType eventType, @NonNull BiFunction<? super Event, CompoundTag, CompoundTag> executeOnTrigger) {
		Preconditions.checkNotNull(eventType);
		Preconditions.checkNotNull(executeOnTrigger);
		Preconditions.checkNotNull(listenerID);
		eventListeners.get(eventType).add(Pair.of(listenerID, executeOnTrigger));
	}
	
	public CompoundTag executeEventListeners(EventType eventType, Event event, CompoundTag dataIn) {
		List<Pair<ResourceLocation, BiFunction<? super Event, CompoundTag, CompoundTag>>> listeners = eventListeners.get(eventType);
		CompoundTag output = TagBuilder.start().withBool(APIUtils.IS_CANCELLED, false).build();
		List<Integer> removals = new ArrayList<>();
		for (int i = 0; i < listeners.size(); i++) {
			CompoundTag funcOutput = listeners.get(i).getSecond().apply(event, dataIn);
			if (funcOutput.contains(APIUtils.IS_CANCELLED)) {
				output = TagUtils.mergeTags(output, funcOutput);
				output.putBoolean(APIUtils.IS_CANCELLED, output.getBoolean(APIUtils.IS_CANCELLED) ? true : funcOutput.getBoolean(APIUtils.IS_CANCELLED));
			}
			else {
				removals.add(i);
			}
				
		}
		removeInvalidListeners(eventType, removals);
		return output;
	}
	
	private void removeInvalidListeners(EventType eventType, List<Integer> removals) {
		for (int i = removals.size()-1; i == 0; i--) {
			MsLoggy.WARN.log(LOG_CODE.API, "Event Listener: [" + eventListeners.get(eventType).get(removals.get(i)).getFirst().toString() +"] did not return a cancel status and was removed.");
			eventListeners.get(eventType).remove((int)removals.get(i));				
		}
	}
}
