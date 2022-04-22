package harmonised.pmmo.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class StatsScreen extends Screen{
	private static final ResourceLocation GUI_BG = new ResourceLocation(Reference.MOD_ID, "textures/gui/screenboxy.png");
	private static final TextComponent MENU_NAME = new TextComponent("Item Detail Screen");
	
	private StatScrollWidget scrollWidget;
	private int renderX, renderY;
	
	private ItemStack stack = null;
	private BlockPos block = null;
	private Entity entity = null;

	public StatsScreen(ItemStack stack) {
		super(MENU_NAME);
		this.stack = stack;
		init();
	}
	public StatsScreen(BlockPos block) {
		super(MENU_NAME);
		this.block = block;
		init();
	}
	public StatsScreen(Entity entity) {
		super(MENU_NAME);
		this.entity = entity;
		init();
	}
	
	protected void init() {
		renderX = this.width/2 - 128;
		renderY = this.height/2 - 128;
		scrollWidget = stack != null ? new StatScrollWidget(206, 200, renderY+30, renderX+25, stack) :
						block != null ? new StatScrollWidget(206, 200, renderY+30, renderX+25, block) :
						entity != null ? new StatScrollWidget(206, 200, renderY+30, renderX+25, entity) 
								: new StatScrollWidget(206, 200, renderY+30, renderX+25);
		super.init();
	}
	
	@Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		renderBackground(stack, 1);
		if (this.stack != null || block != null) {
			ItemStack renderStack = this.stack == null ? new ItemStack(Minecraft.getInstance().player.level.getBlockState(block).getBlock().asItem()) : this.stack;
			this.itemRenderer.renderAndDecorateItem(renderStack, this.renderX+25, this.renderY+15);
			GuiComponent.drawString(stack, font, renderStack.getDisplayName(), this.renderX + 65, this.renderY+15, 0xFFFFFF);
		}
		else if (entity != null && entity instanceof LivingEntity) {
			InventoryScreen.renderEntityInInventory(this.renderX+30, this.renderY+30,  10, (float)(this .renderX+ 51) - mouseX, (float)(this.renderY + 75 - 50) - mouseY, (LivingEntity) entity);
			GuiComponent.drawString(stack, font, this.entity.getDisplayName(), this.renderX + 65, this.renderY+15, 0xFFFFFF);
		}	
		scrollWidget.render(stack, mouseX, mouseY, partialTicks);
		super.render(stack, mouseX, mouseY, partialTicks);
	}
	
	@Override
    public void renderBackground(PoseStack stack, int p_renderBackground_1_) {
		RenderSystem.setShaderTexture(0, GUI_BG);
        this.blit(stack,  renderX, renderY, 0, 0,  256, 256);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrolled) {
		return scrollWidget.mouseScrolled(mouseX, mouseY, scrolled)  || super.mouseScrolled(mouseX, mouseY, scrolled);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int partialTicks) {
		return scrollWidget.mouseClicked(mouseX, mouseY, partialTicks) || super.mouseClicked(mouseX, mouseY, partialTicks);
	}

}
