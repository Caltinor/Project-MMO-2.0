package harmonised.pmmo.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.skills.Skill;
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
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.components.Button.OnPress;

import net.minecraft.client.renderer.entity.ItemRenderer;

public class ListButton extends Button
{
    private final ResourceLocation items = XP.getResLoc( Reference.MOD_ID, "textures/gui/items.png" );
    private final ResourceLocation buttons = XP.getResLoc( Reference.MOD_ID, "textures/gui/buttons.png" );
//    private final Screen screen = new SkillsScreen( new TranslatableComponent( "pmmo.potato" ));
    public int elementOne, elementTwo;
    public int offsetOne, offsetTwo;
    public double mobWidth, mobHeight, mobScale;
    public boolean unlocked = true;
    public ItemStack itemStack;
    public String regKey, title, buttonText;
    public List<Component> text = new ArrayList<>();
    public List<Component> tooltipText = new ArrayList<>();
    Entity testEntity = null;
    LivingEntity entity = null;
    ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
    Minecraft minecraft = Minecraft.getInstance();

    public ListButton( int posX, int posY, int elementOne, int elementTwo, String regKey, JType jType, String buttonText, OnPress onPress )
    {
        super(posX, posY, 32, 32, new TranslatableComponent( "" ), onPress);
        this.regKey = regKey;
        this.buttonText = buttonText;
        this.itemStack = new ItemStack( XP.getItem( regKey ) );
        this.elementOne = elementOne * 32;
        this.elementTwo = elementTwo * 32;

        if( ForgeRegistries.ENTITIES.containsKey( XP.getResLoc( regKey ) ) )
            testEntity = ForgeRegistries.ENTITIES.getValue( XP.getResLoc( regKey ) ).create( Minecraft.getInstance().level );

        if( testEntity instanceof LivingEntity )
            entity = (LivingEntity) testEntity;

        switch( jType )
        {
            case FISH_ENCHANT_POOL:
                this.title = new TranslatableComponent( ForgeRegistries.ENCHANTMENTS.getValue( XP.getResLoc( regKey ) ).getFullname( 1 ).getString().replace( " I", "" ) ).getString();
                break;

            case XP_VALUE_BREED:
            case XP_VALUE_TAME:
            case REQ_KILL:
                this.title = new TranslatableComponent( ForgeRegistries.ENTITIES.getValue( XP.getResLoc( regKey ) ).getDescriptionId() ).getString();
                break;

            case DIMENSION:
                if( regKey.equals( "all_dimensions" ) )
                    this.title = new TranslatableComponent( "pmmo.allDimensions" ).getString();
                else if( regKey.equals( "minecraft:overworld" ) || regKey.equals( "minecraft:the_nether" ) || regKey.equals( "minecraft:the_end" ) )
                    this.title = new TranslatableComponent( regKey ).getString();
//                else if( ForgeRegistries.MOD_DIMENSIONS.containsKey( XP.getResLoc( regKey ) ) )
//                    this.title = new TranslatableComponent( ForgeRegistries.MOD_DIMENSIONS.getValue( XP.getResLoc( regKey ) ).getRegistryName().toString() ).getString();
                //COUT
                break;

            case STATS:
                this.title = new TranslatableComponent( "pmmo." + regKey ).setStyle( Skill.getSkillStyle( regKey ) ).getString();
                break;

            case HISCORE:
                if( XP.playerNames.containsValue( regKey ) )
                    this.title = new TextComponent( regKey ).setStyle( Skill.getSkillStyle( regKey ) ).getString();
                else
                    this.title = new TranslatableComponent( "pmmo." + regKey ).setStyle( Skill.getSkillStyle( regKey ) ).getString();
                break;

            case REQ_BIOME:
//                this.title = new TranslatableComponent( ForgeRegistries.BIOMES.getValue( XP.getResLoc( regKey ) ).getTranslationKey() ).getString();
                this.title = new TranslatableComponent( regKey ).getString();
                break;

            default:
                this.title = new TranslatableComponent( itemStack.getDescriptionId() ).getString();
                break;
        }

        switch( regKey )
        {
            case "pmmo.otherCrafts":
            case "pmmo.otherAnimals":
            case "pmmo.otherPassiveMobs":
            case "pmmo.otherAggresiveMobs":
                this.title = new TranslatableComponent( new TranslatableComponent( regKey ).getString() ).getString();
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

        for( Component a : text )
        {
            height += 9;
        }

        if( height > 32 )
            return height;
        else
            return 32;
    }

    public void clickActionGlossary()
    {
        GlossaryScreen.setButtonsToKey( regKey );
        Minecraft.getInstance().setScreen( new GlossaryScreen( Minecraft.getInstance().player.getUUID(), new TranslatableComponent( "pmmo.glossary" ), false ) );
    }

    public void clickActionSkills()
    {
        if( XP.playerNames.containsValue( regKey ) )
            Minecraft.getInstance().setScreen( new ListScreen( XP.playerUUIDs.get( regKey ), new TranslatableComponent( "" ), regKey, JType.SKILLS, Minecraft.getInstance().player ) );
        else
            Minecraft.getInstance().setScreen( new ListScreen( Minecraft.getInstance().player.getUUID(), new TranslatableComponent( "" ), regKey, JType.HISCORE, Minecraft.getInstance().player ) );
    }

    @Override
    public void renderButton( PoseStack stack, int x, int y, float partialTicks)
    {
        Minecraft minecraft = Minecraft.getInstance();
        Font fontrenderer = minecraft.font;
        RenderSystem.clearColor(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHovered());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        minecraft.getTextureManager().bindForSetup( buttons );
        this.blit( stack, this.x, this.y, this.offsetOne + ( this.isHovered() ? 32 : 0 ), this.elementOne, this.width, this.height);
        minecraft.getTextureManager().bindForSetup( items );
        this.blit( stack, this.x, this.y, this.offsetTwo + ( this.isHovered() ? 32 : 0 ), this.elementTwo, this.width, this.height);
        if( !itemStack.getItem().equals( Items.AIR ) && entity == null )
            itemRenderer.renderGuiItem( itemStack, this.x + 8, this.y + 8 );

        if( entity != null )
        {
            mobHeight = entity.getDimensions( Pose.STANDING ).height;
            mobWidth = entity.getDimensions( Pose.STANDING ).width;
            mobScale = 27;

            if( mobHeight > 0 )
                mobScale /= Math.max(mobHeight, mobWidth);

            //COUT
//            drawEntityOnScreen( this.x + this.width / 2, this.y + this.height - 2, (int) mobScale, entity );
        }

        this.renderBg( stack, minecraft, x, y);
        int j = getFGColor();
        this.drawCenteredString( stack, fontrenderer, this.buttonText, this.x + this.width / 2, this.y + (this.height - 8) / 2, j | Mth.ceil(this.alpha * 255.0F) << 24);
    }

//    public static void drawEntityOnScreen(int posX, int posY, int scale, LivingEntity p_228187_5_)
//    {
//        float f = (float) ( (System.currentTimeMillis() / 25D ) % 360);
//        float f1 = 0;
//        RenderSystem.pushMatrix();
//        RenderSystem.translatef((float)posX, (float)posY, 1050.0F);
//        RenderSystem.scalef(1.0F, 1.0F, -1.0F);
//        PoseStack matrixstack = new PoseStack();
//        matrixstack.translate(0.0D, 0.0D, 1000.0D);
//        matrixstack.scale((float)scale, (float)scale, (float)scale);
//        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
//        Quaternion quaternion1 = Vector3f.XP.rotationDegrees(f1 * 20.0F);
//        quaternion.mul(quaternion1);
//        matrixstack.mulPose(quaternion);
//        float f2 = p_228187_5_.yBodyRot;
//        float f3 = p_228187_5_.yRot;
//        float f4 = p_228187_5_.xRot;
//        float f5 = p_228187_5_.yHeadRotO;
//        float f6 = p_228187_5_.yHeadRot;
//        p_228187_5_.yBodyRot = f;
//        p_228187_5_.yRot = f;
//        p_228187_5_.xRot = -f1 * 20.0F;
//        p_228187_5_.yHeadRot = f;
//        p_228187_5_.yHeadRotO = 0;
//        EntityRenderDispatcher entityrenderermanager = Minecraft.getInstance().getEntityRenderDispatcher();
//        quaternion1.conj();
//        entityrenderermanager.overrideCameraOrientation(quaternion1);
//        entityrenderermanager.setRenderShadow(false);
//        MultiBufferSource.BufferSource irendertypebuffer$impl = Minecraft.getInstance().renderBuffers().bufferSource();
//        entityrenderermanager.render(p_228187_5_, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixstack, irendertypebuffer$impl, 15728880);
//        irendertypebuffer$impl.endBatch();
//        entityrenderermanager.setRenderShadow(true);
//        p_228187_5_.yBodyRot = f2;
//        p_228187_5_.yRot = f3;
//        p_228187_5_.xRot = f4;
//        p_228187_5_.yHeadRotO = f5;
//        p_228187_5_.yHeadRot = f6;
//        RenderSystem.popMatrix();
//    }
}