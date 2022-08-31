package harmonised.pmmo.config.writers;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.registries.ForgeRegistries;

public class PackGenerator {
	public static final String PACKNAME = "generated_pack";
	
	private enum Category {
		ITEMS("pmmo/items", server -> ForgeRegistries.ITEMS.getKeys()),
		BLOCKS("pmmo/blocks", server -> ForgeRegistries.BLOCKS.getKeys()),
		ENTITIES("pmmo/entities", server -> ForgeRegistries.ENTITY_TYPES.getKeys()),
		DIMENSIONS("pmmo/dimensions", server -> new HashSet<>(server.levelKeys().stream().map(key -> key.location()).toList())),
		BIOMES("pmmo/biomes", server -> ForgeRegistries.BIOMES.getKeys()),
		ENCHANTMENTS("pmmo/enchantments", server -> ForgeRegistries.ENCHANTMENTS.getKeys());
		
		public String route;
		public Function<MinecraftServer, Set<ResourceLocation>> valueList;
		Category(String route, Function<MinecraftServer, Set<ResourceLocation>> values) {
			this.route = route;
			this.valueList = values;
		}
	}
	
	public static void generateEmptyPack(MinecraftServer server) {		
		Path filepath = server.getWorldPath(LevelResource.DATAPACK_DIR).resolve(PACKNAME);
		filepath.toFile().mkdirs();
		try {
		Files.write(
				filepath.resolve("pack.mcmeta"), 
				List.of("{","\"pack\":{\"description\":\"Generated Resources\",","\"pack_format\":9}","}"), 
				Charset.defaultCharset(),
				StandardOpenOption.CREATE_NEW,
				StandardOpenOption.WRITE);
		} catch (IOException e) {System.out.println("Error While Generating pack.mcmeta for Generated Data"); e.printStackTrace();}
		
		for (Category category : Category.values()) {
			for (ResourceLocation id : category.valueList.apply(server)) {
				Path finalPath = filepath.resolve("data/"+id.getNamespace()+"/"+category.route);
				finalPath.toFile().mkdirs();
				try {
				Files.write(
						finalPath.resolve(id.getPath()+".json"), 
						List.of("{","}"), 
						Charset.defaultCharset(),
						StandardOpenOption.CREATE_NEW,
						StandardOpenOption.WRITE);
				} catch (IOException e) {System.out.println("Error While Generating Pack File For: "+id.toString()); e.printStackTrace();}
			}			
		}
	}
}
