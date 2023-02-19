package harmonised.pmmo.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.client.gui.component.GuiEnumGroup;
import harmonised.pmmo.client.gui.component.SelectionWidget;
import harmonised.pmmo.client.gui.component.SelectionWidget.SelectionEntry;
import harmonised.pmmo.config.SkillsConfig;
import harmonised.pmmo.core.CoreUtils;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class GlossarySelectScreen extends Screen{
	private static final ResourceLocation GUI_BG = new ResourceLocation(Reference.MOD_ID, "textures/gui/screenboxy.png");
	
	private SelectionWidget<SelectionEntry<SELECTION>> selectSection;
	private SelectionWidget<SelectionEntry<OBJECT>> selectObject;
	private SelectionWidget<SelectionEntry<String>> selectSkills;
	private SelectionWidget<SelectionEntry<GuiEnumGroup>> selectEnum;
	private Button viewButton;
	private SELECTION selection;
	private OBJECT object;
	private String skill = "";
	private GuiEnumGroup type;
	private int renderX, renderY;

	public GlossarySelectScreen () {
		super(Component.literal("pmmo_glossary"));
		init();
	}
	
	protected void init() {
		renderX = this.width/2 - 128;
		renderY = this.height/2 - 128;
		selectSection = new SelectionWidget<>(this.width/2 - 100, renderY + 25, 200, 
				LangProvider.GLOSSARY_DEFAULT_SECTION.asComponent(), 
				this::updateSelection);
		selectSection.setEntries(SELECTION.CHOICE_LIST);
		
		selectObject = new SelectionWidget<>(this.width/2 - 100, renderY + 50, 200, 
				LangProvider.GLOSSARY_DEFAULT_OBJECT.asComponent(), 
				sel -> {object = sel.reference; this.updateEnum(sel);});
		selectObject.visible = false;
		
		selectSkills = new SelectionWidget<>(this.width/2 - 100, renderY + 75, 200, 
				LangProvider.GLOSSARY_DEFAULT_SKILL.asComponent(), 
				sel -> skill = sel.reference);
		
		selectSkills.setEntries(SkillsConfig.SKILLS.get().keySet().stream()
				.sorted()
				.map(skill -> new SelectionEntry<>(
						Component.translatable("pmmo."+skill).setStyle(CoreUtils.getSkillStyle(skill)), 
						skill))
				.toList()
		);
		selectSkills.visible = false;
		
		selectEnum = new SelectionWidget<>(this.width/2 - 100, renderY + 100, 200, 
				LangProvider.GLOSSARY_DEFAULT_ENUM.asComponent(), 
				sel -> type = sel.reference);
		selectEnum.visible = false;
		
		viewButton = Button.builder(LangProvider.GLOSSARY_VIEW_BUTTON.asComponent(), 
				button -> {
					if (selection != null && object != null)
						Minecraft.getInstance().setScreen(new StatsScreen(selection, object, skill, type));
				}).bounds(this.width/2 - 40, renderY + 125, 80, 20).build();
		viewButton.visible = false;
		
		addRenderableWidget(viewButton);
		addRenderableWidget(selectEnum);
		addRenderableWidget(selectSkills);
		addRenderableWidget(selectObject);
		addRenderableWidget(selectSection);		
	}

	@Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		renderBackground(stack, 1);
		super.render(stack, mouseX, mouseY, partialTicks);
	}
	
	@Override
    public void renderBackground(PoseStack stack, int p_renderBackground_1_) {
		RenderSystem.setShaderTexture(0, GUI_BG);
        this.blit(stack,  renderX, renderY, 0, 0,  256, 256);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrolled) {
		if (selectSection.isExtended())
			return selectSection.mouseScrolled(mouseX, mouseY, scrolled) || super.mouseScrolled(mouseX, mouseY, scrolled);
		if (selectObject.isExtended())
			return selectObject.mouseScrolled(mouseX, mouseY, scrolled) || super.mouseScrolled(mouseX, mouseY, scrolled);
		if (selectSkills.isExtended())
			return selectSkills.mouseScrolled(mouseX, mouseY, scrolled) || super.mouseScrolled(mouseX, mouseY, scrolled);
		if (selectEnum.isExtended())
			return selectEnum.mouseScrolled(mouseX, mouseY, scrolled) || super.mouseScrolled(mouseX, mouseY, scrolled);
		return super.mouseScrolled(mouseX, mouseY, scrolled);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int partialTicks) {
		if (selectSection.isExtended())
			return selectSection.mouseClicked(mouseX, mouseY, partialTicks) || super.mouseClicked(mouseX, mouseY, partialTicks);
		if (selectObject.isExtended())
			return selectObject.mouseClicked(mouseX, mouseY, partialTicks) || super.mouseClicked(mouseX, mouseY, partialTicks);
		if (selectSkills.isExtended())
			return selectSkills.mouseClicked(mouseX, mouseY, partialTicks) || super.mouseClicked(mouseX, mouseY, partialTicks);
		if (selectEnum.isExtended())
			return selectEnum.mouseClicked(mouseX, mouseY, partialTicks) || super.mouseClicked(mouseX, mouseY, partialTicks);
		return viewButton.mouseClicked(mouseX, mouseY, partialTicks) || super.mouseClicked(mouseX, mouseY, partialTicks);
	}
	
	private void updateSelection(SelectionEntry<SELECTION> sel) {
		selection = sel.reference;
		selectObject.visible = true;
		selectObject.setEntries(selection.validObjects);
		selectSkills.visible = true;
		selectEnum.visible = selection != SELECTION.SALVAGE && selection != SELECTION.VEIN && selection != SELECTION.PERKS;	
		viewButton.visible = true;
	}
	
	private void updateEnum(SelectionEntry<OBJECT> sel) {
		if (selection == null) return;
		selectEnum.setEntries(switch (sel.reference) {
			case ITEMS -> {
				if (selection == SELECTION.REQS) yield enumToList(ReqType.ITEM_APPLICABLE_EVENTS);
				if (selection == SELECTION.XP) yield enumToList(EventType.ITEM_APPLICABLE_EVENTS);
				if (selection == SELECTION.BONUS) yield enumToList(new ModifierDataType[]{ModifierDataType.HELD, ModifierDataType.WORN});
				else yield new ArrayList<SelectionEntry<GuiEnumGroup>>();
			}
			case BLOCKS ->  {
				if (selection == SELECTION.REQS) yield enumToList(ReqType.BLOCK_APPLICABLE_EVENTS);
				if (selection == SELECTION.XP) yield enumToList(EventType.BLOCK_APPLICABLE_EVENTS);
				else yield new ArrayList<SelectionEntry<GuiEnumGroup>>();
			}
			case ENTITY -> {
				if (selection == SELECTION.REQS) yield enumToList(ReqType.ENTITY_APPLICABLE_EVENTS);
				if (selection == SELECTION.XP) yield enumToList(EventType.ENTITY_APPLICABLE_EVENTS);
				else yield new ArrayList<SelectionEntry<GuiEnumGroup>>();
			}
			case DIMENSIONS -> {
				if (selection == SELECTION.REQS) yield enumToList(new ReqType[]{ReqType.TRAVEL});
				if (selection == SELECTION.BONUS) yield enumToList(new ModifierDataType[] {ModifierDataType.DIMENSION});
				else yield new ArrayList<SelectionEntry<GuiEnumGroup>>();
			}
			case BIOMES ->  {
				if (selection == SELECTION.REQS) yield enumToList(new ReqType[]{ReqType.TRAVEL});
				if (selection == SELECTION.BONUS) yield enumToList(new ModifierDataType[] {ModifierDataType.BIOME});
				else yield new ArrayList<SelectionEntry<GuiEnumGroup>>();
			}
			case ENCHANTS -> {
				if (selection == SELECTION.REQS) yield enumToList(new ReqType[]{ReqType.USE_ENCHANTMENT});
				else yield new ArrayList<SelectionEntry<GuiEnumGroup>>();
			}
			default -> new ArrayList<SelectionEntry<GuiEnumGroup>>();
		});
		
	}
	
	private List<SelectionEntry<GuiEnumGroup>> enumToList(GuiEnumGroup[] array) {
		return Arrays.stream(array).map(val -> new SelectionEntry<GuiEnumGroup>(Component.translatable("pmmo.enum."+val.getName()), val)).toList();
	}
	
	public static enum SELECTION {
		REQS(LangProvider.GLOSSARY_SECTION_REQ.asComponent(), 
				Arrays.stream(OBJECT.values()).map(obj -> new SelectionEntry<OBJECT>(obj.text, obj)).toList()),
		XP(LangProvider.GLOSSARY_SECTION_XP.asComponent(), 
				Arrays.stream(new OBJECT[] {OBJECT.ITEMS, OBJECT.BLOCKS, OBJECT.ENTITY, OBJECT.EFFECTS}).map(obj -> new SelectionEntry<OBJECT>(obj.text, obj)).toList()),
		BONUS(LangProvider.GLOSSARY_SECTION_BONUS.asComponent(), 
				Arrays.stream(new OBJECT[] {OBJECT.ITEMS, OBJECT.DIMENSIONS, OBJECT.BIOMES}).map(obj -> new SelectionEntry<OBJECT>(obj.text, obj)).toList()),
		SALVAGE(LangProvider.GLOSSARY_SECTION_SALVAGE.asComponent(), 
				List.of(new SelectionEntry<OBJECT>(OBJECT.ITEMS.text, OBJECT.ITEMS))),
		VEIN(LangProvider.GLOSSARY_SECTION_VEIN.asComponent(), 
				Arrays.stream(new OBJECT[] {OBJECT.ITEMS, OBJECT.BLOCKS, OBJECT.DIMENSIONS, OBJECT.BIOMES}).map(obj -> new SelectionEntry<OBJECT>(obj.text, obj)).toList()),
		MOB_SCALING(LangProvider.GLOSSARY_SECTION_MOB.asComponent(),
				Arrays.stream(new OBJECT[] {OBJECT.DIMENSIONS, OBJECT.BIOMES}).map(obj -> new SelectionEntry<OBJECT>(obj.text, obj)).toList()),
		PERKS(LangProvider.GLOSSARY_SECTION_PERKS.asComponent(),
				Arrays.stream(new OBJECT[] {OBJECT.PERKS}).map(obj -> new SelectionEntry<OBJECT>(obj.text, obj)).toList());
		
		MutableComponent text;
		List<SelectionEntry<OBJECT>> validObjects;
		SELECTION(MutableComponent text, List<SelectionEntry<OBJECT>> validObjects) {this.text = text; this.validObjects = validObjects;}
		
		public static final List<SelectionEntry<SELECTION>> CHOICE_LIST = Arrays.stream(SELECTION.values()).map(val -> new SelectionEntry<>(val.text, val)).toList();
	}
	
	public static enum OBJECT {
		ITEMS(LangProvider.GLOSSARY_OBJECT_ITEMS.asComponent()),
		BLOCKS(LangProvider.GLOSSARY_OBJECT_BLOCKS.asComponent()),
		ENTITY(LangProvider.GLOSSARY_OBJECT_ENTITIES.asComponent()),
		DIMENSIONS(LangProvider.GLOSSARY_OBJECT_DIMENSIONS.asComponent()),
		BIOMES(LangProvider.GLOSSARY_OBJECT_BIOMES.asComponent()),
		ENCHANTS(LangProvider.GLOSSARY_OBJECT_ENCHANTS.asComponent()),
		EFFECTS(LangProvider.GLOSSARY_OBJECT_EFFECTS.asComponent()),
		PERKS(LangProvider.GLOSSARY_OBJECT_PERKS.asComponent());
		
		MutableComponent text;
		OBJECT(MutableComponent text) {this.text = text;}
		
		public static final List<SelectionEntry<OBJECT>> CHOICE_LIST = Arrays.stream(OBJECT.values()).map(val -> new SelectionEntry<>(val.text, val)).toList();
	}
}
