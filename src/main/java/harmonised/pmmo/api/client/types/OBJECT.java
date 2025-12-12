package harmonised.pmmo.api.client.types;

import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.api.enums.ModifierDataType;
import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.client.gui.component.SelectionWidget;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public enum OBJECT {
    NONE(LangProvider.GLOSSARY_DEFAULT_OBJECT.asComponent()),
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

    private static final List<SelectionWidget.SelectionEntry<OBJECT>> CHOICE_LIST = Arrays.stream(OBJECT.values()).map(val -> new SelectionWidget.SelectionEntry<>(val.text, val)).toList();

    public static SelectionWidget<SelectionWidget.SelectionEntry<OBJECT>> createSelectionWidget(int x, int y, int width, Consumer<SelectionWidget.SelectionEntry<OBJECT>> selectCallback) {
        return new SelectionWidget<>(x, y, width, LangProvider.GLOSSARY_DEFAULT_OBJECT.asComponent(), selectCallback).setEntries(CHOICE_LIST);
    }
    
    public static void onSelect(SelectionWidget.SelectionEntry<SELECTION> sel, SelectionWidget.SelectionEntry<OBJECT> choice, SelectionWidget<SelectionWidget.SelectionEntry<GuiEnumGroup>> enumWidget) {
        var selection = sel == null ? null : sel.reference;
        enumWidget.setEntries(switch (choice.reference) {
            case ITEMS -> {
                if (selection == SELECTION.REQS) yield enumToList(ReqType.ITEM_APPLICABLE_EVENTS);
                if (selection == SELECTION.XP) yield enumToList(EventType.ITEM_APPLICABLE_EVENTS);
                if (selection == SELECTION.BONUS) yield enumToList(new ModifierDataType[]{ModifierDataType.HELD, ModifierDataType.WORN});
                else yield new ArrayList<>();
            }
            case BLOCKS ->  {
                if (selection == SELECTION.REQS) yield enumToList(ReqType.BLOCK_APPLICABLE_EVENTS);
                if (selection == SELECTION.XP) yield enumToList(EventType.BLOCK_APPLICABLE_EVENTS);
                else yield new ArrayList<>();
            }
            case ENTITY -> {
                if (selection == SELECTION.REQS) yield enumToList(ReqType.ENTITY_APPLICABLE_EVENTS);
                if (selection == SELECTION.XP) yield enumToList(EventType.ENTITY_APPLICABLE_EVENTS);
                else yield new ArrayList<>();
            }
            case DIMENSIONS -> {
                if (selection == SELECTION.REQS) yield enumToList(new ReqType[]{ReqType.TRAVEL});
                if (selection == SELECTION.BONUS) yield enumToList(new ModifierDataType[] {ModifierDataType.DIMENSION});
                else yield new ArrayList<>();
            }
            case BIOMES ->  {
                if (selection == SELECTION.REQS) yield enumToList(new ReqType[]{ReqType.TRAVEL});
                if (selection == SELECTION.BONUS) yield enumToList(new ModifierDataType[] {ModifierDataType.BIOME});
                else yield new ArrayList<>();
            }
            case ENCHANTS -> {
                if (selection == SELECTION.REQS) yield enumToList(new ReqType[]{ReqType.USE_ENCHANTMENT});
                else yield new ArrayList<>();
            }
            default -> new ArrayList<>();
        });
    }

    private static List<SelectionWidget.SelectionEntry<GuiEnumGroup>> enumToList(GuiEnumGroup[] array) {
        return Arrays.stream(array).map(val -> new SelectionWidget.SelectionEntry<>(Component.translatable("pmmo.enum."+val.getName()), val)).toList();
    }
}
