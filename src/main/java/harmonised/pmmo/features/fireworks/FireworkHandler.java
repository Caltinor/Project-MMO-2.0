package harmonised.pmmo.features.fireworks;

import org.apache.commons.lang3.function.TriFunction;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.core.Core;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FireworkHandler {
	public static final String FIREWORK_SKILL = "firework_skill";
	//This is technically a perk which is by default used during the SKILL_UP event trigger
	//This should be passed both the skill triggering the event via FIREWORK_SKILL and take a parameter of SKILLNAME from the settings
	public static TriFunction<Player, CompoundTag, Integer, CompoundTag> FIREWORKS = (player, nbt, level) -> {
		String skill = nbt.getString(APIUtils.SKILLNAME);
		if (!skill.equals(nbt.getString(FIREWORK_SKILL))) return new CompoundTag();
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

		int[] colors = new int[] {Core.get(world).getDataConfig().getSkillColor(skill)};

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
