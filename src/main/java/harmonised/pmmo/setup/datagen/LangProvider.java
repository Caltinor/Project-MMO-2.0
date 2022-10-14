package harmonised.pmmo.setup.datagen;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.Reference;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.fml.LogicalSide;

public class LangProvider extends LanguageProvider{
	private String locale;
	
	private enum Locale {
		EN_US("en_us");
		
		public String str;
		Locale(String locale) {str = locale;}
	}

	public LangProvider(DataGenerator gen, String locale) {
		super(gen, Reference.MOD_ID, locale);
		this.locale = locale;
	}
	
	static final List<Translation> impliedTranslations = new ArrayList<>();
	static {
		//=========PROVIDED SKILLS================
		addEN_US("pmmo.power", "Power");
		addEN_US("pmmo.mining", "Mining");
		addEN_US("pmmo.building", "Building");
		addEN_US("pmmo.excavation", "Excavation");
		addEN_US("pmmo.woodcutting", "Woodcutting");
		addEN_US("pmmo.farming", "Farming");
		addEN_US("pmmo.agility", "Agility");
		addEN_US("pmmo.endurance", "Endurance");
		addEN_US("pmmo.combat", "Combat");
		addEN_US("pmmo.archery", "Archery");
		addEN_US("pmmo.smithing", "Smithing");
		addEN_US("pmmo.flying", "Flying");
		addEN_US("pmmo.swimming", "Swimming");
		addEN_US("pmmo.fishing", "Fishing");
		addEN_US("pmmo.crafting", "Crafting");
		addEN_US("pmmo.magic", "Magic");
		addEN_US("pmmo.gunslinging", "Gunslinging");
		addEN_US("pmmo.slayer", "Slayer");
		addEN_US("pmmo.fletching", "Fletching");
		addEN_US("pmmo.taming", "Taming");
		addEN_US("pmmo.hunter", "Hunter");
		addEN_US("pmmo.engineering", "Engineering");
		addEN_US("pmmo.blood_magic", "Blood Magic");
		addEN_US("pmmo.astral_magic", "Astral Magic");
		addEN_US("pmmo.good_magic", "Good Magic");
		addEN_US("pmmo.evil_magic", "Evil Magic");
		addEN_US("pmmo.arcane_magic", "Arcane Magic");
		addEN_US("pmmo.elemental", "Elemental");
		addEN_US("pmmo.earth", "Earth");
		addEN_US("pmmo.water", "Water");
		addEN_US("pmmo.air", "Air");
		addEN_US("pmmo.fire", "Fire");
		addEN_US("pmmo.lightning", "Lightning");
		addEN_US("pmmo.void", "Void");
		addEN_US("pmmo.thaumatic", "Thaumatic");
		addEN_US("pmmo.summoning", "Summoning");
		addEN_US("pmmo.invention", "Invention");
		addEN_US("pmmo.runecrafting", "Runecrafting");
		addEN_US("pmmo.prayer", "Prayer");
		addEN_US("pmmo.cooking", "Cooking");
		addEN_US("pmmo.firemaking", "Firemaking");
		addEN_US("pmmo.afking", "Afking");
		addEN_US("pmmo.trading", "Trading");
		addEN_US("pmmo.sailing", "Sailing");
		addEN_US("pmmo.alchemy", "Alchemy");
		addEN_US("pmmo.construction", "Construction");
		addEN_US("pmmo.leatherworking", "Leatherworking");
		addEN_US("pmmo.exploration", "Exploration");
		addEN_US("pmmo.charisma","Charisma");
		//=========ENUM VALUES=====================
		addEN_US("pmmo.enum.ANVIL_REPAIR","Anvil Repair");
		addEN_US("pmmo.enum.BLOCK_BREAK","Break Block");
		addEN_US("pmmo.enum.BREAK_SPEED","Break Speed");
		addEN_US("pmmo.enum.BLOCK_PLACE","Place Block");
		addEN_US("pmmo.enum.BREATH_CHANGE","Breath Change");
		addEN_US("pmmo.enum.BREED","Breed");
		addEN_US("pmmo.enum.BREW","Brew");
		addEN_US("pmmo.enum.CRAFT","Craft");
		addEN_US("pmmo.enum.CONSUME","Eat/Drink");
		addEN_US("pmmo.enum.RECEIVE_DAMAGE","Receive Damage (Unspecified)");
		addEN_US("pmmo.enum.FROM_MOBS","Receive Mob Damage");
		addEN_US("pmmo.enum.FROM_PLAYERS","Receive Player Damage");
		addEN_US("pmmo.enum.FROM_ANIMALS","Receive Animal Damage");
		addEN_US("pmmo.enum.FROM_PROJECTILES","Receive Projectile Damage");
		addEN_US("pmmo.enum.FROM_MAGIC","Receive Magic Damage");
		addEN_US("pmmo.enum.FROM_ENVIRONMENT","Receive Environmental Damage");
		addEN_US("pmmo.enum.FROM_IMPACT","Receive Impact Damage");
		addEN_US("pmmo.enum.DEAL_MELEE_DAMAGE","Deal Melee Damage (Unspecified)");
		addEN_US("pmmo.enum.MELEE_TO_MOBS","Deal Melee Damage to Mobs");
		addEN_US("pmmo.enum.MELEE_TO_PLAYERS","Deal Melee Damage to Players");
		addEN_US("pmmo.enum.MELEE_TO_ANIMALS","Deal Melee Damage to Animals");
		addEN_US("pmmo.enum.DEAL_RANGED_DAMAGE","Deal Ranged Damage (Unspecified)");
		addEN_US("pmmo.enum.RANGED_TO_MOBS","Deal Ranged Damage to Mobs");
		addEN_US("pmmo.enum.RANGED_TO_PLAYERS","Deal Ranged Damage to Players");
		addEN_US("pmmo.enum.RANGED_TO_ANIMALS","Deal Ranged Damage to Animals");
		addEN_US("pmmo.enum.DEATH","Death");
		addEN_US("pmmo.enum.ENCHANT","Enchant");
		addEN_US("pmmo.enum.FISH","Fish");
		addEN_US("pmmo.enum.SMELT","Smelt/Cook");
		addEN_US("pmmo.enum.GROW","Grow");
		addEN_US("pmmo.enum.HEALTH_CHANGE","Health Change");
		addEN_US("pmmo.enum.JUMP","Jump");
		addEN_US("pmmo.enum.SPRINT_JUMP","Sprint Jump");
		addEN_US("pmmo.enum.CROUCH_JUMP","Crouch Jump");
		addEN_US("pmmo.enum.WORLD_CONNECT","World Connect");
		addEN_US("pmmo.enum.WORLD_DISCONNECT","World Disconnect");
		addEN_US("pmmo.enum.HIT_BLOCK","Hit Block");
		addEN_US("pmmo.enum.ACTIVATE_BLOCK","Activate Block");
		addEN_US("pmmo.enum.ACTIVATE_ITEM","Activate Item");
		addEN_US("pmmo.enum.ENTITY","Interact with Entity");
		addEN_US("pmmo.enum.RESPAWN","Respawn");
		addEN_US("pmmo.enum.RIDING","Riding");
		addEN_US("pmmo.enum.SHIELD_BLOCK","Block with Shield");
		addEN_US("pmmo.enum.SKILL_UP","Level Up");
		addEN_US("pmmo.enum.SLEEP","Sleep");
		addEN_US("pmmo.enum.SPRINTING","Sprinting");
		addEN_US("pmmo.enum.SUBMERGED","Submerged");
		addEN_US("pmmo.enum.SWIMMING","Swimming (above surface)");
		addEN_US("pmmo.enum.DIVING","Diving");
		addEN_US("pmmo.enum.SURFACING","Surfacing");
		addEN_US("pmmo.enum.SWIM_SPRINTING","Fast Swimming");
		addEN_US("pmmo.enum.TAMING","Taming");
		addEN_US("pmmo.enum.VEIN_MINE","Vein Mining");
		addEN_US("pmmo.enum.DISABLE_PERK","Disable Perk");
		
		addEN_US("pmmo.enum.WEAR","Wear Item");
		addEN_US("pmmo.enum.USE_ENCHANTMENT","Use Enchantment");
		addEN_US("pmmo.enum.TOOL","Use as Tool");
		addEN_US("pmmo.enum.WEAPON","Use as Weapon");
		addEN_US("pmmo.enum.USE","Activate Item Ability");
		addEN_US("pmmo.enum.PLACE","Place Block");
		addEN_US("pmmo.enum.BREAK","Break Block");
		//addEN_US("pmmo.enum.BIOME","Enter Biome");
		addEN_US("pmmo.enum.KILL","Kill Entity");
		addEN_US("pmmo.enum.TRAVEL","Travel to");
		addEN_US("pmmo.enum.RIDE","Ride/Drive");
		addEN_US("pmmo.enum.TAME","Tame Animal");
		addEN_US("pmmo.enum.INTERACT","Interact with Block");
		addEN_US("pmmo.enum.ENTITY_INTERACT","Interact with Entity");
		
		addEN_US("pmmo.enum.BIOME","Biome");
	    addEN_US("pmmo.enum.HELD","Held In Hand");
	    addEN_US("pmmo.enum.WORN","Worn");
	    addEN_US("pmmo.enum.DIMENSION","Dimension");
	}

	//=========KEY BINDINGS====================
	public static final Translation KEYBIND_CATEGORY = Translation.Builder.start("category.pmmo")
			.addLocale(Locale.EN_US, "Project MMO").build();
	public static final Translation KEYBIND_SHOWVEIN = Translation.Builder.start("key.pmmo.showVein")
			.addLocale(Locale.EN_US, "Toggle Vein Gauge").build();
	public static final Translation KEYBIND_ADDVEIN = Translation.Builder.start("key.pmmo.addVein")
			.addLocale(Locale.EN_US, "Increase Vein Size").build();
	public static final Translation KEYBIND_SUBVEIN = Translation.Builder.start("key.pmmo.subVein")
			.addLocale(Locale.EN_US, "Decrease Vein Size").build();
	public static final Translation KEYBIND_VEINCYCLE = Translation.Builder.start("key.pmmo.cyclevein")
			.addLocale(Locale.EN_US, "Cycle Vein Mode").build();
	public static final Translation KEYBIND_SHOWLIST = Translation.Builder.start("key.pmmo.showList")
			.addLocale(Locale.EN_US, "Toggle Skill List").build();
	public static final Translation KEYBIND_VEIN = Translation.Builder.start("key.pmmo.vein")
			.addLocale(Locale.EN_US, "Vein Mine Marker").build();
	public static final Translation KEYBIND_OPENMENU = Translation.Builder.start("key.pmmo.openMenu")
			.addLocale(Locale.EN_US, "Open Glossary").build();
	
	//=========LOGIN HANDLER===================
	public static final Translation WELCOME_TEXT = Translation.Builder.start("pmmo.welcomeText")
			.addLocale(Locale.EN_US, "Welcome! Project MMO is more fun with datapacks. download one %s").build();
	public static final Translation CLICK_ME = Translation.Builder.start("pmmo.clickMe")
			.addLocale(Locale.EN_US, "HERE").build();
	
	//=========KEY PRESS HANLDER===============
	public static final Translation VEIN_BLACKLIST = Translation.Builder.start("pmmo.veinBlacklist")
			.addLocale(Locale.EN_US, "Blacklisted blocks from Veining").build();
	public static final Translation VEIN_SHAPE = Translation.Builder.start("pmmo.veinshape")
			.addLocale(Locale.EN_US, "Vein Shape Set To: %s").build();
	
	//=========TOOLTIP HEADERS=================
	public static final Translation REQ_WEAR = Translation.Builder.start("pmmo.toWear")
			.addLocale(Locale.EN_US, "To Wear").build();
	public static final Translation REQ_TOOL = Translation.Builder.start("pmmo.tool")
			.addLocale(Locale.EN_US, "Tool").build();
	public static final Translation REQ_WEAPON = Translation.Builder.start("pmmo.weapon")
			.addLocale(Locale.EN_US, "Weapon").build();
	public static final Translation REQ_USE = Translation.Builder.start("pmmo.use")
			.addLocale(Locale.EN_US, "Use").build();
	public static final Translation REQ_PLACE = Translation.Builder.start("pmmo.place")
			.addLocale(Locale.EN_US, "To Place").build();
	public static final Translation REQ_ENCHANT = Translation.Builder.start("pmmo.use_enchant")
			.addLocale(Locale.EN_US, "Use Enchantment").build();
	public static final Translation REQ_BREAK = Translation.Builder.start("pmmo.break")
			.addLocale(Locale.EN_US, "To Break").build();
	public static final Translation XP_VALUE_BREAK = Translation.Builder.start("pmmo.xpValueBreak")
			.addLocale(Locale.EN_US, "Break Xp Value").build();
	public static final Translation XP_VALUE_CRAFT = Translation.Builder.start("pmmo.xpValueCraft")
			.addLocale(Locale.EN_US, "Craft Xp Value").build();
	public static final Translation XP_VALUE_SMELT = Translation.Builder.start("pmmo.xpValueSmelt")
			.addLocale(Locale.EN_US, "Smelt Xp Value").build();
	public static final Translation XP_VALUE_BREW = Translation.Builder.start("pmmo.xpValueBrew")
			.addLocale(Locale.EN_US, "Brew Xp Value").build();
	public static final Translation XP_VALUE_GROW = Translation.Builder.start("pmmo.xpValueGrow")
			.addLocale(Locale.EN_US, "Grow Xp Value").build();
	public static final Translation XP_VALUE_PLACE = Translation.Builder.start("pmmo.xpValuePlace")
			.addLocale(Locale.EN_US, "Place Xp Value").build();
	public static final Translation BOOST_HELD = Translation.Builder.start("pmmo.itemXpBoostHeld")
			.addLocale(Locale.EN_US, "Xp Boost In Hand").build();
	public static final Translation BOOST_WORN = Translation.Builder.start("pmmo.itemXpBoostWorn")
			.addLocale(Locale.EN_US, "Xp Boost Worn").build();
	public static final Translation VEIN_TOOLTIP = Translation.Builder.start("pmmo.veintooltip")
			.addLocale(Locale.EN_US, "Vein Mining").build();
	public static final Translation VEIN_DATA = Translation.Builder.start("pmmo.veindata")
			.addLocale(Locale.EN_US, "Charge Cap %1$s, recharges %2$s/s").build();
	public static final Translation VEIN_BREAK = Translation.Builder.start("pmmo.veinbreak")
			.addLocale(Locale.EN_US, "Cost to break as block: %s").build();
	
	//=========STAT SCROLL WIDGET=================
	public static final Translation EVENT_HEADER = Translation.Builder.start("pmmo.event_header")
			.addLocale(Locale.EN_US, "XP Award Events").build();
	public static final Translation REQ_HEADER = Translation.Builder.start("pmmo.req_header")
			.addLocale(Locale.EN_US, "Requirements").build();
	public static final Translation REQ_EFFECTS_HEADER = Translation.Builder.start("pmmo.req_effects_header")
			.addLocale(Locale.EN_US, "Negative Effects for unmet Reqs").build();
	public static final Translation MODIFIER_HEADER = Translation.Builder.start("pmmo.modifier_header")
			.addLocale(Locale.EN_US, "XP Modifiers").build();
	
	//=========FEATURES===========================
	public static final Translation FOUND_TREASURE = Translation.Builder.start("pmmo.youFoundTreasure")
			.addLocale(Locale.EN_US, "You Found Treasure!").build();
	public static final Translation LEVELED_UP = Translation.Builder.start("pmmo.leveled_up")
			.addLocale(Locale.EN_US, "You leveled up to %s in %s").build();
	
	@Override
	protected void addTranslations() {
		for (Field entry : this.getClass().getDeclaredFields()) {
			if (entry.getType() == Translation.class) {
				try {add((Translation)entry.get(LangProvider.class));}
				catch(Exception e) {e.printStackTrace();}
			}
		}
		for (Translation implied : impliedTranslations) {
			add(implied);
		}
	}
	
	private void add(Translation translation) {
		add(translation.key(), translation.localeMap().getOrDefault(locale, ""));
	}
	
	private static void addEN_US(String key, String translation) {
		impliedTranslations.add(Translation.Builder.start(key).addLocale(Locale.EN_US, translation).build());
	}
	
	public static MutableComponent skill(String skill) {
		return Component.translatable("pmmo."+skill).withStyle(style -> style.withColor(Core.get(LogicalSide.SERVER).getDataConfig().getSkillColor(skill)));
	}
	
	public static record Translation(String key, Map<String, String> localeMap) {
		public TranslatableComponent asComponent() {
			return new TranslatableComponent(key());
		}
		public TranslatableComponent asComponent(Object...objs) {
			return new TranslatableComponent(key(), objs);
		}
		public static class Builder {
			private final String key;
			private Map<String, String> localeMap;
			private Builder(String key) {this.key = key; localeMap = new HashMap<>();}
			
			public static Builder start(String key) {
				return new Builder(key);
			}
			public Builder addLocale(Locale locale, String translation) {
				this.localeMap.put(locale.str, translation);
				return this;
			}
			public Translation build() {
				return new Translation(key, localeMap);
			}
		}
	}
}
