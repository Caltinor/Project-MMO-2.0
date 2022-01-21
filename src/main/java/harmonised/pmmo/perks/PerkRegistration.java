package harmonised.pmmo.perks;

import org.apache.commons.lang3.function.TriFunction;
import harmonised.pmmo.api.perks.PerkRegistry;
import harmonised.pmmo.util.Reference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class PerkRegistration {
	public static void init() {
		//Attribute Perks
		PerkRegistry.registerPerk(rl("reach"), AttributePerks.REACH, AttributePerks.REACH_TERM);
		PerkRegistry.registerPerk(rl("damage"), AttributePerks.DAMAGE, AttributePerks.DAMAGE_TERM);
		PerkRegistry.registerPerk(rl("speed"), AttributePerks.SPEED, AttributePerks.SPEED_TERM);
		PerkRegistry.registerPerk(rl("health"), AttributePerks.HEALTH, AttributePerks.HEALTH_TERM);
		//Event Perks
		PerkRegistry.registerPerk(rl("jump_boost"), EventPerks.JUMP, NONE);
		PerkRegistry.registerPerk(rl("breath"), EventPerks.BREATH, NONE);
		PerkRegistry.registerPerk(rl("fall_save"), EventPerks.FALL_SAVE, NONE);
		PerkRegistry.registerPerk(rl("damage_boost"), EventPerks.DAMAGE_BOOST, NONE);
		//Effect Perks
		PerkRegistry.registerPerk(rl("night_vision"), EffectPerks.NIGHT_VISION, EffectPerks.NIGHT_VISION_TERM);
		PerkRegistry.registerPerk(rl("regen"), EffectPerks.REGEN, NONE);
	}
	
	private static TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> NONE = (a,b,c) -> {return new CompoundTag();};
	
	private static ResourceLocation rl(String str) {
		return new ResourceLocation(Reference.MOD_ID, str);
	}
}
