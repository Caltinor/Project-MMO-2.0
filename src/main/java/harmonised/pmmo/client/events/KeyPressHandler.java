package harmonised.pmmo.client.events;

import harmonised.pmmo.client.gui.StatsScreen;
import harmonised.pmmo.client.utils.VeinTracker;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.network.Networking;
import harmonised.pmmo.network.serverpackets.SP_OtherExpRequest;
import harmonised.pmmo.network.serverpackets.SP_SetVeinLimit;
import harmonised.pmmo.network.serverpackets.SP_SetVeinShape;
import harmonised.pmmo.network.serverpackets.SP_UpdateVeinTarget;
import harmonised.pmmo.setup.ClientSetup;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.RegistryUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE, value=Dist.CLIENT)
public class KeyPressHandler {

	@SubscribeEvent
    public static void keyPressEvent(net.minecraftforge.client.event.InputEvent.Key event)
    {
		Minecraft mc = Minecraft.getInstance();
        if(mc.player != null)
        {
            if(ClientSetup.VEIN_KEY.isDown() && mc.hitResult != null && mc.hitResult.getType().equals(Type.BLOCK)) {
            	BlockHitResult bhr = (BlockHitResult) mc.hitResult;
            	Block block = mc.player.level.getBlockState(bhr.getBlockPos()).getBlock();
            	if (!Core.get(LogicalSide.CLIENT).getLoader().DIMENSION_LOADER
            			.getData(mc.player.level.dimension().location())
            			.veinBlacklist().contains(RegistryUtil.getId(block))
            		&& !Core.get(LogicalSide.CLIENT).getLoader().BIOME_LOADER
            			.getData(RegistryUtil.getId(mc.player.level.getBiome(mc.player.blockPosition()).get()))
            			.veinBlacklist().contains(RegistryUtil.getId(block))) {
	            	VeinTracker.setTarget(bhr.getBlockPos());
	            	Networking.sendToServer(new SP_UpdateVeinTarget(bhr.getBlockPos()));
            	}
            	else
            		mc.player.sendSystemMessage(LangProvider.VEIN_BLACKLIST.asComponent());
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
            		if (mc.hitResult.getType().equals(Type.BLOCK)) 
            			mc.setScreen(new StatsScreen(((BlockHitResult)mc.hitResult).getBlockPos()));
            		else if (mc.hitResult.getType().equals(Type.ENTITY)) {
            			EntityHitResult ehr = (EntityHitResult) mc.hitResult;
            			Networking.sendToServer(new SP_OtherExpRequest(ehr.getEntity().getUUID()));
            			mc.setScreen(new StatsScreen(ehr.getEntity()));
            		}
            		else if (mc.hitResult.getType().equals(Type.MISS))
            			mc.setScreen(new StatsScreen(mc.player));
            	}
            	else if (mc.player.isCrouching())
            		mc.setScreen(new StatsScreen()); 
            }
        }
    }

}
