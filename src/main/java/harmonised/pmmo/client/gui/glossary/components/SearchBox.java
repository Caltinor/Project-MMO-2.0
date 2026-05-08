package harmonised.pmmo.client.gui.glossary.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * EditBox suitable for embedding inside another screen (especially container
 * screens like the player inventory) where the host's hotkeys would otherwise
 * intercept letter keys.
 *   <ul>
 *     <li>{@code keyPressed} consumes any key while focused (except ESC/TAB) so host hotkeys
 *         like the inventory's 'e' don't close the screen mid-search.</li>
 *     <li>Renders a clear (×) glyph at the right edge whenever there's text.</li>
 *     <li>Clicking the × empties the field and keeps the box focused for further input.</li>
 *   </ul>
 */
public class SearchBox extends EditBox {
    private static final int CLEAR_GLYPH_SIZE = 8;
    private static final int CLEAR_GLYPH_RIGHT_MARGIN = 2;
    private static final int CLEAR_COLOR_DEFAULT = 0xFFAAAAAA;
    private static final int CLEAR_COLOR_HOVER = 0xFFFFFFFF;

    public SearchBox(int width, int height) {
        super(Minecraft.getInstance().font, 0, 0, width, height, Component.literal("Search"));
        setHint(Component.literal("Search..."));
        setBordered(true);
    }

    /** X coordinate of the left edge of the clear glyph's clickable hitbox. */
    private int clearGlyphLeft() {
        return this.getX() + this.width - CLEAR_GLYPH_SIZE - CLEAR_GLYPH_RIGHT_MARGIN;
    }

    /** Y coordinate of the top of the clear glyph's clickable hitbox (vertically centered in the box). */
    private int clearGlyphTop() {
        return this.getY() + (this.height - CLEAR_GLYPH_SIZE) / 2;
    }

    /** True when the cursor is over the clear glyph and the field has text to clear. */
    private boolean isOverClearGlyph(double mouseX, double mouseY) {
        if (this.getValue().isEmpty()) return false;
        int left = clearGlyphLeft();
        int top = clearGlyphTop();
        return mouseX >= left && mouseX < left + CLEAR_GLYPH_SIZE
                && mouseY >= top && mouseY < top + CLEAR_GLYPH_SIZE;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
        // Vanilla EditBox.keyPressed only handles control keys (arrows, backspace, etc).
        // Letter keys fall through and would reach the host screen — which closes on its hotkey.
        // While focused, claim every key we don't pass back through (ESC and TAB still bubble
        // so the user can close the screen or move focus normally).
        if (handled || !isFocused()) return handled;
        return keyCode != GLFW.GLFW_KEY_ESCAPE && keyCode != GLFW.GLFW_KEY_TAB;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        if (this.getValue().isEmpty()) return;
        // Brighter when hovered, dim grey otherwise. The +1 nudges the glyph to optically center inside its 8px box.
        int glyphColor = isOverClearGlyph(mouseX, mouseY) ? CLEAR_COLOR_HOVER : CLEAR_COLOR_DEFAULT;
        graphics.drawString(Minecraft.getInstance().font, "×", clearGlyphLeft() + 1, clearGlyphTop(), glyphColor, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isOverClearGlyph(mouseX, mouseY)) {
            this.setValue("");
            // Refocus immediately so the user can start typing a new query without re-clicking.
            this.setFocused(true);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
