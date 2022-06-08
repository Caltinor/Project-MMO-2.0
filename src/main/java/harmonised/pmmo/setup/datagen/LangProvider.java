package harmonised.pmmo.setup.datagen;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.util.Reference;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
		// TODO Auto-generated constructor stub
	}
	
	public static final Translation ENUM_ANVIL_REPAIR = Translation.Builder.start("pmmo.enum."+EventType.ANVIL_REPAIR.name())
			.addLocale(Locale.EN_US, "Anvil Repair").build();
	public static final Translation ENUM_BLOCK_BREAK = Translation.Builder.start("pmmo.enum."+EventType.BLOCK_BREAK.name())
			.addLocale(Locale.EN_US, "Break Block").build();
	/*
	"pmmo.enum.BREAK_SPEED":"Break Speed",
	"pmmo.enum.BLOCK_PLACE":"Place Block",
	"pmmo.enum.BREATH_CHANGE":"Breath Change",
	"pmmo.enum.BREED":"Breed",
	"pmmo.enum.BREW":"Brew",
	"pmmo.enum.CRAFT":"Craft",
	"pmmo.enum.CONSUME":"Eat/Drink",
	"pmmo.enum.RECEIVE_DAMAGE":"Receive Damage (Unspecified)",
	"pmmo.enum.FROM_MOBS":"Receive Mob Damage",
	"pmmo.enum.FROM_PLAYERS":"Receive Player Damage",
	"pmmo.enum.FROM_ANIMALS":"Receive Animal Damage",
	"pmmo.enum.FROM_PROJECTILES":"Receive Projectile Damage",
	"pmmo.enum.FROM_MAGIC":"Receive Magic Damage",
	"pmmo.enum.FROM_ENVIRONMENT":"Receive Environmental Damage",
	"pmmo.enum.FROM_IMPACT":"Receive Impact Damage",
	"pmmo.enum.DEAL_MELEE_DAMAGE":"Deal Melee Damage (Unspecified)",
	"pmmo.enum.MELEE_TO_MOBS":"Deal Melee Damage to Mobs",
	"pmmo.enum.MELEE_TO_PLAYERS":"Deal Melee Damage to Players",
	"pmmo.enum.MELEE_TO_ANIMALS":"Deal Melee Damage to Animals",
	"pmmo.enum.DEAL_RANGED_DAMAGE":"Deal Ranged Damage (Unspecified)",
	"pmmo.enum.RANGED_TO_MOBS":"Deal Ranged Damage to Mobs",
	"pmmo.enum.RANGED_TO_PLAYERS":"Deal Ranged Damage to Players",
	"pmmo.enum.RANGED_TO_ANIMALS":"Deal Ranged Damage to Animals",
	"pmmo.enum.DEATH":"Death",
	"pmmo.enum.ENCHANT":"Enchant",
	"pmmo.enum.FISH":"Fish",
	"pmmo.enum.SMELT":"Smelt/Cook",
	"pmmo.enum.GROW":"Grow",
	"pmmo.enum.HEALTH_CHANGE":"Health Change",
	"pmmo.enum.JUMP":"Jump",
	"pmmo.enum.SPRINT_JUMP":"Sprint Jump",
	"pmmo.enum.CROUCH_JUMP":"Crouch Jump",
	"pmmo.enum.WORLD_CONNECT":"World Connect",
	"pmmo.enum.WORLD_DISCONNECT":"World Disconnect",
	"pmmo.enum.HIT_BLOCK":"Hit Block",
	"pmmo.enum.ACTIVATE_BLOCK":"Activate Block",
	"pmmo.enum.ACTIVATE_ITEM":"Activate Item",
	"pmmo.enum.ENTITY":"Interact with Entity",
	"pmmo.enum.RESPAWN":"Respawn",
	"pmmo.enum.RIDING":"Riding",
	"pmmo.enum.SHIELD_BLOCK":"Block with Shield",
	"pmmo.enum.SKILL_UP":"Level Up",
	"pmmo.enum.SLEEP":"Sleep",
	"pmmo.enum.SPRINTING":"Sprinting",
	"pmmo.enum.SUBMERGED":"Submerged",
	"pmmo.enum.SWIMMING":"Swimming (above surface)",
	"pmmo.enum.DIVING":"Diving",
	"pmmo.enum.SURFACING":"Surfacing",
	"pmmo.enum.SWIM_SPRINTING":"Fast Swimming",
	"pmmo.enum.TAMING":"Taming",
	"pmmo.enum.VEIN_MINE":"Vein Mining",
	"pmmo.enum.DISABLE_PERK":"Disable Perk",
	 */
	
	
	@Override
	protected void addTranslations() {
		for (Field entry : this.getClass().getDeclaredFields()) {
			if (entry.getType() == Translation.class) {
				//add((Translation)entry.get(LangProvider.class));
			}
		}
		add(ENUM_ANVIL_REPAIR);
		add(ENUM_BLOCK_BREAK);
	}
	
	private void add(Translation translation) {
		add(translation.key(), translation.localeMap().getOrDefault(locale, ""));
	}
	
	public static record Translation(String key, Map<String, String> localeMap) {
		public MutableComponent asComponent() {
			return Component.translatable(key());
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
