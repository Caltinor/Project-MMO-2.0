package harmonised.pmmo.features.fireworks;

import org.apache.commons.lang3.function.TriFunction;

import harmonised.pmmo.api.APIUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FireworkHandler {
	//This is technically a perk which is by default used during the SKILL_UP event trigger
	public static TriFunction<ServerPlayer, CompoundTag, Integer, CompoundTag> FIREWORKS = (player, nbt, level) -> {
		String skill = nbt.contains(APIUtils.SKILLNAME) ? nbt.getString(APIUtils.SKILLNAME) : "none";
		BlockPos pos = player.blockPosition();
		//GET STRING COLOR
		spawnRocket(player.getLevel(), new Vec3(pos.getX(), pos.getY(), pos.getZ()), skill);
		return new CompoundTag();
	};
	
	public static void spawnRocket(Level world, Vec3 pos, String skill/*, @Nullable WorldText explosionText*/)
	{
		CompoundTag nbt = new CompoundTag();
		CompoundTag fw = new CompoundTag();
		ListTag explosion = new ListTag();
		CompoundTag l = new CompoundTag();

		int[] colors = new int[1];
		//colors[0] = Skill.getSkillColor(skill);
//		int[] fadeColors = {0xff0000, 0x00ff00, 0x0000ff};

		l.putInt("Flicker", 1);
		l.putInt("Trail", 0);
		l.putInt("Type", 1);
		l.put("Colors", new IntArrayTag(colors));
//		l.put("FadeColors", new IntArrayNBT(fadeColors));
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
