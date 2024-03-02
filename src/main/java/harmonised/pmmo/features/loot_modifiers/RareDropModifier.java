package harmonised.pmmo.features.loot_modifiers;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import harmonised.pmmo.util.RegistryUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

public class RareDropModifier extends LootModifier{
	
	public static final Codec<RareDropModifier> CODEC = RecordCodecBuilder.create(instance -> codecStart(instance).and(instance.group(
			ResourceLocation.CODEC.fieldOf("item").forGetter(tlm -> RegistryUtil.getId(tlm.drop)),
			Codec.INT.fieldOf("count").forGetter(tlm -> tlm.drop.getCount()),
			Codec.DOUBLE.fieldOf("chance").forGetter(tlm -> tlm.chance),
			Codec.BOOL.optionalFieldOf("per_level").forGetter(tlm -> Optional.of(tlm.perLevel)),
			Codec.STRING.optionalFieldOf("skill").forGetter(tlm -> Optional.of(tlm.skill))
			)).apply(instance, RareDropModifier::new));

	public ItemStack drop;
	public double chance;
	public boolean perLevel;
	public String skill;

	public RareDropModifier(LootItemCondition[] conditionsIn, ResourceLocation lootItemID, int count, double chance) {
		this(conditionsIn, lootItemID, count, chance, Optional.of(false), Optional.empty());
	}
	public RareDropModifier(LootItemCondition[] conditionsIn, ResourceLocation lootItemID, int count, double chance,
							Optional<Boolean> perLevel, Optional<String> skill) {
		super(conditionsIn);
		this.chance = chance;
		this.drop = new ItemStack(BuiltInRegistries.ITEM.get(lootItemID));
		this.drop.setCount(count);
		this.perLevel = perLevel.orElse(false);
		this.skill = skill.orElse("");
	}

	@Override
	public Codec<? extends IGlobalLootModifier> codec() {
		return CODEC;
	}

	@Override
	protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		if (!Config.TREASURE_ENABLED.get()) return generatedLoot;
		if (perLevel && context.getParam(LootContextParams.THIS_ENTITY) instanceof Player player) {
			chance *= Core.get(player.level()).getData().getLevel(skill, player.getUUID());
		}
		double rand = MsLoggy.DEBUG.logAndReturn(context.getRandom().nextDouble(), LOG_CODE.FEATURE, "Rand: {} as test for "+drop.save(new CompoundTag()).toString());
		if (rand <= chance) {
			generatedLoot.add(drop.copy());
		}
		return generatedLoot;
	}

}
