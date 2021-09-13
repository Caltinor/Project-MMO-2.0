package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.*;

public class PrefsChoiceScreen extends Screen
{
    private final List<GuiEventListener> children = Lists.newArrayList();
    private final ResourceLocation box = XP.getResLoc( Reference.MOD_ID, "textures/gui/screenboxy.png" );
    private static TileButton exitButton;

    Minecraft minecraft = Minecraft.getInstance();
    Window sr = minecraft.getWindow();
    Font font = minecraft.font;
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x;
    private int y;
    private List<TileButton> tileButtons;

    public PrefsChoiceScreen( Component titleIn )
    {
        super(titleIn);
    }

    @Override
    protected void init()
    {
        tileButtons = new ArrayList<>();

        x = ( (sr.getGuiScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getGuiScaledHeight() / 2) - (boxHeight / 2) );

        exitButton = new TileButton(x + boxWidth - 24, y - 8, 7, 0, "pmmo.exit", JType.NONE, (something) ->
        {
            Minecraft.getInstance().setScreen( new MainScreen( Minecraft.getInstance().player.getUUID(), new TranslatableComponent( "pmmo.potato" ) ) );
        });

        TileButton settingsButton = new TileButton( (int) ( x + 24 + 36 * 1.5 ), (int) ( y + 24 + 36 * 2.5 ), 3, 7, "pmmo.settings", JType.SETTINGS, (button) ->
        {
            Minecraft.getInstance().setScreen( new PrefsScreen( new TranslatableComponent( ((TileButton) button).transKey ), JType.SETTINGS ) );
        });

        TileButton guiSettingsButton = new TileButton( (int) ( x + 24 + 36 * 3.5 ), (int) ( y + 24 + 36 * 2.5 ), 3, 7, "pmmo.guiSettings", JType.GUI_SETTINGS, (button) ->
        {
            Minecraft.getInstance().setScreen( new PrefsScreen( new TranslatableComponent( ((TileButton) button).transKey ), JType.GUI_SETTINGS ) );
        });

        addButton( exitButton );
        tileButtons.add( settingsButton );
        tileButtons.add( guiSettingsButton );

        for( TileButton button : tileButtons )
        {
            addButton( button );
        }
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks)
    {
        renderBackground( stack,  1 );
        super.render( stack, mouseX, mouseY, partialTicks );

        x = ( (sr.getGuiScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getGuiScaledHeight() / 2) - (boxHeight / 2) );

//        fillGradient( stack, x + 20, y + 52, x + 232, y + 164, 0x22444444, 0x33222222);

        for( TileButton button : tileButtons )
        {
            if( mouseX > button.x && mouseY > button.y && mouseX < button.x + 32 && mouseY < button.y + 32 )
                renderTooltip( stack, new TranslatableComponent( button.transKey ), mouseX, mouseY );
        }
    }

    @Override
    public void renderBackground( PoseStack stack, int p_renderBackground_1_)
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
        Minecraft.getInstance().getTextureManager().bind( box );
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
