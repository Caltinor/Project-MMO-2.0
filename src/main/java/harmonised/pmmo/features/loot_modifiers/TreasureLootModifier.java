package harmonised.pmmo.features.loot_modifiers;

import java.util.List;

import com.google.gson.JsonObject;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;

public class TreasureLootModifier extends LootModifier{

	public ItemStack drop;
	public double chance;
	
	public TreasureLootModifier(LootItemCondition[] conditionsIn, ResourceLocation lootItemID, int count, double chance) {
		super(conditionsIn);
		this.chance = chance;
		this.drop = new ItemStack(ForgeRegistries.ITEMS.getValue(lootItemID));
		this.drop.setCount(count);
	}

	@Override
	protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
		if (context.getRandom().nextDouble() <= chance) {
			
			//this section checks if the drop is air and replaces it with the block
			//being broken.  is is the logic for Extra Drops
			BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
			if (state != null && drop.getItem() == Items.AIR) {
				int count = drop.getCount();
				drop = new ItemStack(state.getBlock().asItem());
				drop.setCount(count);
			}
			
			//Notify player that their skill awarded them an extra drop.
			Entity breaker = context.getParamOrNull(LootContextParams.THIS_ENTITY);
			if (breaker != null && breaker instanceof Player) {
				((Player)breaker).sendMessage(LangProvider.FOUND_TREASURE.asComponent(), breaker.getUUID());
			}
			generatedLoot.add(drop.copy());
		}
		return generatedLoot;
	}
	
	public static class Serializer extends GlobalLootModifierSerializer<TreasureLootModifier> {
	    public Serializer() {super();}
	    
		@Override
		public TreasureLootModifier read(ResourceLocation location, JsonObject object,	LootItemCondition[] ailootcondition) {
			ResourceLocation lootItemID = new ResourceLocation(object.get("item").getAsString());
			int count = object.get("count").getAsInt();
			double chance = object.get("chance").getAsDouble();
			return new TreasureLootModifier(ailootcondition, lootItemID, count, chance);
		}

		@Override
		public JsonObject write(TreasureLootModifier instance) {
			JsonObject json = makeConditions(instance.conditions);
			json.addProperty("item", instance.drop.getItem().getRegistryName().toString());
			json.addProperty("count", instance.drop.getCount());
			json.addProperty("chance", instance.chance);
			return json;
		}
	}
}
