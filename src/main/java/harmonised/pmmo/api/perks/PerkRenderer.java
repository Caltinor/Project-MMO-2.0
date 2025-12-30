package harmonised.pmmo.api.perks;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.client.PanelWidget;
import harmonised.pmmo.api.client.ResponsiveLayout;
import harmonised.pmmo.api.client.types.DisplayType;
import harmonised.pmmo.api.client.types.PositionType;
import harmonised.pmmo.api.client.wrappers.PositionConstraints;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.client.gui.glossary.components.parts.DividerWidget;
import harmonised.pmmo.client.utils.DP;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.Reference;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;

@FunctionalInterface
public interface PerkRenderer {
    /// Creates a runtime widget using compiled configuration data similar to how Perks
    /// function when invoked.  This allows for the display of perk data reflective of
    /// the current state of the game for the player.  For example using the player's
    /// current level in a configured skill to display the current value of a per_level
    /// perk effect.
    ///
    /// @param width the width of the parent widget
    /// @param player the player instance generating this glossary value
    /// @param config the compiled values from the config and the runtime data as would
    ///               be present during perk invocation.
    ///
    /// @return a {@link PanelWidget} instance which can be displayed in the glossary
    PanelWidget getWidget(int width, Player player, CompoundTag config);

    /// Creates a new instance of the default panel type.  This is used internally when Perks
    /// do not register a renderer to provide a default.
    static DefaultPerkPanel DEFAULT(int width, Player player, CompoundTag tag) {return new DefaultPerkPanel(0x88394045, width, player, tag);}

    /// Helper reference for {@link StringWidget} height constraints
    SizeConstraints textConstraint = SizeConstraints.builder().absoluteHeight(12).build();

    /// Helper method for displaying the impact of default values that PMMO uses on all Perks.
    /// this is used to be consistent with the default glossary and to avoid repeating the
    /// implementation of these values where no special use case exists.
    ///
    /// @param layout the {@link PanelWidget} to add the common elements to
    /// @param config pass through for the config provided by {@link PerkRenderer}
    static void commonElements(ResponsiveLayout layout, CompoundTag config) {
        String skill = config.getString(APIUtils.SKILLNAME);
        if (!skill.isBlank())
            layout.addString(LangProvider.PERK_DEFAULT_SKILLNAME.asComponent(LangProvider.skill(skill)), PositionConstraints.offset(10, 0), textConstraint);
        if (config.contains(APIUtils.MIN_LEVEL))
            layout.addString(LangProvider.PERK_DEFAULT_MIN_LEVEL.asComponent(config.getLong(APIUtils.MIN_LEVEL)), PositionConstraints.offset(10, 0), textConstraint);
        if (config.contains(APIUtils.MAX_LEVEL))
            layout.addString(LangProvider.PERK_DEFAULT_MAX_LEVEL.asComponent(config.getLong(APIUtils.MAX_LEVEL)), PositionConstraints.offset(10, 0), textConstraint);
        if (config.contains(APIUtils.MODULUS))
            layout.addString(LangProvider.PERK_DEFAULT_MOD_LEVEL.asComponent(config.getLong(APIUtils.MODULUS)), PositionConstraints.offset(10, 0), textConstraint);
        if (config.contains(APIUtils.MILESTONES))
            layout.addString(LangProvider.PERK_DEFAULT_MILESTONE.asComponent(config.getList(APIUtils.MILESTONES, CompoundTag.TAG_INT)), PositionConstraints.offset(10, 0), textConstraint);
        if (config.contains(APIUtils.CHANCE))
            layout.addString(LangProvider.PERK_DEFAULT_CHANCE.asComponent(DP.dpSoft(config.getDouble(APIUtils.CHANCE)) + "%"), PositionConstraints.offset(10, 0), textConstraint);
        if (config.contains(APIUtils.COOLDOWN))
            layout.addString(LangProvider.PERK_DEFAULT_COOLDOWN.asComponent(config.getInt(APIUtils.CHANCE)/2), PositionConstraints.offset(10, 0), textConstraint);
    }

    /// A default implementation capturing only the common elements of Perks.
    /// This is used as a fallback to prevent Perks from displaying nothing in
    /// the glossary when the implementing mod/script does not provide a custom
    /// implementation
    class DefaultPerkPanel extends PanelWidget {
        private final String id;
        private final String name;
        private final String skill;

        public DefaultPerkPanel(int color, int width, Player player, CompoundTag config) {
            super(color, width);
            ResourceLocation rl = Reference.of(config.getString("perk"));
            this.id = rl.toString();
            MutableComponent title = Component.translatable("perk.%s.%s".formatted(rl.getNamespace(), rl.getPath()));
            this.name = title.toString();
            this.skill = config.contains(APIUtils.SKILLNAME) ? config.getString(APIUtils.SKILLNAME) : null;
            addString(title.withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GOLD), PositionType.STATIC.constraint, textConstraint);
            MutableComponent descr = Component.translatable("perk.%s.%s.description".formatted(rl.getNamespace(), rl.getPath()));
            addString(descr.withStyle(ChatFormatting.GRAY), PositionType.STATIC.constraint, textConstraint);

            commonElements(this, config);

            addChild(new DividerWidget(200, 2, 0xFF000000), PositionType.STATIC.constraint, SizeConstraints.builder()
                    .absoluteHeight(2).build());
            this.setHeight(getChildren().stream().map(poser -> poser.get().getHeight()).reduce(Integer::sum).orElse(0));
        }

        @Override
        public DisplayType getDisplayType() {return DisplayType.BLOCK;}

        @Override
        public boolean applyFilter(Filter filter) {
            return !(skill != null && filter.matchesSkill(List.of(skill)))
                    || (!filter.matchesTextFilter(id)
                    && !filter.matchesTextFilter(name));
        }
    }
}
