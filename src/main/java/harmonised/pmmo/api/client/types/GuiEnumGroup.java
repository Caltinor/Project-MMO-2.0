package harmonised.pmmo.api.client.types;

import java.util.Arrays;
import java.util.stream.Stream;

public interface GuiEnumGroup {
	String getName();

	static GuiEnumGroup[] combine(GuiEnumGroup[]...options) {
		GuiEnumGroup[] output = options[0];
		for (int i = 1; i < options.length; i++) {
            output = Stream.concat(Arrays.stream(output), Arrays.stream(options[i])).toArray(GuiEnumGroup[]::new);
		}
		return output;
	}
}
