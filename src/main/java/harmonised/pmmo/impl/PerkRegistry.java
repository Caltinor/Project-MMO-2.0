package harmonised.pmmo.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.function.TriFunction;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.storage.PmmoSavedData;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.TagUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class PerkRegistry {
	public PerkRegistry() {}
	
	private Map<ResourceLocation, TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag>> perkExecutions = new HashMap<>();
	private Map<ResourceLocation, TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag>> perkTerminations = new HashMap<>();
	private Map<EventType, LinkedListMultimap<String, JsonObject>> perkSettings = new HashMap<>();
	
	public void setSettings(Map<EventType, LinkedListMultimap<String, JsonObject>> settings) {
		perkSettings = settings;
	}
	
	public void registerPerk(
			ResourceLocation perkID, 
			TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> onExecute, 
			TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> onConclude) {
		Preconditions.checkNotNull(perkID);
		Preconditions.checkNotNull(onExecute);
		Preconditions.checkNotNull(onConclude);
		perkExecutions.put(perkID, onExecute);
		perkTerminations.put(perkID, onConclude);
		MsLoggy.debug("Registered Perk: "+perkID.toString());
	}
	
	public CompoundTag executePerk(EventType cause, ServerPlayer player) {
		return executePerk(cause, player, new CompoundTag());
	}
	
	public CompoundTag executePerk(EventType cause, ServerPlayer player, CompoundTag dataIn) {
		LinkedListMultimap<String, JsonObject> map =  perkSettings.getOrDefault(cause, LinkedListMultimap.create());
		CompoundTag output = new CompoundTag();
		for (String skill : map.keySet()) {
			List<JsonObject> entries = map.get(skill);
			int skillLevel = PmmoSavedData.get().getPlayerSkillLevel(skill, player.getUUID());
			for (int i = 0; i < entries.size(); i++) {
				CompoundTag src = tagFromJson(entries.get(i));
				src.merge(dataIn);
				ResourceLocation perkID = new ResourceLocation(src.getString("perk"));
				output = TagUtils.mergeTags(output, perkExecutions.getOrDefault(perkID, (a,b,c) -> new CompoundTag()).apply(player, src, skillLevel));
			}
		}
		return output;
	}
	
	public CompoundTag terminatePerk(EventType cause, ServerPlayer player) {
		return terminatePerk(cause, player, new CompoundTag());
	}
	
	public CompoundTag terminatePerk(EventType cause, ServerPlayer player, CompoundTag dataIn) {
		LinkedListMultimap<String, JsonObject> map = perkSettings.getOrDefault(cause, LinkedListMultimap.create());
		CompoundTag output = new CompoundTag();
		for (String skill : map.keySet()) {
			List<JsonObject> entries = map.get(skill);
			int skillLevel = PmmoSavedData.get().getPlayerSkillLevel(skill, player.getUUID());
			for (int i = 0; i < entries.size(); i++) {
				CompoundTag src = tagFromJson(entries.get(i));
				src.merge(dataIn);
				ResourceLocation perkID = new ResourceLocation(src.getString("perk"));
				output = TagUtils.mergeTags(output, perkTerminations.getOrDefault(perkID, (a,b,c) -> new CompoundTag()).apply(player, src, skillLevel));
			}
		}
		return output;
	}
	
	private CompoundTag tagFromJson(JsonObject json) {
		try {
			return TagParser.parseTag(json.toString());
		} catch(CommandSyntaxException e) {
			e.printStackTrace();
			return new CompoundTag();
		}
	}
}
