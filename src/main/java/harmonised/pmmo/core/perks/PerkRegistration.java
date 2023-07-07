package harmonised.pmmo.core.perks;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.PerkSide;
import harmonised.pmmo.features.fireworks.FireworkHandler;
import harmonised.pmmo.util.Reference;
import net.minecraft.resources.ResourceLocation;

public class PerkRegistration {
	public static void init() {
		//Default Feature Perks
		APIUtils.registerPerk(rl("break_speed"), PerksImpl.BREAK_SPEED, PerkSide.BOTH);
		APIUtils.registerPerk(rl("fireworks"), FireworkHandler.FIREWORK, PerkSide.SERVER);
		APIUtils.registerPerk(rl("attribute"), FeaturePerks.ATTRIBUTE, PerkSide.SERVER);
		APIUtils.registerPerk(rl("temp_attribute"),FeaturePerks.TEMP_ATTRIBUTE, PerkSide.SERVER);
		//Event Perks
		APIUtils.registerPerk(rl("jump_boost"), FeaturePerks.JUMP_CLIENT, PerkSide.CLIENT);
		APIUtils.registerPerk(rl("jump_boost"), FeaturePerks.JUMP_SERVER, PerkSide.SERVER);
		
		APIUtils.registerPerk(rl("breath"), FeaturePerks.BREATH, PerkSide.SERVER);
		APIUtils.registerPerk(rl("fall_save"), FeaturePerks.FALL_SAVE, PerkSide.SERVER);
		APIUtils.registerPerk(rl("damage_boost"),FeaturePerks.DAMAGE_BOOST, PerkSide.SERVER);
		APIUtils.registerPerk(rl("command"), FeaturePerks.RUN_COMMAND, PerkSide.SERVER);
		//Effect Perks
		APIUtils.registerPerk(rl("effect"), FeaturePerks.EFFECT, PerkSide.SERVER);
		APIUtils.registerPerk(rl("tame_boost"), PerksImpl.TAME_BOOST, PerkSide.SERVER);
	}
	
	private static ResourceLocation rl(String str) {
		return new ResourceLocation(Reference.MOD_ID, str);
	}
}
