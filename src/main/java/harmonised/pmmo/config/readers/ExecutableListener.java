package harmonised.pmmo.config.readers;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.simple.SimpleChannel;

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
	 * Calling this method automatically subscribes a packet-sender to {@link OnDatapackSyncEvent}.
	 * @param <PACKET> the packet type that will be sent on the given channel
	 * @param channel The networking channel of your mod
	 * @param packetFactory  A packet constructor or factory method that converts the given map to a packet object to send on the given channel
	 * @return this manager object
	 */
	public <PACKET> ExecutableListener subscribeAsSyncable(final SimpleChannel channel,
		final Supplier<PACKET> packetFactory)
	{
		NeoForge.EVENT_BUS.addListener(this.getDatapackSyncListener(channel, packetFactory));
		return this;
	}
	
	/** Generate an event listener function for the on-datapack-sync event **/
	private <PACKET> Consumer<OnDatapackSyncEvent> getDatapackSyncListener(final SimpleChannel channel,
																		   final Supplier<PACKET> packetFactory)
	{
		return event -> {
			ServerPlayer player = event.getPlayer();
			PacketDistributor.PacketTarget target = player == null
				? PacketDistributor.ALL.noArg()
				: PacketDistributor.PLAYER.with(() -> player);
			channel.send(target, packetFactory.get());
		};
	}
}
