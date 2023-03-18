package harmonised.pmmo.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import harmonised.pmmo.client.gui.GlossarySelectScreen.OBJECT;
import harmonised.pmmo.client.gui.GlossarySelectScreen.SELECTION;
import harmonised.pmmo.client.gui.component.GuiEnumGroup;
import harmonised.pmmo.client.gui.component.StatScrollWidget;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
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
	private Button openGlossary;
	private int renderX, renderY;
	
	private ItemStack stack = null;
	private BlockPos block = null;
	private Entity entity = null;
	private SELECTION selection = null;
	private OBJECT object = null;
	private String skill = null;
	private GuiEnumGroup type = null;

	public StatsScreen() {
		super(MENU_NAME);
		init();
	}
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
	
	public StatsScreen(SELECTION selection, OBJECT object, String skill, GuiEnumGroup type) {
		super(MENU_NAME);
		this.selection = selection;
		this.object = object;
		this.skill = skill;
		this.type = type;
		init();
	}
	
	protected void init() {
		renderX = this.width/2 - 128;
		renderY = this.height/2 - 128;
		scrollWidget = stack != null ? new StatScrollWidget(206, 200, renderY+30, renderX+25, stack, this.itemRenderer) :
						block != null ? new StatScrollWidget(206, 200, renderY+30, renderX+25, block, this.itemRenderer) :
						entity != null ? new StatScrollWidget(206, 200, renderY+30, renderX+25, entity) :
						selection != null ? new StatScrollWidget(206, 200, renderY+30, renderX+25, selection, object, skill ,type, this.itemRenderer)
								: new StatScrollWidget(206, 200, renderY+30, renderX+25, 0);
		openGlossary = Button.builder(LangProvider.OPEN_GLOSSARY.asComponent(), b -> Minecraft.getInstance().setScreen(new GlossarySelectScreen()))
				.bounds(this.width-84, 4, 80, 20).build();
		
		this.addRenderableWidget(scrollWidget);
		this.addRenderableWidget(openGlossary);
		super.init();
	}
	
	@Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		renderBackground(stack);
		if (this.stack != null || block != null) {
			ItemStack renderStack = this.stack == null ? new ItemStack(Minecraft.getInstance().player.level.getBlockState(block).getBlock().asItem()) : this.stack;
			this.itemRenderer.renderAndDecorateItem(stack, renderStack, this.renderX+25, this.renderY+15);
			GuiComponent.drawString(stack, font, renderStack.getDisplayName(), this.renderX + 65, this.renderY+15, 0xFFFFFF);
		}
		else if (entity != null && entity instanceof LivingEntity) {
			InventoryScreen.renderEntityInInventoryFollowsAngle(stack, this.renderX+width - 20, this.renderY+12, 10, (float)(this.renderX+ 51) - 100, (float)(this.renderY + 75 - 50) - 100, (LivingEntity) entity);
			GuiComponent.drawString(stack, font, this.entity.getDisplayName(), this.renderX + 65, this.renderY+15, 0xFFFFFF);
		}	
		super.render(stack, mouseX, mouseY, partialTicks);
	}
	
	@Override
    public void renderBackground(PoseStack stack) {
		RenderSystem.setShaderTexture(0, GUI_BG);
        GuiComponent.blit(stack,  renderX, renderY, 0, 0,  256, 256);
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
