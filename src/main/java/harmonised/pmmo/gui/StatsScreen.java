package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
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
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StatsScreen extends Screen
{
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png" );
    private final List<IGuiEventListener> children = Lists.newArrayList();
    private static TileButton exitButton;

    Minecraft minecraft = Minecraft.getInstance();
    MainWindow sr = minecraft.getMainWindow();
    FontRenderer font = minecraft.fontRenderer;
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x, y;
    private StatsScrollPanel scrollPanel;
    private List<StatsEntry> statsEntries;
    private UUID uuid;
    private JType jType = JType.STATS;

    public StatsScreen( UUID uuid, ITextComponent titleIn )
    {
        super( titleIn );
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
        statsEntries = new ArrayList<>();
        ArrayList<TextComponent> text;

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        exitButton = new TileButton(x + boxWidth - 24, y - 8, 7, 0, "pmmo.exit", JType.NONE, (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new MainScreen( uuid, new TranslationTextComponent( "pmmo.skills" ) ) );
        });

        text = new ArrayList<>();
        text.add( new TranslationTextComponent( "pmmo.heartsSummary" ) );
        statsEntries.add( new StatsEntry( 0, 0, new TranslationTextComponent( "pmmo.hearts" ), text ) );
        scrollPanel = new StatsScrollPanel( new MatrixStack(), Minecraft.getInstance(), boxWidth - 40, boxHeight - 21, y + 10, x + 16, statsEntries );

        text = new ArrayList<>();
        text.add( new TranslationTextComponent( "pmmo.heartsSummary" ) );
        statsEntries.add( new StatsEntry( 0, 0, new TranslationTextComponent( "pmmo.hearts" ), text ) );
        scrollPanel = new StatsScrollPanel( new MatrixStack(), Minecraft.getInstance(), boxWidth - 40, boxHeight - 21, y + 10, x + 16, statsEntries );

        text = new ArrayList<>();
        text.add( new TranslationTextComponent( "pmmo.heartsSummary" ) );
        statsEntries.add( new StatsEntry( 0, 0, new TranslationTextComponent( "pmmo.hearts" ), text ) );
        scrollPanel = new StatsScrollPanel( new MatrixStack(), Minecraft.getInstance(), boxWidth - 40, boxHeight - 21, y + 10, x + 16, statsEntries );

        text = new ArrayList<>();
        text.add( new TranslationTextComponent( "pmmo.heartsSummary" ) );
        statsEntries.add( new StatsEntry( 0, 0, new TranslationTextComponent( "pmmo.hearts" ), text ) );
        scrollPanel = new StatsScrollPanel( new MatrixStack(), Minecraft.getInstance(), boxWidth - 40, boxHeight - 21, y + 10, x + 16, statsEntries );

        text = new ArrayList<>();
        text.add( new TranslationTextComponent( "pmmo.heartsSummary" ) );
        statsEntries.add( new StatsEntry( 0, 0, new TranslationTextComponent( "pmmo.hearts" ), text ) );
        scrollPanel = new StatsScrollPanel( new MatrixStack(), Minecraft.getInstance(), boxWidth - 40, boxHeight - 21, y + 10, x + 16, statsEntries );

        text = new ArrayList<>();
        text.add( new TranslationTextComponent( "pmmo.heartsSummary" ) );
        statsEntries.add( new StatsEntry( 0, 0, new TranslationTextComponent( "pmmo.hearts" ), text ) );
        scrollPanel = new StatsScrollPanel( new MatrixStack(), Minecraft.getInstance(), boxWidth - 40, boxHeight - 21, y + 10, x + 16, statsEntries );

        text = new ArrayList<>();
        text.add( new TranslationTextComponent( "pmmo.heartsSummary" ) );
        statsEntries.add( new StatsEntry( 0, 0, new TranslationTextComponent( "pmmo.hearts" ), text ) );
        scrollPanel = new StatsScrollPanel( new MatrixStack(), Minecraft.getInstance(), boxWidth - 40, boxHeight - 21, y + 10, x + 16, statsEntries );

        text = new ArrayList<>();
        text.add( new TranslationTextComponent( "pmmo.heartsSummary" ) );
        statsEntries.add( new StatsEntry( 0, 0, new TranslationTextComponent( "pmmo.hearts" ), text ) );
        scrollPanel = new StatsScrollPanel( new MatrixStack(), Minecraft.getInstance(), boxWidth - 40, boxHeight - 21, y + 10, x + 16, statsEntries );

        text = new ArrayList<>();
        text.add( new TranslationTextComponent( "pmmo.heartsSummary" ) );
        statsEntries.add( new StatsEntry( 0, 0, new TranslationTextComponent( "pmmo.hearts" ), text ) );
        scrollPanel = new StatsScrollPanel( new MatrixStack(), Minecraft.getInstance(), boxWidth - 40, boxHeight - 21, y + 10, x + 16, statsEntries );

        text = new ArrayList<>();
        text.add( new TranslationTextComponent( "pmmo.heartsSummary" ) );
        statsEntries.add( new StatsEntry( 0, 0, new TranslationTextComponent( "pmmo.hearts" ), text ) );
        scrollPanel = new StatsScrollPanel( new MatrixStack(), Minecraft.getInstance(), boxWidth - 40, boxHeight - 21, y + 10, x + 16, statsEntries );

        text = new ArrayList<>();
        text.add( new TranslationTextComponent( "pmmo.heartsSummary" ) );
        text.add( new TranslationTextComponent( "pmmo.heartsSummary" ) );
        statsEntries.add( new StatsEntry( 0, 0, new TranslationTextComponent( "pmmo.hearts" ), text ) );
        scrollPanel = new StatsScrollPanel( new MatrixStack(), Minecraft.getInstance(), boxWidth - 40, boxHeight - 21, y + 10, x + 16, statsEntries );

        text = new ArrayList<>();
        text.add( new TranslationTextComponent( "pmmo.heartsSummary" ) );
        statsEntries.add( new StatsEntry( 0, 0, new TranslationTextComponent( "pmmo.hearts" ), text ) );
        scrollPanel = new StatsScrollPanel( new MatrixStack(), Minecraft.getInstance(), boxWidth - 40, boxHeight - 21, y + 10, x + 16, statsEntries );

        text = new ArrayList<>();
        text.add( new TranslationTextComponent( "pmmo.heartsSummary" ) );
        statsEntries.add( new StatsEntry( 0, 0, new TranslationTextComponent( "pmmo.hearts" ), text ) );
        scrollPanel = new StatsScrollPanel( new MatrixStack(), Minecraft.getInstance(), boxWidth - 40, boxHeight - 21, y + 10, x + 16, statsEntries );

        text = new ArrayList<>();
        text.add( new TranslationTextComponent( "pmmo.heartsSummary" ) );
        statsEntries.add( new StatsEntry( 0, 0, new TranslationTextComponent( "pmmo.hearts" ), text ) );
        scrollPanel = new StatsScrollPanel( new MatrixStack(), Minecraft.getInstance(), boxWidth - 40, boxHeight - 21, y + 10, x + 16, statsEntries );

        text = new ArrayList<>();
        text.add( new TranslationTextComponent( "pmmo.heartsSummary" ) );
        statsEntries.add( new StatsEntry( 0, 0, new TranslationTextComponent( "pmmo.hearts" ), text ) );
        scrollPanel = new StatsScrollPanel( new MatrixStack(), Minecraft.getInstance(), boxWidth - 40, boxHeight - 21, y + 10, x + 16, statsEntries );


        if( !MainScreen.scrollAmounts.containsKey( jType ) )
            MainScreen.scrollAmounts.put( jType, 0 );
        scrollPanel.setScroll( MainScreen.scrollAmounts.get( jType ) );
        children.add( scrollPanel );
        addButton(exitButton);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        renderBackground( stack,  1 );

        if( font.getStringWidth( title.getString() ) > 220 )
            drawCenteredString( stack,  font, title.getString(), sr.getScaledWidth() / 2, y - 10, 0xffffff );
        else
            drawCenteredString( stack,  font, title.getString(), sr.getScaledWidth() / 2, y - 5, 0xffffff );

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        scrollPanel.render( stack, mouseX, mouseY, partialTicks );
        super.render( stack, mouseX, mouseY, partialTicks );
    }

    @Override
    public void renderBackground( MatrixStack stack, int p_renderBackground_1_)
    {
        if (this.minecraft != null)
        {
            this.fillGradient( stack, 0, 0, this.width, this.height, 0x66222222, 0x66333333 );
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent( this, stack ));
        }

        boxHeight = 256;
        boxWidth = 256;
        Minecraft.getInstance().getTextureManager().bindTexture( box );

        this.blit( stack,  x, y, 0, 0,  boxWidth, boxHeight );
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
    {
        scrollPanel.mouseScrolled(mouseX, mouseY, scroll);
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

        scrollPanel.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        scrollPanel.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        scrollPanel.mouseDragged( mouseX, mouseY, button, deltaX, deltaY) ;
        return super.mouseDragged( mouseX, mouseY, button, deltaX, deltaY );
    }

}
