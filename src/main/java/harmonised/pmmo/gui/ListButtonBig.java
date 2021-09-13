package harmonised.pmmo.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.components.Button.OnPress;

import net.minecraft.client.renderer.entity.ItemRenderer;

public class ListButtonBig extends Button
{
    public static final Logger LOGGER = LogManager.getLogger();

    private final ResourceLocation items = XP.getResLoc( Reference.MOD_ID, "textures/gui/items_big.png" );
    private final ResourceLocation items2 = XP.getResLoc( Reference.MOD_ID, "textures/gui/items_big_2.png" );
//    private final ResourceLocation buttons = XP.getResLoc( Reference.MOD_ID, "textures/gui/buttons.png" );
//    private final Screen screen = new SkillsScreen( new TranslationTextComponent( "pmmo.potato" ));
    private int page = 0;
    public int elementOne, elementTwo;
    public int offsetOne, offsetTwo;
    public double mobWidth, mobHeight, mobScale;
    public boolean unlocked = true;
    public ItemStack itemStack;
    public String regKey, title, buttonText;
    public List<String> text = new ArrayList<>();
    public List<Component> tooltipText = new ArrayList<>();
    String playerName;
    Entity testEntity = null;
    LivingEntity entity = null;
    ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
    Minecraft minecraft = Minecraft.getInstance();

    public ListButtonBig(int posX, int posY, int elementOne, int elementTwo, String buttonText, String playerName, @Nullable String tooltip, OnPress onPress )
    {
        super(posX, posY, 64, 64, new TranslatableComponent( buttonText ), onPress);
//        this.regKey = regKey;
        this.buttonText = buttonText;
        this.itemStack = new ItemStack( XP.getItem( regKey ) );
        this.elementOne = elementOne * 64;
        this.elementTwo = elementTwo * 64;
        this.playerName = playerName;
        tooltipText.add( new TranslatableComponent( playerName ) );
        if( tooltip != null )
            this.tooltipText.add( new TranslatableComponent( tooltip ) );

        if( ForgeRegistries.ENTITIES.containsKey( XP.getResLoc( regKey ) ) )
            testEntity = ForgeRegistries.ENTITIES.getValue( XP.getResLoc( regKey ) ).create( Minecraft.getInstance().level );
        if( testEntity instanceof LivingEntity )
            entity = (LivingEntity) testEntity;

        if( elementOne > 3 )
            offsetOne = 128;
        else
            offsetOne = 0;

        if( elementTwo >= 7 )
        {
            page = 1;
            elementTwo -= 8;
        }

        if( elementTwo > 3 )
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
    public void renderButton( PoseStack stack, int mouseX, int mouseY, float partialTicks )
    {
        isHovered = mouseX > this.x + 3 && mouseY > this.y && mouseX < this.x + 60 && mouseY < this.y + 64;
        Minecraft minecraft = Minecraft.getInstance();
        Font fontrenderer = minecraft.font;
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        minecraft.getTextureManager().bind( items );
        this.blit( stack, this.x, this.y, this.offsetOne + ( this.isHovered() ? 64 : 0 ), this.elementOne, this.width, this.height);
        minecraft.getTextureManager().bind( page == 0 ? items : items2 );
        this.blit( stack, this.x, this.y, this.offsetTwo + ( this.isHovered() ? 64 : 0 ), this.elementTwo, this.width, this.height);
        if( !itemStack.getItem().equals( Items.AIR ) && entity == null )
            itemRenderer.renderGuiItem( itemStack, this.x + 8, this.y + 8 );

        if( entity != null )
        {
            mobHeight = entity.getDimensions( Pose.STANDING ).height;
            mobWidth = entity.getDimensions( Pose.STANDING ).width;
            mobScale = 54;

            if( mobHeight > 0 )
                mobScale /= Math.max(mobHeight, mobWidth);

            drawEntityOnScreen( this.x + this.width / 2, this.y + this.height - 2, (int) mobScale, entity );
        }

        this.renderBg( stack, minecraft, x, y );
        int j = getFGColor();
        this.drawCenteredString( stack, fontrenderer, this.buttonText, this.x + this.width / 2, this.y + (this.height - 8) / 2, j | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    public void clickAction()
    {
        LOGGER.debug( "Clicked " + this.title + " Button" );
    }

    public static void drawEntityOnScreen(int posX, int posY, int scale, LivingEntity p_228187_5_)
    {
        float f = (float) ( (System.currentTimeMillis() / 25D ) % 360);
        float f1 = 0;
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)posX, (float)posY, 1050.0F);
        RenderSystem.scalef(1.0F, 1.0F, -1.0F);
        PoseStack matrixstack = new PoseStack();
        matrixstack.translate(0.0D, 0.0D, 1000.0D);
        matrixstack.scale((float)scale, (float)scale, (float)scale);
        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion quaternion1 = Vector3f.XP.rotationDegrees(f1 * 20.0F);
        quaternion.mul(quaternion1);
        matrixstack.mulPose(quaternion);
        float f2 = p_228187_5_.yBodyRot;
        float f3 = p_228187_5_.yRot;
        float f4 = p_228187_5_.xRot;
        float f5 = p_228187_5_.yHeadRotO;
        float f6 = p_228187_5_.yHeadRot;
        p_228187_5_.yBodyRot = f;
        p_228187_5_.yRot = f;
        p_228187_5_.xRot = -f1 * 20.0F;
        p_228187_5_.yHeadRot = f;
        p_228187_5_.yHeadRotO = 0;
        EntityRenderDispatcher entityrenderermanager = Minecraft.getInstance().getEntityRenderDispatcher();
        quaternion1.conj();
        entityrenderermanager.overrideCameraOrientation(quaternion1);
        entityrenderermanager.setRenderShadow(false);
        MultiBufferSource.BufferSource irendertypebuffer$impl = Minecraft.getInstance().renderBuffers().bufferSource();
        entityrenderermanager.render(p_228187_5_, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixstack, irendertypebuffer$impl, 15728880);
        irendertypebuffer$impl.endBatch();
        entityrenderermanager.setRenderShadow(true);
        p_228187_5_.yBodyRot = f2;
        p_228187_5_.yRot = f3;
        p_228187_5_.xRot = f4;
        p_228187_5_.yHeadRotO = f5;
        p_228187_5_.yHeadRot = f6;
        RenderSystem.popMatrix();
    }
}
