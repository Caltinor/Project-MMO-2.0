package harmonised.pmmo.testing;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class CodeDump {
	public static List<ResourceLocation> getTagMembers(String tag)
	{
		ResourceLocation tagRL = new ResourceLocation(tag);
		List<ResourceLocation> results = new ArrayList<>();

		if (ItemTags.getAllTags().getAllTags().containsKey(tagRL)) {
			for(Item element : ItemTags.getAllTags().getAllTags().get(tagRL).getValues())	{
				try	{
					results.add(element.getRegistryName());
				} catch(Exception e){ /* Failed, don't care */ };
			}
		}

		if (BlockTags.getAllTags().getAllTags().containsKey(tagRL)) {
			for(Block element : BlockTags.getAllTags().getAllTags().get(tagRL).getValues()) {
				try	{
					results.add(element.getRegistryName());
				} catch(Exception e){ /* Failed, don't care */ };
			}
		}

		if(FluidTags.getAllTags().getAllTags().containsKey(tagRL)) {
			for(Fluid element : FluidTags.getAllTags().getAllTags().get(tagRL).getValues()) {
				try	{
					results.add(element.getRegistryName());
				} catch(Exception e){ /* Failed, don't care */ };
			}
		}
		
		if (EntityTypeTags.getAllTags().getAllTags().containsKey(tagRL)) {
			for(EntityType<?> element : EntityTypeTags.getAllTags().getAllTags().get(tagRL).getValues()) {
				try	{
					results.add(element.getRegistryName());
				} catch(Exception e){ /* Failed, don't care */ };
			}
		}

		return results;
	}	
}
