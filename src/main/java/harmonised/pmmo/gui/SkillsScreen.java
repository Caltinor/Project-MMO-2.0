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
        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        Button exitButton = new TileButton(x + boxWidth - 24, y - 8, 0, 7, I18n.format(""), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen(new MainScreen(new TranslationTextComponent("pmmo.potato")));
        });

        addButton(exitButton);

        Button wearButton = new TileButton(x + 24, y + 24, 1, 3, I18n.format("Wear"), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen(new ScrollScreen(new TranslationTextComponent("pmmo.armor"), "wear", Minecraft.getInstance().player));
        });

        addButton(wearButton);

        Button toolButton = new TileButton( x + 24 + 36, y + 24, 1, 3, I18n.format("Tool" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.tool" ), "tool", Minecraft.getInstance().player ) );
        });

        addButton( toolButton );

        Button weaponButton = new TileButton( x + 24 + 36 * 2, y + 24, 1, 3, I18n.format("Weapon" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.weapon" ), "weapon", Minecraft.getInstance().player ) );
        });

        addButton( weaponButton );

        Button useButton = new TileButton( x + 24 + 36 * 3, y + 24, 1, 3, I18n.format("Use" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.use" ), "use", Minecraft.getInstance().player ) );
        });

        addButton( useButton );

        Button placeButton = new TileButton( x + 24 + 36 * 4, y + 24, 1, 8, I18n.format("Place" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.place" ), "place", Minecraft.getInstance().player ) );
        });

        addButton( placeButton );

        Button breakButton = new TileButton( x + 24 + 36 * 5, y + 24, 1, 0, I18n.format("Break" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.break" ), "break", Minecraft.getInstance().player ) );
        });

        addButton( breakButton );

        Button biomeButton = new TileButton( x + 24, y + 24 + 36, 1, 9, I18n.format("Biome" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.biome" ), "biome", Minecraft.getInstance().player ) );
        });

        addButton( biomeButton );

        Button oreButton = new TileButton( x + 24 + 36, y + 24 + 36, 1, 0, I18n.format("Ores" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.ore" ), "ore", Minecraft.getInstance().player ) );
        });

        addButton( oreButton );

        Button logButton = new TileButton( x + 24 + 36 * 2, y + 24 + 36, 1, 0, I18n.format("Logs" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.log" ), "log", Minecraft.getInstance().player ) );
        });

        addButton( logButton );

        Button plantButton = new TileButton( x + 24 + 36 * 3, y + 24 + 36, 1, 0, I18n.format("Plants" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.plant" ), "plant", Minecraft.getInstance().player ) );
        });

        addButton( plantButton );

        Button heldXpButton = new TileButton( x + 24 + 36 * 4, y + 24 + 36, 1, 0, I18n.format("Held" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.held" ), "held", Minecraft.getInstance().player ) );
        });

        addButton( heldXpButton );

        Button wornXpButton = new TileButton( x + 24 + 36 * 5, y + 24 + 36, 1, 0, I18n.format("Worn" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.worn" ), "worn", Minecraft.getInstance().player ) );
        });

        addButton( wornXpButton );

        Button breedXpButton = new TileButton( x + 24, y + 24 + 36 * 2, 1, 0, I18n.format("BreedXp" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.breedXp" ), "breedXp", Minecraft.getInstance().player ) );
        });

        addButton( breedXpButton );

        Button tameXpButton = new TileButton( x + 24 + 36, y + 24 + 36 * 2, 1, 0, I18n.format("TameXp" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.tameXp" ), "tameXp", Minecraft.getInstance().player ) );
        });

        addButton( tameXpButton );

        Button craftXpButton = new TileButton( x + 24 + 36 * 2, y + 24 + 36 * 2, 1, 0, I18n.format("CraftXp" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.craftXp" ), "craftXp", Minecraft.getInstance().player ) );
        });

        addButton( craftXpButton );

        Button breakXpButton = new TileButton( x + 24 + 36 * 3, y + 24 + 36 * 2, 1, 0, I18n.format("BreakXp" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.breakXp" ), "breakXp", Minecraft.getInstance().player ) );
        });

        addButton( breakXpButton );

        Button dimensionButton = new TileButton( x + 24 + 36 * 4, y + 24 + 36 * 2, 1, 9, I18n.format("Dimension" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.dimension" ), "dimension", Minecraft.getInstance().player ) );
        });

        addButton( dimensionButton );

        Button fishPoolButton = new TileButton( x + 24 + 36 * 5, y + 24 + 36 * 2, 1, 9, I18n.format("FishPool" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.fishPool" ), "fishPool", Minecraft.getInstance().player ) );
        });

        addButton( fishPoolButton );

        Button mobButton = new TileButton( x + 24, y + 24 + 36 * 3, 1, 9, I18n.format("Mobs" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.mobInfo" ), "killreq", Minecraft.getInstance().player ) );
        });

        addButton( mobButton );

        Button fishEnchantButton = new TileButton( x + 24 + 36, y + 24 + 36 * 3, 1, 9, I18n.format("FishEnchant" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.fishEnchantPool" ), "fishEnchantPool", Minecraft.getInstance().player ) );
        });

        addButton( fishEnchantButton );

        Button salvageToButton = new TileButton( x + 24 + 36 * 2, y + 24 + 36 * 3, 1, 9, I18n.format("SalvagesTo" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.salvagesTo" ), "salvagesTo", Minecraft.getInstance().player ) );
        });

        addButton( salvageToButton );

        Button salvageFromButton = new TileButton( x + 24 + 36 * 3, y + 24 + 36 * 3, 1, 9, I18n.format("SalvagesFrom" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ScrollScreen( new TranslationTextComponent( "pmmo.salvagesFrom" ), "salvagesFrom", Minecraft.getInstance().player ) );
        });

        addButton( salvageFromButton );

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