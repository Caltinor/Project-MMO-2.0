package harmonised.pmmo.compat.crafttweaker;

import java.util.Locale;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.BracketEnum;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import org.openzen.zencode.java.ZenCodeType;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ObjectType;
import harmonised.pmmo.api.enums.ReqType;


@ZenRegister
public class ExtendedEnums {
	@NativeTypeRegistration(value = ObjectType.class, zenCodeName = "mods.pmmo.ObjectType")
	@BracketEnum("pmmo:objecttype")
	@ZenRegister
	public static class ExtendedObjectType {
		@ZenCodeType.Method
	    @ZenCodeType.Getter("commandString")
	    public static String getCommandString(ObjectType internal) {
	        return "<constant:pmmo:objecttype:" + internal.name().toLowerCase(Locale.ROOT) + ">";
	    }
	}

	@NativeTypeRegistration(value = EventType.class, zenCodeName = "mods.pmmo.EventType")
	@BracketEnum("pmmo:eventtype")
	@ZenRegister
	public static class ExtendedEventType {
		@ZenCodeType.Method
	    @ZenCodeType.Getter("commandString")
	    public static String getCommandString(EventType internal) {
	        return "<constant:pmmo:eventtype:" + internal.name().toLowerCase(Locale.ROOT) + ">";
	    }
	}

	@NativeTypeRegistration(value = ReqType.class, zenCodeName = "mods.pmmo.ReqType")
	@BracketEnum("pmmo:reqtype")
	@ZenRegister
	public static class ExtendedReqType {
		@ZenCodeType.Method
	    @ZenCodeType.Getter("commandString")
	    public static String getCommandString(ReqType internal) {
	        return "<constant:pmmo:reqtype:" + internal.name().toLowerCase(Locale.ROOT) + ">";
	    }
	}

	@NativeTypeRegistration(value = ModifierDataType.class, zenCodeName = "mods.pmmo.ModifierDataType")
	@BracketEnum("pmmo:modifierdatatype")
	@ZenRegister
	public static class ExtendedModifierDataType {
		@ZenCodeType.Method
	    @ZenCodeType.Getter("commandString")
	    public static String getCommandString(ModifierDataType internal) {
	        return "<constant:pmmo:modifierdatatype:" + internal.name().toLowerCase(Locale.ROOT) + ">";
	    }
	}
}
