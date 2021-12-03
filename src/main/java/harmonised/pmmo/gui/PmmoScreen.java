package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class PmmoScreen extends Screen
{
    protected final List<GuiEventListener> children = Lists.newArrayList();
    protected List<TileButton> tileButtons;

    Minecraft mc = Minecraft.getInstance();
    Window sr = mc.getWindow();
    Font font = mc.font;

    protected int boxWidth = 256;
    protected int boxHeight = 256;

    protected PmmoScreen(Component p_96550_)
    {
        super(p_96550_);
    }

    public static void enableAlpha(float alpha)
    {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void disableAlpha(float alpha)
    {
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
