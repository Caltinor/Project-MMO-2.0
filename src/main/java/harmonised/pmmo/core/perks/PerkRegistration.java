package harmonised.pmmo.core.perks;

import org.apache.commons.lang3.function.TriFunction;

import harmonised.pmmo.api.enums.PerkSide;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.features.fireworks.FireworkHandler;
import harmonised.pmmo.util.Reference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.LogicalSide;

public class PerkRegistration {
	public static void init() {
		//Default Feature Perks
		Core.get(LogicalSide.SERVER).getPerkRegistry().registerPerk(rl("break_speed"), PerksImpl.BREAK_SPEED, NONE, PerkSide.BOTH);
		Core.get(LogicalSide.CLIENT).getPerkRegistry().registerPerk(rl("break_speed"), PerksImpl.BREAK_SPEED, NONE, PerkSide.BOTH);
		
		Core.get(LogicalSide.SERVER).getPerkRegistry().registerPerk(rl("fireworks"), FireworkHandler.FIREWORKS, NONE, PerkSide.SERVER);
		//Attribute Perks
		Core.get(LogicalSide.SERVER).getPerkRegistry().registerPerk(rl("reach"), FeaturePerks.REACH, FeaturePerks.REACH_TERM, PerkSide.SERVER);
		Core.get(LogicalSide.SERVER).getPerkRegistry().registerPerk(rl("damage"), FeaturePerks.DAMAGE, FeaturePerks.DAMAGE_TERM, PerkSide.SERVER);
		Core.get(LogicalSide.SERVER).getPerkRegistry().registerPerk(rl("speed"), FeaturePerks.SPEED, FeaturePerks.SPEED_TERM, PerkSide.SERVER);
		Core.get(LogicalSide.SERVER).getPerkRegistry().registerPerk(rl("health"), FeaturePerks.HEALTH, FeaturePerks.HEALTH_TERM, PerkSide.SERVER);
		//Event Perks
		Core.get(LogicalSide.SERVER).getPerkRegistry().registerPerk(rl("jump_boost"), FeaturePerks.JUMP, NONE, PerkSide.BOTH);
		Core.get(LogicalSide.CLIENT).getPerkRegistry().registerPerk(rl("jump_boost"), FeaturePerks.JUMP, NONE, PerkSide.BOTH);
		
		Core.get(LogicalSide.SERVER).getPerkRegistry().registerPerk(rl("breath"), FeaturePerks.BREATH, NONE, PerkSide.SERVER);
		Core.get(LogicalSide.SERVER).getPerkRegistry().registerPerk(rl("fall_save"), FeaturePerks.FALL_SAVE, NONE, PerkSide.SERVER);
		Core.get(LogicalSide.SERVER).getPerkRegistry().registerPerk(rl("damage_boost"), FeaturePerks.DAMAGE_BOOST, NONE, PerkSide.SERVER);
		//Effect Perks
		Core.get(LogicalSide.SERVER).getPerkRegistry().registerPerk(rl("night_vision"), FeaturePerks.NIGHT_VISION, FeaturePerks.NIGHT_VISION_TERM, PerkSide.SERVER);
		Core.get(LogicalSide.SERVER).getPerkRegistry().registerPerk(rl("regen"), FeaturePerks.REGEN, NONE, PerkSide.SERVER);
	}
	
	private static TriFunction<Player, CompoundTag, Integer, CompoundTag> NONE = (a,b,c) -> {return new CompoundTag();};
	
	private static ResourceLocation rl(String str) {
		return new ResourceLocation(Reference.MOD_ID, str);
	}
}
