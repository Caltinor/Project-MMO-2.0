package harmonised.pmmo.client.events;

import java.util.ArrayList;
import java.util.List;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.SkillsConfig;
import harmonised.pmmo.config.codecs.SkillData;
import harmonised.pmmo.core.CoreUtils;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import harmonised.pmmo.util.MsLoggy.LOG_CODE;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=Reference.MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE, value=Dist.CLIENT)
public class ClientTickHandler {
	private static int ticksElapsed = 0;
	
	public static void tickGUI() {ticksElapsed++;}	
	public static boolean isRefreshTick() {return ticksElapsed >= 15;}
	public static void resetTicks() {ticksElapsed = 0;}
	
	public static final List<GainEntry> xpGains = new ArrayList<>();
	
	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		ticksElapsed++;
		tickDownGainList();
	}
	
	public static void tickDownGainList() {
		for (GainEntry gain : xpGains) {
			gain.downTick();
		}
	}
	
	public static void addToGainList(String skill, long amount) {
		SkillData skillData = SkillsConfig.SKILLS.get().getOrDefault(skill, SkillData.Builder.getDefault());
		if (Config.GAIN_BLACKLIST.get().contains(skill) 
				|| (skillData.isSkillGroup() && skillData.getGroup().containsKey(skill)))
			return;
		if (xpGains.size() >= Config.GAIN_LIST_SIZE.get()) 
			xpGains.remove(0);
		xpGains.add(new GainEntry(skill, amount));
	}
	
	public static class GainEntry {
		public int duration;
		public final Component display;
		public GainEntry(String skill, long value) {
			this.duration = MsLoggy.DEBUG.logAndReturn(Config.GAIN_LIST_LINGER_DURATION.get()
								, LOG_CODE.GUI, "Gain Duration Set as: {}");
			display = Component.literal("+"+value+" ")
					.append(Component.translatable("pmmo."+skill))
					.setStyle(CoreUtils.getSkillStyle(skill));
		}
		public void downTick() {
			duration--;
		}
		@Override
		public String toString() {
			return "Duration:"+duration+"|"+display.toString();
		}
	}
}
