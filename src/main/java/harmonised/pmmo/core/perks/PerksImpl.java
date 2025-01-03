package harmonised.pmmo.core.perks;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.perks.Perk;
import harmonised.pmmo.client.utils.DP;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.TagBuilder;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PerksImpl {
	private static Set<ItemAbility> DIG_ACTIONS = Set.of(
			ItemAbilities.PICKAXE_DIG,
			ItemAbilities.AXE_DIG, 
			ItemAbilities.SHOVEL_DIG, 
			ItemAbilities.HOE_DIG, 
			ItemAbilities.SHEARS_DIG, 
			ItemAbilities.SWORD_DIG);
	private static final CompoundTag NONE = new CompoundTag();
	public static final Map<Player, Boolean> breakSpeedEnabled = new HashMap<>();
	public static Perk BREAK_SPEED = Perk.begin()
			.addConditions((p,t) -> breakSpeedEnabled.getOrDefault(p, true))
			.addDefaults(getDefaults())
			.setStart((player, nbt) -> {
				float speedBonus = getRatioForTool(player.getMainHandItem(), nbt);
				if (speedBonus == 0) return NONE;

				float existingSpeedModification = nbt.getFloat(APIUtils.BREAK_SPEED_OUTPUT_VALUE);
				float speedModification = Math.max(0, nbt.getInt(APIUtils.SKILL_LEVEL) * speedBonus) + existingSpeedModification;
				speedModification = Math.min(nbt.getInt(APIUtils.MAX_BOOST), speedModification);
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
		for (ItemAbility action : DIG_ACTIONS) {
			if (tool.canPerformAction(action))
				ratio += nbt.getFloat(action.name());
		}
		return ratio;
	}
	
	public static CompoundTag getDefaults() {
		TagBuilder builder = TagBuilder.start();
		builder.withInt(APIUtils.MAX_BOOST, Integer.MAX_VALUE);
		for (ItemAbility action : DIG_ACTIONS) {
			builder.withFloat(action.name(), 0f);
		}
		return builder.build();
	}
	
	private static final ResourceLocation ATTRIBUTE_ID = Reference.rl("tame_boost");
	private static final Map<Holder<Attribute>, Double> ANIMAL_ATTRIBUTES = Map.of(
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
					UUID animalID = nbt.getUUID(ANIMAL_ID);
					LivingEntity animal = (LivingEntity) world.getEntities().get(animalID);
					if (animal == null) return NONE;
					double perLevel = nbt.getDouble(APIUtils.PER_LEVEL);
					
					for (Map.Entry<Holder<Attribute>, Double> atr : ANIMAL_ATTRIBUTES.entrySet()) {
						AttributeInstance instance = animal.getAttribute(atr.getKey());
						if (instance == null) continue;
						double boost = Mth.clamp(perLevel * atr.getValue() * nbt.getInt(APIUtils.SKILL_LEVEL), 0, nbt.getDouble(APIUtils.MAX_BOOST));
						AttributeModifier modifier = new AttributeModifier(ATTRIBUTE_ID, boost, Operation.ADD_VALUE);
						instance.addPermanentModifier(modifier);
					}
				}
				return NONE;
			})
			.setDescription(LangProvider.PERK_TAME_BOOST_DESC.asComponent())
			.setStatus((player, settings) -> {
				List<MutableComponent> lines = new ArrayList<>();
				double perLevel = settings.getDouble(APIUtils.PER_LEVEL);
				for (Map.Entry<Holder<Attribute>, Double> atr : ANIMAL_ATTRIBUTES.entrySet()) {
					lines.add(LangProvider.PERK_TAME_BOOST_STATUS_1.asComponent(
							Component.translatable(atr.getKey().value().getDescriptionId()),
							DP.dpCustom(perLevel * atr.getValue(), 4)));
				}
				return lines;
			}).build();
}
