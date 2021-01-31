package harmonised.pmmo.gui;

import harmonised.pmmo.config.JType;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class ListButton extends GuiButton
{
    private final ResourceLocation items = XP.getResLoc( Reference.MOD_ID, "textures/gui/items.png" );
    private final ResourceLocation buttons = XP.getResLoc( Reference.MOD_ID, "textures/gui/buttons.png" );
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
    Entity testEntity = null;
    EntityLiving entity = null;
    ItemRenderer itemRenderer = Minecraft.getMinecraft().getItemRenderer();
    ScaledResolution sr;

    public ListButton( int id, int posX, int posY, int elementOne, int elementTwo, String regKey, JType jType, String buttonText, IPressable onPress )
    {
        super( id, posX, posY, 32, 32, "" );
        this.regKey = regKey;
        this.buttonText = buttonText;
        this.itemStack = new ItemStack( XP.getItem( regKey ) );
        this.elementOne = elementOne * 32;
        this.elementTwo = elementTwo * 32;
        this.onPress = onPress;

        if( ForgeRegistries.ENTITIES.containsKey( XP.getResLoc( regKey ) ) )
            testEntity = ForgeRegistries.ENTITIES.getValue( XP.getResLoc( regKey ) ).newInstance( Minecraft.getMinecraft().world );

        if( testEntity instanceof EntityLiving )
            entity = (EntityLiving) testEntity;

        sr = new ScaledResolution( Minecraft.getMinecraft() );

        switch( jType )
        {
            case FISH_ENCHANT_POOL:
                this.title = new TextComponentTranslation( ForgeRegistries.ENCHANTMENTS.getValue( XP.getResLoc( regKey ) ).getTranslatedName( 1 ).replace( " I", "" ) ).getUnformattedText();
                break;

            case XP_VALUE_BREED:
            case XP_VALUE_TAME:
            case REQ_KILL:
                try
                {
                    this.title = new TextComponentTranslation( ForgeRegistries.ENTITIES.getValue( XP.getResLoc( regKey ) ).getName() ).getUnformattedText();
                }
                catch( Exception e )
                {
                    this.title = "No Name";
                }
                break;

//            case DIMENSION:
//                if( regKey.equals( "all_dimensions" ) )
//                    this.title = new TextComponentTranslation( "pmmo.allDimensions" ).getFormattedText();
//                else if( regKey.equals( "minecraft:overworld" ) || regKey.equals( "minecraft:the_nether" ) || regKey.equals( "minecraft:the_end" ) )
//                    this.title = new TextComponentTranslation( regKey ).getFormattedText();
//                else if( ForgeRegistries.MOD_DIMENSIONS.containsKey( XP.getResLoc( regKey ) ) )
//                    this.title = new TextComponentTranslation( ForgeRegistries.MOD_DIMENSIONS.getValue( XP.getResLoc( regKey ) ).getRegistryName().toString() ).getFormattedText();
//                break;
            //COUT GUI DIMENSIONS

            case STATS:
                this.title = new TextComponentTranslation( "pmmo." + regKey ).setStyle( Skill.getSkillStyle( regKey ) ).getFormattedText();
                break;

            case HISCORE:
                if( XP.playerNames.containsValue( regKey ) )
                    this.title = new TextComponentString( regKey ).setStyle( Skill.getSkillStyle( regKey ) ).getFormattedText();
                else
                    this.title = new TextComponentTranslation( "pmmo." + regKey ).setStyle( Skill.getSkillStyle( regKey ) ).getFormattedText();
                break;

            case REQ_BIOME:
//                this.title = new TextComponentTranslation( ForgeRegistries.BIOMES.getValue( XP.getResLoc( regKey ) ).getTranslationKey() ).getUnformattedText();
                this.title = new TextComponentTranslation( regKey ).getUnformattedText();
                break;

            default:
                this.title = new TextComponentTranslation( itemStack.getDisplayName() ).getUnformattedText();
                break;
        }

        switch( regKey )
        {
            case "pmmo.otherCrafts":
            case "pmmo.otherAnimals":
            case "pmmo.otherPassiveMobs":
            case "pmmo.otherAggresiveMobs":
                this.title = new TextComponentTranslation( new TextComponentTranslation( regKey ).getFormattedText() ).getUnformattedText();
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

    public int getHeight()
    {
        int height = 11;

        for( String a : text )
        {
            height += 9;
        }

        return Math.max( height, 32 );
    }

    public void clickActionGlossary()
    {
        this.playPressSound( Minecraft.getMinecraft().getSoundHandler() );
        GlossaryScreen.setButtonsToKey( regKey );
        Minecraft.getMinecraft().displayGuiScreen( new GlossaryScreen( Minecraft.getMinecraft().player.getUniqueID(), new TextComponentString( "pmmo.glossary" ), false ) );
    }

    public void clickActionSkills()
    {
        this.playPressSound( Minecraft.getMinecraft().getSoundHandler() );
        if( XP.playerNames.containsValue( regKey ) )
            Minecraft.getMinecraft().displayGuiScreen( new ListScreen( XP.playerUUIDs.get( regKey ), new TextComponentString( "" ), regKey, JType.SKILLS, Minecraft.getMinecraft().player ) );
        else
            Minecraft.getMinecraft().displayGuiScreen( new ListScreen( Minecraft.getMinecraft().player.getUniqueID(), new TextComponentString( "" ), regKey, JType.HISCORE, Minecraft.getMinecraft().player ) );
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
        this.drawTexturedModalRect(this.x, this.y, this.offsetOne + ( this.hovered ? 32 : 0 ), this.elementOne, this.width, this.height);
        mc.getTextureManager().bindTexture( items );
        this.drawTexturedModalRect(this.x, this.y, this.offsetTwo + ( this.hovered ? 32 : 0 ), this.elementTwo, this.width, this.height);
        if( !itemStack.getItem().equals( Items.AIR ) && entity == null )
            mc.getRenderItem().renderItemIntoGUI( itemStack, this.x + 8, this.y + 8 );

        if( entity != null )
        {
            mobHeight = entity.height;
            mobWidth = entity.width;
            mobScale = 27;

            if( mobHeight > 0 )
                mobScale /= Math.max(mobHeight, mobWidth);

            GuiInventory.drawEntityOnScreen( this.x + this.width / 2, this.y + this.height - 2, (int) mobScale, -(mouseX - x - width/2F), -(mouseY - y - height/2F), entity );
        }

        this.drawCenteredString(fontrenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, 0xffffff );
    }

    public interface IPressable
    {
        void onPress( ListButton button );
    }

    public void onPress()
    {
        onPress.onPress( this );
    }

    @Override
    public boolean mousePressed( Minecraft mc, int mouseX, int mouseY )
    {
        if( hovered )
        {
            onPress();
            return true;
        }
        else
            return false;
    }
}
