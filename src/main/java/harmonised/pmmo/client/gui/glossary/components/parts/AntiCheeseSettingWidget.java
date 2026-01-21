package harmonised.pmmo.client.gui.glossary.components.parts;

import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.api.client.types.GuiEnumGroup;
import harmonised.pmmo.api.client.types.OBJECT;
import harmonised.pmmo.api.client.types.PositionType;
import harmonised.pmmo.api.client.types.SELECTION;
import harmonised.pmmo.api.client.wrappers.PositionConstraints;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.api.enums.EventType;
import harmonised.pmmo.client.gui.glossary.components.ReactiveWidget;
import harmonised.pmmo.client.gui.glossary.components.panels.AntiCheesePanelWidget;
import harmonised.pmmo.client.utils.DP;
import harmonised.pmmo.features.anticheese.CheeseTracker;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.Reference;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntitySpawnReason;

import java.util.List;

public class AntiCheeseSettingWidget extends ReactiveWidget {
    private final List<String> sources;
    private final GuiEnumGroup type;
    public AntiCheeseSettingWidget(EventType event, CheeseTracker.Setting setting, AntiCheesePanelWidget.Type type) {
        super(0,0, 0, 0);
        this.sources = setting.source();
        this.type = event;
        addString(LangProvider.GLOSSARY_CONFIG_ANTI_EVENT.asComponent(event.getName()).withStyle(ChatFormatting.GREEN), PositionType.STATIC.constraint, textConstraint);
        addString(LangProvider.GLOSSARY_CONFIG_ANTI_RETENTION.asComponent(setting.retention()), PositionConstraints.offset(10, 0), textConstraint);

        if (!setting.source().isEmpty()) {
            RegistryAccess access = Minecraft.getInstance().player.registryAccess();
            var itemReg = access.lookupOrThrow(Registries.ITEM);
            var blockReg = access.lookupOrThrow(Registries.BLOCK);
            var entityReg = access.lookupOrThrow(Registries.ENTITY_TYPE);
            addString(LangProvider.GLOSSARY_CONFIG_ANTI_SOURCE.asComponent(), PositionConstraints.offset(10, 0), textConstraint);
            for (ResourceLocation entry : setting.source().stream().map(Reference::of).toList()) {
                if (event.itemApplicable) {
                    itemReg.get(ResourceKey.create(Registries.ITEM, entry)).ifPresent(item -> {
                        addChild(new ItemStackWidget(item.value()), PositionConstraints.offset(20, 0),
                                SizeConstraints.builder().absoluteHeight(18).absoulteWidth(18).build());
                        addString(item.value().getDefaultInstance().getDisplayName(), PositionType.STATIC.constraint, textConstraint);
                    });
                }
                if (event.blockApplicable) {
                    blockReg.get(ResourceKey.create(Registries.BLOCK, entry)).ifPresent(block -> {
                        addChild(new ItemStackWidget(block.value().asItem()), PositionConstraints.offset(20, 0),
                                SizeConstraints.builder().absoluteHeight(18).absoulteWidth(18).build());
                        addString(block.value().getName(), PositionType.STATIC.constraint, textConstraint);
                    });
                }
                if (event.entityApplicable) {
                    entityReg.get(ResourceKey.create(Registries.ENTITY_TYPE, entry)).ifPresent(entity -> {
                        addChild(new EntityWidget(entity.value().create(Minecraft.getInstance().level, EntitySpawnReason.COMMAND)), PositionConstraints.offset(20, 0),
                                SizeConstraints.builder().absoluteHeight(18).absoulteWidth(18).build());
                        addString(entity.value().getDescription(), PositionType.STATIC.constraint, textConstraint);
                    });
                }
            }
        }
        switch (type) {
          case AFK -> {
              addString(LangProvider.GLOSSARY_CONFIG_ANTI_TOLERANCE_AFK.asComponent(setting.toleranceFlat(), setting.minTime()), PositionConstraints.offset(10, 0), textConstraint);
              if (setting.strictTolerance())
                  addString(LangProvider.GLOSSARY_CONFIG_ANTI_STRICT_AFK.asComponent(), PositionConstraints.offset(10, 0), textConstraint);
              addString(LangProvider.GLOSSARY_CONFIG_ANTI_COOLDOWN.asComponent(setting.cooloff()), PositionConstraints.offset(10, 0), textConstraint);
              addString(LangProvider.GLOSSARY_CONFIG_ANTI_REDUX.asComponent(DP.dpSoft(setting.reduction()*100)), PositionConstraints.offset(10, 0), textConstraint);
          }
          case DIM -> {
              addString(LangProvider.GLOSSARY_CONFIG_ANTI_MINDUR.asComponent(setting.minTime()), PositionConstraints.offset(10, 0), textConstraint);
              addString(LangProvider.GLOSSARY_CONFIG_ANTI_REDUX.asComponent(DP.dpSoft(setting.reduction()*100)), PositionConstraints.offset(10, 0), textConstraint);
          }
          case NORM -> {
              addString(LangProvider.GLOSSARY_CONFIG_ANTI_TOLERANCE_NORM.asComponent(DP.dpSoft(setting.tolerancePercent()*100), setting.toleranceFlat()),
                      PositionConstraints.offset(10, 0), textConstraint);
          }
        };
        this.setHeight(getChildren().stream().map(poser -> poser.get().getHeight()).reduce(Integer::sum).orElse(0) - (setting.source().size() * 12));
    }

    @Override
    public DisplayType getDisplayType() {return DisplayType.INLINE;}

    @Override
    public boolean applyFilter(Filter filter) {
        boolean filtered = !filter.matchesEnum(type) || sources.stream().noneMatch(filter::matchesTextFilter);
        setHeight(filtered ? 0 : getChildren().stream().map(poser -> poser.get().getHeight()).reduce(Integer::sum).orElse(0));
        return filtered;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
