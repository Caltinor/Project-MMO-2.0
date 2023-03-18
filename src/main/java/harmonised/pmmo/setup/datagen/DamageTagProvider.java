package harmonised.pmmo.setup.datagen;

import java.util.concurrent.CompletableFuture;

import harmonised.pmmo.util.Reference;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraftforge.common.data.ExistingFileHelper;

public class DamageTagProvider extends TagsProvider<DamageType>{

	public DamageTagProvider(PackOutput output, CompletableFuture<Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
		super(output, Registries.DAMAGE_TYPE, lookupProvider, Reference.MOD_ID, existingFileHelper);
	}

	@Override
	protected void addTags(Provider provider) {
		tag(Reference.FROM_ENVIRONMENT)
			.addTag(DamageTypeTags.IS_DROWNING)
			.addTag(DamageTypeTags.IS_EXPLOSION)
			.addTag(DamageTypeTags.IS_FIRE)
			.addTag(DamageTypeTags.IS_FREEZING)
			.addTag(DamageTypeTags.IS_LIGHTNING)
			.add(DamageTypes.CACTUS)
			.add(DamageTypes.CRAMMING)
			.add(DamageTypes.DROWN)
			.add(DamageTypes.FALLING_ANVIL)
			.add(DamageTypes.FALLING_BLOCK)
			.add(DamageTypes.FREEZE)
			.add(DamageTypes.IN_FIRE)
			.add(DamageTypes.LIGHTNING_BOLT)
			.add(DamageTypes.ON_FIRE)
			.add(DamageTypes.LAVA)
			.add(DamageTypes.HOT_FLOOR)
			.add(DamageTypes.IN_WALL)
			.add(DamageTypes.STARVE)
			.add(DamageTypes.SWEET_BERRY_BUSH);
		
		tag(Reference.FROM_IMPACT)
			.add(DamageTypes.FALL)
			.add(DamageTypes.STALAGMITE)
			.add(DamageTypes.FLY_INTO_WALL);
		
		tag(Reference.FROM_MAGIC)
			.add(DamageTypes.MAGIC)
			.add(DamageTypes.INDIRECT_MAGIC);
		
		tag(Reference.FROM_RANGED)
			.addTag(DamageTypeTags.IS_PROJECTILE)
			.add(DamageTypes.ARROW)
			.add(DamageTypes.FIREBALL)
			.add(DamageTypes.MOB_PROJECTILE)
			.add(DamageTypes.THROWN)
			.add(DamageTypes.TRIDENT)
			.add(DamageTypes.WITHER_SKULL);
		
		tag(Reference.FROM_PLAYER)
			.add(DamageTypes.PLAYER_ATTACK);
		
		tag(Reference.FROM_MELEE)
			.add(DamageTypes.MOB_ATTACK);
	}
}
