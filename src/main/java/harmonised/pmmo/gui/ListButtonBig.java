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

    public ListButtonBig(int posX, int posY, int elementOne, int elementTwo, String buttonText, String playerName, @Nullable String tooltip, IPressable onPress )
    {
        super(posX, posY, 64, 64, buttonText, onPress);
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
            testEntity = ForgeRegistries.ENTITIES.getValue( XP.getResLoc( regKey ) ).create( Minecraft.getMinecraft().world );
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

    @Override
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
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.hovered);
        GlStateManager.enableBlend();
        GlStateManager.defaultBlendFunc();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        minecraft.getTextureManager().bindTexture( items );
        this.drawTexturedModalRect(this.x, this.y, this.offsetOne + ( this.hovered ? 64 : 0 ), this.elementOne, this.width, this.height);
        this.drawTexturedModalRect(this.x, this.y, this.offsetTwo + ( this.hovered ? 64 : 0 ), this.elementTwo, this.width, this.height);
        if( !itemStack.getItem().equals( Items.AIR ) && entity == null )
            itemRenderer.renderItemIntoGUI( itemStack, this.x + 8, this.y + 8 );

        if( entity != null )
        {
            mobHeight = entity.getSize( Pose.STANDING ).height;
            mobWidth = entity.getSize( Pose.STANDING ).width;
            mobScale = 54;

            if( mobHeight > 0 )
                mobScale /= Math.max(mobHeight, mobWidth);

            GuiInventory.drawEntityOnScreen( this.x + this.width / 2, this.y + this.height - 2, (int) mobScale, mouseX, mouseY, entity );
        }

        this.renderBg(minecraft, x, y);
        int j = getFGColor();
        this.drawCenteredString(fontrenderer, this.buttonText, this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
    }

    public void clickAction()
    {
        LOGGER.info( "Clicked " + this.title + " Button" );
    }
}
