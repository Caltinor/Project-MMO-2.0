package harmonised.pmmo.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class TileButtonBig extends Button
{
    private final ResourceLocation items = XP.getResLoc( Reference.MOD_ID, "textures/gui/items_big.png" );
    public int elementOne;
    public int offsetOne;
    public int elementTwo;
    public int offsetTwo;
    public int index;
    public String transKey;

    public TileButtonBig( int posX, int posY, int elementOne, int elementTwo, String transKey, String text, IPressable onPress )
    {
        super(posX, posY, 64, 64, text, onPress);
        this.elementOne = elementOne * 64;
        this.elementTwo = elementTwo * 64;
        this.transKey = transKey;

        if( elementOne > 3 )
            offsetOne = 128;
        else
            offsetOne = 0;

        if( elementTwo > 3 )
            offsetTwo = 128;
        else
            offsetTwo = 0;
    }

    @Override
    public void renderButton(int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_)
    {
        Minecraft minecraft = Minecraft.getInstance();
        FontRenderer fontrenderer = minecraft.fontRenderer;
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        minecraft.getTextureManager().bindTexture( items );
        this.blit(this.x, this.y, this.offsetOne + ( this.isHovered() ? 64 : 0 ), this.elementOne, this.width, this.height );
//        minecraft.getTextureManager().bindTexture( items );
        this.blit(this.x, this.y, this.offsetTwo + ( this.isHovered() ? 64 : 0 ), this.elementTwo, this.width, this.height );
        this.renderBg(minecraft, p_renderButton_1_, p_renderButton_2_);
        int j = getFGColor();
        this.drawCenteredString(fontrenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
    }
}
