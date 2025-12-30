package harmonised.pmmo.core.perks;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.PerkSide;
import harmonised.pmmo.features.fireworks.FireworkHandler;
import harmonised.pmmo.util.Reference;
import net.minecraft.resources.ResourceLocation;

public class PerkRegistration {
	public static void init() {
		//Default Feature Perks
		APIUtils.registerPerk(rl("break_speed"), PerksImpl.BREAK_SPEED, PerkSide.BOTH, null);
		APIUtils.registerPerk(rl("fireworks"), FireworkHandler.FIREWORK, PerkSide.SERVER, null);
		APIUtils.registerPerk(rl("attribute"), FeaturePerks.ATTRIBUTE, PerkSide.SERVER, PerkRenderers.Attribute);
		APIUtils.registerPerk(rl("temp_attribute"),FeaturePerks.TEMP_ATTRIBUTE, PerkSide.SERVER, null);
		//Event Perks
		APIUtils.registerPerk(rl("jump_boost"), FeaturePerks.JUMP_CLIENT, PerkSide.CLIENT, null);
		APIUtils.registerPerk(rl("jump_boost"), FeaturePerks.JUMP_SERVER, PerkSide.SERVER, null);
		
		APIUtils.registerPerk(rl("breath"), FeaturePerks.BREATH, PerkSide.SERVER, null);
		APIUtils.registerPerk(rl("damage_reduce"), FeaturePerks.DAMAGE_REDUCE, PerkSide.SERVER, null);
		APIUtils.registerPerk(rl("damage_boost"),FeaturePerks.DAMAGE_BOOST, PerkSide.SERVER, null);
		APIUtils.registerPerk(rl("command"), FeaturePerks.RUN_COMMAND, PerkSide.SERVER, null);
		APIUtils.registerPerk(rl("villager_boost"),FeaturePerks.VILLAGER_TRADING, PerkSide.SERVER, null);
		//Effect Perks
		APIUtils.registerPerk(rl("effect"), FeaturePerks.EFFECT, PerkSide.SERVER, null);
		APIUtils.registerPerk(rl("tame_boost"), PerksImpl.TAME_BOOST, PerkSide.SERVER, null);
	}
	
	private static ResourceLocation rl(String str) {
		return Reference.rl(str);
	}
}
