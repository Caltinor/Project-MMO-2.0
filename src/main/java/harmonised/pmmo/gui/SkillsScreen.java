package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.XP;
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

import java.util.ArrayList;
import java.util.List;

public class SkillsScreen extends Screen
{
    private final List<IGuiEventListener> children = Lists.newArrayList();
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png");

    MainWindow sr = Minecraft.getInstance().getMainWindow();;
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x;
    private int y;
    private MyScrollPanel myList;

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
        List<Button> tileButtons = new ArrayList<>();
        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        Button exitButton = new TileButton(x + boxWidth - 24, y - 8, 0, 7, I18n.format(""), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new MainScreen( new TranslationTextComponent( "pmmo.potato" ) ) );
        });
        Button wearButton = new TileButton(0, 0, 1, 3, "", (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen ( new TranslationTextComponent( "pmmo.wearTitle" ), "wear", Minecraft.getInstance().player));
        });
        Button toolButton = new TileButton( 0, 0, 1, 3, "", (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.toolTitle" ), "tool", Minecraft.getInstance().player ) );
        });
        Button weaponButton = new TileButton( 0, 0, 1, 3, "", (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.weaponTitle" ), "weapon", Minecraft.getInstance().player ) );
        });
        Button useButton = new TileButton( 0, 0, 1, 3, "", (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.useTitle" ), "use", Minecraft.getInstance().player ) );
        });
        Button placeButton = new TileButton( 0, 0, 1, 8, "", (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.placeTitle" ), "place", Minecraft.getInstance().player ) );
        });
        Button breakButton = new TileButton( 0, 0, 1, 0, "", (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.breakTitle" ), "break", Minecraft.getInstance().player ) );
        });
        Button biomeButton = new TileButton( 0, 0, 1, 9, "", (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.biomeTitle" ), "biome", Minecraft.getInstance().player ) );
        });
        Button oreButton = new TileButton( 0, 0, 1, 0, "", (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.oreTitle" ), "ore", Minecraft.getInstance().player ) );
        });
        Button logButton = new TileButton( 0, 0, 1, 0, "", (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.logTitle" ), "log", Minecraft.getInstance().player ) );
        });
        Button plantButton = new TileButton( 0, 0, 1, 0, "", (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.plantTitle" ), "plant", Minecraft.getInstance().player ) );
        });
        Button heldXpButton = new TileButton( 0, 0, 1, 0, "", (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.heldTitle" ), "held", Minecraft.getInstance().player ) );
        });
        Button wornXpButton = new TileButton( 0, 0, 1, 0, "", (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.wornTitle" ), "worn", Minecraft.getInstance().player ) );
        });
        Button breedXpButton = new TileButton( 0, 0, 1, 0, "", (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.breedXpTitle" ), "breedXp", Minecraft.getInstance().player ) );
        });
        Button tameXpButton = new TileButton( 0, 0, 1, 0, "", (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.tameXpTitle" ), "tameXp", Minecraft.getInstance().player ) );
        });
        Button craftXpButton = new TileButton( 0, 0, 1, 0, "", (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.craftXpTitle" ), "craftXp", Minecraft.getInstance().player ) );
        });
        Button breakXpButton = new TileButton( 0, 0, 1, 0, "", (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.breakXpTitle" ), "breakXp", Minecraft.getInstance().player ) );
        });
        Button dimensionButton = new TileButton( 0, 0, 1, 9, "", (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.dimensionTitle" ), "dimension", Minecraft.getInstance().player ) );
        });
        Button fishPoolButton = new TileButton( 0, 0, 1, 9, "", (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.fishPoolTitle" ), "fishPool", Minecraft.getInstance().player ) );
        });
        Button mobButton = new TileButton( 0, 0, 1, 9, "", (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.mobTitle" ), "kill", Minecraft.getInstance().player ) );
        });
        Button fishEnchantButton = new TileButton( 0, 0, 1, 9, "", (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.fishEnchantTitle" ), "fishEnchantPool", Minecraft.getInstance().player ) );
        });
        Button salvageToButton = new TileButton( 0, 0, 1, 9, "", (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.salvagesToTitle" ), "salvagesTo", Minecraft.getInstance().player ) );
        });
        Button salvageFromButton = new TileButton( 0, 0, 1, 9, "", (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.salvagesFromTitle" ), "salvagesFrom", Minecraft.getInstance().player ) );
        });

        addButton(exitButton);

        tileButtons.add(wearButton);
        tileButtons.add( toolButton );
        tileButtons.add( weaponButton );
        tileButtons.add( useButton );
        tileButtons.add( placeButton );
        tileButtons.add( breakButton );
        tileButtons.add( biomeButton );
        tileButtons.add( oreButton );
        tileButtons.add( logButton );
        tileButtons.add( plantButton );
        tileButtons.add( heldXpButton );
        tileButtons.add( wornXpButton );
        tileButtons.add( breedXpButton );
        tileButtons.add( tameXpButton );
        tileButtons.add( craftXpButton );
        tileButtons.add( breakXpButton );
        tileButtons.add( dimensionButton );
        tileButtons.add( fishPoolButton );
        tileButtons.add( mobButton );
        tileButtons.add( fishEnchantButton );
        tileButtons.add( salvageToButton );
        tileButtons.add( salvageFromButton );

        int col = 0;
        int row = 0;

        for( Button button : tileButtons )
        {
            button.x = x + 24 + col * 36;
            button.y = y + 24 + row * 36;
            addButton( button );
            if( ++col > 5 )
            {
                col = 0;
                row++;
            }
        }

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
//                    tileButtons.add( button );
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