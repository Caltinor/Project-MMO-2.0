package harmonised.pmmo.config.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import harmonised.pmmo.config.readers.ConfigListener;

public interface ConfigData<T> extends DataSource<T>{
    MapCodec<T> getCodec();

    ConfigListener.ServerConfigs getType();
 }
