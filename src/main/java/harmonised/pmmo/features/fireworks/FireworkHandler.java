package harmonised.pmmo.features.fireworks;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.perks.Perk;
import harmonised.pmmo.core.CoreUtils;
import harmonised.pmmo.setup.datagen.LangProvider;
import harmonised.pmmo.util.TagBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
			.setDescription(LangProvider.PERK_FIREWORK_DESC.asComponent())
			.setStatus((p, nbt) -> List.of(LangProvider.PERK_FIREWORK_STATUS_1
					.asComponent(Component.translatable("pmmo."+nbt.getString(APIUtils.SKILLNAME))
							.withStyle(CoreUtils.getSkillStyle(nbt.getString(APIUtils.SKILLNAME)))))).build();
	
	public static void spawnRocket(Level world, Vec3 pos, String skill/*, @Nullable WorldText explosionText*/)
	{
		CompoundTag nbt = new CompoundTag();
		CompoundTag fw = new CompoundTag();
		ListTag explosion = new ListTag();
		CompoundTag l = new CompoundTag();

		int[] colors = new int[] {CoreUtils.getSkillColor(skill)};

		l.putInt("Flicker", 1);
		l.putInt("Trail", 0);
		l.putInt("Type", 1);
		l.put("Colors", new IntArrayTag(colors));
		explosion.add(l);

		fw.put("Explosions", explosion);
		fw.putInt("Flight", 0);
		nbt.put("Fireworks", fw);

		ItemStack itemStack = new ItemStack(Items.FIREWORK_ROCKET);
		itemStack.setTag(nbt);

		PMMOFireworkEntity fireworkRocketEntity = new PMMOFireworkEntity(world, pos.x() + 0.5D, pos.y() + 0.5D, pos.z() + 0.5D, itemStack);
		//if(explosionText != null)
		//	fireworkRocketEntity.setExplosionText(explosionText);
		world.addFreshEntity(fireworkRocketEntity);
	}
}
