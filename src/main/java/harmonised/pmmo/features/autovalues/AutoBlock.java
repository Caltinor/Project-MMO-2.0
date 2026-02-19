package harmonised.pmmo.features.autovalues;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.util.Reference;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.neoforged.neoforge.common.Tags;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoBlock {
	private static final double BASE_HARDNESS = 4;
	
	public static final ReqType[] REQTYPES = {ReqType.BREAK};
	public static final EventType[] EVENTTYPES = {EventType.BLOCK_BREAK, EventType.BLOCK_PLACE, EventType.GROW};
	
	public static Map<String, Long> processReqs(ReqType type, Identifier blockID) {

		//exit early if the type is not valid for a block
		if (!type.blockApplicable || isWorldSensitive(blockID) || !Config.autovalue().reqEnabled(type))
			return new HashMap<>();
		
		Block block = BuiltInRegistries.BLOCK.getValue(blockID);
		Map<String, Long> outMap = new HashMap<>();
		switch (type) {
		case BREAK -> {
			//this water check exists solely to capture breaking waterlogged blocks
			//while autovalues are on, which spams chat with notification the user
			//is unable to break water blocks.
			if (block.equals(Blocks.WATER)) break;

			float breakSpeed = block.defaultBlockState().getDestroySpeed(EmptyBlockGetter.INSTANCE, null);
			Config.autovalue().reqs().blockDefault().forEach((skill, level) -> {
				outMap.put(skill, (long)Math.max(0, (breakSpeed - BASE_HARDNESS) * Config.autovalue().tweaks().hardnessModifier()));
			});
		}
		default -> {}}
		return outMap;
	}
	
	public static Map<String, Long> processXpGains(EventType type, Identifier blockID) {
		//exit early if the type is not valid for a block
		if (!type.blockApplicable || isWorldSensitive(blockID) || !Config.autovalue().xpEnabled(type))
			return new HashMap<>();
		
		Holder<Block> block = BuiltInRegistries.BLOCK.wrapAsHolder(BuiltInRegistries.BLOCK.getValue(blockID));
		Map<String, Long> outMap = new HashMap<>();
		if (block == null) return outMap;

		switch (type) {
		case BLOCK_BREAK: case BLOCK_PLACE: {
			if (block.is(Reference.CROPS))
				outMap.putAll(Config.autovalue().xpAwards().block(EventType.GROW));
			else if (block.is(Reference.MINABLE_AXE))
				outMap.putAll(Config.autovalue().xpAwards().axeOverride());
			else if (block.is(Reference.MINABLE_HOE))
				outMap.putAll(Config.autovalue().xpAwards().hoeOverride());
			else if (block.is(Reference.MINABLE_SHOVEL))
				outMap.putAll(Config.autovalue().xpAwards().shovelOverride());
			else
				Config.autovalue().xpAwards().block(type).forEach((skill, level) -> {
					float breakSpeed = Math.max(1, block.value().defaultBlockState().getDestroySpeed(null, null));
					long xpOut = Double.valueOf(Math.max(1, breakSpeed * Config.autovalue().tweaks().hardnessModifier() * level)).longValue();
					if (block.is(Tags.Blocks.ORES))
						xpOut *= Config.autovalue().xpAwards().raritiesMultiplier().longValue();
					outMap.put(skill, xpOut);
				});
			break;
		}
		case GROW: {
			if (block.value() instanceof CropBlock) {
				outMap.putAll(Config.autovalue().xpAwards().block(type));
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
	private static boolean isWorldSensitive(Identifier id) {
		return WORLD_SENSITIVE_MOD_IDS.contains(id.getNamespace());
	}
}
