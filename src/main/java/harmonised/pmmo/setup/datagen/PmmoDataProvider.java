package harmonised.pmmo.setup.datagen;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import harmonised.pmmo.config.codecs.DataSource;
import harmonised.pmmo.util.MsLoggy;
import harmonised.pmmo.util.Reference;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public abstract class PmmoDataProvider<T extends DataSource<?>> implements DataProvider {
    private final Path destination;
    private final String dataPath;
    private final PackOutput output;
    private final Codec<T> codec;
    private final Map<ResourceLocation, JsonElement> toSerialize = new HashMap<>();
    public PmmoDataProvider(PackOutput gen, String packName, String dataPath, Codec<T> codec) {
        this.output = gen;
        this.destination = Path.of("resourcepacks").resolve(packName).resolve("data");
        this.dataPath = dataPath;
        this.codec = codec;
    }

    public void add(ResourceLocation id, T instance) {
        JsonElement json = codec.encodeStart(JsonOps.INSTANCE, instance).getOrThrow(false, s -> MsLoggy.WARN.log(MsLoggy.LOG_CODE.DATA, s));
        this.toSerialize.put(id, json);
    }
    protected abstract void start();
    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        start();
        ImmutableList.Builder<CompletableFuture<?>> futuresBuilder = new ImmutableList.Builder<>();

        toSerialize.forEach(LamdbaExceptionUtils.rethrowBiConsumer((name, json) -> {
            Path modifierPath = output.getOutputFolder().resolve(destination).resolve(name.getNamespace()).resolve(dataPath).resolve(name.getPath() + ".json");
            futuresBuilder.add(DataProvider.saveStable(cache, json, modifierPath));
        }));
        return CompletableFuture.allOf(futuresBuilder.build().toArray(CompletableFuture[]::new));
    }
    @Override
    public String getName() {
        return "Project MMO Object Data : " + Reference.MOD_ID;
    }
}
