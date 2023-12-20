package harmonised.pmmo.features.autovalues;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.util.Reference;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.neoforged.neoforge.common.Tags;

public class AutoBlock {
	private static final double BASE_HARDNESS = 4;
	
	public static final ReqType[] REQTYPES = {ReqType.BREAK};
	public static final EventType[] EVENTTYPES = {EventType.BLOCK_BREAK, EventType.BLOCK_PLACE, EventType.GROW};
	
	public static Map<String, Integer> processReqs(ReqType type, ResourceLocation blockID) {

		//exit early if the type is not valid for a block
		if (!type.blockApplicable || isWorldSensitive(blockID))
			return new HashMap<>();
		
		Block block = BuiltInRegistries.BLOCK.get(blockID);
		Map<String, Integer> outMap = new HashMap<>();
		switch (type) {
		case BREAK -> {
			//this water check exists solely to capture breaking waterlogged blocks
			//while autovalues are on, which spams chat with notification the user
			//is unable to break water blocks.
			if (block.equals(Blocks.WATER)) break;

			float breakSpeed = block.defaultBlockState().getDestroySpeed(null, null);
			AutoValueConfig.getBlockReq(type).forEach((skill, level) -> {
				outMap.put(skill, (int)Math.max(0, (breakSpeed - BASE_HARDNESS) * AutoValueConfig.HARDNESS_MODIFIER.get()));
			});
		}
		default -> {}}
		return outMap;
	}
	
	public static Map<String, Long> processXpGains(EventType type, ResourceLocation blockID) {
		//exit early if the type is not valid for a block
		if (!type.blockApplicable || isWorldSensitive(blockID))
			return new HashMap<>();
		
		Holder.Reference<Block> block = BuiltInRegistries.BLOCK.getHolder(ResourceKey.create(Registries.BLOCK, blockID)).orElse(null);
		Map<String, Long> outMap = new HashMap<>();
		if (block == null) return outMap;

		switch (type) {
		case BLOCK_BREAK: case BLOCK_PLACE: {
			if (block.is(Reference.CROPS))
				outMap.putAll(AutoValueConfig.getBlockXpAward(EventType.GROW));
			else if (block.is(Reference.MINABLE_AXE))
				outMap.putAll(AutoValueConfig.AXE_OVERRIDE.get());
			else if (block.is(Reference.MINABLE_HOE))
				outMap.putAll(AutoValueConfig.HOE_OVERRIDE.get());
			else if (block.is(Reference.MINABLE_SHOVEL))
				outMap.putAll(AutoValueConfig.SHOVEL_OVERRIDE.get());
			else
				AutoValueConfig.getBlockXpAward(type).forEach((skill, level) -> {
					float breakSpeed = Math.max(1, block.value().defaultBlockState().getDestroySpeed(null, null));
					long xpOut = Double.valueOf(Math.max(1, breakSpeed * AutoValueConfig.HARDNESS_MODIFIER.get() * level)).longValue();
					if (block.is(Tags.Blocks.ORES))
						xpOut *= AutoValueConfig.RARITIES_MODIFIER.get();
					outMap.put(skill, xpOut);
				});
			break;
		}
		case GROW: {
			if (block.value() instanceof CropBlock) {
				outMap.putAll(AutoValueConfig.getBlockXpAward(type));
			}
			break;
		}
		default: }
		return outMap;	
	}
	
	private static final List<String> WORLD_SENSITIVE_MOD_IDS = List.of("dynamictrees", "dtbop");
	
	/**This checker exists to prevent mods which rely on world context
	 * to evaluate break speed from having their blocks checked against
	 * AutoValues.
	 * 
	 * @param id the object ID being checked
	 * @return whether the namespace exists in the known incompatibility list
	 */
	private static boolean isWorldSensitive(ResourceLocation id) {
		return WORLD_SENSITIVE_MOD_IDS.contains(id.getNamespace());
	}
}
