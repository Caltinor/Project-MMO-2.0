package harmonised.pmmo.gui;

import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StatsScreen extends GuiScreen
{
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png" );
    private static TileButton exitButton;

    Minecraft mc = Minecraft.getMinecraft();
    ScaledResolution sr = new ScaledResolution( mc );
    FontRenderer font = mc.fontRenderer;
    private ITextComponent title;
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x, y;
    private StatsScrollPanel scrollPanel;
    private List<StatsEntry> statsEntries;
    private UUID uuid;
    private JType jType = JType.STATS;

    public StatsScreen( UUID uuid, ITextComponent titleIn )
    {
        super();
        this.title = titleIn;
        this.uuid = uuid;
    }

//    @Override
//    public boolean isPauseScreen()
//    {
//        return false;
//    }

    @Override
    public void initGui()
    {
        sr = new ScaledResolution( mc );
        statsEntries = new ArrayList<>();
        ITextComponent title;
        ArrayList<ITextComponent> text;

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        exitButton = new TileButton(1337, x + boxWidth - 24, y - 8, 7, 0, "pmmo.exit", JType.NONE, (something) ->
        {
            Minecraft.getMinecraft().displayGuiScreen( new MainScreen( uuid, new TextComponentTranslation( "pmmo.skills" ) ) );
        });

        EntityPlayer player = Minecraft.getMinecraft().player;

        text = new ArrayList<>();
        title = new TextComponentTranslation( "pmmo.damage" );
        text.add( new TextComponentTranslation( "pmmo.damageBonusMelee", Skill.COMBAT.getLevel( player ) / FConfig.levelsPerDamageMelee ) );
        text.add( new TextComponentTranslation( "pmmo.damageBonusArchery", Skill.ARCHERY.getLevel( player ) / FConfig.levelsPerDamageArchery ) );
        text.add( new TextComponentTranslation( "pmmo.damageBonusMagic", Skill.MAGIC.getLevel( player ) / FConfig.levelsPerDamageMagic ) );
        statsEntries.add( new StatsEntry( 0, 0, title, text ) );
        
        scrollPanel = new StatsScrollPanel( Minecraft.getMinecraft(), boxWidth - 40, boxHeight - 21, y + 10, x + 16, statsEntries );
        if( !MainScreen.scrollAmounts.containsKey( jType ) )
            MainScreen.scrollAmounts.put( jType, 0 );
        scrollPanel.setScroll( MainScreen.scrollAmounts.get( jType ) );
        addButton(exitButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawBackground( 1 );

        if( font.getStringWidth( title.getFormattedText() ) > 220 )
            drawCenteredString( font, title.getFormattedText(), sr.getScaledWidth() / 2, y - 10, 0xffffff );
        else
            drawCenteredString( font, title.getFormattedText(), sr.getScaledWidth() / 2, y - 5, 0xffffff );

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        scrollPanel.drawScreen( mouseX, mouseY, partialTicks );
        super.drawScreen( mouseX, mouseY, partialTicks );
    }

    @Override
    public void drawBackground(int p_renderBackground_1_)
    {
        if (this.mc != null)
        {
            this.drawGradientRect(0, 0, this.width, this.height, 0x66222222, 0x66333333 );
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this));
        }

        boxHeight = 256;
        boxWidth = 256;
        Minecraft.getMinecraft().getTextureManager().bindTexture( box );

        this.drawTexturedModalRect( x, y, 0, 0,  boxWidth, boxHeight );
    }

//    @Override
//    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
//    {
//        scrollPanel.mouseScrolled(mouseX, mouseY, scroll);
//        return super.mouseScrolled(mouseX, mouseY, scroll);
//    }

    @Override
    public void handleMouseInput() throws IOException
    {
        super.handleMouseInput();

        scrollPanel.scroll( Mouse.getEventDWheel() );
    }

    @Override
    public void mouseClicked( int mouseX, int mouseY, int button) throws IOException
    {
        if( button == 1 )
        {
            exitButton.onPress();
            return;
        }

        scrollPanel.mouseClicked(mouseX, mouseY, button);
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased( int mouseX, int mouseY, int button)
    {
        scrollPanel.mouseReleased(mouseX, mouseY, button);
        super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void mouseClickMove( int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick )
    {
        scrollPanel.mouseClickMove( mouseX, mouseY, clickedMouseButton, timeSinceLastClick );
        super.mouseClickMove( mouseX, mouseY, clickedMouseButton, timeSinceLastClick );
    }

}
