package harmonised.pmmo.config.readers;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ExecutableListener extends SimplePreparableReloadListener<Boolean> {
	private Runnable executor;
	
	public ExecutableListener(Runnable executor) {
		this.executor = executor;
	}

	@Override
	protected Boolean prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {return false;}

	@Override
	protected void apply(Boolean pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
		executor.run();		
	}

	/**
	 * This should be called at most once, during construction of your mod (static init of your main mod class is fine)
	 * (FMLCommonSetupEvent *may* work as well)
	 * @param packetFactory  A packet constructor or factory method that converts the given map to a packet object to send on the given channel
	 * @return this manager object
	 */
	public ExecutableListener subscribeAsSyncable(
		final Supplier<CustomPacketPayload> packetFactory)
	{
		NeoForge.EVENT_BUS.addListener(this.getDatapackSyncListener(packetFactory));
		return this;
	}
	
	/** Generate an event listener function for the on-datapack-sync event **/
	private Consumer<OnDatapackSyncEvent> getDatapackSyncListener(final Supplier<CustomPacketPayload> packetFactory)
	{
		return event -> {
			ServerPlayer player = event.getPlayer();
			PacketDistributor.PacketTarget target = player == null
				? PacketDistributor.ALL.noArg()
				: PacketDistributor.PLAYER.with(player);
			target.send(packetFactory.get());
		};
	}
}
