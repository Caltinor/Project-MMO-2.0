package harmonised.pmmo.setup.datagen;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import harmonised.pmmo.core.CoreUtils;
import harmonised.pmmo.util.Reference;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.common.data.LanguageProvider;

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

	//=========PERK NAMES======================
	public static final Translation PERK_BREAK_SPEED = Translation.Builder.start("pmmo.pmmo.break_speed")
			.addLocale(Locale.EN_US, "Break Speed Modifier").build();
	public static final Translation PERK_FIREWORK = Translation.Builder.start("pmmo.pmmo.fireworks")
			.addLocale(Locale.EN_US, "Firework").build();
	public static final Translation PERK_REACH = Translation.Builder.start("pmmo.pmmo.reach")
			.addLocale(Locale.EN_US, "Player Reach Distance").build();
	public static final Translation PERK_DAMAGE = Translation.Builder.start("pmmo.pmmo.damage")
			.addLocale(Locale.EN_US, "Melee Damage").build();
	public static final Translation PERK_SPEED = Translation.Builder.start("pmmo.pmmo.speed")
			.addLocale(Locale.EN_US, "Player Move Speed").build();
	public static final Translation PERK_HEALTH = Translation.Builder.start("pmmo.pmmo.health")
			.addLocale(Locale.EN_US, "Extra Hearts").build();
	public static final Translation PERK_JUMP_BOOST = Translation.Builder.start("pmmo.pmmo.jump_boost")
			.addLocale(Locale.EN_US, "Extra Jump Height").build();
	public static final Translation PERK_BREATH = Translation.Builder.start("pmmo.pmmo.breath")
			.addLocale(Locale.EN_US, "Breath Refresh").build();
	public static final Translation PERK_DAMAGE_BOOST = Translation.Builder.start("pmmo.pmmo.damage_boost")
			.addLocale(Locale.EN_US, "Damage Modifier").build();
	public static final Translation PERK_COMMAND = Translation.Builder.start("pmmo.pmmo.command")
			.addLocale(Locale.EN_US, "Custom Commands").build();
	public static final Translation PERK_NIGHT_VISION = Translation.Builder.start("pmmo.pmmo.night_vision")
			.addLocale(Locale.EN_US, "Night Vision").build();
	public static final Translation PERK_REGEN = Translation.Builder.start("pmmo.pmmo.regen")
			.addLocale(Locale.EN_US, "Regeneration").build();
	public static final Translation PERK_EFFECT = Translation.Builder.start("pmmo.pmmo.effect")
			.addLocale(Locale.EN_US, "Status Effect").build();
	public static final Translation PERK_FALL_SAVE = Translation.Builder.start("pmmo.pmmo.fall_save")
			.addLocale(Locale.EN_US, "Reduce Fall Damage").build();
	
	//=========SKILL NAMES=====================
	public static final Translation SKILL_HEALTH = Translation.Builder.start("pmmo.health")
			.addLocale(Locale.EN_US, "Health").build();
	public static final Translation SKILL_SPEED = Translation.Builder.start("pmmo.speed")
			.addLocale(Locale.EN_US, "Speed").build();
	public static final Translation SKILL_DAMAGE = Translation.Builder.start("pmmo.damage")
			.addLocale(Locale.EN_US, "Damage").build();
	public static final Translation SKILL_POWER = Translation.Builder.start("pmmo.power")
			.addLocale(Locale.EN_US, "Power").build();
	public static final Translation SKILL_MINING = Translation.Builder.start("pmmo.mining")
			.addLocale(Locale.EN_US, "Mining").build();
	public static final Translation SKILL_BUILDING = Translation.Builder.start("pmmo.building")
			.addLocale(Locale.EN_US, "Building").build();
	public static final Translation SKILL_EXCAVATION = Translation.Builder.start("pmmo.excavation")
			.addLocale(Locale.EN_US, "Excavation").build();
	public static final Translation SKILL_WOODCUTTING = Translation.Builder.start("pmmo.woodcutting")
			.addLocale(Locale.EN_US, "Woodcutting").build();
	public static final Translation SKILL_FARMING = Translation.Builder.start("pmmo.farming")
			.addLocale(Locale.EN_US, "Farming").build();
	public static final Translation SKILL_AGILITY = Translation.Builder.start("pmmo.agility")
			.addLocale(Locale.EN_US, "Agility").build();
	public static final Translation SKILL_ENDURANCE = Translation.Builder.start("pmmo.endurance")
			.addLocale(Locale.EN_US, "Endurance").build();
	public static final Translation SKILL_COMBAT = Translation.Builder.start("pmmo.combat")
			.addLocale(Locale.EN_US, "Combat").build();
	public static final Translation SKILL_ARCHERY = Translation.Builder.start("pmmo.archery")
			.addLocale(Locale.EN_US, "Archery").build();
	public static final Translation SKILL_SMITHING = Translation.Builder.start("pmmo.smithing")
			.addLocale(Locale.EN_US, "Smithing").build();
	public static final Translation SKILL_FLYING = Translation.Builder.start("pmmo.flying")
			.addLocale(Locale.EN_US, "Flying").build();
	public static final Translation SKILL_SWIMMING = Translation.Builder.start("pmmo.swimming")
			.addLocale(Locale.EN_US, "Swimming").build();
	public static final Translation SKILL_FISHING = Translation.Builder.start("pmmo.fishing")
			.addLocale(Locale.EN_US, "Fishing").build();
	public static final Translation SKILL_CRAFTING = Translation.Builder.start("pmmo.crafting")
			.addLocale(Locale.EN_US, "Crafting").build();
	public static final Translation SKILL_MAGIC = Translation.Builder.start("pmmo.magic")
			.addLocale(Locale.EN_US, "Magic").build();
	public static final Translation SKILL_GUNSLINGING = Translation.Builder.start("pmmo.gunslinging")
			.addLocale(Locale.EN_US, "Gunslinging").build();
	public static final Translation SKILL_SLAYER = Translation.Builder.start("pmmo.slayer")
			.addLocale(Locale.EN_US, "Slayer").build();
	public static final Translation SKILL_FLETCHING = Translation.Builder.start("pmmo.fletching")
			.addLocale(Locale.EN_US, "Fletching").build();
	public static final Translation SKILL_TAMING = Translation.Builder.start("pmmo.taming")
			.addLocale(Locale.EN_US, "Taming").build();
	public static final Translation SKILL_HUNTER = Translation.Builder.start("pmmo.hunter")
			.addLocale(Locale.EN_US, "Hunter").build();
	public static final Translation SKILL_ENGINEERING = Translation.Builder.start("pmmo.engineering")
			.addLocale(Locale.EN_US, "Engineering").build();
	public static final Translation SKILL_BLOOD_MAGIC = Translation.Builder.start("pmmo.blood_magic")
			.addLocale(Locale.EN_US, "Blood Magic").build();
	public static final Translation SKILL_ASTRAL_MAGIC = Translation.Builder.start("pmmo.astral_magic")
			.addLocale(Locale.EN_US, "Astral Magic").build();
	public static final Translation SKILL_GOOD_MAGIC = Translation.Builder.start("pmmo.good_magic")
			.addLocale(Locale.EN_US, "Good Magic").build();
	public static final Translation SKILL_EVIL_MAGIC = Translation.Builder.start("pmmo.evil_magic")
			.addLocale(Locale.EN_US, "Evil Magic").build();
	public static final Translation SKILL_ARCANE_MAGIC = Translation.Builder.start("pmmo.arcane_magic")
			.addLocale(Locale.EN_US, "Arcane Magic").build();
	public static final Translation SKILL_ELEMENTAL = Translation.Builder.start("pmmo.elemental")
			.addLocale(Locale.EN_US, "Elemental").build();
	public static final Translation SKILL_EARTH = Translation.Builder.start("pmmo.earth")
			.addLocale(Locale.EN_US, "Earth").build();
	public static final Translation SKILL_WATER = Translation.Builder.start("pmmo.water")
			.addLocale(Locale.EN_US, "Water").build();
	public static final Translation SKILL_AIR = Translation.Builder.start("pmmo.air")
			.addLocale(Locale.EN_US, "Air").build();
	public static final Translation SKILL_FIRE = Translation.Builder.start("pmmo.fire")
			.addLocale(Locale.EN_US, "Fire").build();
	public static final Translation SKILL_LIGHTNING = Translation.Builder.start("pmmo.lightning")
			.addLocale(Locale.EN_US, "Lightning").build();
	public static final Translation SKILL_VOID = Translation.Builder.start("pmmo.void")
			.addLocale(Locale.EN_US, "Void").build();
	public static final Translation SKILL_THAUMATIC = Translation.Builder.start("pmmo.thaumatic")
			.addLocale(Locale.EN_US, "Thaumatic").build();
	public static final Translation SKILL_SUMMONING = Translation.Builder.start("pmmo.summoning")
			.addLocale(Locale.EN_US, "Summoning").build();
	public static final Translation SKILL_INVENTION = Translation.Builder.start("pmmo.invention")
			.addLocale(Locale.EN_US, "Invention").build();
	public static final Translation SKILL_RUNECRAFTING = Translation.Builder.start("pmmo.runecrafting")
			.addLocale(Locale.EN_US, "Runecrafting").build();
	public static final Translation SKILL_PRAYER = Translation.Builder.start("pmmo.prayer")
			.addLocale(Locale.EN_US, "Prayer").build();
	public static final Translation SKILL_COOKING = Translation.Builder.start("pmmo.cooking")
			.addLocale(Locale.EN_US, "Cooking").build();
	public static final Translation SKILL_FIREMAKING = Translation.Builder.start("pmmo.firemaking")
			.addLocale(Locale.EN_US, "Firemaking").build();
	public static final Translation SKILL_AFKING = Translation.Builder.start("pmmo.afking")
			.addLocale(Locale.EN_US, "Afking").build();
	public static final Translation SKILL_TRADING = Translation.Builder.start("pmmo.trading")
			.addLocale(Locale.EN_US, "Trading").build();
	public static final Translation SKILL_SAILING = Translation.Builder.start("pmmo.sailing")
			.addLocale(Locale.EN_US, "Sailing").build();
	public static final Translation SKILL_ALCHEMY = Translation.Builder.start("pmmo.alchemy")
			.addLocale(Locale.EN_US, "Alchemy").build();
	public static final Translation SKILL_CONSTRUCTION = Translation.Builder.start("pmmo.construction")
			.addLocale(Locale.EN_US, "Construction").build();
	public static final Translation SKILL_LEATHERWORKING = Translation.Builder.start("pmmo.leatherworking")
			.addLocale(Locale.EN_US, "Leatherworking").build();
	public static final Translation SKILL_EXPLORATION = Translation.Builder.start("pmmo.exploration")
			.addLocale(Locale.EN_US, "Exploration").build();
	public static final Translation SKILL_CHARISMA = Translation.Builder.start("pmmo.charisma")
			.addLocale(Locale.EN_US, "Charisma").build();
	
	//=========ENUM VALUES=====================
	public static final Translation ENUM_ANVIL_REPAIR = Translation.Builder.start("pmmo.enum.ANVIL_REPAIR")
			.addLocale(Locale.EN_US, "Anvil Repair").build();
	public static final Translation ENUM_BLOCK_BREAK = Translation.Builder.start("pmmo.enum.BLOCK_BREAK")
			.addLocale(Locale.EN_US, "Break Block").build();
	public static final Translation ENUM_BREAK_SPEED = Translation.Builder.start("pmmo.enum.BREAK_SPEED")
			.addLocale(Locale.EN_US, "Break Speed").build();
	public static final Translation ENUM_BLOCK_PLACE = Translation.Builder.start("pmmo.enum.BLOCK_PLACE")
			.addLocale(Locale.EN_US, "Place Block").build();
	public static final Translation ENUM_BREATH_CHANGE = Translation.Builder.start("pmmo.enum.BREATH_CHANGE")
			.addLocale(Locale.EN_US, "Breath Change").build();
	public static final Translation ENUM_BREED = Translation.Builder.start("pmmo.enum.BREED")
			.addLocale(Locale.EN_US, "Breed").build();
	public static final Translation ENUM_BREW = Translation.Builder.start("pmmo.enum.BREW")
			.addLocale(Locale.EN_US, "Brew").build();
	public static final Translation ENUM_CRAFT = Translation.Builder.start("pmmo.enum.CRAFT")
			.addLocale(Locale.EN_US, "Craft").build();
	public static final Translation ENUM_CONSUME = Translation.Builder.start("pmmo.enum.CONSUME")
			.addLocale(Locale.EN_US, "Eat/Drink").build();
	public static final Translation ENUM_RECEIVE_DAMAGE = Translation.Builder.start("pmmo.enum.RECEIVE_DAMAGE")
			.addLocale(Locale.EN_US, "Receive Damage (Unspecified)").build();
	public static final Translation ENUM_FROM_MOBS = Translation.Builder.start("pmmo.enum.FROM_MOBS")
			.addLocale(Locale.EN_US, "Receive Mob Damage").build();
	public static final Translation ENUM_FROM_PLAYERS = Translation.Builder.start("pmmo.enum.FROM_PLAYERS")
			.addLocale(Locale.EN_US, "Receive Player Damage").build();
	public static final Translation ENUM_FROM_ANIMALS = Translation.Builder.start("pmmo.enum.FROM_ANIMALS")
			.addLocale(Locale.EN_US, "Receive Animal Damage").build();
	public static final Translation ENUM_FROM_PROJECTILES = Translation.Builder.start("pmmo.enum.FROM_PROJECTILES")
			.addLocale(Locale.EN_US, "Receive Projectile Damage").build();
	public static final Translation ENUM_FROM_MAGIC = Translation.Builder.start("pmmo.enum.FROM_MAGIC")
			.addLocale(Locale.EN_US, "Receive Magic Damage").build();
	public static final Translation ENUM_FROM_ENVIRONMENT = Translation.Builder.start("pmmo.enum.FROM_ENVIRONMENT")
			.addLocale(Locale.EN_US, "Receive Environmental Damage").build();
	public static final Translation ENUM_FROM_IMPACT = Translation.Builder.start("pmmo.enum.FROM_IMPACT")
			.addLocale(Locale.EN_US, "Receive Impact Damage").build();
	public static final Translation ENUM_DEAL_MELEE_DAMAGE = Translation.Builder.start("pmmo.enum.DEAL_MELEE_DAMAGE")
			.addLocale(Locale.EN_US, "Deal Melee Damage (Unspecified)").build();
	public static final Translation ENUM_MELEE_TO_MOBS = Translation.Builder.start("pmmo.enum.MELEE_TO_MOBS")
			.addLocale(Locale.EN_US, "Deal Melee Damage to Mobs").build();
	public static final Translation ENUM_MELEE_TO_PLAYERS = Translation.Builder.start("pmmo.enum.MELEE_TO_PLAYERS")
			.addLocale(Locale.EN_US, "Deal Melee Damage to Players").build();
	public static final Translation ENUM_MELEE_TO_ANIMALS = Translation.Builder.start("pmmo.enum.MELEE_TO_ANIMALS")
			.addLocale(Locale.EN_US, "Deal Melee Damage to Animals").build();
	public static final Translation ENUM_DEAL_RANGED_DAMAGE = Translation.Builder.start("pmmo.enum.DEAL_RANGED_DAMAGE")
			.addLocale(Locale.EN_US, "Deal Ranged Damage (Unspecified)").build();
	public static final Translation ENUM_RANGED_TO_MOBS = Translation.Builder.start("pmmo.enum.RANGED_TO_MOBS")
			.addLocale(Locale.EN_US, "Deal Ranged Damage to Mobs").build();
	public static final Translation ENUM_RANGED_TO_PLAYERS = Translation.Builder.start("pmmo.enum.RANGED_TO_PLAYERS")
			.addLocale(Locale.EN_US, "Deal Ranged Damage to Players").build();
	public static final Translation ENUM_RANGED_TO_ANIMALS = Translation.Builder.start("pmmo.enum.RANGED_TO_ANIMALS")
			.addLocale(Locale.EN_US, "Deal Ranged Damage to Animals").build();
	public static final Translation ENUM_DEATH = Translation.Builder.start("pmmo.enum.DEATH")
			.addLocale(Locale.EN_US, "Death").build();
	public static final Translation ENUM_ENCHANT = Translation.Builder.start("pmmo.enum.ENCHANT")
			.addLocale(Locale.EN_US, "Enchant").build();
	public static final Translation ENUM_FISH = Translation.Builder.start("pmmo.enum.FISH")
			.addLocale(Locale.EN_US, "Fish").build();
	public static final Translation ENUM_SMELT = Translation.Builder.start("pmmo.enum.SMELT")
			.addLocale(Locale.EN_US, "Smelt/Cook").build();
	public static final Translation ENUM_GROW = Translation.Builder.start("pmmo.enum.GROW")
			.addLocale(Locale.EN_US, "Grow").build();
	public static final Translation ENUM_HEALTH_CHANGE = Translation.Builder.start("pmmo.enum.HEALTH_CHANGE")
			.addLocale(Locale.EN_US, "Health Change").build();
	public static final Translation ENUM_JUMP = Translation.Builder.start("pmmo.enum.JUMP")
			.addLocale(Locale.EN_US, "Jump").build();
	public static final Translation ENUM_SPRINT_JUMP = Translation.Builder.start("pmmo.enum.SPRINT_JUMP")
			.addLocale(Locale.EN_US, "Sprint Jump").build();
	public static final Translation ENUM_CROUCH_JUMP = Translation.Builder.start("pmmo.enum.CROUCH_JUMP")
			.addLocale(Locale.EN_US, "Crouch Jump").build();
	public static final Translation ENUM_WORLD_CONNECT = Translation.Builder.start("pmmo.enum.WORLD_CONNECT")
			.addLocale(Locale.EN_US, "World Connect").build();
	public static final Translation ENUM_WORLD_DISCONNECT = Translation.Builder.start("pmmo.enum.WORLD_DISCONNECT")
			.addLocale(Locale.EN_US, "World Disconnect").build();
	public static final Translation ENUM_HIT_BLOCK = Translation.Builder.start("pmmo.enum.HIT_BLOCK")
			.addLocale(Locale.EN_US, "Hit Block").build();
	public static final Translation ENUM_ACTIVATE_BLOCK = Translation.Builder.start("pmmo.enum.ACTIVATE_BLOCK")
			.addLocale(Locale.EN_US, "Activate Block").build();
	public static final Translation ENUM_ACTIVATE_ITEM = Translation.Builder.start("pmmo.enum.ACTIVATE_ITEM")
			.addLocale(Locale.EN_US, "Activate Item").build();
	public static final Translation ENUM_ENTITY = Translation.Builder.start("pmmo.enum.ENTITY")
			.addLocale(Locale.EN_US, "Interact with Entity").build();
	public static final Translation ENUM_RESPAWN = Translation.Builder.start("pmmo.enum.RESPAWN")
			.addLocale(Locale.EN_US, "Respawn").build();
	public static final Translation ENUM_RIDING = Translation.Builder.start("pmmo.enum.RIDING")
			.addLocale(Locale.EN_US, "Riding").build();
	public static final Translation ENUM_SHIELD_BLOCK = Translation.Builder.start("pmmo.enum.SHIELD_BLOCK")
			.addLocale(Locale.EN_US, "Block with Shield").build();
	public static final Translation ENUM_SKILL_UP = Translation.Builder.start("pmmo.enum.SKILL_UP")
			.addLocale(Locale.EN_US, "Level Up").build();
	public static final Translation ENUM_SLEEP = Translation.Builder.start("pmmo.enum.SLEEP")
			.addLocale(Locale.EN_US, "Sleep").build();
	public static final Translation ENUM_SPRINTING = Translation.Builder.start("pmmo.enum.SPRINTING")
			.addLocale(Locale.EN_US, "Sprinting").build();
	public static final Translation ENUM_SUBMERGED = Translation.Builder.start("pmmo.enum.SUBMERGED")
			.addLocale(Locale.EN_US, "Submerged").build();
	public static final Translation ENUM_SWIMMING = Translation.Builder.start("pmmo.enum.SWIMMING")
			.addLocale(Locale.EN_US, "Swimming (above surface)").build();
	public static final Translation ENUM_DIVING = Translation.Builder.start("pmmo.enum.DIVING")
			.addLocale(Locale.EN_US, "Diving").build();
	public static final Translation ENUM_SURFACING = Translation.Builder.start("pmmo.enum.SURFACING")
			.addLocale(Locale.EN_US, "Surfacing").build();
	public static final Translation ENUM_SWIM_SPRINTING = Translation.Builder.start("pmmo.enum.SWIM_SPRINTING")
			.addLocale(Locale.EN_US, "Fast Swimming").build();
	public static final Translation ENUM_TAMING = Translation.Builder.start("pmmo.enum.TAMING")
			.addLocale(Locale.EN_US, "Taming").build();
	public static final Translation ENUM_VEIN_MINE = Translation.Builder.start("pmmo.enum.VEIN_MINE")
			.addLocale(Locale.EN_US, "Vein Mining").build();
	public static final Translation ENUM_DISABLE_PERK = Translation.Builder.start("pmmo.enum.DISABLE_PERK")
			.addLocale(Locale.EN_US, "Disable Perk").build();
	public static final Translation ENUM_WEAR = Translation.Builder.start("pmmo.enum.WEAR")
			.addLocale(Locale.EN_US, "Wear Item").build();
	public static final Translation ENUM_USE_ENCHANTMENT = Translation.Builder.start("pmmo.enum.USE_ENCHANTMENT")
			.addLocale(Locale.EN_US, "Use Enchantment").build();
	public static final Translation ENUM_TOOL = Translation.Builder.start("pmmo.enum.TOOL")
			.addLocale(Locale.EN_US, "Use as Tool").build();
	public static final Translation ENUM_WEAPON = Translation.Builder.start("pmmo.enum.WEAPON")
			.addLocale(Locale.EN_US, "Use as Weapon").build();
	public static final Translation ENUM_USE = Translation.Builder.start("pmmo.enum.USE")
			.addLocale(Locale.EN_US, "Activate Item Ability").build();
	public static final Translation ENUM_PLACE = Translation.Builder.start("pmmo.enum.PLACE")
			.addLocale(Locale.EN_US, "Place Block").build();
	public static final Translation ENUM_BREAK = Translation.Builder.start("pmmo.enum.BREAK")
			.addLocale(Locale.EN_US, "Break Block").build();
	public static final Translation ENUM_KILL = Translation.Builder.start("pmmo.enum.KILL")
			.addLocale(Locale.EN_US, "Kill Entity").build();
	public static final Translation ENUM_TRAVEL = Translation.Builder.start("pmmo.enum.TRAVEL")
			.addLocale(Locale.EN_US, "Travel to").build();
	public static final Translation ENUM_RIDE = Translation.Builder.start("pmmo.enum.RIDE")
			.addLocale(Locale.EN_US, "Ride/Drive").build();
	public static final Translation ENUM_TAME = Translation.Builder.start("pmmo.enum.TAME")
			.addLocale(Locale.EN_US, "Tame Animal").build();
	public static final Translation ENUM_INTERACT = Translation.Builder.start("pmmo.enum.INTERACT")
			.addLocale(Locale.EN_US, "Interact with Block").build();
	public static final Translation ENUM_ENTITY_INTERACT = Translation.Builder.start("pmmo.enum.ENTITY_INTERACT")
			.addLocale(Locale.EN_US, "Interact with Entity").build();
	public static final Translation ENUM_BIOME = Translation.Builder.start("pmmo.enum.BIOME")
			.addLocale(Locale.EN_US, "Biome").build();

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
	public static final Translation OPEN_GLOSSARY = Translation.Builder.start("pmmo.gui.stat_screen.open_glossary")
			.addLocale(Locale.EN_US, "Open Glossary").build();
	public static final Translation EVENT_HEADER = Translation.Builder.start("pmmo.event_header")
			.addLocale(Locale.EN_US, "XP Award Events").build();
	public static final Translation REQ_HEADER = Translation.Builder.start("pmmo.req_header")
			.addLocale(Locale.EN_US, "Requirements").build();
	public static final Translation REQ_EFFECTS_HEADER = Translation.Builder.start("pmmo.req_effects_header")
			.addLocale(Locale.EN_US, "Negative Effects for unmet Reqs").build();
	public static final Translation MODIFIER_HEADER = Translation.Builder.start("pmmo.modifier_header")
			.addLocale(Locale.EN_US, "XP Modifiers").build();
	public static final Translation SALVAGE_HEADER = Translation.Builder.start("pmmo.salvage_header")
			.addLocale(Locale.EN_US, "Salvage").build();
	public static final Translation SALVAGE_LEVEL_REQ = Translation.Builder.start("pmmo.salvage_levelreq")
			.addLocale(Locale.EN_US, "Required level to obtain").build();
	public static final Translation SALVAGE_CHANCE = Translation.Builder.start("pmmo.salvage_chance")
			.addLocale(Locale.EN_US, "Chance: %1$s / %2$s").build();
	public static final Translation SALVAGE_MAX = Translation.Builder.start("pmmo.salvage_max")
			.addLocale(Locale.EN_US, "Max Obtainable: %1$s").build();
	public static final Translation SALVAGE_CHANCE_MOD = Translation.Builder.start("pmmo.salvage_chance_modifier")
			.addLocale(Locale.EN_US, "Chance boost based on level").build();
	public static final Translation SALVAGE_XP_AWARD = Translation.Builder.start("pmmo.salvage_xpAward_header")
			.addLocale(Locale.EN_US, "Xp awarded on success").build();
	public static final Translation VEIN_HEADER = Translation.Builder.start("pmmo.vein_header")
			.addLocale(Locale.EN_US, "Vein Mining Attributes").build();
	public static final Translation VEIN_RATE = Translation.Builder.start("pmmo.veindata_rate")
			.addLocale(Locale.EN_US, "Vein Recharge Rate Per Second: %1$s").build();
	public static final Translation VEIN_CAP = Translation.Builder.start("pmmo.veindata_cap")
			.addLocale(Locale.EN_US, "Vein Capacity Added By Item: %1$s").build();
	public static final Translation VEIN_CONSUME = Translation.Builder.start("pmmo.veindata_consume")
			.addLocale(Locale.EN_US, "Vein Consumed on Break: %1$s").build();
	public static final Translation PLAYER_HEADER = Translation.Builder.start("pmmo.playerspecific_header")
			.addLocale(Locale.EN_US, "Player-Specific Settings").build();
	public static final Translation PLAYER_IGNORE_REQ = Translation.Builder.start("pmmo.playerspecific.ignorereq")
			.addLocale(Locale.EN_US, "Ignore Reqs: %1$s").build();
	public static final Translation PLAYER_BONUSES = Translation.Builder.start("pmmo.playerspecific.bonus")
			.addLocale(Locale.EN_US, "Player Bonuses:").build();
	public static final Translation SKILL_LIST_HEADER = Translation.Builder.start("pmmo.skilllist_header")
			.addLocale(Locale.EN_US, "Player Skills").build();
	public static final Translation DIMENSION_HEADER = Translation.Builder.start("pmmo.dimension_header")
			.addLocale(Locale.EN_US, "Dimension: %1$s").build();
	public static final Translation VEIN_BLACKLIST_HEADER = Translation.Builder.start("pmmo.vein_blacklist_header")
			.addLocale(Locale.EN_US, "Vein Blacklisted Blocks").build();
	public static final Translation MOB_MODIFIER_HEADER = Translation.Builder.start("pmmo.mob_modifier_header")
			.addLocale(Locale.EN_US, "Mob Modifiers").build();
	public static final Translation BIOME_HEADER = Translation.Builder.start("pmmo.biome_header")
			.addLocale(Locale.EN_US, "Mob Modifiers").build();
	public static final Translation BIOME_EFFECT_NEG = Translation.Builder.start("pmmo.biome_negative")
			.addLocale(Locale.EN_US, "Penalty Effects").build();
	public static final Translation BIOME_EFFECT_POS = Translation.Builder.start("pmmo.biome_positive")
			.addLocale(Locale.EN_US, "Bonus Effects").build();
	public static final Translation ADDON_AFFECTED_ATTRIBUTE = Translation.Builder.start("pmmo.gui.statscroll.addon_affected")
			.addLocale(Locale.EN_US, "This Property is Dynamically Defined").build();
	
	//=========GLOSSARY SELECT SCREEN=============
	public static final Translation GLOSSARY_DEFAULT_SECTION = Translation.Builder.start("pmmo.gui.glossary.default_section")
			.addLocale(Locale.EN_US, "Choose Section").build();
	public static final Translation GLOSSARY_SECTION_REQ = Translation.Builder.start("pmmo.gui.glossary.section.req")
			.addLocale(Locale.EN_US, "Requirements").build();
	public static final Translation GLOSSARY_SECTION_XP = Translation.Builder.start("pmmo.gui.glossary.section.xp_sources")
			.addLocale(Locale.EN_US, "XP Sources").build();
	public static final Translation GLOSSARY_SECTION_BONUS = Translation.Builder.start("pmmo.gui.glossary.section.bonuses")
			.addLocale(Locale.EN_US, "Bonuses").build();
	public static final Translation GLOSSARY_SECTION_SALVAGE = Translation.Builder.start("pmmo.gui.glossary.section.salvage")
			.addLocale(Locale.EN_US, "Salvage").build();
	public static final Translation GLOSSARY_SECTION_VEIN = Translation.Builder.start("pmmo.gui.glossary.section.vein")
			.addLocale(Locale.EN_US, "Vein Mining").build();
	public static final Translation GLOSSARY_SECTION_MOB = Translation.Builder.start("pmmo.gui.glossary.section.mobscaling")
			.addLocale(Locale.EN_US, "Mob Scaling").build();
	public static final Translation GLOSSARY_SECTION_PERKS = Translation.Builder.start("pmmo.gui.glossary.section.perks")
			.addLocale(Locale.EN_US, "Perks").build();
	
	public static final Translation GLOSSARY_DEFAULT_OBJECT = Translation.Builder.start("pmmo.gui.glossary.default_object")
			.addLocale(Locale.EN_US, "All Content").build();
	public static final Translation GLOSSARY_OBJECT_ITEMS = Translation.Builder.start("pmmo.gui.glossary.object.items")
			.addLocale(Locale.EN_US, "Items").build();
	public static final Translation GLOSSARY_OBJECT_BLOCKS = Translation.Builder.start("pmmo.gui.glossary.object.blocks")
			.addLocale(Locale.EN_US, "Blocks").build();
	public static final Translation GLOSSARY_OBJECT_ENTITIES = Translation.Builder.start("pmmo.gui.glossary.object.entities")
			.addLocale(Locale.EN_US, "Animals/Mobs").build();
	public static final Translation GLOSSARY_OBJECT_DIMENSIONS = Translation.Builder.start("pmmo.gui.glossary.object.dimensions")
			.addLocale(Locale.EN_US, "Dimensions").build();
	public static final Translation GLOSSARY_OBJECT_BIOMES = Translation.Builder.start("pmmo.gui.glossary.object.biomes")
			.addLocale(Locale.EN_US, "Biomes").build();
	public static final Translation GLOSSARY_OBJECT_ENCHANTS = Translation.Builder.start("pmmo.gui.glossary.object.enchantments")
			.addLocale(Locale.EN_US, "Enchantments").build();
	public static final Translation GLOSSARY_OBJECT_EFFECTS = Translation.Builder.start("pmmo.gui.glossary.object.effects")
			.addLocale(Locale.EN_US, "Effects").build();
	public static final Translation GLOSSARY_OBJECT_PERKS = Translation.Builder.start("pmmo.gui.glossary.object.perks")
			.addLocale(Locale.EN_US, "Perks").build();
	
	public static final Translation GLOSSARY_DEFAULT_SKILL = Translation.Builder.start("pmmo.gui.glossary.default_skill")
			.addLocale(Locale.EN_US, "All Skills").build();
	public static final Translation GLOSSARY_DEFAULT_ENUM = Translation.Builder.start("pmmo.gui.glossary.default_enum")
			.addLocale(Locale.EN_US, "All Event/Req/Type").build();
	public static final Translation GLOSSARY_VIEW_BUTTON = Translation.Builder.start("pmmo.gui.glossary.view_button")
			.addLocale(Locale.EN_US, "View Info").build();
	
	//=========FEATURES===========================
	public static final Translation FOUND_TREASURE = Translation.Builder.start("pmmo.youFoundTreasure")
			.addLocale(Locale.EN_US, "You Found Treasure!").build();
	public static final Translation LEVELED_UP = Translation.Builder.start("pmmo.leveled_up")
			.addLocale(Locale.EN_US, "You leveled up to %s in %s").build();
	public static final Translation PERK_BREATH_REFRESH = Translation.Builder.start("pmmo.perks.breathrefresh")
			.addLocale(Locale.EN_US, "Your skill extended your breath").build();
	public static final Translation VEIN_LIMIT = Translation.Builder.start("pmmo.veinLimit")
			.addLocale(Locale.EN_US, "Vein Limit: %1$s").build();
	public static final Translation VEIN_CHARGE = Translation.Builder.start("pmmo.veinCharge")
			.addLocale(Locale.EN_US, "Vein Ability: %1$s/%2$s").build();
	
	//=========COMMANDS===========================
	public static final Translation SET_LEVEL = Translation.Builder.start("pmmo.setLevel")
			.addLocale(Locale.EN_US, "%1$s has been set to level %2$s for %3$s").build();
	public static final Translation SET_XP = Translation.Builder.start("pmmo.setXp")
			.addLocale(Locale.EN_US, "%1$s has been set to %2$sxp for %3$s").build();
	public static final Translation ADD_LEVEL = Translation.Builder.start("pmmo.addLevel")
			.addLocale(Locale.EN_US, "%1$s has been changed by %2$s levels for %3$s").build();
	public static final Translation ADD_XP = Translation.Builder.start("pmmo.addXp")
			.addLocale(Locale.EN_US, "%1$s has been changed by %2$sxp for %3$s").build();
	public static final Translation PARTY_ALREADY_IN = Translation.Builder.start("pmmo.youAreAlreadyInAParty")
			.addLocale(Locale.EN_US, "You are already in a Party").build();
	public static final Translation PARTY_CREATED = Translation.Builder.start("pmmo.partyCreated")
			.addLocale(Locale.EN_US, "You have created a Party").build();
	public static final Translation PARTY_LEFT = Translation.Builder.start("pmmo.youLeftTheParty")
			.addLocale(Locale.EN_US, "You have left the Party").build();
	public static final Translation PARTY_NOT_IN = Translation.Builder.start("pmmo.youAreNotInAParty")
			.addLocale(Locale.EN_US, "You are not in a Party").build();
	public static final Translation PARTY_INVITE = Translation.Builder.start("pmmo.youHaveInvitedAPlayerToYourParty")
			.addLocale(Locale.EN_US, "You invited %1$s to your Party").build();
	public static final Translation PARTY_MEMBER_TOTAL = Translation.Builder.start("pmmo.totalMembers")
			.addLocale(Locale.EN_US, "Total Members: %1$s").build();
	public static final Translation PARTY_MEMBER_LIST = Translation.Builder.start("pmmo.partyMemberListEntry")
			.addLocale(Locale.EN_US, "%1$s").build();
	public static final Translation PARTY_DECLINE = Translation.Builder.start("pmmo.youHaveDeclinedPartyInvitation")
			.addLocale(Locale.EN_US, "You have declined the Party invitation").build();
	public static final Translation PARTY_NO_INVITES = Translation.Builder.start("pmmo.youAreNotInvitedToAnyParty")
			.addLocale(Locale.EN_US, "You have no pending Party invitations").build();
	public static final Translation PARTY_JOINED = Translation.Builder.start("pmmo.youJoinedAParty")
			.addLocale(Locale.EN_US, "You have joined the Party!").build();
	public static final Translation PARTY_RESCIND_INVITE = Translation.Builder.start("pmmo.msg.rescindInvite")
			.addLocale(Locale.EN_US, "You have removed the invite for %1$s").build();
	public static final Translation PARTY_ACCEPT = Translation.Builder.start("pmmo.msg.accept")
			.addLocale(Locale.EN_US, "Accept").build();
	public static final Translation PARTY_DECLINE_INVITE = Translation.Builder.start("pmmo.msg.decline")
			.addLocale(Locale.EN_US, "Decline").build();
	public static final Translation PARTY_PLAYER_INVITED = Translation.Builder.start("pmmo.playerInvitedYouToAParty")
			.addLocale(Locale.EN_US, "%1$s invited you to their Party, %2$s|%3$s").build();
	
	//=========DENIAL MESSAGES====================
	public static final Translation DENIAL_WEAR = Translation.Builder.start("pmmo.msg.denial.wear")
			.addLocale(Locale.EN_US, "You are not skilled enough to wear %1$s").build();
	public static final Translation DENIAL_USE_ENCHANT = Translation.Builder.start("pmmo.msg.denial.use_enchantment")
			.addLocale(Locale.EN_US, "You are not skilled enough to use %1$s with %2$s enchantment").build();
	public static final Translation DENIAL_TOOL = Translation.Builder.start("pmmo.msg.denial.tool")
			.addLocale(Locale.EN_US, "You are not skilled enough to use %1$s as tool").build();
	public static final Translation DENIAL_WEAPON = Translation.Builder.start("pmmo.msg.denial.weapon")
			.addLocale(Locale.EN_US, "You are not skilled enough to use %1$s as a weapon").build();
	public static final Translation DENIAL_USE = Translation.Builder.start("pmmo.msg.denial.use")
			.addLocale(Locale.EN_US, "You are not skilled enough to use %1$s").build();
	public static final Translation DENIAL_PLACE = Translation.Builder.start("pmmo.msg.denial.place")
			.addLocale(Locale.EN_US, "You are not skilled enough to place %1$s").build();
	public static final Translation DENIAL_BREAK = Translation.Builder.start("pmmo.msg.denial.break")
			.addLocale(Locale.EN_US, "You are not skilled enough to break %1$s").build();
	public static final Translation DENIAL_BIOME = Translation.Builder.start("pmmo.msg.denial.biome")
			.addLocale(Locale.EN_US, "You are not skilled enough to survive in %1$s").build();
	public static final Translation DENIAL_KILL = Translation.Builder.start("pmmo.msg.denial.kill")
			.addLocale(Locale.EN_US, "You are not skilled enough to kill %1$s").build();
	public static final Translation DENIAL_TRAVEL = Translation.Builder.start("pmmo.msg.denial.travel")
			.addLocale(Locale.EN_US, "Travel to %1$s requires %2$s").build();
	public static final Translation DENIAL_RIDE = Translation.Builder.start("pmmo.msg.denial.ride")
			.addLocale(Locale.EN_US, "You are not skilled enough to ride %1$s").build();
	public static final Translation DENIAL_TAME = Translation.Builder.start("pmmo.msg.denial.tame")
			.addLocale(Locale.EN_US, "You are not skilled enough to tame %1$s").build();
	public static final Translation DENIAL_ENTITY_INTERACT = Translation.Builder.start("pmmo.msg.denial.entity_interact")
			.addLocale(Locale.EN_US, "You are not skilled enough to interact with %1$s").build();
	
	//==========TUTORIAL TEXT======================
	public static final Translation SALVAGE_TUTORIAL_HEADER = Translation.Builder.start("pmmo.client.tutorial.salvage.header")
			.addLocale(Locale.EN_US, "Salvage Block").build();
	public static final Translation SALVAGE_TUTORIAL_USAGE = Translation.Builder.start("pmmo.client.tutorial.salvage.usage")
			.addLocale(Locale.EN_US, "While crouching, right click this block to salvage the items in your hand.").build();
	
	@Override
	protected void addTranslations() {
		for (Field entry : this.getClass().getDeclaredFields()) {
			if (entry.getType() == Translation.class) {
				try {add((Translation)entry.get(LangProvider.class));}
				catch(Exception e) {e.printStackTrace();}
			}
		}
	}
	
	private void add(Translation translation) {
		add(translation.key(), translation.localeMap().getOrDefault(locale, ""));
	}
	
	public static MutableComponent skill(String skill) {
		return new TranslatableComponent("pmmo."+skill).withStyle(style -> style.withColor(CoreUtils.getSkillColor(skill)));
	}
	
	public static record Translation(String key, Map<String, String> localeMap) {
		public MutableComponent asComponent() {
			return new TranslatableComponent(key());
		}
		public MutableComponent asComponent(Object...obj) {
			return new TranslatableComponent(key(), obj);
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
