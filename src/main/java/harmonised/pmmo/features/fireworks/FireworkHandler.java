package harmonised.pmmo.features.fireworks;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.perks.Perk;
import harmonised.pmmo.core.CoreUtils;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.TagBuilder;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class FireworkHandler {
	/**A sorely misplaced key used by the SKILL_UP event to mark what skill leveled up.*/
	public static final String FIREWORK_SKILL = "firework_skill";
	
	public static final Perk FIREWORK = Perk.begin()
			.addDefaults(TagBuilder.start().withString(FIREWORK_SKILL, "none").build())
			.setStart((player, nbt) -> {
				BlockPos pos = player.blockPosition();
				spawnRocket(player.level(), new Vec3(pos.getX(), pos.getY(), pos.getZ()), nbt.getString(FIREWORK_SKILL));
				return new CompoundTag();
			})
//			.setDescription(LangProvider.PERK_FIREWORK_DESC.asComponent())
//			.setStatus((p, nbt) -> List.of(LangProvider.PERK_FIREWORK_STATUS_1
//					.asComponent(Component.translatable("pmmo."+nbt.getString(APIUtils.SKILLNAME))
//							.withStyle(CoreUtils.getSkillStyle(nbt.getString(APIUtils.SKILLNAME))))))
			.build();
	
	public static void spawnRocket(Level world, Vec3 pos, String skill/*, @Nullable WorldText explosionText*/)
	{
		var colors = IntList.of(CoreUtils.getSkillColor(skill));
		ItemStack itemStack = new ItemStack(Items.FIREWORK_ROCKET);
		var explosion = new FireworkExplosion(FireworkExplosion.DEFAULT.shape(), colors, colors, false, true);
		itemStack.set(DataComponents.FIREWORKS, new Fireworks(0, List.of(explosion)));

		PMMOFireworkEntity fireworkRocketEntity = new PMMOFireworkEntity(world, pos.x() + 0.5D, pos.y() + 0.5D, pos.z() + 0.5D, itemStack);
		//if(explosionText != null)
		//	fireworkRocketEntity.setExplosionText(explosionText);
		world.addFreshEntity(fireworkRocketEntity);
	}
}
