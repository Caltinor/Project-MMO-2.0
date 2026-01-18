package harmonised.pmmo.client.events;

import harmonised.pmmo.api.client.PanelWidget;
import harmonised.pmmo.client.gui.glossary.GlossaryLoadingScreen;
import harmonised.pmmo.client.gui.glossary.components.panels.BiomeObjectPanelWidget;
import harmonised.pmmo.client.gui.glossary.components.panels.BlockObjectPanelWidget;
import harmonised.pmmo.client.gui.glossary.components.panels.EntityObjectPanelWidget;
import harmonised.pmmo.client.gui.glossary.components.panels.PlayerObjectPanelWidget;
import harmonised.pmmo.client.utils.VeinTracker;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.network.serverpackets.SP_OtherExpRequest;
import harmonised.pmmo.network.serverpackets.SP_SetVeinLimit;
import harmonised.pmmo.network.serverpackets.SP_SetVeinShape;
import harmonised.pmmo.network.serverpackets.SP_ToggleBreakSpeed;
import harmonised.pmmo.network.serverpackets.SP_UpdateVeinTarget;
import harmonised.pmmo.setup.ClientSetup;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid=Reference.MOD_ID, bus=EventBusSubscriber.Bus.GAME, value= Dist.CLIENT)
public class KeyPressHandler {

	@SubscribeEvent
    public static void keyPressEvent(InputEvent.Key event)
	{
		Minecraft mc = Minecraft.getInstance();
        if(mc.player != null)
        {
            if(ClientSetup.VEIN_KEY.isDown() && mc.hitResult != null && mc.hitResult.getType().equals(Type.BLOCK)) {
            	BlockHitResult bhr = (BlockHitResult) mc.hitResult;
            	Block block = mc.player.level().getBlockState(bhr.getBlockPos()).getBlock();
            	if (!Core.get(LogicalSide.CLIENT).getLoader().DIMENSION_LOADER
            			.getData(mc.player.level().dimension().location())
            			.veinBlacklist().contains(RegistryUtil.getId(block))
            		&& !Core.get(LogicalSide.CLIENT).getLoader().BIOME_LOADER
            			.getData(RegistryUtil.getId(mc.player.level().getBiome(mc.player.blockPosition())))
            			.veinBlacklist().contains(RegistryUtil.getId(block))) {
	            	VeinTracker.setTarget(bhr.getBlockPos());
	            	Networking.sendToServer(new SP_UpdateVeinTarget(bhr.getBlockPos()));
            	}
            	else
            		mc.player.sendSystemMessage(LangProvider.VEIN_BLACKLIST.asComponent());
            }
			if (ClientSetup.TOGGLE_BRKSPD.isDown()) {
				Config.BREAK_SPEED_PERKS.set(!Config.BREAK_SPEED_PERKS.get());
				Networking.sendToServer(new SP_ToggleBreakSpeed(Config.BREAK_SPEED_PERKS.get()));
				mc.player.displayClientMessage(Config.BREAK_SPEED_PERKS.get()
						? LangProvider.TOGGLE_BRKSPD_ENABLED.asComponent()
						: LangProvider.TOGGLE_BRKSPD_DISABLED.asComponent()
				, false);
			}
            if (ClientSetup.SHOW_LIST.isDown()) {
            	Config.SKILL_LIST_DISPLAY.set(!Config.SKILL_LIST_DISPLAY.get());
            }
            if (ClientSetup.SHOW_VEIN.isDown()) {
            	Config.VEIN_GAUGE_DISPLAY.set(!Config.VEIN_GAUGE_DISPLAY.get());
            }
            if (ClientSetup.ADD_VEIN.isDown()) {
            	Config.VEIN_LIMIT.set(Config.VEIN_LIMIT.get()+1);
            	Networking.sendToServer(new SP_SetVeinLimit(Config.VEIN_LIMIT.get()));
            }
            if (ClientSetup.SUB_VEIN.isDown()) {
            	Config.VEIN_LIMIT.set(Config.VEIN_LIMIT.get()-1);
            	Networking.sendToServer(new SP_SetVeinLimit(Config.VEIN_LIMIT.get()));
            }
            if (ClientSetup.CYCLE_VEIN.isDown()) {
            	VeinTracker.nextMode();
            	mc.player.displayClientMessage(LangProvider.VEIN_SHAPE.asComponent(VeinTracker.mode.name()), false);
            	Networking.sendToServer(new SP_SetVeinShape(VeinTracker.mode));
            }
            if (mc.screen == null && ClientSetup.OPEN_MENU.isDown()) {
            	if (mc.hitResult != null && !mc.player.isCrouching()) {
            		if (mc.hitResult.getType().equals(Type.BLOCK)) {
						BlockHitResult bhr = (BlockHitResult) mc.hitResult;
						BlockState state = mc.level.getBlockState(bhr.getBlockPos());
						BlockEntity be = mc.level.getBlockEntity(bhr.getBlockPos());
						PanelWidget widget = new BlockObjectPanelWidget(0x88394045, 400, state.getBlock(), be);
						mc.setScreen(new GlossaryLoadingScreen(widget));
					}
            		else if (mc.hitResult.getType().equals(Type.ENTITY)) {
            			EntityHitResult ehr = (EntityHitResult) mc.hitResult;
						PanelWidget widget;
						if (ehr.getEntity() instanceof Player player) {
							Networking.sendToServer(new SP_OtherExpRequest(ehr.getEntity().getUUID()));
							widget = new PlayerObjectPanelWidget(0x88394045, 400, player);
						}
						else
							widget = new EntityObjectPanelWidget(0x88394045, 400, ehr.getEntity());
            			mc.setScreen(new GlossaryLoadingScreen(widget));
            		}
            		else if (mc.hitResult.getType().equals(Type.MISS))
            			mc.setScreen(new GlossaryLoadingScreen(null));
            	}
            	else if (mc.player.isCrouching()) {
					PanelWidget widget = new BiomeObjectPanelWidget(0x88394045, 400, mc.level.getBiome(mc.player.blockPosition()));
					mc.setScreen(new GlossaryLoadingScreen(widget));
				}
            }
        }
    }

}
