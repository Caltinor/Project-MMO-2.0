package harmonised.pmmo.gui;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.skills.Skill;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class WorldXpDrop
{
    public static float worldXpDropsRotationCap = (float) ( 0f + Config.forgeConfig.worldXpDropsRotationCap.get() );

    private final ResourceLocation worldResLoc;
    private final Vector3d pos;
    private final String skill;
    private final int color;
    private float rotation;
    private float size = 1;
    private double decaySpeed = 1;
    public double xp, startXp;

    public WorldXpDrop( ResourceLocation worldResLoc, double x, double y, double z, double maxOffsetFromMiddle, double xp, String skill )
    {
        this.worldResLoc = worldResLoc;
        this.pos = new Vector3d( x + Math.random()*maxOffsetFromMiddle*2 - maxOffsetFromMiddle, y + Math.random()*maxOffsetFromMiddle*2 - maxOffsetFromMiddle, z + Math.random()*maxOffsetFromMiddle*2 - maxOffsetFromMiddle );
        this.startXp = xp;
        this.xp = this.startXp;
        this.skill = skill;
        this.color = Skill.getSkillColor( skill );
        this.rotation = getRandomRotation();
    }

    public WorldXpDrop( ResourceLocation worldResLoc, Vector3d pos, double maxOffsetFromMiddle, double xp, String skill )
    {
        this.worldResLoc = worldResLoc;
        this.pos = maxOffsetFromMiddle == 0 ? pos : new Vector3d( pos.getX() + Math.random()*maxOffsetFromMiddle*2 - maxOffsetFromMiddle, pos.getY() + Math.random()*maxOffsetFromMiddle*2 - maxOffsetFromMiddle, pos.getZ() + Math.random()*maxOffsetFromMiddle*2 - maxOffsetFromMiddle );
        this.startXp = xp;
        this.xp = this.startXp;
        this.skill = skill;
        this.color = Skill.getSkillColor( skill );
        this.rotation = getRandomRotation();
    }

    public WorldXpDrop( ResourceLocation worldResLoc, Vector3d pos, double xp, String skill )
    {
        this.worldResLoc = worldResLoc;
        this.pos = pos;
        this.startXp = xp;
        this.xp = this.startXp;
        this.skill = skill;
        this.color = Skill.getSkillColor( skill );
        this.rotation = getRandomRotation();
    }

    public WorldXpDrop( ResourceLocation worldResLoc, BlockPos pos, double maxOffsetFromMiddle, double xp, String skill )
    {
        this.worldResLoc = worldResLoc;
        this.pos = new Vector3d( pos.getX() + 0.5 + Math.random()*maxOffsetFromMiddle*2 - maxOffsetFromMiddle, pos.getY() + 0.5 + Math.random()*maxOffsetFromMiddle*2 - maxOffsetFromMiddle, pos.getZ() + 0.5 + Math.random()*maxOffsetFromMiddle*2 - maxOffsetFromMiddle );
        this.startXp = xp;
        this.xp = this.startXp;
        this.skill = skill;
        this.color = Skill.getSkillColor( skill );
        this.rotation = getRandomRotation();
    }

    private static float getRandomRotation()
    {
        return (float) ( Math.random()*worldXpDropsRotationCap*2 - worldXpDropsRotationCap );
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

    public double getStartXp()
    {
        return startXp;
    }

    public float getRotation()
    {
        return rotation;
    }

    public void setRotation( float rotation )
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

    public double getDecaySpeed()
    {
        return decaySpeed;
    }

    public void setDecaySpeed( double decaySpeed )
    {
        this.decaySpeed = decaySpeed;
    }

    public ResourceLocation getWorldResLoc()
    {
        return worldResLoc;
    }
}
