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
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListButton extends Button
{
    private final ResourceLocation bar = new ResourceLocation( Reference.MOD_ID, "textures/gui/items.png" );
//    private final Screen screen = new SkillsScreen( new TranslationTextComponent( "pmmo.potato" ));
    public int elementOne, elementTwo;
    public int offsetOne, offsetTwo;
    public ItemStack itemStack;
    public String regKey, title;
    public List<ITextComponent> text = new ArrayList<>();

    public ListButton(int posX, int posY, int elementOne, int elementTwo, String regKey, String type, IPressable onPress)
    {
        super(posX, posY, 32, 32, "", onPress);
        this.regKey = regKey;
        this.itemStack = new ItemStack( XP.getItem( regKey ) );
        this.elementOne = elementOne * 32;
        this.elementTwo = elementTwo * 32;

        if( type.equals( "biome" ) )
            this.title = new TranslationTextComponent( ForgeRegistries.BIOMES.getValue( new ResourceLocation( regKey ) ).getTranslationKey() ).getString();
        else
            this.title = new TranslationTextComponent( itemStack.getTranslationKey() ).getString();

        if( elementOne > 23 )
            offsetOne = 192;
        else if( elementOne > 15 )
            offsetOne = 128;
        else if( elementOne > 7 )
            offsetOne = 64;
        else
            offsetOne = 0;

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
    public int getHeight()
    {
        int height = 11;

        for( ITextComponent a : text )
        {
            height += 9;
        }

        if( height > 32 )
            return height;
        else
            return 32;
    }

    @Override
    public void renderButton(int x, int y, float partialTicks)
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
            this.blit(this.x, this.y, this.offsetOne + 32, this.elementOne, this.width, this.height);
            this.blit(this.x, this.y, this.offsetTwo + 32, this.elementTwo, this.width, this.height);
        }
        else
        {
            this.blit(this.x, this.y, this.offsetOne, this.elementOne, this.width, this.height);
            this.blit(this.x, this.y, this.offsetTwo, this.elementTwo, this.width, this.height);
        }



        this.renderBg(minecraft, x, y);
        int j = getFGColor();
        this.drawCenteredString(fontrenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
    }
}
