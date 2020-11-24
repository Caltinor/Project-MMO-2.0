package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.*;

public class MainScreen extends Screen
{
    private final List<IGuiEventListener> children = Lists.newArrayList();
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png" );
    private final ResourceLocation logo = XP.getResLoc( Reference.MOD_ID, "textures/gui/logo.png" );
    private static TileButton exitButton;
    public static Map<JType, Integer> scrollAmounts = new HashMap<>();

    Minecraft minecraft = Minecraft.getInstance();
    MainWindow sr = minecraft.getMainWindow();
    FontRenderer font = minecraft.fontRenderer;
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x;
    private int y;
    private List<TileButton> tileButtons;
    private UUID uuid;

    public MainScreen( UUID uuid, ITextComponent titleIn )
    {
        super(titleIn);
        this.uuid = uuid;
    }

//    @Override
//    public boolean isPauseScreen()
//    {
//        return false;
//    }

    @Override
    protected void init()
    {
        tileButtons = new ArrayList<>();

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        exitButton = new TileButton(x + boxWidth - 24, y - 8, 7, 0, "pmmo.exit", JType.NONE, (something) ->
        {
            Minecraft.getInstance().player.closeScreen();
        });

        TileButton glossaryButton = new TileButton(x + 24 + 36, y + 24 + 36 * 4, 3, 5, "pmmo.glossary", JType.NONE, (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new GlossaryScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), true ) );
        });

        TileButton creditsButton = new TileButton( x + 24 + 36 * 2, y + 24 + 36 * 4, 3, 4, "pmmo.credits", JType.NONE, (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new CreditsScreen( uuid, new TranslationTextComponent( ((TileButton) button).transKey ), JType.CREDITS ) );
        });

        TileButton prefsButton = new TileButton( x + 24 + 36 * 3, y + 24 + 36 * 4, 3, 7, "pmmo.preferences", JType.NONE, (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new PrefsChoiceScreen( new TranslationTextComponent( ((TileButton) button).transKey ) ) );
        });


        TileButton skillsButton = new TileButton( x + 24 + 36 * 4, y + 24 + 36 * 4, 3, 6, "pmmo.stats", JType.NONE, (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid,  new TranslationTextComponent( ((TileButton) button).transKey ), "", JType.SKILLS, Minecraft.getInstance().player ) );
        });

//        TileButton statsButton = new TileButton( x + 24 + 36 * 5, y + 24 + 36 * 4, 3, 6, "pmmo.skills", JType.NONE, (button) ->
//        {
//            Minecraft.getInstance().displayGuiScreen( new ListScreen( uuid,  new TranslationTextComponent( ((TileButton) button).transKey ), "", JType.STATS, Minecraft.getInstance().player ) );
//        });

        addButton(exitButton);
        tileButtons.add(glossaryButton);
        tileButtons.add(creditsButton);
        tileButtons.add(prefsButton);
        tileButtons.add(skillsButton);
//        tileButtons.add(statsButton);

        for( TileButton button : tileButtons )
        {
            addButton( button );
        }
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        renderBackground( stack,  1 );
        super.render( stack, mouseX, mouseY, partialTicks );

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

//        fillGradient( stack, x + 20, y + 52, x + 232, y + 164, 0x22444444, 0x33222222);

        for( TileButton button : tileButtons )
        {
            if( mouseX > button.x && mouseY > button.y && mouseX < button.x + 32 && mouseY < button.y + 32 )
                renderTooltip( stack, new TranslationTextComponent( button.transKey ), mouseX, mouseY );
        }

        RenderSystem.enableBlend();
        Minecraft.getInstance().getTextureManager().bindTexture( logo );
        this.blit( stack,  sr.getScaledWidth() / 2 - 100, sr.getScaledHeight() / 2 - 80, 0, 0,  200, 60 );
    }

    @Override
    public void renderBackground( MatrixStack stack, int p_renderBackground_1_)
    {
        if (this.minecraft != null)
        {
            this.fillGradient( stack, 0, 0, this.width, this.height, 0x66222222, 0x66333333 );
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent( this, stack ));
        }
        else
            this.renderBackground( stack, p_renderBackground_1_ );


        boxHeight = 256;
        boxWidth = 256;
        Minecraft.getInstance().getTextureManager().bindTexture( box );
        RenderSystem.disableBlend();
        this.blit( stack,  x, y, 0, 0,  boxWidth, boxHeight );
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
    {
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if( button == 1 )
        {
            exitButton.onPress();
            return true;
        }
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
