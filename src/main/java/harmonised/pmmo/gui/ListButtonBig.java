package harmonised.pmmo.gui;


import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.*;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;

import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ListButtonBig extends GuiButton
{
    public static final Logger LOGGER = LogManager.getLogger();

    private final ResourceLocation items = XP.getResLoc( Reference.MOD_ID, "textures/gui/items_big.png" );
//    private final ResourceLocation buttons = XP.getResLoc( Reference.MOD_ID, "textures/gui/buttons.png" );
    //    private final Screen screen = new SkillsScreen( new TextComponentTranslation( "pmmo.potato" ));
    public IPressable onPress;
    public int elementOne, elementTwo;
    public int offsetOne, offsetTwo;
    public double mobWidth, mobHeight, mobScale;
    public boolean unlocked = true;
    public ItemStack itemStack;
    public String regKey, title, buttonText;
    public List<String> text = new ArrayList<>();
    public List<String> tooltipText = new ArrayList<>();
    String playerName;
    Entity testEntity = null;
    EntityLiving entity = null;
    ItemRenderer itemRenderer = Minecraft.getMinecraft().getItemRenderer();

    public ListButtonBig( int id, int posX, int posY, int elementOne, int elementTwo, String buttonText, String playerName, @Nullable String tooltip, IPressable onPress )
    {
        super( id, posX, posY, 64, 64, "" );
//        this.regKey = regKey;
        this.buttonText = buttonText;
        this.itemStack = new ItemStack( XP.getItem( regKey ) );
        this.elementOne = elementOne * 64;
        this.elementTwo = elementTwo * 64;
        this.playerName = playerName;
        tooltipText.add( playerName );
        if( tooltip != null )
            this.tooltipText.add( tooltip );

        if( ForgeRegistries.ENTITIES.containsKey( XP.getResLoc( regKey ) ) )
            testEntity = ForgeRegistries.ENTITIES.getValue( XP.getResLoc( regKey ) ).newInstance( Minecraft.getMinecraft().world );
        if( testEntity instanceof EntityLiving )
            entity = (EntityLiving) testEntity;

        if( elementOne > 3 )
            offsetOne = 128;
        else
            offsetOne = 0;

        if( elementTwo> 3 )
            offsetTwo = 128;
        else
            offsetTwo = 0;
    }

    public int getHeight()
    {
        return height;
    }

    @Override
    public void drawButton( Minecraft mc, int mouseX, int mouseY, float partialTicks)
    {
        hovered = mouseX > this.x + 3 && mouseY > this.y && mouseX < this.x + 60 && mouseY < this.y + 64;
        Minecraft minecraft = Minecraft.getMinecraft();
        FontRenderer fontrenderer = minecraft.fontRenderer;
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        minecraft.getTextureManager().bindTexture( items );
        this.drawTexturedModalRect(this.x, this.y, this.offsetOne + ( this.hovered ? 64 : 0 ), this.elementOne, this.width, this.height);
        this.drawTexturedModalRect(this.x, this.y, this.offsetTwo + ( this.hovered ? 64 : 0 ), this.elementTwo, this.width, this.height);
        if( !itemStack.getItem().equals( Items.AIR ) && entity == null )
            itemRenderer.renderItemIntoGUI( itemStack, this.x + 8, this.y + 8 );

        if( entity != null )
        {
            mobHeight = entity.height;
            mobWidth = entity.width;
            mobScale = 54;

            if( mobHeight > 0 )
                mobScale /= Math.max(mobHeight, mobWidth);

            GuiInventory.drawEntityOnScreen( this.x + this.width / 2, this.y + this.height - 2, (int) mobScale, mouseX, mouseY, entity );
        }

        this.drawCenteredString(fontrenderer, this.buttonText, this.x + this.width / 2, this.y + (this.height - 8) / 2, 0xffffff );
    }

    public void clickAction()
    {
        LOGGER.info( "Clicked " + this.title + " Button" );
    }

    public interface IPressable
    {
        void onPress( ListButtonBig button );
    }

    @Override
    public boolean mousePressed( Minecraft mc, int mouseX, int mouseY )
    {
        this.onPress.onPress( this );
        return true;
    }
}
