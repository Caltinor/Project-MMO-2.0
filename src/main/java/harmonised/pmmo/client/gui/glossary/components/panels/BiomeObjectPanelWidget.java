package harmonised.pmmo.client.gui.glossary.components.panels;

import harmonised.pmmo.api.client.types.OBJECT;
import harmonised.pmmo.api.client.wrappers.PositionConstraints;
import harmonised.pmmo.api.client.wrappers.SizeConstraints;
import harmonised.pmmo.client.gui.glossary.components.parts.BonusSectionWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.DividerWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.EntityWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.MobModifierSectionWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.PosNegEffectSectionWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.ReqSectionWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.VeinBlacklistSectionWidget;
import harmonised.pmmo.client.gui.glossary.components.parts.XpSectionWidget;
import harmonised.pmmo.config.codecs.LocationData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.fml.LogicalSide;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BiomeObjectPanelWidget extends ObjectPanelWidget {
    private final String id;
    private final String name;
    private final PosNegEffectSectionWidget effects;
    private final BonusSectionWidget bonuses;
    private final VeinBlacklistSectionWidget blacklist;
    private final MobModifierSectionWidget modifiers;
    private final Set<String> skills = new HashSet<>();

    public BiomeObjectPanelWidget(int color, int width, Holder<Biome> biome) {
        super(color, width, Core.get(LogicalSide.CLIENT));
        Identifier rl = RegistryUtil.getId(biome);
        Identifier texture = Identifier.fromNamespaceAndPath(rl.getNamespace(),
                "textures/biome/" +rl.getPath()+ ".png");
        try {
            Minecraft.getInstance().getTextureManager().register(texture, Minecraft.getInstance().getTextureManager().getTexture(texture));
        }catch (Exception e) {
            texture = Identifier.withDefaultNamespace("textures/biome/plains.png");
        }
        LocationData data = core.getLoader().BIOME_LOADER.getData(rl);
        skills.addAll(data.bonusMap().values().stream().map(Map::keySet).flatMap(Set::stream).toList());
        skills.addAll(data.travelReq().keySet());
        this.id = rl.toString();
        this.name = this.id;
        addChild(ImageWidget.texture(18, 18, texture, 18, 18), PositionConstraints.grid(0, 0), SizeConstraints.builder()
                .absoluteHeight(18).absoulteWidth(18).build());
        addString(Component.literal(name), PositionConstraints.grid(0,1), SizeConstraints.builder().absoluteHeight(12).build());
        this.effects = new PosNegEffectSectionWidget(data, true);
        addChild((AbstractWidget) effects, PositionConstraints.grid(1,1), SizeConstraints.builder().internalHeight().build());
        this.bonuses = BonusSectionWidget.create(biome);
        addChild((AbstractWidget) bonuses, PositionConstraints.grid(2,1), SizeConstraints.builder().internalHeight().build());
        this.blacklist = new VeinBlacklistSectionWidget(data.veinBlacklist());
        addChild((AbstractWidget) blacklist, PositionConstraints.grid(3, 1), SizeConstraints.builder().internalHeight().build());
        this.modifiers = new MobModifierSectionWidget(data);
        addChild((AbstractWidget) modifiers, PositionConstraints.grid(4, 1), SizeConstraints.builder().internalHeight().build());
        addChild(new DividerWidget(200, 2, 0xFF000000), PositionConstraints.grid(5,1), SizeConstraints.builder()
                .absoluteHeight(2).build());
        this.setHeight(18 + effects.getHeight() + bonuses.getHeight() + blacklist.getHeight() + modifiers.getHeight() + 2);

    }

    @Override
    public boolean applyFilter(Filter filter) {
        effects.visible = !effects.applyFilter(filter);
        bonuses.visible = !bonuses.applyFilter(filter);
        blacklist.visible = !blacklist.applyFilter(filter);
        modifiers.visible = !modifiers.applyFilter(filter);
        boolean filtered = (!effects.visible && !bonuses.visible && !blacklist.visible && !modifiers.visible)
                || !filter.matchesSkill(skills)
                || !filter.matchesObject(OBJECT.BIOMES)
                || (!filter.matchesTextFilter(id)
                && !filter.matchesTextFilter(name));
        this.setHeight(filtered ? 0 : 18 + effects.getHeight() + bonuses.getHeight() + blacklist.getHeight() + modifiers.getHeight() + 2);
        return filtered;
    }
}
