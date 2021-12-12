package harmonised.pmmo.gui;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.skills.Skill;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class WorldXpDrop
{
    public static float worldXpDropsRotationCap = (float) (0f + Config.forgeConfig.worldXpDropsRotationCap.get());

    private final ResourceLocation worldResLoc;
    private final Vector3d pos;
    private final String skill;
    private final int color;
    private float rotation;
    private float size = 1;
    private float decaySpeed = 1;
    public float xp, startXp;

    public static WorldXpDrop fromXYZ(ResourceLocation worldResLoc, double x, double y, double z, double maxOffset, double xp, String skill)
    {
        return fromXYZ(worldResLoc, x, y, z, maxOffset, (float) xp, skill);
    }

    public static WorldXpDrop fromXYZ(ResourceLocation worldResLoc, double x, double y, double z, double maxOffset, float xp, String skill)
    {
        return new WorldXpDrop(worldResLoc, new Vector3d(x + Math.random()*maxOffset*2 - maxOffset, y + Math.random()*maxOffset*2 - maxOffset, z + Math.random()*maxOffset*2 - maxOffset), xp, skill);
    }

    public static WorldXpDrop fromVector(ResourceLocation worldResLoc, Vector3d pos, double maxOffset, double xp, String skill)
    {
        return fromVector(worldResLoc, pos, maxOffset, (float) xp, skill);
    }

    public static WorldXpDrop fromVector(ResourceLocation worldResLoc, Vector3d pos, double maxOffset, float xp, String skill)
    {
        return new WorldXpDrop(worldResLoc, maxOffset == 0 ? pos : new Vector3d(pos.getX() + Math.random()*maxOffset*2 - maxOffset, pos.getY() + Math.random()*maxOffset*2 - maxOffset, pos.getZ() + Math.random()*maxOffset*2 - maxOffset), xp, skill);
    }

    public static WorldXpDrop fromBlockPos(ResourceLocation worldResLoc, BlockPos pos, double maxOffset, double xp, String skill)
    {
        return fromBlockPos(worldResLoc, pos, maxOffset, (float) xp, skill);
    }

    public static WorldXpDrop fromBlockPos(ResourceLocation worldResLoc, BlockPos pos, double maxOffset, float xp, String skill)
    {
        return new WorldXpDrop(worldResLoc, new Vector3d(pos.getX() + 0.5 + Math.random()*maxOffset*2 - maxOffset, pos.getY() + 0.5 + Math.random()*maxOffset*2 - maxOffset, pos.getZ() + 0.5 + Math.random()*maxOffset*2 - maxOffset), xp, skill);
    }

    private WorldXpDrop(ResourceLocation worldResLoc, Vector3d pos, float xp, String skill)
    {
        this.worldResLoc = worldResLoc;
        this.pos = pos;
        this.startXp = xp;
        this.xp = this.startXp;
        this.skill = skill;
        this.color = Skill.getSkillColor(skill);
        this.rotation = getRandomRotation();
    }

    private static float getRandomRotation()
    {
        return (float) (Math.random()*worldXpDropsRotationCap*2 - worldXpDropsRotationCap);
    }

    public Vector3d getPos()
    {
        return pos;
    }

    public String getSkill()
    {
        return skill;
    }

    public int getColor()
    {
        return color;
    }

    public float getStartXp()
    {
        return startXp;
    }

    public float getRotation()
    {
        return rotation;
    }

    public void setRotation(float rotation)
    {
        this.rotation = rotation;
    }

    public float getSize()
    {
        return size;
    }

    public void setSize(float size)
    {
        this.size = size;
    }

    public float getDecaySpeed()
    {
        return decaySpeed;
    }

    public void setDecaySpeed(double decaySpeed)
    {
        this.decaySpeed = (float) decaySpeed;
    }

    public void setDecaySpeed(float decaySpeed)
    {
        this.decaySpeed = decaySpeed;
    }

    public ResourceLocation getWorldResLoc()
    {
        return worldResLoc;
    }
}
