package harmonised.pmmo.registry;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import harmonised.pmmo.core.Core;
import harmonised.pmmo.network.clientpackets.CP_ApplyConfigRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.PacketTarget;
import net.minecraftforge.network.simple.SimpleChannel;

/**This registry contains default and override configurations for 
 * all data objects.
 * 
 * @author Caltinor
 *
 */
public class ConfigurationRegistry {
	private ConfigurationRegistry() {}
	private static ConfigurationRegistry INSTANCE = new ConfigurationRegistry();
	
	/**Obtain the singleton instance of this class
	 * 
	 * @return a side-shared instance of this
	 */
	public static ConfigurationRegistry get() {return INSTANCE;}
	
	//TODO move default settings to the loader it corresponds to and skip the consumer
	private Set<Consumer<Core>> defaultSettings = new HashSet<>();
	private Set<Consumer<Core>> overrideSettings = new HashSet<>();
	
	/**Executes the default consumers on the given side for the 
	 * {@link harmonised.pmmo.core.Core Core} instance provided.
	 * <p>Defaults apply before datapacks are read, so configurations
	 * applied during this sequence will be overriden by anything
	 * that follows.</p>
	 * 
	 * @param core
	 */
	public void applyDefaults(Core core) {
		defaultSettings.forEach(consumer -> consumer.accept(core));
	}
	
	/**Executes the override consumers on the given side for the 
	 * {@link harmonised.pmmo.core.Core Core} instance provided.
	 * <p>Overrides apply after datapacks are read, so configurations
	 * applied during this sequence will override anything
	 * that preceded it.</p>
	 * 
	 * @param core
	 */
	public void applyOverrides(Core core) {
		overrideSettings.forEach(consumer -> consumer.accept(core));
	}
	
	/**Adds a new default consumer to the defaults list.
	 * <p><i>Note: this does not check for duplicates.
	 * Any registrations should be done during mod construction
	 * and never called again. The purpose of this registration
	 * is to cache the processor for repeated use at a
	 * later point, such as data reloads.
	 * 
	 * @param processor
	 */
	public void registerDefault(Consumer<Core> processor) {
		defaultSettings.add(processor);
	}
	
	/**Adds a new override consumer to the defaults list.
	 * <p><i>Note: this does not check for duplicates.
	 * Any registrations should be done during mod construction
	 * and never called again. The purpose of this registration
	 * is to cache the processor for repeated use at a
	 * later point, such as data reloads.
	 * 
	 * @param processor
	 */
	public void registerOverride(Consumer<Core> processor) {
		overrideSettings.add(processor);
	}
	
	
	/**Used by {@link harmonised.pmmo.network.Networking#registerDataSyncPackets()}
	 * to send a packet to the player during various stages of the reload
	 * process to trigger client side application of the configurations.
	 * 
	 * @param channel the network channel the packet is being sent through
	 * @param isOverride should the client behavior apply the overrides (true) or the defaults (false)
	 */
	public static void addSyncPacket(SimpleChannel channel, boolean isOverride) {
		MinecraftForge.EVENT_BUS.addListener(ConfigurationRegistry.onDataReload(channel, isOverride));
	}
	
	private static Consumer<OnDatapackSyncEvent> onDataReload(SimpleChannel channel, boolean isOverride) {
		return event -> {
			ServerPlayer player = event.getPlayer();
			PacketTarget target = player == null
					? PacketDistributor.ALL.noArg()
					: PacketDistributor.PLAYER.with(() -> player);
			channel.send(target, new CP_ApplyConfigRegistry(isOverride));
		};
	}
}
