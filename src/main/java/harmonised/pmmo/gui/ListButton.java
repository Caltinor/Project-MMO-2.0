package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.pmmo.skills.XP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Arrays;
import java.util.List;

public class ListButton extends Button
{
    private final ResourceLocation bar = new ResourceLocation( Reference.MOD_ID, "textures/gui/items.png" );
    private final Screen screen = new SkillsScreen( new TranslationTextComponent( "pmmo.potato" ));
    public int element;
    public int offset;
    public Item item;

    public ListButton(int posX, int posY, int element, String regKey, String text, IPressable onPress)
    {
        super(posX, posY, 32, 32, text, onPress);
        this.item = XP.getItem( regKey );
        this.element = element * 32;

        if( element > 23 )
            offset = 192;
        else if( element > 15 )
            offset = 128;
        else if( element > 7 )
            offset = 64;
        else
            offset = 0;
    }

    @Override
    public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_)
    {
        Minecraft minecraft = Minecraft.getInstance();
        FontRenderer fontrenderer = minecraft.fontRenderer;
        minecraft.getTextureManager().bindTexture( bar );
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        if( this.isHovered() )
        {
            this.blit(this.x, this.y, this.offset + 32, this.element, this.width, this.height);

        }
        else
            this.blit(this.x, this.y, this.offset, this.element, this.width, this.height);
        this.renderBg(minecraft, p_renderButton_1_, p_renderButton_2_);
        int j = getFGColor();
        this.drawCenteredString(fontrenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
    }
}
