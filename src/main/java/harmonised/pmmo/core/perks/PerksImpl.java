package harmonised.pmmo.core.perks;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.function.TriFunction;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.util.TagBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

public class PerksImpl {
	private static final CompoundTag NONE = new CompoundTag();
	
	public static TriFunction<Player, CompoundTag, Integer, CompoundTag> DUMMY = (player, nbt, level) -> {
		return NONE;
	};	
	
	public static TriFunction<Player, CompoundTag, Integer, CompoundTag> BREAK_SPEED = (player, nbt, level) -> {
		float speedIn = nbt.getFloat(APIUtils.BREAK_SPEED_INPUT_VALUE);
		float speedBonus = getRatioForTool(player.getMainHandItem(), nbt);
		if (speedBonus == 0) return NONE;
		float newSpeed = speedIn * Math.max(0, 1 + level * speedBonus);
		return TagBuilder.start().withFloat(APIUtils.BREAK_SPEED_OUTPUT_VALUE, newSpeed).build();
	};
	
	private static Set<ToolAction> DIG_ACTIONS = Set.of(ToolActions.PICKAXE_DIG, ToolActions.AXE_DIG, ToolActions.SHOVEL_DIG, ToolActions.HOE_DIG, ToolActions.SHEARS_DIG, ToolActions.SWORD_DIG);
	
	private static float getRatioForTool(ItemStack tool, CompoundTag nbt) {
		float ratio = 0f;
		for (ToolAction action : DIG_ACTIONS) {
			if (tool.canPerformAction(action))
				ratio += nbt.getFloat(action.name());
		}
		return ratio;
	}
	
	public static CompoundTag getDefaults() {
		TagBuilder builder = TagBuilder.start();
		for (ToolAction action : DIG_ACTIONS) {
			builder.withFloat(action.name(), 0);
		}
		return builder.build();
	}
	
	private static final UUID ATTRIBUTE_ID = UUID.fromString("b902b6aa-8393-4bdc-8f0d-b937268ef5af");
	public static final CompoundTag TAME_DEFAULTS = TagBuilder.start().withDouble(APIUtils.PER_LEVEL, 1d).build();
	private static final Map<Attribute, Double> ANIMAL_ATTRIBUTES = Map.of(
			Attributes.JUMP_STRENGTH, 0.005, 
			Attributes.MAX_HEALTH, 1.0,
			Attributes.MOVEMENT_SPEED, 0.01,
			Attributes.ARMOR, 0.01,
			Attributes.ATTACK_DAMAGE, 0.01);
	public static final String ANIMAL_ID = "tamed";
	public static TriFunction<Player, CompoundTag, Integer, CompoundTag> TAME_BOOST = (player, nbt, level) -> {
		if (player.level instanceof ServerLevel) {
			ServerLevel world = (ServerLevel) player.level;
			UUID animalID = nbt.getUUID(ANIMAL_ID);
			LivingEntity animal = (LivingEntity) world.getEntities().get(animalID);
			if (animal == null) return NONE;
			double perLevel = nbt.getDouble(APIUtils.PER_LEVEL);
			
			for (Map.Entry<Attribute, Double> atr : ANIMAL_ATTRIBUTES.entrySet()) {
				AttributeInstance instance = animal.getAttribute(atr.getKey());
				if (instance == null) continue;
				AttributeModifier modifier = new AttributeModifier(ATTRIBUTE_ID, "Taming boost", perLevel * atr.getValue() * level, Operation.ADDITION);
				instance.addPermanentModifier(modifier);
			}
		}
		return NONE;
	};
}
