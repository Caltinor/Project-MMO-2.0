package harmonised.pmmo.client.gui.glossary;

import harmonised.pmmo.client.utils.ClientUtils;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.CreativeModeTabs;

import java.util.concurrent.CompletableFuture;

public class GlossaryLoadingScreen extends Screen {
    public GlossaryLoadingScreen() {
        super(Component.literal("Loading Screen"));
        LocalPlayer player = Minecraft.getInstance().player;
        CreativeModeTabs.tryRebuildTabContents(player.connection.enabledFeatures(), player.canUseGameMasterBlocks(), player.clientLevel.registryAccess());
    }

    @Override
    protected void init() {
        super.init();
        LinearLayout layout = LinearLayout.vertical();
        layout.setPosition(this.width/4, this.height/3);
        layout.defaultCellSetting().alignHorizontallyCenter().alignVerticallyMiddle();
        layout.addChild(new StringWidget(LangProvider.LOADING_HEADER.asComponent().withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.UNDERLINE), this.font));
        this.font.getSplitter().splitLines(LangProvider.LOADING_EXPLANATION.asComponent().getString(), this.width/2, Style.EMPTY).forEach(fcs ->
                layout.addChild(new StringWidget(Component.literal(fcs.getString()), this.font).alignCenter()));
        layout.arrangeElements();
        layout.visitWidgets(this::addRenderableWidget);
        if (ClientUtils.glossary == null) {
            CompletableFuture.supplyAsync(Glossary::new).thenAccept(glossary -> ClientUtils.glossary = glossary);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (ClientUtils.glossary != null)
            Minecraft.getInstance().setScreen(ClientUtils.glossary);
    }
}
