package harmonised.pmmo.setup.datagen;

import java.util.concurrent.CompletableFuture;

import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import harmonised.pmmo.util.Reference;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;


public class EntityTagProvider extends EntityTypeTagsProvider{

	public EntityTagProvider(PackOutput pOutput, CompletableFuture<Provider> pProvider,	@Nullable ExistingFileHelper existingFileHelper) {
		super(pOutput, pProvider, Reference.MOD_ID, existingFileHelper);
	}
	
	@Override
	protected void addTags(Provider pProvider) {
		tag(Reference.ANIMAL_TAG)
			.add(EntityType.BAT)
			.add(EntityType.BEE)
			.add(EntityType.CAT)
			.add(EntityType.CHICKEN)
			.add(EntityType.COD)
			.add(EntityType.COW)
			.add(EntityType.DOLPHIN)
			.add(EntityType.DONKEY)
			.add(EntityType.FOX)
			.add(EntityType.FROG)
			.add(EntityType.GLOW_SQUID)
			.add(EntityType.GOAT)
			.add(EntityType.HORSE)
			.add(EntityType.IRON_GOLEM)
			.add(EntityType.LLAMA)
			.add(EntityType.MOOSHROOM)
			.add(EntityType.MULE)
			.add(EntityType.OCELOT)
			.add(EntityType.PANDA)
			.add(EntityType.PARROT)
			.add(EntityType.PIG)
			.add(EntityType.POLAR_BEAR)
			.add(EntityType.PUFFERFISH)
			.add(EntityType.RABBIT)
			.add(EntityType.SALMON)
			.add(EntityType.SHEEP)
			.add(EntityType.SNOW_GOLEM)
			.add(EntityType.SQUID)
			.add(EntityType.STRIDER)
			.add(EntityType.TRADER_LLAMA)
			.add(EntityType.TROPICAL_FISH)
			.add(EntityType.TURTLE)
			.add(EntityType.WOLF);
		
		tag(Reference.BREEDABLE_TAG)
			.add(EntityType.AXOLOTL)
			.add(EntityType.BEE)
			.add(EntityType.CAT)
			.add(EntityType.CHICKEN)
			.add(EntityType.COW)
			.add(EntityType.DONKEY)
			.add(EntityType.FOX)
			.add(EntityType.FROG)
			.add(EntityType.GOAT)
			.add(EntityType.HOGLIN)
			.add(EntityType.HORSE)
			.add(EntityType.LLAMA)
			.add(EntityType.MOOSHROOM)
			.add(EntityType.OCELOT)
			.add(EntityType.PANDA)
			.add(EntityType.PIG)
			.add(EntityType.RABBIT)
			.add(EntityType.SHEEP)
			.add(EntityType.STRIDER)
			.add(EntityType.TRADER_LLAMA)
			.add(EntityType.TURTLE)
			.add(EntityType.WOLF);
		
		tag(Reference.MOB_TAG)
			.addTag(EntityTypeTags.RAIDERS)
			.addTag(EntityTypeTags.SKELETONS)
			.add(EntityType.BLAZE)
			.add(EntityType.CAVE_SPIDER)
			.add(EntityType.CREEPER)
			.add(EntityType.DROWNED)
			.add(EntityType.ELDER_GUARDIAN)
			.add(EntityType.ENDER_DRAGON)
			.add(EntityType.ENDERMAN)
			.add(EntityType.ENDERMITE)
			.add(EntityType.GHAST)
			.add(EntityType.GIANT)
			.add(EntityType.GUARDIAN)
			.add(EntityType.HOGLIN)
			.add(EntityType.HUSK)
			.add(EntityType.MAGMA_CUBE)
			.add(EntityType.PHANTOM)
			.add(EntityType.PIGLIN)
			.add(EntityType.PIGLIN_BRUTE)
			.add(EntityType.SHULKER)
			.add(EntityType.SILVERFISH)
			.add(EntityType.SKELETON_HORSE)
			.add(EntityType.SLIME)
			.add(EntityType.SPIDER)
			.add(EntityType.VEX)
			.add(EntityType.WARDEN)
			.add(EntityType.WITCH)
			.add(EntityType.WITHER)
			.add(EntityType.ZOGLIN)
			.add(EntityType.ZOMBIE)
			.add(EntityType.ZOMBIE_HORSE)
			.add(EntityType.ZOMBIE_VILLAGER)
			.add(EntityType.ZOMBIFIED_PIGLIN);
		
		tag(Reference.RIDEABLE_TAG)
			.add(EntityType.BOAT)
			.add(EntityType.DONKEY)
			.add(EntityType.HORSE)
			.add(EntityType.MULE)
			.add(EntityType.PIG)
			.add(EntityType.SKELETON_HORSE)
			.add(EntityType.STRIDER);
		
		tag(Reference.TAMABLE_TAG)
			.add(EntityType.ALLAY)
			.add(EntityType.CAT)
			.add(EntityType.DONKEY)
			.add(EntityType.HORSE)
			.add(EntityType.LLAMA)
			.add(EntityType.MULE)
			.add(EntityType.OCELOT)
			.add(EntityType.SKELETON_HORSE)
			.add(EntityType.TRADER_LLAMA)
			.add(EntityType.WOLF);
	}
}
