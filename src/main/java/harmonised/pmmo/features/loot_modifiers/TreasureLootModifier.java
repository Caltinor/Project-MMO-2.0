package harmonised.pmmo.features.loot_modifiers;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.RegistryUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;

public class TreasureLootModifier extends LootModifier{
	
	public static final Codec<TreasureLootModifier> CODEC = RecordCodecBuilder.create(instance -> codecStart(instance).and(instance.group(
			ResourceLocation.CODEC.fieldOf("item").forGetter(tlm -> RegistryUtil.getId(tlm.drop)),
			Codec.INT.fieldOf("count").forGetter(tlm -> tlm.drop.getCount()),
			Codec.DOUBLE.fieldOf("chance").forGetter(tlm -> tlm.chance)
			)).apply(instance, TreasureLootModifier::new));

	public ItemStack drop;
	public double chance;
	
	public TreasureLootModifier(LootItemCondition[] conditionsIn, ResourceLocation lootItemID, int count, double chance) {
		super(conditionsIn);
		this.chance = chance;
		this.drop = new ItemStack(ForgeRegistries.ITEMS.getValue(lootItemID));
		this.drop.setCount(count);
	}

	@Override
	public Codec<? extends IGlobalLootModifier> codec() {
		return CODEC;
	}

	@Override
	protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot,	LootContext context) {
		if (context.getRandom().nextDouble() <= chance) {
			Entity breaker = context.getParamOrNull(LootContextParams.THIS_ENTITY);
			if (breaker != null && breaker instanceof Player) {
				((Player)breaker).sendSystemMessage(LangProvider.FOUND_TREASURE.asComponent());
			}
			generatedLoot.add(drop.copy());
		}
		return generatedLoot;
	}
}
