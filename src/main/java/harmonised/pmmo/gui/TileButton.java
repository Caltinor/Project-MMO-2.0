package harmonised.pmmo.gui;

import harmonised.pmmo.config.JType;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class TileButton extends GuiButton
{
    private final ResourceLocation items = XP.getResLoc( Reference.MOD_ID, "textures/gui/items.png" );
    private final ResourceLocation items2 = XP.getResLoc( Reference.MOD_ID, "textures/gui/items2.png" );
    private final ResourceLocation buttons = XP.getResLoc( Reference.MOD_ID, "textures/gui/buttons.png" );
    public IPressable onPress;
    public int elementOne;
    public int offsetOne;
    public int elementTwo;
    public int offsetTwo;
    public int index;
    public int page = 0;
    public String transKey;
    public JType jType;

    public TileButton( int id, int posX, int posY, int elementOne, int elementTwo, String transKey, JType jType, IPressable onPress )
    {
        super( id, posX, posY, 32, 32, "" );

        this.jType = jType;

        this.elementOne = elementOne * 32;
        this.elementTwo = elementTwo * 32;
        this.transKey = transKey;
        this.onPress = onPress;

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
    public void drawButton( Minecraft mc, int mouseX, int mouseY, float partialTicks )
    {
        hovered = mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
        FontRenderer fontrenderer = mc.fontRenderer;
        mc.getTextureManager().bindTexture( buttons );
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        mc.getTextureManager().bindTexture( buttons );
        this.drawTexturedModalRect(this.x, this.y, this.offsetOne + ( this.hovered ? 32 : 0 ), this.elementOne, this.width, this.height );
        mc.getTextureManager().bindTexture( page == 0 ? items : items2 );
        this.drawTexturedModalRect(this.x, this.y, this.offsetTwo + ( this.hovered ? 32 : 0 ), this.elementTwo, this.width, this.height );
    }

    public interface IPressable
    {
        void onPress( TileButton button );
    }

    @Override
    public boolean mousePressed( Minecraft mc, int mouseX, int mouseY )
    {
        if( mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height )
        {
            this.onPress.onPress( this );
            return true;
        }
        else
            return false;
    }

    public void onPress()
    {
        onPress.onPress( this );
    }
}
