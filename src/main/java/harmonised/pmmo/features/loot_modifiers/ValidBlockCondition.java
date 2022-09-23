package harmonised.pmmo.features.loot_modifiers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.registries.ForgeRegistries;

public class ValidBlockCondition implements LootItemCondition{
	
	public TagKey<Block> tag;
	public Block block;
	
	public ValidBlockCondition(TagKey<Block> tag) {
		this.tag = tag;
	}
	public ValidBlockCondition(Block block) {
		this.block = block;
	}

	@Override
	public boolean test(LootContext t) {
		if (t.getParamOrNull(LootContextParams.THIS_ENTITY) == null)
			return false;
		BlockState brokenBlock = t.getParamOrNull(LootContextParams.BLOCK_STATE);
		if (brokenBlock != null) {
			if (tag != null)
				return ForgeRegistries.BLOCKS.tags().getTag(tag).contains(brokenBlock.getBlock());
			if (block != null)
				return brokenBlock.getBlock().equals(block);
		}
		return false;
	}

	@Override
	public LootItemConditionType getType() {
		return GLMRegistry.VALID_BLOCK.get();
	}
	
	public static final class Serializer implements net.minecraft.world.level.storage.loot.Serializer<ValidBlockCondition> {
		@Override
		public void serialize(JsonObject pJson, ValidBlockCondition pValue, JsonSerializationContext pSerializationContext) {
			if (pValue.tag != null)
				pJson.addProperty("tag", pValue.tag.location().toString());
			if (pValue.block != null)
				pJson.addProperty("block", RegistryUtil.getId(pValue.block).toString());			
		}
		@Override
		public ValidBlockCondition deserialize(JsonObject pJson, JsonDeserializationContext pSerializationContext) {
			if (pJson.get("tag") != null)
				return new ValidBlockCondition(TagKey.create(ForgeRegistries.Keys.BLOCKS, new ResourceLocation(pJson.get("tag").getAsString())));
			if (pJson.get("block") != null)
				return new ValidBlockCondition(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(pJson.get("block").getAsString())));
			return null;
		}		
	}

}
