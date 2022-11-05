package harmonised.pmmo.core.perks;

import org.apache.commons.lang3.function.TriFunction;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.enums.PerkSide;
import harmonised.pmmo.features.fireworks.FireworkHandler;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.TagBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.TriPredicate;

public class PerkRegistration {
	public static void init() {
		//Default Feature Perks
		APIUtils.registerPerk(rl("break_speed"),
				PerksImpl.getDefaults(),
				PASS_TRUE,
				PerksImpl.BREAK_SPEED, 
				NONE, 
				PerkSide.BOTH);
		
		APIUtils.registerPerk(rl("fireworks"), 
				TagBuilder.start().withString(FireworkHandler.FIREWORK_SKILL, "none").withString(APIUtils.SKILLNAME, "none").build(),
				PASS_TRUE,
				FireworkHandler.FIREWORKS, 
				NONE, 
				PerkSide.SERVER);
		//Attribute Perks
		APIUtils.registerPerk(rl("reach"),
				TagBuilder.start().withDouble(APIUtils.MAX_BOOST, 10d).withDouble(APIUtils.PER_LEVEL, 0.1).build(),
				PASS_TRUE,
				FeaturePerks.REACH, 
				FeaturePerks.REACH_TERM, 
				PerkSide.SERVER);
		APIUtils.registerPerk(rl("damage"), 
				TagBuilder.start().withDouble(APIUtils.MAX_BOOST, 1d).withDouble(APIUtils.PER_LEVEL, 0.005).build(),
				PASS_TRUE,
				FeaturePerks.DAMAGE, 
				FeaturePerks.DAMAGE_TERM, 
				PerkSide.SERVER);
		APIUtils.registerPerk(rl("speed"),
				TagBuilder.start().withDouble(APIUtils.MAX_BOOST, 1d).withDouble(APIUtils.PER_LEVEL, 0.005).build(),
				PASS_TRUE,
				FeaturePerks.SPEED, 
				FeaturePerks.SPEED_TERM, 
				PerkSide.SERVER);
		APIUtils.registerPerk(rl("health"),
				TagBuilder.start().withDouble(APIUtils.MAX_BOOST, 10d).withDouble(APIUtils.PER_LEVEL, 0.05).build(),
				PASS_TRUE,
				FeaturePerks.HEALTH, 
				FeaturePerks.HEALTH_TERM, 
				PerkSide.SERVER);
		//Event Perks
		APIUtils.registerPerk(rl("jump_boost"),
				TagBuilder.start().withDouble(APIUtils.PER_LEVEL, 0.0005).withDouble(APIUtils.MAX_BOOST, 0.033).build(),
				PASS_TRUE,
				FeaturePerks.JUMP_CLIENT, 
				NONE, 
				PerkSide.CLIENT);
		APIUtils.registerPerk(rl("jump_boost"), 
				TagBuilder.start().withDouble(APIUtils.PER_LEVEL, 0.0005).withDouble(APIUtils.MAX_BOOST, 0.033).build(),
				PASS_TRUE,
				FeaturePerks.JUMP_SERVER, 
				NONE, 
				PerkSide.SERVER);
		
		APIUtils.registerPerk(rl("breath"),
				TagBuilder.start().withLong(APIUtils.COOLDOWN, 300l).withDouble(APIUtils.PER_LEVEL, 1d).build(),
				FeaturePerks.BREATH_CHECK,
				FeaturePerks.BREATH, 
				NONE, 
				PerkSide.SERVER);
		APIUtils.registerPerk(rl("fall_save"), 
				TagBuilder.start().withDouble(APIUtils.PER_LEVEL, 0.025).withFloat(APIUtils.DAMAGE_IN, 0).build(),
				PASS_TRUE,
				FeaturePerks.FALL_SAVE, NONE, PerkSide.SERVER);
		APIUtils.registerPerk(rl("damage_boost"),
				TagBuilder.start()
					.withFloat(APIUtils.DAMAGE_IN, 0)
					.withList(FeaturePerks.APPLICABLE_TO, StringTag.valueOf("weapon:id"))
					.withDouble(APIUtils.PER_LEVEL, 0.05).build(),
				FeaturePerks.DAMAGE_BOOST_CHECK,
				FeaturePerks.DAMAGE_BOOST, 
				NONE, 
				PerkSide.SERVER);
		APIUtils.registerPerk(rl("command"), 
				new CompoundTag(), //TODO update the perk to show these and not use null as a conditional
				PASS_TRUE,
				FeaturePerks.RUN_COMMAND, 
				NONE, 
				PerkSide.SERVER);
		//Effect Perks
		APIUtils.registerPerk(rl("night_vision"),
				TagBuilder.start().withInt(APIUtils.DURATION, 100).build(),
				FeaturePerks.NIGHT_VISION_CHECK,
				FeaturePerks.NIGHT_VISION, 
				NONE, 
				PerkSide.SERVER);
		APIUtils.registerPerk(rl("regen"),
				TagBuilder.start()
					.withLong(APIUtils.COOLDOWN, 300l)
					.withInt(APIUtils.DURATION, 1)
					.withDouble(APIUtils.PER_LEVEL, 0.02).build(),
				PASS_TRUE,
				FeaturePerks.REGEN, 
				NONE, 
				PerkSide.SERVER);
		APIUtils.registerPerk(rl("effect"), 
				TagBuilder.start()
					.withString(FeaturePerks.EFFECT, "modid:effect")
					.withInt(APIUtils.PER_LEVEL, 20)
					.withInt(APIUtils.MODIFIER, 0)
					.withBool(APIUtils.AMBIENT, false)
					.withBool(APIUtils.VISIBLE, true).build(),
				PASS_TRUE,
				FeaturePerks.GIVE_EFFECT, 
				NONE, 
				PerkSide.SERVER);
	}
	
	private static TriFunction<Player, CompoundTag, Integer, CompoundTag> NONE = (a,b,c) -> {return new CompoundTag();};
	private static TriPredicate<Player, CompoundTag, Integer> PASS_TRUE = (a,b,c) -> true;
	
	private static ResourceLocation rl(String str) {
		return new ResourceLocation(Reference.MOD_ID, str);
	}
}
