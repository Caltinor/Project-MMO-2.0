package harmonised.pmmo.setup.datagen;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.util.Reference;
import net.minecraft.data.DataGenerator;
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
		addEN_US("pmmo.enum.BIOME","Enter Biome");
		addEN_US("pmmo.enum.KILL","Kill Entity");
		addEN_US("pmmo.enum.TRAVEL","Travel to");
		addEN_US("pmmo.enum.RIDE","Ride/Drive");
		addEN_US("pmmo.enum.TAME","Tame Animal");
		addEN_US("pmmo.enum.BREED","Breed Animal");
		addEN_US("pmmo.enum.INTERACT","Interact with Block");
		addEN_US("pmmo.enum.ENTITY_INTERACT","Interact with Entity");
		
		addEN_US("pmmo.enum.BIOME","Biome Modifier");
	    addEN_US("pmmo.enum.HELD","Held Modifier");
	    addEN_US("pmmo.enum.WORN","Worn Modifier");
	    addEN_US("pmmo.enum.DIMENSION","Dimension Modifier");
	}

	
	//=========LOGIN HANDLER===================
	public static final Translation WELCOME_TEXT = Translation.Builder.start("pmmo.welcomeText")
			.addLocale(Locale.EN_US, "Welcome! Project MMO is more fun with datapacks. download one %s").build();
	public static final Translation CLICK_ME = Translation.Builder.start("pmmo.clickMe")
			.addLocale(Locale.EN_US, "HERE").build();
	
	
	@Override
	protected void addTranslations() {
		for (Field entry : this.getClass().getDeclaredFields()) {
			if (entry.getType() == Translation.class) {
				//add((Translation)entry.get(LangProvider.class));
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
	
	public static record Translation(String key, Map<String, String> localeMap) {
		public TranslatableComponent asComponent() {
			return new TranslatableComponent(key());
		}
		public static class Builder {
			String key;
			Map<String, String> localeMap;
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
