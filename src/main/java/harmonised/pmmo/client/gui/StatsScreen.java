package harmonised.pmmo.client.gui;

import java.util.function.BiFunction;

import harmonised.pmmo.client.gui.GlossarySelectScreen.OBJECT;
import harmonised.pmmo.client.gui.GlossarySelectScreen.SELECTION;
import harmonised.pmmo.client.gui.component.GuiEnumGroup;
import harmonised.pmmo.client.gui.component.StatScrollWidget;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class StatsScreen extends Screen{
	private static final ResourceLocation GUI_BG = new ResourceLocation(Reference.MOD_ID, "textures/gui/screenboxy.png");
	private static final MutableComponent MENU_NAME = Component.literal("Item Detail Screen");
	
	private StatScrollWidget scrollWidget;
	private BiFunction<Integer, Integer, StatScrollWidget> scrollSupplier;
	private int renderX, renderY;
	
	private ItemStack stack = null;
	private BlockPos block = null;
	private Entity entity = null;

	public StatsScreen() {
		super(MENU_NAME);
		scrollSupplier = (x,y) -> scrollWidget = new StatScrollWidget(206, 200, y, x, 0);		
	}
	public StatsScreen(ItemStack stack) {
		super(MENU_NAME);
		scrollSupplier = (x,y) -> scrollWidget = new StatScrollWidget(206, 200, y, x, this.stack = stack);
		
	}
	public StatsScreen(BlockPos block) {
		super(MENU_NAME);
		scrollSupplier = (x,y) -> scrollWidget = new StatScrollWidget(206, 200, y, x, this.block = block);
		
	}
	public StatsScreen(Entity entity) {
		super(MENU_NAME);
		scrollSupplier = (x,y) -> scrollWidget = new StatScrollWidget(206, 200, y, x, this.entity = entity);
		
	}	
	public StatsScreen(SELECTION selection, OBJECT object, String skill, GuiEnumGroup type) {
		super(MENU_NAME);
		scrollSupplier = (x,y) -> scrollWidget = new StatScrollWidget(206, 200, y, x, selection, object, skill ,type);	
	}
	
	@Override
	protected void init() {
		renderX = this.width / 2 - 128;
		renderY = this.height / 2 - 128;
		addRenderableWidget(scrollSupplier.apply(renderX + 25, renderY + 30));
		addRenderableWidget(Button.builder(LangProvider.OPEN_GLOSSARY.asComponent(), b -> Minecraft.getInstance().setScreen(new GlossarySelectScreen()))
				.bounds(this.width-84, 4, 80, 20).build());
	}
	
	@Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
//		renderX = this.width/2 - 128;
//		renderY = this.height/2 - 128;
		renderBackground(graphics, mouseX, mouseY, partialTicks);
		super.render(graphics, mouseX, mouseY, partialTicks);
		if (this.stack != null || block != null) {
			ItemStack renderStack = this.stack == null ? new ItemStack(Minecraft.getInstance().player.level().getBlockState(block).getBlock().asItem()) : this.stack;
			graphics.renderItem(renderStack, this.renderX+25, this.renderY+15);
			graphics.drawString(font, renderStack.getDisplayName(), this.renderX + 65, this.renderY+15, 0xFFFFFF);
		}
		else if (entity != null && entity instanceof LivingEntity) {
			InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, this.renderX, this.renderY, this.renderX + 65, this.renderY + 40,
				10, 0.06F, mouseX, mouseY, (LivingEntity) entity);
			graphics.drawString(font, this.entity.getDisplayName(), this.renderX + 65, this.renderY+15, 0xFFFFFF);
		}			
	}
	
	@Override
    public void renderBackground(GuiGraphics graphics, int i, int j, float k) {
        graphics.blit(GUI_BG, renderX, renderY, 0, 0,  256, 256);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrolled, double delta) {
		return scrollWidget.mouseScrolled(mouseX, mouseY, scrolled, delta)  || super.mouseScrolled(mouseX, mouseY, scrolled, delta);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int partialTicks) {
		return scrollWidget.mouseClicked(mouseX, mouseY, partialTicks) || super.mouseClicked(mouseX, mouseY, partialTicks);
	}

}
