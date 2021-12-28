package harmonised.pmmo.api.perks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.function.TriFunction;
import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class PerkRegistry {
	
	private static Map<ResourceLocation, TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag>> perkExecutions = new HashMap<>();
	private static Map<ResourceLocation, TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag>> perkTerminations = new HashMap<>();
	private static Map<PerkTrigger, LinkedListMultimap<String, JsonObject>> perkSettings = new HashMap<>();
	
	public static void setSettings(Map<PerkTrigger, LinkedListMultimap<String, JsonObject>> settings) {
		perkSettings = settings;
	}
	
	public static void registerPerk(
			ResourceLocation perkID, 
			TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> onExecute, 
			TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> onConclude) {
		Preconditions.checkNotNull(perkID);
		Preconditions.checkNotNull(onExecute);
		Preconditions.checkNotNull(onConclude);
		perkExecutions.put(perkID, onExecute);
		perkTerminations.put(perkID, onConclude);
	}
	
	public static CompoundTag executePerk(PerkTrigger cause, ServerPlayer player, int skillLevel) {
		return executePerk(cause, player, skillLevel, new CompoundTag());
	}
	
	public static CompoundTag executePerk(PerkTrigger cause, ServerPlayer player, int skillLevel, CompoundTag dataIn) {
		LinkedListMultimap<String, JsonObject> map = perkSettings.get(cause);
		CompoundTag output = new CompoundTag();
		for (String skill : map.keySet()) {
			List<JsonObject> entries = map.get(skill);
			for (int i = 0; i < entries.size(); i++) {
				CompoundTag src = tagFromJson(entries.get(i));
				src.merge(dataIn);
				ResourceLocation perkID = new ResourceLocation(src.getString("perk"));
				output.merge(perkExecutions.getOrDefault(perkID, (a,b,c) -> new CompoundTag()).apply(player, src, skillLevel));
			}
		}
		return output;
	}
	
	public static CompoundTag terminatePerk(PerkTrigger cause, ServerPlayer player, int skillLevel) {
		return terminatePerk(cause, player, skillLevel, new CompoundTag());
	}
	
	public static CompoundTag terminatePerk(PerkTrigger cause, ServerPlayer player, int skillLevel, CompoundTag dataIn) {
		LinkedListMultimap<String, JsonObject> map = perkSettings.get(cause);
		CompoundTag output = new CompoundTag();
		for (String skill : map.keySet()) {
			List<JsonObject> entries = map.get(skill);
			for (int i = 0; i < entries.size(); i++) {
				CompoundTag src = tagFromJson(entries.get(i));
				src.merge(dataIn);
				ResourceLocation perkID = new ResourceLocation(src.getString("perk"));
				output.merge(perkTerminations.getOrDefault(perkID, (a,b,c) -> new CompoundTag()).apply(player, src, skillLevel));
			}
		}
		return output;
	}
	
	private static CompoundTag tagFromJson(JsonObject json) {
		try {
			return TagParser.parseTag(json.getAsString());
		} catch(CommandSyntaxException e) {
			e.printStackTrace();
			return new CompoundTag();
		}
	}
}
