package harmonised.pmmo.client.gui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

public class PlayerStatScreen extends Screen{
	private static final TextComponent GUI_NAME = new TextComponent("player_stats");
	
	protected PlayerStatScreen() {
		super(GUI_NAME);
	}
}
