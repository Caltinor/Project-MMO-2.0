package harmonised.pmmo.api.client.types;

import harmonised.pmmo.client.gui.component.SelectionWidget;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.network.chat.MutableComponent;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public enum SELECTION {
    NONE(LangProvider.GLOSSARY_DEFAULT_SECTION.asComponent(),
            Arrays.stream(OBJECT.values()).map(obj -> new SelectionWidget.SelectionEntry<OBJECT>(obj.text, obj)).toList()),
    REQS(LangProvider.GLOSSARY_SECTION_REQ.asComponent(),
            Arrays.stream(OBJECT.values()).map(obj -> new SelectionWidget.SelectionEntry<OBJECT>(obj.text, obj)).toList()),
    XP(LangProvider.GLOSSARY_SECTION_XP.asComponent(),
            Arrays.stream(new OBJECT[] {OBJECT.ITEMS, OBJECT.BLOCKS, OBJECT.ENTITY, OBJECT.EFFECTS}).map(obj -> new SelectionWidget.SelectionEntry<OBJECT>(obj.text, obj)).toList()),
    BONUS(LangProvider.GLOSSARY_SECTION_BONUS.asComponent(),
            Arrays.stream(new OBJECT[] {OBJECT.ITEMS, OBJECT.DIMENSIONS, OBJECT.BIOMES}).map(obj -> new SelectionWidget.SelectionEntry<OBJECT>(obj.text, obj)).toList()),
    SALVAGE(LangProvider.GLOSSARY_SECTION_SALVAGE.asComponent(),
            List.of(new SelectionWidget.SelectionEntry<OBJECT>(OBJECT.ITEMS.text, OBJECT.ITEMS))),
    VEIN(LangProvider.GLOSSARY_SECTION_VEIN.asComponent(),
            Arrays.stream(new OBJECT[] {OBJECT.ITEMS, OBJECT.BLOCKS, OBJECT.DIMENSIONS, OBJECT.BIOMES}).map(obj -> new SelectionWidget.SelectionEntry<OBJECT>(obj.text, obj)).toList()),
    MOB_SCALING(LangProvider.GLOSSARY_SECTION_MOB.asComponent(),
            Arrays.stream(new OBJECT[] {OBJECT.DIMENSIONS, OBJECT.BIOMES}).map(obj -> new SelectionWidget.SelectionEntry<OBJECT>(obj.text, obj)).toList()),
    PERKS(LangProvider.GLOSSARY_SECTION_PERKS.asComponent(),
            Arrays.stream(new OBJECT[] {OBJECT.PERKS}).map(obj -> new SelectionWidget.SelectionEntry<OBJECT>(obj.text, obj)).toList());

    MutableComponent text;
    List<SelectionWidget.SelectionEntry<OBJECT>> validObjects;
    SELECTION(MutableComponent text, List<SelectionWidget.SelectionEntry<OBJECT>> validObjects) {this.text = text; this.validObjects = validObjects;}

    private static final List<SelectionWidget.SelectionEntry<SELECTION>> CHOICE_LIST = Arrays.stream(SELECTION.values()).map(val -> new SelectionWidget.SelectionEntry<>(val.text, val)).toList();

    public static SelectionWidget<SelectionWidget.SelectionEntry<SELECTION>> createSelectionWidget(int x, int y, int width, Consumer<SelectionWidget.SelectionEntry<SELECTION>> selectCallback) {
        return new SelectionWidget<>(x, y, width, LangProvider.GLOSSARY_DEFAULT_SECTION.asComponent(), selectCallback).setEntries(CHOICE_LIST);
    }

    public static void onSelect(SelectionWidget.SelectionEntry<SELECTION> choice, SelectionWidget<SelectionWidget.SelectionEntry<OBJECT>> objectWidget) {
        objectWidget.setEntries(choice.reference.validObjects);
    }
}
