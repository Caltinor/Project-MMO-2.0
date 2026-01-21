package harmonised.pmmo.client.gui.glossary.components.parts;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

public class MobEffectWidget extends AbstractWidget{
    private final TextureAtlasSprite sprite;
    public MobEffectWidget(Holder<MobEffect> holder) {this(0, 0, 18, 18, holder.value().getDisplayName(), holder);}
    public MobEffectWidget(int x, int y, int width, int height, Component message, Holder<MobEffect> holder) {
        super(x, y, width, height, message);
        this.sprite = null;//Minecraft.getInstance().getTextureManager().getTexture(holder.unwrapKey().get().location()).getTextureView().t;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
//TODO        guiGraphics.blitSprite(RenderPipelines.GUI, sprite, this.getX(), this.getY(),this.width, this.height);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

}
