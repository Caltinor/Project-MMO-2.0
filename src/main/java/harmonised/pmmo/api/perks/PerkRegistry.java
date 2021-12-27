package harmonised.pmmo.api.perks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.util.TriConsumer;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import harmonised.pmmo.skills.Skill;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class PerkRegistry {
	
	private static Map<ResourceLocation, TriConsumer<ServerPlayer, CompoundTag, Integer>> perkExecutions = new HashMap<>();
	private static Map<ResourceLocation, TriConsumer<ServerPlayer, CompoundTag, Integer>> perkTerminations = new HashMap<>();
	private static Map<PerkTrigger, LinkedListMultimap<Skill, JsonObject>> perkSettings = new HashMap<>();
	
	public static void registerPerk(
			ResourceLocation perkID, 
			TriConsumer<ServerPlayer, CompoundTag, Integer> onExecute, 
			TriConsumer<ServerPlayer, CompoundTag, Integer> onConclude) {
		Preconditions.checkNotNull(perkID);
		Preconditions.checkNotNull(onExecute);
		Preconditions.checkNotNull(onConclude);
		perkExecutions.put(perkID, onExecute);
		perkTerminations.put(perkID, onConclude);
	}
	
	public static void executePerk(PerkTrigger cause, ServerPlayer player, int skillLevel) {
		LinkedListMultimap<Skill, JsonObject> map = perkSettings.get(cause);
		for (Skill skill : map.keySet()) {
			List<JsonObject> entries = map.get(skill);
			for (int i = 0; i < entries.size(); i++) {
				CompoundTag src = tagFromJson(entries.get(i));
				ResourceLocation perkID = new ResourceLocation(src.getString("perk"));
				perkExecutions.getOrDefault(perkID, (a,b,c) -> {}).accept(player, src, skillLevel);
			}
		}
	}
	
	public static void terminatePerk(PerkTrigger cause, ServerPlayer player, int skillLevel) {
		LinkedListMultimap<Skill, JsonObject> map = perkSettings.get(cause);
		for (Skill skill : map.keySet()) {
			List<JsonObject> entries = map.get(skill);
			for (int i = 0; i < entries.size(); i++) {
				CompoundTag src = tagFromJson(entries.get(i));
				ResourceLocation perkID = new ResourceLocation(src.getString("perk"));
				perkTerminations.getOrDefault(perkID, (a,b,c) -> {}).accept(player, src, skillLevel);
			}
		}
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
