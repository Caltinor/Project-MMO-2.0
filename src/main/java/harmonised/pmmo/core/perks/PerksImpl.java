package harmonised.pmmo.core.perks;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.perks.Perk;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.TagBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PerksImpl {
	private static final CompoundTag NONE = new CompoundTag();
	public static final Map<Player, Boolean> breakSpeedEnabled = new HashMap<>();
	public static Perk BREAK_SPEED = Perk.begin()
			.addConditions((p,t) -> breakSpeedEnabled.getOrDefault(p, true))
			.addDefaults(getDefaults())
			.setStart((player, nbt) -> {
				BlockPos pos = BlockPos.of(nbt.getLongOr(APIUtils.BLOCK_POS, new BlockPos(0,0,0).asLong()));
				BlockState state = player.level().getBlockState(pos);
				float speedBonus = getRatioForTool(player.getMainHandItem(), nbt, state);
				if (speedBonus == 0) return NONE;

				float existingSpeedModification = nbt.contains(APIUtils.BREAK_SPEED_OUTPUT_VALUE)
						? nbt.getFloatOr(APIUtils.BREAK_SPEED_OUTPUT_VALUE, 0f)
						: nbt.getFloatOr(APIUtils.BREAK_SPEED_INPUT_VALUE, 0f);
				float speedModification = Math.max(0, nbt.getIntOr(APIUtils.SKILL_LEVEL, 0) * speedBonus) + existingSpeedModification;
				speedModification = Math.min(nbt.getIntOr(APIUtils.MAX_BOOST, 0), speedModification);
				return TagBuilder.start().withFloat(APIUtils.BREAK_SPEED_OUTPUT_VALUE, speedModification).build();
			}).build();

	/*
		{"ratios": {
			"minecraft:pickaxe": {
				"minecraft:mineable_with_pickaxe": 0.005
			}
		}}
	 */
	private static float getRatioForTool(ItemStack tool, CompoundTag nbt, BlockState state) {
		float ratio = 0f;
		CompoundTag ratios = nbt.getCompoundOrEmpty("ratios");
		for (String toolTag : ratios.keySet()) {
			if (tool.is(TagKey.create(Registries.ITEM, Reference.of(toolTag)))) {
				CompoundTag blockTags = ratios.getCompoundOrEmpty(toolTag);
				for (String blockTag : blockTags.keySet()) {
					if (state.is(TagKey.create(Registries.BLOCK, Reference.of(blockTag))))
						ratio += blockTags.getFloatOr(blockTag, 0f);
				}
			}
		}
		return ratio;
	}
	
	public static CompoundTag getDefaults() {
		TagBuilder builder = TagBuilder.start();
		builder.withInt(APIUtils.MAX_BOOST, Integer.MAX_VALUE);
		builder.with("ratios", new CompoundTag());
		return builder.build();
	}
	
	private static final Identifier ATTRIBUTE_ID = Reference.rl("tame_boost");
	public static final Map<Holder<Attribute>, Double> ANIMAL_ATTRIBUTES = Map.of(
			Attributes.JUMP_STRENGTH, 0.005, 
			Attributes.MAX_HEALTH, 1.0,
			Attributes.MOVEMENT_SPEED, 0.01,
			Attributes.ARMOR, 0.01,
			Attributes.ATTACK_DAMAGE, 0.01);
	public static final String ANIMAL_ID = "tamed";
	
	public static final Perk TAME_BOOST = Perk.begin()
			.addDefaults(TagBuilder.start()
					.withString(APIUtils.SKILLNAME, "taming")
					.withDouble(APIUtils.PER_LEVEL, 1d)
					.withDouble(APIUtils.MAX_BOOST, Double.MAX_VALUE).build())
			.setStart((player, nbt) -> {
				if (player.level() instanceof ServerLevel) {
					ServerLevel world = (ServerLevel) player.level();
					UUID animalID = UUID.fromString(nbt.getString(ANIMAL_ID).get());
					LivingEntity animal = (LivingEntity) world.getEntities().get(animalID);
					if (animal == null) return NONE;
					double perLevel = nbt.getDouble(APIUtils.PER_LEVEL).get();
					
					for (Map.Entry<Holder<Attribute>, Double> atr : ANIMAL_ATTRIBUTES.entrySet()) {
						AttributeInstance instance = animal.getAttribute(atr.getKey());
						if (instance == null) continue;
						double boost = Mth.clamp(perLevel * atr.getValue() * nbt.getInt(APIUtils.SKILL_LEVEL).get(), 0, nbt.getDouble(APIUtils.MAX_BOOST).get());
						AttributeModifier modifier = new AttributeModifier(ATTRIBUTE_ID, boost, Operation.ADD_VALUE);
						instance.addPermanentModifier(modifier);
					}
				}
				return NONE;
			}).build();
}
