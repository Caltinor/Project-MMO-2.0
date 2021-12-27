package harmonised.pmmo.perks;

import org.apache.logging.log4j.util.TriConsumer;

import harmonised.pmmo.api.perks.PerkRegistry;
import harmonised.pmmo.util.Reference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class PerkRegistration {
	public static void init() {
		//Attribute Perks
		PerkRegistry.registerPerk(rl("reach"), AttributePerks.REACH, NONE);
		PerkRegistry.registerPerk(rl("damage"), AttributePerks.DAMAGE, NONE);
		PerkRegistry.registerPerk(rl("speed"), AttributePerks.SPEED, AttributePerks.SPEED_TERM);
		PerkRegistry.registerPerk(rl("health"), AttributePerks.HEALTH, NONE);
		//Event Perks
		PerkRegistry.registerPerk(rl("jump_boost"), EventPerks.JUMP, NONE);
		//Effect Perks
		PerkRegistry.registerPerk(rl("night_vision"), EffectPerks.NIGHT_VISION, EffectPerks.NIGHT_VISION_TERM);
	}
	
	private static TriConsumer<ServerPlayer, CompoundTag, Integer> NONE = (a,b,c) -> {};
	
	private static ResourceLocation rl(String str) {
		return new ResourceLocation(Reference.MOD_ID, str);
	}
/* night vision				-Effect
 * fall save				-Event
 * regen					-Effect
 * longer breath			-Event
 */
}
