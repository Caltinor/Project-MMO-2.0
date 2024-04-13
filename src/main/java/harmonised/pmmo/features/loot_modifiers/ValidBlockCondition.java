package harmonised.pmmo.features.loot_modifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import java.util.Optional;

public class ValidBlockCondition implements LootItemCondition{
	
	public Optional<TagKey<Block>> tag = Optional.empty();
	public Optional<Block> block = Optional.empty();

	public ValidBlockCondition(Optional<TagKey<Block>> tag, Optional<Block> block) {
		this.tag = tag;
		this.block = block;
	}
	public ValidBlockCondition(TagKey<Block> tag) {
		this.tag = Optional.of(tag);
	}
	public ValidBlockCondition(Block block) {this.block = Optional.of(block);}

	@Override
	public boolean test(LootContext t) {
		if (t.getParamOrNull(LootContextParams.THIS_ENTITY) == null)
			return false;
		BlockState brokenBlock = t.getParamOrNull(LootContextParams.BLOCK_STATE);
		if (brokenBlock != null) {
			if (tag.isPresent())
				return brokenBlock.is(tag.get());
			if (block.isPresent())
				return brokenBlock.getBlock().equals(block.get());
		}
		return false;
	}

	@Override
	public LootItemConditionType getType() {
		return GLMRegistry.VALID_BLOCK.get();
	}

	public static final Codec<ValidBlockCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.optionalFieldOf("tag")
					.xmap(s -> s.map(v -> TagKey.create(Registries.BLOCK, new ResourceLocation(v))), t -> t.map(k -> k.location().toString()))
					.forGetter(c -> c.tag),
			Codec.STRING.optionalFieldOf("block")
					.xmap(s -> s.map(v -> BuiltInRegistries.BLOCK.get(new ResourceLocation(v))), t -> t.map(k -> RegistryUtil.getId(k).toString()))
					.forGetter(c -> c.block)
	).apply(instance, ValidBlockCondition::new));
}
