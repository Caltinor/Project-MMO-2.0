package harmonised.pmmo.config.codecs;

import com.mojang.serialization.MapCodec;
import harmonised.pmmo.config.readers.ConfigListener;

import java.util.Map;

public interface ConfigData<T> extends DataSource<T>{
    MapCodec<T> getCodec();
    ConfigListener.ServerConfigs getType();
    ConfigData<T> getFromScripting(String param, Map<String, String> value);
 }
