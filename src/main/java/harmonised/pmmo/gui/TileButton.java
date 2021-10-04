package harmonised.pmmo.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.TranslatableComponent;

import net.minecraft.client.gui.components.Button.OnPress;

public class TileButton extends Button
{
    private final ResourceLocation items = XP.getResLoc( Reference.MOD_ID, "textures/gui/items.png" );
    private final ResourceLocation items2 = XP.getResLoc( Reference.MOD_ID, "textures/gui/items2.png" );
    private final ResourceLocation buttons = XP.getResLoc( Reference.MOD_ID, "textures/gui/buttons.png" );
    private int page = 0;
    public int elementOne;
    public int offsetOne;
    public int elementTwo;
    public int offsetTwo;
    public int index;
    public String transKey;
    public JType jType;

    public TileButton(int posX, int posY, int elementOne, int elementTwo, String transKey, JType jType, OnPress onPress )
    {
        super(posX, posY, 32, 32, new TranslatableComponent( "" ), onPress);

        this.jType = jType;

        this.elementOne = elementOne * 32;
        this.elementTwo = elementTwo * 32;
        this.transKey = transKey;

        if( elementOne > 23 )
            offsetOne = 192;
        else if( elementOne > 15 )
            offsetOne = 128;
        else if( elementOne > 7 )
            offsetOne = 64;
        else
            offsetOne = 0;

        if( elementTwo >= 32 )
        {
            page = 1;
            elementTwo -= 32;
        }

        if( elementTwo > 23 )
            offsetTwo = 192;
        else if( elementTwo > 15 )
            offsetTwo = 128;
        else if( elementTwo > 7 )
            offsetTwo = 64;
        else
            offsetTwo = 0;
    }

    @Override
    public void renderButton(PoseStack stack, int p_renderButton_1_, int p_renderButton_2_, float p_renderButton_3_)
    {
        Minecraft minecraft = Minecraft.getInstance();
//        FontRenderer fontrenderer = minecraft.fontRenderer;
        RenderSystem.clearColor(1.0F, 1.0F, 1.0F, this.alpha);
//        int i = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        minecraft.getTextureManager().bindForSetup( buttons );
        this.blit( stack, this.x, this.y, this.offsetOne + ( this.isHovered() ? 32 : 0 ), this.elementOne, this.width, this.height );
        minecraft.getTextureManager().bindForSetup( page == 0 ? items : items2 );
        this.blit( stack, this.x, this.y, this.offsetTwo + ( this.isHovered() ? 32 : 0 ), this.elementTwo, this.width, this.height );
        this.renderBg( stack, minecraft, p_renderButton_1_, p_renderButton_2_);
//        int j = getFGColor();
//        drawCenteredString( stack, fontrenderer, new TranslatableComponent( transKey ).getString(), this.x + this.width / 2, this.y + (this.height - 8) / 2, 0xffffff );
    }
}