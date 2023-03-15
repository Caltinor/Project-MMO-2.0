package harmonised.pmmo.client.utils;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.MutableComponent;

public class ClientUtils {
	public static List<ClientTooltipComponent> ctc(Minecraft mc, MutableComponent component, int width) {
		return mc.font.split(component, width).stream().map(fcs -> ClientTooltipComponent.create(fcs)).toList();
	}
}
