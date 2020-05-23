package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class SkillsScreen extends Screen
{
    private final List<IGuiEventListener> children = Lists.newArrayList();
    private final ResourceLocation box = new ResourceLocation( Reference.MOD_ID, "textures/gui/screenboxy.png" );

    MainWindow sr = Minecraft.getInstance().getMainWindow();;
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x;
    private int y;
    private MyScrollPanel myList;
    private Button exitButton;
    private Button wearButton, toolButton, weaponButton, useButton, placeButton, breakButton, biomeButton;

    public SkillsScreen(ITextComponent titleIn)
    {
        super(titleIn);
    }

//    @Override
//    public boolean isPauseScreen()
//    {
//        return false;
//    }

    @Override
    protected void init()
    {
        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        exitButton = new TileButton( x + boxWidth - 24, y - 8, 0, 2, I18n.format("" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new MainScreen( new TranslationTextComponent( "pmmo.potato" ) ) );
        });

        addButton( exitButton );

        wearButton = new TileButton( x + 24, y + 24, 1, 3, I18n.format("Wear" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.armor" ), "wear", Minecraft.getInstance().player ) );
        });

        addButton( wearButton );

        wearButton = new TileButton( x + 24 + 36, y + 24, 1, 3, I18n.format("Wear" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.armor" ), "wear", Minecraft.getInstance().player ) );
        });

        addButton( wearButton );

        toolButton = new TileButton( x + 24 + 36 * 2, y + 24, 1, 3, I18n.format("Tool" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.tool" ), "tool", Minecraft.getInstance().player ) );
        });

        addButton( toolButton );

        weaponButton = new TileButton( x + 24 + 36 * 3, y + 24, 1, 3, I18n.format("Weapon" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.weapon" ), "weapon", Minecraft.getInstance().player ) );
        });

        addButton( weaponButton );

        useButton = new TileButton( x + 24 + 36 * 4, y + 24, 1, 3, I18n.format("Use" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.use" ), "use", Minecraft.getInstance().player ) );
        });

        addButton( useButton );

        placeButton = new TileButton( x + 24 + 36 * 5, y + 24, 1, 3, I18n.format("Place" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.place" ), "place", Minecraft.getInstance().player ) );
        });

        addButton( placeButton );

        breakButton = new TileButton( x + 24, y + 24 + 36, 1, 3, I18n.format("Break" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.break" ), "break", Minecraft.getInstance().player ) );
        });

        addButton( breakButton );

        biomeButton = new TileButton( x + 24 + 36, y + 24 + 36, 1, 3, I18n.format("Biome" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.biome" ), "biome", Minecraft.getInstance().player ) );
        });

        addButton( biomeButton );

        int column = 1;
        int skillID;
//        for( int i = 0; i < 4; i++ )
//        {
//            for( int j = 1; j <= 6; j++ )
//            {
//                skillID = i * 6 + j;
//                if(Skill.getSkill( j ) != Skill.INVALID_SKILL )
//                {
//                    Button button = new TileButton( x + 24 + (j - 1) * 34, y + 16 + i * 34, 1, 0, I18n.format("" ), (something) ->
//                    {
//                        something.
//                    });
//
//                    addButton( button );
//                }
//
//                if( i % 6 == 0 )
//                    column++;
//            }
//        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        renderBackground( 1 );

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderBackground(int p_renderBackground_1_)
    {
        if (this.minecraft != null)
        {
            this.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this));
        }

        boxHeight = 256;
        boxWidth = 256;
        Minecraft.getInstance().getTextureManager().bindTexture( box );

        this.blit( x, y, 0, 0,  boxWidth, boxHeight );
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
    {
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

}