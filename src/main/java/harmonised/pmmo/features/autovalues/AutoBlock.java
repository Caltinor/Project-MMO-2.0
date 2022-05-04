package harmonised.pmmo.features.autovalues;

import java.util.HashMap;
import java.util.Map;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraftforge.registries.ForgeRegistries;

public class AutoBlock {
	private static final double BASE_HARDNESS = 4;
	
	public static Map<String, Integer> processReqs(ReqType type, ResourceLocation blockID) {
		//exit early if the type is not valid for a block
		if (!type.blockApplicable)
			return new HashMap<>();
		
		Block block = ForgeRegistries.BLOCKS.getValue(blockID);
		Map<String, Integer> outMap = new HashMap<>();
		switch (type) {
		case PLACE: case BREAK: {
			float breakSpeed = block.defaultBlockState().getDestroySpeed(null, null);
			AutoValueConfig.getBlockReq(type).forEach((skill, level) -> {
				outMap.put(skill, (int)Math.max(0, (breakSpeed - BASE_HARDNESS) * AutoValueConfig.HARDNESS_MODIFIER.get()));
			});
			break;
		}
		case INTERACT: {
			//Too nuanced to define.
			break;
		}
		default: }
		return outMap;
	}
	
	public static Map<String, Long> processXpGains(EventType type, ResourceLocation blockID) {
		//exit early if the type is not valide for a block
		if (!type.blockApplicable)
			return new HashMap<>();
		
		Block block = ForgeRegistries.BLOCKS.getValue(blockID);
		Map<String, Long> outMap = new HashMap<>();
		switch (type) {
		case BLOCK_BREAK: case BLOCK_PLACE: {
			float breakSpeed = block.defaultBlockState().getDestroySpeed(null, null);
			AutoValueConfig.getBlockXpAward(type).forEach((skill, level) -> {
				outMap.put(skill, Double.valueOf(Math.max(0, (breakSpeed - BASE_HARDNESS) * AutoValueConfig.HARDNESS_MODIFIER.get())).longValue());
			});
			break;
		}
		case GROW: {
			if (block instanceof CropBlock) {
				outMap.putAll(AutoValueConfig.getBlockXpAward(type));
			}
			break;
		}
		case HIT_BLOCK: {
			//too nuanced.
			break;
		}
		case ACTIVATE_BLOCK: {
			//too nuanced.
			break;
		}
		default: }
		return outMap;	
	}
}
