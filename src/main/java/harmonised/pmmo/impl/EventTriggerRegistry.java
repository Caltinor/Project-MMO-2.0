package harmonised.pmmo.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import com.mojang.datafixers.util.Pair;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;

public class EventTriggerRegistry {
	public static final String IS_CANCELLED = "is_cancelled";
	
	private static LinkedListMultimap<EventType, Pair<ResourceLocation, BiFunction<? super Event, CompoundTag, CompoundTag>>> eventListeners = LinkedListMultimap.create();
	
	public static void registerListener(@NonNull ResourceLocation listenerID, @NonNull EventType eventType, @NonNull BiFunction<? super Event, CompoundTag, CompoundTag> executeOnTrigger) {
		Preconditions.checkNotNull(eventType);
		Preconditions.checkNotNull(executeOnTrigger);
		Preconditions.checkNotNull(listenerID);
		eventListeners.get(eventType).add(Pair.of(listenerID, executeOnTrigger));
	}
	
	public static CompoundTag executeEventListeners(EventType eventType, Event event, CompoundTag dataIn) {
		List<Pair<ResourceLocation, BiFunction<? super Event, CompoundTag, CompoundTag>>> listeners = eventListeners.get(eventType);
		CompoundTag output = new CompoundTag();
		List<Integer> removals = new ArrayList<>();
		for (int i = 0; i < listeners.size(); i++) {
			CompoundTag funcOutput = listeners.get(i).getSecond().apply(event, dataIn);
			if (funcOutput.contains(IS_CANCELLED)) 
				output = TagUtils.mergeTags(output, funcOutput);
			else {
				removals.add(i);
				MsLoggy.debug("Event Listener: [" + listeners.get(i).getFirst().toString() +"] did not return a cancel status and was removed.");
			}
				
		}
		return output;
	}
}
