package harmonised.pmmo.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public class Reference 
{
	public static final String MOD_ID = "pmmo";
	public static final String NAME = "Project MMO";
	public static final String ACCEPTED_VERSIONS = "[1.16.5]";
	public static final String MC_VERSION = "1.16";
	
	public static final TagKey<Block> FORGE_ORES = TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), new ResourceLocation("forge:ores"));
	public static final TagKey<Block> FORGE_LOGS = TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), new ResourceLocation("forge:logs"));
	public static final TagKey<Block> FORGE_PLANTS = TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), new ResourceLocation("forge:plants"));
	public static final TagKey<Block> FORGE_CROPS = TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), new ResourceLocation("forge:crops"));
}
