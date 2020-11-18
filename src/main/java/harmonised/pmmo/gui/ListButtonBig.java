package harmonised.pmmo.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.GlStateManager;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.Pose;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ListButtonBig extends Button
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
    public void renderButton(int mouseX, int mouseY, double partialTicks)
    {
        isHovered = mouseX > this.x + 3 && mouseY > this.y && mouseX < this.x + 60 && mouseY < this.y + 64;
        Minecraft minecraft = Minecraft.getMinecraft();
        FontRenderer fontrenderer = minecraft.fontRenderer;
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHovered());
        GlStateManager.enableBlend();
        GlStateManager.defaultBlendFunc();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        minecraft.getTextureManager().bindTexture( items );
        this.blit(this.x, this.y, this.offsetOne + ( this.isHovered() ? 64 : 0 ), this.elementOne, this.width, this.height);
        this.blit(this.x, this.y, this.offsetTwo + ( this.isHovered() ? 64 : 0 ), this.elementTwo, this.width, this.height);
        if( !itemStack.getItem().equals( Items.AIR ) && entity == null )
            itemRenderer.renderItemIntoGUI( itemStack, this.x + 8, this.y + 8 );

        if( entity != null )
        {
            mobHeight = entity.getSize( Pose.STANDING ).height;
            mobWidth = entity.getSize( Pose.STANDING ).width;
            mobScale = 54;

            if( mobHeight > 0 )
                mobScale /= Math.max(mobHeight, mobWidth);

            drawEntityOnScreen( this.x + this.width / 2, this.y + this.height - 2, (int) mobScale, entity );
        }

        this.renderBg(minecraft, x, y);
        int j = getFGColor();
        this.drawCenteredString(fontrenderer, this.buttonText, this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
    }

    public void clickAction()
    {
        LOGGER.info( "Clicked " + this.title + " Button" );
    }

    public static void drawEntityOnScreen(int posX, int posY, int scale, EntityLiving p_228187_5_)
    {
        double f = (double) ( (System.currentTimeMillis() / 25D ) % 360);
        double f1 = 0;
        GlStateManager.pushMatrix();
        GlStateManager.translatef((double)posX, (double)posY, 1050.0F);
        GlStateManager.scalef(1.0F, 1.0F, -1.0F);
        MatrixStack matrixstack = new MatrixStack();
        matrixstack.translate(0.0D, 0.0D, 1000.0D);
        matrixstack.scale((double)scale, (double)scale, (double)scale);
        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion quaternion1 = Vector3f.XP.rotationDegrees(f1 * 20.0F);
        quaternion.multiply(quaternion1);
        matrixstack.rotate(quaternion);
        double f2 = p_228187_5_.renderYawOffset;
        double f3 = p_228187_5_.rotationYaw;
        double f4 = p_228187_5_.rotationPitch;
        double f5 = p_228187_5_.prevRotationYawHead;
        double f6 = p_228187_5_.rotationYawHead;
        p_228187_5_.renderYawOffset = f;
        p_228187_5_.rotationYaw = f;
        p_228187_5_.rotationPitch = -f1 * 20.0F;
        p_228187_5_.rotationYawHead = f;
        p_228187_5_.prevRotationYawHead = 0;
        EntityRendererManager entityrenderermanager = Minecraft.getMinecraft().getRenderManager();
        quaternion1.conjugate();
        entityrenderermanager.setCameraOrientation(quaternion1);
        entityrenderermanager.setRenderShadow(false);
        IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getMinecraft().getRenderTypeBuffers().getBufferSource();
        entityrenderermanager.renderEntityStatic(p_228187_5_, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixstack, irendertypebuffer$impl, 15728880);
        irendertypebuffer$impl.finish();
        entityrenderermanager.setRenderShadow(true);
        p_228187_5_.renderYawOffset = f2;
        p_228187_5_.rotationYaw = f3;
        p_228187_5_.rotationPitch = f4;
        p_228187_5_.prevRotationYawHead = f5;
        p_228187_5_.rotationYawHead = f6;
        GlStateManager.popMatrix();
    }
}
