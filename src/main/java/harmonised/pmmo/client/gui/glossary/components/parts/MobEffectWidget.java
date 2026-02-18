package harmonised.pmmo.client.gui.glossary.components.parts;

import harmonised.pmmo.util.Functions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

public class MobEffectWidget extends AbstractWidget{
    private final Identifier sprite;
    public MobEffectWidget(Holder<MobEffect> holder) {this(0, 0, 18, 18, holder.value().getDisplayName(), holder);}
    public MobEffectWidget(int x, int y, int width, int height, Component message, Holder<MobEffect> holder) {
        super(x, y, width, height, message);
        this.sprite = Identifier.fromNamespaceAndPath(
                holder.unwrapKey().get().identifier().getNamespace(),
                "textures/mob_effect/" + holder.unwrapKey().get().identifier().getPath() + ".png");
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprite, this.getX(), this.getY(), 0, 0, this.width, this.height, 18, 18, 18, 18);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

}
