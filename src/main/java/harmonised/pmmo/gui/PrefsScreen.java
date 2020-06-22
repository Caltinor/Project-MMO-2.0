package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.potion.Effect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.widget.Slider;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class PrefsScreen extends Screen
{
    private final List<IGuiEventListener> children = Lists.newArrayList();
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png" );
    private static Button exitButton;

    MainWindow sr = Minecraft.getInstance().getMainWindow();
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x, y, scrollX, scrollY, buttonX, buttonY, accumulativeHeight, buttonsSize, buttonsLoaded, futureHeight, minCount, maxCount;
    private PrefsScrollPanel scrollPanel;
    private ArrayList<ListButton> listButtons = new ArrayList<>();
    private ArrayList<PrefSlider> prefSliders = new ArrayList<>();
    private ITextComponent title;

    public PrefsScreen( ITextComponent titleIn )
    {
        super(titleIn);
        this.title = titleIn;
    }

//    @Override
//    public boolean isPauseScreen()
//    {
//        return false;
//    }

    @Override
    protected void init()
    {
        x = (sr.getScaledWidth() / 2) - (boxWidth / 2);
        y = (sr.getScaledHeight() / 2) - (boxHeight / 2);
        scrollX = x + 16;
        scrollY = y + 10;
        buttonX = scrollX + 4;

        exitButton = new TileButton(x + boxWidth - 24, y - 8, 7, 0, "", "", (button) ->
        {
            Minecraft.getInstance().displayGuiScreen( new MainScreen( Minecraft.getInstance().player.getUniqueID(), new TranslationTextComponent( "pmmo.stats" ) ) );
        });

        listButtons = new ArrayList<>();

        prefSliders.add( new PrefSlider("girth", "", "%", 0, 100, 50, 50, false, true ) );
        prefSliders.add( new PrefSlider("length", "", "%", 0, 100, 50, 50, false, true ) );

        int i = 0;

        for( PrefSlider prefSlider : prefSliders )
        {
            addButton( prefSlider.button );
            addButton( prefSlider.slider );
            addButton( prefSlider.textField );
            prefSlider.updateX( x + 24 );
            prefSlider.updateY( y + 24 + 18 * i++ );
        }

//        new Slider()
//        new TextFieldWidget()

        scrollPanel = new PrefsScrollPanel( Minecraft.getInstance(), boxWidth - 40, boxHeight - 21, scrollY, scrollX,  listButtons );
        children.add( scrollPanel );
        addButton(exitButton);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        renderBackground( 1 );

        if( font.getStringWidth( title.getString() ) > 220 )
            drawCenteredString( font, title.getFormattedText(), sr.getScaledWidth() / 2, y - 10, 0xffffff );
        else
            drawCenteredString( font, title.getFormattedText(), sr.getScaledWidth() / 2, y - 5, 0xffffff );

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );

        scrollPanel.render( mouseX, mouseY, partialTicks );

        ListButton button;

        accumulativeHeight = 0;
        buttonsSize = listButtons.size();

        super.render(mouseX, mouseY, partialTicks);

        for( int i = 0; i < buttonsSize; i++ )
        {
            button = listButtons.get( i );

            buttonX = mouseX - button.x;
            buttonY = mouseY - button.y;

            if( mouseY >= scrollPanel.getTop() && mouseY <= scrollPanel.getBottom() && buttonX >= 0 && buttonX < 32 && buttonY >= 0 && buttonY < 32 )
            {
                renderTooltip("over button", mouseX, mouseY );
            }

            accumulativeHeight += button.getHeight();
        }

//        renderTooltip( mouseX + " " + mouseY, mouseX, mouseY );
    }

    @Override
    public void renderBackground(int p_renderBackground_1_)
    {
        if (this.minecraft != null)
        {
            this.fillGradient(0, 0, this.width, this.height, 0x66222222, 0x66333333 );
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
        scrollPanel.mouseScrolled( mouseX, mouseY, scroll );
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

        for( ListButton a : listButtons )
        {
            int buttonX = (int) mouseX - a.x;
            int buttonY = (int) mouseY- a.y;

            if( mouseY >= scrollPanel.getTop() && mouseY <= scrollPanel.getBottom() && buttonX >= 0 && buttonX < 32 && buttonY >= 0 && buttonY < 32 )
            {
                a.onClick( mouseX, mouseY );
            }
        }
        scrollPanel.mouseClicked( mouseX, mouseY, button );
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        scrollPanel.mouseReleased( mouseX, mouseY, button );
        return super.mouseReleased( mouseX, mouseY, button );
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        for( PrefSlider prefSlider : prefSliders )
        {
            if( mouseX > prefSlider.slider.x && mouseX < prefSlider.slider.x - prefSlider.slider.getWidth() && mouseY > prefSlider.slider.y && mouseY < prefSlider.slider.y - prefSlider.slider.getHeight() )
            {
                prefSlider.textField.setText( DP.dpSoft( prefSlider.slider.getValue() ) );
                break;
            }
        }

        scrollPanel.mouseDragged( mouseX, mouseY, button, deltaX, deltaY );
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public static TranslationTextComponent getTransComp( String translationKey, Object... args )
    {
        return new TranslationTextComponent( translationKey, args );
    }

}