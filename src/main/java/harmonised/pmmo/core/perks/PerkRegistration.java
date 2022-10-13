package harmonised.pmmo.core.perks;

import org.apache.commons.lang3.function.TriFunction;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.PerkSide;
import harmonised.pmmo.features.fireworks.FireworkHandler;
import harmonised.pmmo.util.Reference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class PerkRegistration {
	public static void init() {
		//Default Feature Perks
		APIUtils.registerPerk(rl("break_speed"), PerksImpl.BREAK_SPEED, NONE, PerkSide.BOTH);
		
		APIUtils.registerPerk(rl("fireworks"), FireworkHandler.FIREWORKS, NONE, PerkSide.SERVER);
		//Attribute Perks
		APIUtils.registerPerk(rl("reach"), FeaturePerks.REACH, FeaturePerks.REACH_TERM, PerkSide.SERVER);
		APIUtils.registerPerk(rl("damage"), FeaturePerks.DAMAGE, FeaturePerks.DAMAGE_TERM, PerkSide.SERVER);
		APIUtils.registerPerk(rl("speed"), FeaturePerks.SPEED, FeaturePerks.SPEED_TERM, PerkSide.SERVER);
		APIUtils.registerPerk(rl("health"), FeaturePerks.HEALTH, FeaturePerks.HEALTH_TERM, PerkSide.SERVER);
		//Event Perks
		APIUtils.registerPerk(rl("jump_boost"), FeaturePerks.JUMP_CLIENT, NONE, PerkSide.CLIENT);
		APIUtils.registerPerk(rl("jump_boost"), FeaturePerks.JUMP_SERVER, NONE, PerkSide.SERVER);
		
		APIUtils.registerPerk(rl("breath"), FeaturePerks.BREATH, NONE, PerkSide.SERVER);
		APIUtils.registerPerk(rl("fall_save"), FeaturePerks.FALL_SAVE, NONE, PerkSide.SERVER);
		APIUtils.registerPerk(rl("damage_boost"), FeaturePerks.DAMAGE_BOOST, NONE, PerkSide.SERVER);
		APIUtils.registerPerk(rl("command"), FeaturePerks.RUN_COMMAND, NONE, PerkSide.SERVER);
		//Effect Perks
		APIUtils.registerPerk(rl("night_vision"), FeaturePerks.NIGHT_VISION, NONE, PerkSide.SERVER);
		APIUtils.registerPerk(rl("regen"), FeaturePerks.REGEN, NONE, PerkSide.SERVER);
		APIUtils.registerPerk(rl("effect"), FeaturePerks.GIVE_EFFECT, NONE, PerkSide.SERVER);
	}
	
	private static TriFunction<Player, CompoundTag, Integer, CompoundTag> NONE = (a,b,c) -> {return new CompoundTag();};
	
	private static ResourceLocation rl(String str) {
		return new ResourceLocation(Reference.MOD_ID, str);
	}
}
