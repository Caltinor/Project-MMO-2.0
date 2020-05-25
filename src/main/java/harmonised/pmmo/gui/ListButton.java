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
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
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
    private final ResourceLocation bar = XP.getResLoc( Reference.MOD_ID, "textures/gui/items.png" );
//    private final Screen screen = new SkillsScreen( new TranslationTextComponent( "pmmo.potato" ));
    public int elementOne, elementTwo;
    public int offsetOne, offsetTwo;
    public double mobWidth, mobHeight, mobScale;
    public boolean unlocked = true;
    public ItemStack itemStack;
    public String regKey, title, buttonText;
    public List<String> text = new ArrayList<>();
    public List<String> tooltipText = new ArrayList<>();
    Entity testEntity = null;
    LivingEntity entity = null;
    ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

    public ListButton(int posX, int posY, int elementOne, int elementTwo, String regKey, String type, String buttonText, IPressable onPress)
    {
        super(posX, posY, 32, 32, "", onPress);
        this.regKey = regKey;
        this.buttonText = buttonText;
        this.itemStack = new ItemStack( XP.getItem( regKey ) );
        this.elementOne = elementOne * 32;
        this.elementTwo = elementTwo * 32;

        if( ForgeRegistries.ENTITIES.containsKey( XP.getResLoc( regKey ) ) )
            testEntity = ForgeRegistries.ENTITIES.getValue( XP.getResLoc( regKey ) ).create( Minecraft.getInstance().world );

        if( testEntity instanceof LivingEntity )
            entity = (LivingEntity) testEntity;

        switch( type )
        {
            case "fishEnchantPool":
                this.title = new TranslationTextComponent( ForgeRegistries.ENCHANTMENTS.getValue( XP.getResLoc( regKey ) ).getDisplayName( 1 ).getString().replace( " I", "" ) ).getString();
                break;

            case "biome":
                this.title = new TranslationTextComponent( ForgeRegistries.BIOMES.getValue( XP.getResLoc( regKey ) ).getTranslationKey() ).getString();
                break;

            case "breedXp":
            case "tameXp":
            case "killReq":
                this.title = new TranslationTextComponent( ForgeRegistries.ENTITIES.getValue( XP.getResLoc( regKey ) ).getTranslationKey() ).getString();
                break;

            case "dimension":
                if( regKey.equals( "all_dimensions" ) )
                    this.title = new TranslationTextComponent( "pmmo.allDimensions" ).getFormattedText();
                else if( regKey.equals( "minecraft:overworld" ) || regKey.equals( "minecraft:the_nether" ) || regKey.equals( "minecraft:the_end" ) )
                    this.title = new TranslationTextComponent( regKey ).getFormattedText();
                else if( ForgeRegistries.MOD_DIMENSIONS.containsKey( XP.getResLoc( regKey ) ) )
                    this.title = new TranslationTextComponent( ForgeRegistries.MOD_DIMENSIONS.getValue( XP.getResLoc( regKey ) ).getRegistryName().toString() ).getFormattedText();
                break;

            default:
                this.title = new TranslationTextComponent( itemStack.getTranslationKey() ).getString();
                break;
        }

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

        for( String a : text )
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

        if( !itemStack.getItem().equals( Items.AIR ) && entity == null )
            itemRenderer.renderItemIntoGUI( itemStack, this.x + 8, this.y + 8 );

        if( entity != null )
        {
            mobHeight = entity.getSize( Pose.STANDING ).height;
            mobWidth = entity.getSize( Pose.STANDING ).width;
            mobScale = 27;

            if( mobHeight > 0 )
                mobScale /= Math.max(mobHeight, mobWidth);

            drawEntityOnScreen( this.x + this.width / 2, this.y + this.height - 2, (int) mobScale, entity );
        }

        this.renderBg(minecraft, x, y);
        int j = getFGColor();
        this.drawCenteredString(fontrenderer, this.buttonText, this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
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
