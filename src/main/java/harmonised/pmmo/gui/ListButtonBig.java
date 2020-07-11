package harmonised.pmmo.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.LogHandler;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ListButtonBig extends Button
{
    private final ResourceLocation items = XP.getResLoc( Reference.MOD_ID, "textures/gui/items_big.png" );
//    private final ResourceLocation buttons = XP.getResLoc( Reference.MOD_ID, "textures/gui/buttons.png" );
    //    private final Screen screen = new SkillsScreen( new TranslationTextComponent( "pmmo.potato" ));
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
    LivingEntity entity = null;
    ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

    public ListButtonBig(int posX, int posY, int elementOne, int elementTwo, String buttonText, String playerName, @Nullable String tooltip, IPressable onPress )
    {
        super(posX, posY, 64, 64, new TranslationTextComponent( buttonText ), onPress);
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
            testEntity = ForgeRegistries.ENTITIES.getValue( XP.getResLoc( regKey ) ).create( Minecraft.getInstance().world );
        if( testEntity instanceof LivingEntity )
            entity = (LivingEntity) testEntity;

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
    public void renderButton( MatrixStack stack, int mouseX, int mouseY, float partialTicks )
    {
        isHovered = mouseX > this.x + 3 && mouseY > this.y && mouseX < this.x + 60 && mouseY < this.y + 64;
        Minecraft minecraft = Minecraft.getInstance();
        FontRenderer fontrenderer = minecraft.fontRenderer;
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        minecraft.getTextureManager().bindTexture( items );
        this.blit( stack, this.x, this.y, this.offsetOne + ( this.isHovered() ? 64 : 0 ), this.elementOne, this.width, this.height);
        this.blit( stack, this.x, this.y, this.offsetTwo + ( this.isHovered() ? 64 : 0 ), this.elementTwo, this.width, this.height);
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

        this.renderBg( stack, minecraft, x, y );
        int j = getFGColor();
        this.drawCenteredString( stack, fontrenderer, this.buttonText, this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
    }

    public void clickAction()
    {
        LogHandler.LOGGER.debug( "Clicked " + this.title + " Button" );
    }

    public static void drawEntityOnScreen(int posX, int posY, int scale, LivingEntity p_228187_5_)
    {
        float f = (float) ( (System.currentTimeMillis() / 25D ) % 360);
        float f1 = 0;
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)posX, (float)posY, 1050.0F);
        RenderSystem.scalef(1.0F, 1.0F, -1.0F);
        MatrixStack matrixstack = new MatrixStack();
        matrixstack.translate(0.0D, 0.0D, 1000.0D);
        matrixstack.scale((float)scale, (float)scale, (float)scale);
        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion quaternion1 = Vector3f.XP.rotationDegrees(f1 * 20.0F);
        quaternion.multiply(quaternion1);
        matrixstack.rotate(quaternion);
        float f2 = p_228187_5_.renderYawOffset;
        float f3 = p_228187_5_.rotationYaw;
        float f4 = p_228187_5_.rotationPitch;
        float f5 = p_228187_5_.prevRotationYawHead;
        float f6 = p_228187_5_.rotationYawHead;
        p_228187_5_.renderYawOffset = f;
        p_228187_5_.rotationYaw = f;
        p_228187_5_.rotationPitch = -f1 * 20.0F;
        p_228187_5_.rotationYawHead = f;
        p_228187_5_.prevRotationYawHead = 0;
        EntityRendererManager entityrenderermanager = Minecraft.getInstance().getRenderManager();
        quaternion1.conjugate();
        entityrenderermanager.setCameraOrientation(quaternion1);
        entityrenderermanager.setRenderShadow(false);
        IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        entityrenderermanager.renderEntityStatic(p_228187_5_, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixstack, irendertypebuffer$impl, 15728880);
        irendertypebuffer$impl.finish();
        entityrenderermanager.setRenderShadow(true);
        p_228187_5_.renderYawOffset = f2;
        p_228187_5_.rotationYaw = f3;
        p_228187_5_.rotationPitch = f4;
        p_228187_5_.prevRotationYawHead = f5;
        p_228187_5_.rotationYawHead = f6;
        RenderSystem.popMatrix();
    }
}
