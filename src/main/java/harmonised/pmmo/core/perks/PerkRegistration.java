package harmonised.pmmo.core.perks;

import org.apache.commons.lang3.function.TriFunction;

import harmonised.pmmo.features.fireworks.FireworkHandler;
import harmonised.pmmo.impl.PerkRegistry;
import harmonised.pmmo.util.Reference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class PerkRegistration {
	public static void init() {
		//Default Feature Perks
		PerkRegistry.registerPerk(rl("break_speed"), PerksImpl.BREAK_SPEED, NONE);
		PerkRegistry.registerPerk(rl("fireworks"), FireworkHandler.FIREWORKS, NONE);
		//Attribute Perks
		PerkRegistry.registerPerk(rl("reach"), FeaturePerks.REACH, FeaturePerks.REACH_TERM);
		PerkRegistry.registerPerk(rl("damage"), FeaturePerks.DAMAGE, FeaturePerks.DAMAGE_TERM);
		PerkRegistry.registerPerk(rl("speed"), FeaturePerks.SPEED, FeaturePerks.SPEED_TERM);
		PerkRegistry.registerPerk(rl("health"), FeaturePerks.HEALTH, FeaturePerks.HEALTH_TERM);
		//Event Perks
		PerkRegistry.registerPerk(rl("jump_boost"), FeaturePerks.JUMP, NONE);
		PerkRegistry.registerPerk(rl("breath"), FeaturePerks.BREATH, NONE);
		PerkRegistry.registerPerk(rl("fall_save"), FeaturePerks.FALL_SAVE, NONE);
		PerkRegistry.registerPerk(rl("damage_boost"), FeaturePerks.DAMAGE_BOOST, NONE);
		//Effect Perks
		PerkRegistry.registerPerk(rl("night_vision"), FeaturePerks.NIGHT_VISION, FeaturePerks.NIGHT_VISION_TERM);
		PerkRegistry.registerPerk(rl("regen"), FeaturePerks.REGEN, NONE);
	}
	
	private static TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> NONE = (a,b,c) -> {return new CompoundTag();};
	
	private static ResourceLocation rl(String str) {
		return new ResourceLocation(Reference.MOD_ID, str);
	}
}
