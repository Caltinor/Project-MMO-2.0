package harmonised.pmmo.config.readers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import harmonised.pmmo.config.GlobalsConfig;
import harmonised.pmmo.config.PerksConfig;
import harmonised.pmmo.config.SkillsConfig;
import harmonised.pmmo.config.codecs.ConfigData;
import harmonised.pmmo.config.codecs.ServerData;
import harmonised.pmmo.features.anticheese.AntiCheeseConfig;
import harmonised.pmmo.features.autovalues.AutoValueConfig;
import harmonised.pmmo.network.clientpackets.CP_SyncConfig;
import harmonised.pmmo.util.MsLoggy;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.common.IExtensibleEnum;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ConfigListener extends SimplePreparableReloadListener<ConfigData<?>> {
    private final Gson gson = new Gson();
    public enum ServerConfigs implements StringRepresentable, IExtensibleEnum {
        SERVER(ServerData.CODEC, "server", ServerData::new),
        AUTOVALUES(AutoValueConfig.CODEC, "autovalues", AutoValueConfig::new),
        SKILLS(SkillsConfig.CODEC, "skills", SkillsConfig::new),
        PERKS(PerksConfig.CODEC, "perks", PerksConfig::new),
        GLOBALS(GlobalsConfig.CODEC, "globals", GlobalsConfig::new),
        ANTICHEESE(AntiCheeseConfig.CODEC, "anticheese", AntiCheeseConfig::new);

        public Codec<? extends ConfigData<?>> codec;
        public String filename;
        public Supplier<ConfigData<?>> defaultSupplier;
        ServerConfigs(Codec<? extends ConfigData<?>> codec, String filename, Supplier<ConfigData<?>> defaultSupplier) {
            this.codec = codec;
            this.filename = filename;
            this.defaultSupplier = defaultSupplier;
        }
        public static final Codec<ServerConfigs> CODEC = IExtensibleEnum.createCodecForExtensibleEnum(ServerConfigs::values, ServerConfigs::byName);
        private static final Map<String, ServerConfigs> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(ServerConfigs::getSerializedName, s -> s));

        public Codec<? extends ConfigData<?>> codec() {return codec;}
        @Override
        public String getSerializedName() {return this.name();}
        public static ServerConfigs byName(String name) {return BY_NAME.get(name);}
        public static ServerConfigs create(String name, Codec<? extends ConfigData<?>> codec, String filename, Supplier<ConfigData<?>> defaultSupplier) {throw new IllegalStateException("Enum not extended");}

        public static ServerConfigs fromFilename(String filename) {
            return Arrays.stream(values()).filter(sc -> sc.filename.equals(filename)).findFirst().orElse(null);
        }

        public static final Codec<ConfigData<?>> MAPPER = ExtraCodecs.lazyInitializedCodec(() ->
                ConfigListener.ServerConfigs.CODEC.dispatch("type", ConfigData::getType, x -> x.codec));
    }
    private Map<ServerConfigs, ConfigData<?>> configs = new HashMap<>();

    public void setData(ServerConfigs type, ConfigData<?> data) {this.configs.put(type, data);}
    public ServerData server() {return (ServerData) configs.computeIfAbsent(ServerConfigs.SERVER, a -> a.defaultSupplier.get());}
    public AutoValueConfig autovalues() {return (AutoValueConfig) configs.computeIfAbsent(ServerConfigs.AUTOVALUES, a -> a.defaultSupplier.get());}
    public GlobalsConfig globals() {return (GlobalsConfig) configs.computeIfAbsent(ServerConfigs.GLOBALS, a -> a.defaultSupplier.get());}
    public PerksConfig perks() {return (PerksConfig) configs.computeIfAbsent(ServerConfigs.PERKS, a -> a.defaultSupplier.get());}
    public SkillsConfig skills() {return (SkillsConfig) configs.computeIfAbsent(ServerConfigs.SKILLS, a -> a.defaultSupplier.get());}
    public AntiCheeseConfig anticheese() {return (AntiCheeseConfig) configs.computeIfAbsent(ServerConfigs.ANTICHEESE, a -> a.defaultSupplier.get());}
    public ConfigListener() {}

    @Override
    protected ConfigData<?> prepare(ResourceManager manager, ProfilerFiller profilerFiller) {
        for (Map.Entry<ResourceLocation, Resource> entry : manager.listResources("config",
                file -> file.getNamespace().equals("pmmo") && file.getPath().endsWith(".json")).entrySet()
        ){
            try (final Reader reader = entry.getValue().openAsReader()) {
                String filename = entry.getKey().getPath().substring(entry.getKey().getPath().lastIndexOf("/")+1);
                ServerConfigs type = ServerConfigs.fromFilename(filename);
                if (type == null) continue;
                final JsonElement jsonElement = GsonHelper.fromJson(this.gson, reader, JsonElement.class);
                type.codec.parse(JsonOps.INSTANCE, jsonElement)
                        .resultOrPartial(JsonParseException::new)
                        .ifPresent(obj -> this.configs.put(type, obj));
            }
            catch(RuntimeException | IOException exception) {
                MsLoggy.ERROR.log(MsLoggy.LOG_CODE.DATA, exception.getMessage());
            }
        }
        return null;
    }

    @Override
    protected void apply(ConfigData<?> p_10793_, ResourceManager p_10794_, ProfilerFiller p_10795_) {
        MsLoggy.INFO.log(MsLoggy.LOG_CODE.DATA, "Beginning loading of data for CONFIG loader");
    }


    public void subscribeAsSyncable()	{
		NeoForge.EVENT_BUS.addListener((final OnDatapackSyncEvent event) -> {
            ServerPlayer player = event.getPlayer();
            List<CustomPacketPayload> packets = new ArrayList<>();
            this.configs.forEach((key, value) -> packets.add(new CP_SyncConfig(key, value)));

            PacketDistributor.PacketTarget target = player == null
                    ? PacketDistributor.ALL.noArg()
                    : PacketDistributor.PLAYER.with(player);
            packets.forEach(target::send);
        });
    }
}
