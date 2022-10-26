package harmonised.pmmo.features.loot_modifiers;

import java.util.List;

import com.google.gson.JsonObject;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;

public class RareDropModifier extends LootModifier{
	public ItemStack drop;
	public double chance;
	
	public RareDropModifier(LootItemCondition[] conditionsIn, ResourceLocation lootItemID, int count, double chance) {
		super(conditionsIn);
		this.chance = chance;
		this.drop = new ItemStack(ForgeRegistries.ITEMS.getValue(lootItemID));
		this.drop.setCount(count);
	}

	@Override
	protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
		double rand = MsLoggy.DEBUG.logAndReturn(context.getRandom().nextDouble(), LOG_CODE.FEATURE, "Rand: {} as test for "+drop.serializeNBT().toString());
		if (rand <= chance) {
			generatedLoot.add(drop.copy());
		}
		return generatedLoot;
	}

    public static class Serializer extends GlobalLootModifierSerializer<RareDropModifier> {
        public Serializer() {super();}
        
		@Override
		public RareDropModifier read(ResourceLocation location, JsonObject object,	LootItemCondition[] ailootcondition) {
			ResourceLocation lootItemID = new ResourceLocation(object.get("item").getAsString());
			int count = object.get("count").getAsInt();
			double chance = object.get("chance").getAsDouble();
			return new RareDropModifier(ailootcondition, lootItemID, count, chance);
		}

		@Override
		public JsonObject write(RareDropModifier instance) {
			JsonObject json = makeConditions(instance.conditions);
			json.addProperty("item", instance.drop.getItem().getRegistryName().toString());
			json.addProperty("count", instance.drop.getCount());
			json.addProperty("chance", instance.chance);
			return json;
		}

    }
}
