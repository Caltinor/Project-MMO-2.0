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
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.client.event.ScreenEvent;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class MainScreen extends PmmoScreen
{
    private final ResourceLocation box = XP.getResLoc(Reference.MOD_ID, "textures/gui/screenboxy.png");
    private final ResourceLocation logo = XP.getResLoc(Reference.MOD_ID, "textures/gui/logo.png");
    private final ResourceLocation star = XP.getResLoc(Reference.MOD_ID, "textures/gui/star.png");
    private static TileButton exitButton;
    public static Map<JType, Integer> scrollAmounts = new HashMap<>();

    private int x;
    private int y;
    private UUID uuid;

    public MainScreen(UUID uuid, Component titleIn)
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

        x = ((sr.getGuiScaledWidth() / 2) - (boxWidth / 2));
        y = ((sr.getGuiScaledHeight() / 2) - (boxHeight / 2));

        exitButton = new TileButton(x + boxWidth - 24, y - 8, 7, 0, "pmmo.exit", JType.NONE, (something) ->
        {
            Minecraft.getInstance().player.closeContainer();
        });

        TileButton glossaryButton = new TileButton(0, 0, 3, 5, "pmmo.glossary", JType.NONE, (button) ->
        {
            Minecraft.getInstance().setScreen(new GlossaryScreen(uuid, new TranslatableComponent(((TileButton) button).transKey), true));
        });

        TileButton creditsButton = new TileButton(0, 0, 3, 4, "pmmo.credits", JType.NONE, (button) ->
        {
            Minecraft.getInstance().setScreen(new CreditsScreen(uuid, new TranslatableComponent(((TileButton) button).transKey), JType.CREDITS));
        });

        TileButton prefsButton = new TileButton(0, 0, 3, 7, "pmmo.preferences", JType.NONE, (button) ->
        {
            Minecraft.getInstance().setScreen(new PrefsChoiceScreen(new TranslatableComponent(((TileButton) button).transKey)));
        });


        TileButton skillsButton = new TileButton(0, 0, 3, 6, "pmmo.skills", JType.NONE, (button) ->
        {
            Minecraft.getInstance().setScreen(new ListScreen(uuid,  new TranslatableComponent(((TileButton) button).transKey), "", JType.SKILLS, Minecraft.getInstance().player));
        });

        TileButton statsButton = new TileButton(0, 0, 3, 6, "pmmo.stats", JType.NONE, (button) ->
        {
            Minecraft.getInstance().setScreen(new StatsScreen(uuid,  new TranslatableComponent(((TileButton) button).transKey)));
        });

        TileButton infoButton = new TileButton(0, 0, 3, 5, "pmmo.info", JType.NONE, (button) ->
        {
            Minecraft.getInstance().setScreen(new InfoScreen(uuid,  new TranslatableComponent(((TileButton) button).transKey)));
        });

        addWidget(exitButton);
        tileButtons.add(glossaryButton);
        tileButtons.add(creditsButton);
        tileButtons.add(prefsButton);
        tileButtons.add(skillsButton);
        tileButtons.add(statsButton);
        tileButtons.add(infoButton);

//        y + 24 + 36

        int buttonCount = tileButtons.size();
        for(int i = 0; i < buttonCount; i++)
        {
            TileButton button = tileButtons.get(i);
//            button.x = sr.getScaledWidth()/2 + i*(button.getWidth()+2) - tileButtons.size()*(button.getWidth()+2)/2 - 1;
            button.x = sr.getGuiScaledWidth()/2 - button.getWidth()/2;
            button.y = y + 155;
            button.x += Math.cos(i/(float)buttonCount * 6.2824)*40;
            button.y += Math.sin(i/(float)buttonCount * 6.2824)*40;
            addWidget(button);
        }
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks)
    {
        renderBackground(stack,  1);
        super.render(stack, mouseX, mouseY, partialTicks);

        x = ((sr.getGuiScaledWidth() / 2) - (boxWidth / 2));
        y = ((sr.getGuiScaledHeight() / 2) - (boxHeight / 2));

        fillGradient(stack, x + 20, y + 30, x + 232, y + 235, 0x22444444, 0x33222222);

        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, logo);
        this.blit(stack,  sr.getGuiScaledWidth() / 2 - 100, sr.getGuiScaledHeight() / 2 - 80, 0, 0,  200, 60);

        for(TileButton button : tileButtons)
            button.render(stack, mouseX, mouseY, partialTicks);
        for(TileButton button : tileButtons)
        {
            if(mouseX > button.x && mouseY > button.y && mouseX < button.x + 32 && mouseY < button.y + 32)
                renderTooltip(stack, new TranslatableComponent(button.transKey), mouseX, mouseY);
        }
    }

    @Override
    public void renderBackground(PoseStack stack, int p_renderBackground_1_)
    {
        if (this.mc != null)
        {
            this.fillGradient(stack, 0, 0, this.width, this.height, 0x66222222, 0x66333333);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new ScreenEvent.BackgroundDrawnEvent(this, stack));
        }
        else
            this.renderBackground(stack, p_renderBackground_1_);

        boxHeight = 256;
        boxWidth = 256;
        RenderSystem.setShaderTexture(0, box);
        RenderSystem.disableBlend();
        this.blit(stack,  x, y, 0, 0,  boxWidth, boxHeight);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
    {
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(button == 1)
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
