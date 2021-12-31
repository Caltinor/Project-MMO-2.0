package harmonised.pmmo.api.perks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.function.TriFunction;
import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import harmonised.pmmo.api.APIUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ShortTag;
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
	
	public static CompoundTag executePerk(PerkTrigger cause, ServerPlayer player) {
		return executePerk(cause, player, new CompoundTag());
	}
	
	public static CompoundTag executePerk(PerkTrigger cause, ServerPlayer player, CompoundTag dataIn) {
		LinkedListMultimap<String, JsonObject> map =  perkSettings.getOrDefault(cause, LinkedListMultimap.create());
		CompoundTag output = new CompoundTag();
		for (String skill : map.keySet()) {
			List<JsonObject> entries = map.get(skill);
			int skillLevel = APIUtils.getLevel(skill, player);
			for (int i = 0; i < entries.size(); i++) {
				CompoundTag src = tagFromJson(entries.get(i));
				src.merge(dataIn);
				ResourceLocation perkID = new ResourceLocation(src.getString("perk"));
				mergeTags(output, perkExecutions.getOrDefault(perkID, (a,b,c) -> new CompoundTag()).apply(player, src, skillLevel));
			}
		}
		return output;
	}
	
	public static CompoundTag terminatePerk(PerkTrigger cause, ServerPlayer player) {
		return terminatePerk(cause, player, new CompoundTag());
	}
	
	public static CompoundTag terminatePerk(PerkTrigger cause, ServerPlayer player, CompoundTag dataIn) {
		LinkedListMultimap<String, JsonObject> map = perkSettings.getOrDefault(cause, LinkedListMultimap.create());
		CompoundTag output = new CompoundTag();
		for (String skill : map.keySet()) {
			List<JsonObject> entries = map.get(skill);
			int skillLevel = APIUtils.getLevel(skill, player);
			for (int i = 0; i < entries.size(); i++) {
				CompoundTag src = tagFromJson(entries.get(i));
				src.merge(dataIn);
				ResourceLocation perkID = new ResourceLocation(src.getString("perk"));
				mergeTags(output, perkTerminations.getOrDefault(perkID, (a,b,c) -> new CompoundTag()).apply(player, src, skillLevel));
			}
		}
		return output;
	}
	
	private static CompoundTag tagFromJson(JsonObject json) {
		try {
			return TagParser.parseTag(json.toString());
		} catch(CommandSyntaxException e) {
			e.printStackTrace();
			return new CompoundTag();
		}
	}
	
	private static CompoundTag mergeTags(CompoundTag tag1, CompoundTag tag2) {
		CompoundTag output = new CompoundTag();
		for (String key : tag1.getAllKeys()) {
			if (tag2.contains(key)) {
				if (tag1.get(key) instanceof NumericTag) {
					if (tag1.get(key) instanceof DoubleTag)
						output.putDouble(key, tag1.getDouble(key) + tag2.getDouble(key));
					else if (tag1.get(key) instanceof FloatTag)
						output.putFloat(key, tag1.getFloat(key) + tag2.getFloat(key));
					else if (tag1.get(key) instanceof IntTag)
						output.putInt(key, tag1.getInt(key) + tag2.getInt(key));
					else if (tag1.get(key) instanceof LongTag)
						output.putLong(key, tag1.getLong(key) + tag2.getLong(key));
					else if (tag1.get(key) instanceof ShortTag) 
						output.putShort(key, (short)(tag1.getShort(key) + tag2.getShort(key)));
					else
						output.put(key, tag1.get(key));
				}
				else
					output.put(key, tag1.get(key));
			}
		}
		return output;
	}
}
