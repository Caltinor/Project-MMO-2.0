package harmonised.pmmo.core.perks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.perks.Perk;
import harmonised.pmmo.client.utils.DP;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.TagBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

public class PerksImpl {
	private static Set<ToolAction> DIG_ACTIONS = Set.of(
			ToolActions.PICKAXE_DIG, 
			ToolActions.AXE_DIG, 
			ToolActions.SHOVEL_DIG, 
			ToolActions.HOE_DIG, 
			ToolActions.SHEARS_DIG, 
			ToolActions.SWORD_DIG);
	private static final CompoundTag NONE = new CompoundTag();
	
	public static Perk BREAK_SPEED = Perk.begin()
			.addDefaults(getDefaults())
			.setStart((player, nbt) -> {
				float speedBonus = getRatioForTool(player.getMainHandItem(), nbt);
				if (speedBonus == 0) return NONE;

				float existingSpeedModification = nbt.getFloat(APIUtils.BREAK_SPEED_OUTPUT_VALUE);
				float speedModification = Math.max(0, nbt.getInt(APIUtils.SKILL_LEVEL) * speedBonus) + existingSpeedModification;
				return TagBuilder.start().withFloat(APIUtils.BREAK_SPEED_OUTPUT_VALUE, speedModification).build();
			})
			.setDescription(LangProvider.PERK_BREAK_SPEED_DESC.asComponent())
			.setStatus((player, settings) -> {
				List<MutableComponent> lines = new ArrayList<>();
				int skillLevel = settings.getInt(APIUtils.SKILL_LEVEL);
				DIG_ACTIONS.stream()
					.filter(action -> settings.getFloat(action.name()) > 0)
					.forEach(action -> lines.add(LangProvider.PERK_BREAK_SPEED_STATUS_1
							.asComponent(action.name(), settings.getFloat(action.name()) * (float)skillLevel))
				);
				return lines;
			}).build();
	
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
			builder.withFloat(action.name(), 0f);
		}
		return builder.build();
	}
	
	private static final UUID ATTRIBUTE_ID = UUID.fromString("b902b6aa-8393-4bdc-8f0d-b937268ef5af");
	private static final Map<Attribute, Double> ANIMAL_ATTRIBUTES = Map.of(
			Attributes.JUMP_STRENGTH, 0.005, 
			Attributes.MAX_HEALTH, 1.0,
			Attributes.MOVEMENT_SPEED, 0.01,
			Attributes.ARMOR, 0.01,
			Attributes.ATTACK_DAMAGE, 0.01);
	public static final String ANIMAL_ID = "tamed";
	
	public static final Perk TAME_BOOST = Perk.begin()
			.addDefaults(TagBuilder.start().withString(APIUtils.SKILLNAME, "taming").withDouble(APIUtils.PER_LEVEL, 1d).build())
			.setStart((player, nbt) -> {
				if (player.level() instanceof ServerLevel) {
					ServerLevel world = (ServerLevel) player.level();
					UUID animalID = nbt.getUUID(ANIMAL_ID);
					LivingEntity animal = (LivingEntity) world.getEntities().get(animalID);
					if (animal == null) return NONE;
					double perLevel = nbt.getDouble(APIUtils.PER_LEVEL);
					
					for (Map.Entry<Attribute, Double> atr : ANIMAL_ATTRIBUTES.entrySet()) {
						AttributeInstance instance = animal.getAttribute(atr.getKey());
						if (instance == null) continue;
						AttributeModifier modifier = new AttributeModifier(ATTRIBUTE_ID, "Taming boost", perLevel * atr.getValue() * nbt.getInt(APIUtils.SKILL_LEVEL), Operation.ADDITION);
						instance.addPermanentModifier(modifier);
					}
				}
				return NONE;
			})
			.setDescription(LangProvider.PERK_TAME_BOOST_DESC.asComponent())
			.setStatus((player, settings) -> {
				List<MutableComponent> lines = new ArrayList<>();
				double perLevel = settings.getDouble(APIUtils.PER_LEVEL);
				for (Map.Entry<Attribute, Double> atr : ANIMAL_ATTRIBUTES.entrySet()) {
					lines.add(LangProvider.PERK_TAME_BOOST_STATUS_1.asComponent(
							Component.translatable(atr.getKey().getDescriptionId()),
							DP.dpCustom(perLevel * atr.getValue(), 4)));
				}
				return lines;
			}).build();
}
