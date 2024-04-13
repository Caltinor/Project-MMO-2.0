package harmonised.pmmo.config.codecs;

import com.mojang.serialization.Codec;
import harmonised.pmmo.config.readers.ConfigListener;

public interface ConfigData<T> extends DataSource<T>{
    Codec<T> getCodec();

    ConfigListener.ServerConfigs getType();
 }
