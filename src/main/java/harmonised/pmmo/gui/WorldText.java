package harmonised.pmmo.gui;

import harmonised.pmmo.config.Config;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.NBTHelper;
import harmonised.pmmo.util.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class WorldText
{
    private static Map<PresetTextOption, CompoundTag> presetTextOptions = new HashMap<>();

    public static float worldXpDropsSizeMultiplier = (float) ( 0f + Config.forgeConfig.worldXpDropsSizeMultiplier.get() );
    public static float worldXpDropsDecaySpeedMultiplier = (float) ( 0f + Config.forgeConfig.worldXpDropsDecaySpeedMultiplier.get() );
    public static boolean worldXpDropsShowSkill =  Config.forgeConfig.worldXpDropsShowSkill.get();

    private ResourceLocation worldResLoc;
    private Vec3 startPos, endPos;
    private String text = "EMPTY";
    private float secondsLifespan = 1;
    private float maxOffset = 0;
    private byte preset = 0;
    private float age = 0, spanRatio;

    private boolean hueColor = false;
    private int color = 0xffffff;
    private float startHue = 0;
    private float startSaturation = 1;
    private float startBrightness = 1;
    private float endHue = 360;
    private float endSaturation = 1;
    private float endBrightness = 1;

    private boolean showValue = false;
    private float startValue = 0;
    private float endValue = 0;
    private float valueDecaySpeed = 1;
    private boolean decayByValue = false;
    private float value = 0;

    private float startSize = 1;
    private float endSize = 0;

    private float startRot = 0;
    private float endRot = 0;

    public static WorldText fromBlockPos( ResourceLocation worldResLoc, BlockPos pos )
    {
        return fromBlockPos( worldResLoc, pos, pos );
    }

    public static WorldText fromBlockPos( ResourceLocation worldResLoc, BlockPos startPos, BlockPos endPos )
    {
        return new WorldText( worldResLoc, new Vec3( startPos.getX() + 0.5, startPos.getY() + 0.5, startPos.getZ() + 0.5 ), new Vec3( endPos.getX() + 0.5, endPos.getY() + 0.5, endPos.getZ() + 0.5 ) );
    }

    public static WorldText fromVector( ResourceLocation worldResLoc, Vec3 pos )
    {
        return fromVector( worldResLoc, pos, pos );
    }

    public static WorldText fromVector( ResourceLocation worldResLoc, Vec3 startPos, Vec3 endPos )
    {
        return new WorldText( worldResLoc, startPos, endPos );
    }

    private WorldText( ResourceLocation worldResLoc, Vec3 startPos, Vec3 endPos )
    {
        this.worldResLoc = worldResLoc;
        this.startPos = startPos;
        this.endPos = endPos;
    }

    public boolean tick( double d )
    {
        if( decayByValue )    //Value
        {
            value -= Math.max( 0.01523, value * valueDecaySpeed * worldXpDropsDecaySpeedMultiplier * d );
            spanRatio = (float) Util.map( value, startValue, endValue, 1, 0 );
        }
        else    //Age
        {
            age += d;
            spanRatio = 1 - age/secondsLifespan;
        }

        return spanRatio > 0; //Returning false kills the text
    }

    public ResourceLocation getWorldResLoc()
    {
        return worldResLoc;
    }

    public Vec3 getStartPos()
    {
        return startPos;
    }

    public Vec3 getEndPos()
    {
        return endPos;
    }

    public Vec3 getPos()
    {
        return new Vec3
        (
            Util.map( spanRatio, 1, 0, startPos.x(), endPos.x() ),
            Util.map( spanRatio, 1, 0, startPos.y(), endPos.y() ),
            Util.map( spanRatio, 1, 0, startPos.z(), endPos.z() )
        );
    }

    public String getText()
    {
        if( showValue )
            return "+" + DP.dpSoft( value ) + " " + text;
        else
            return text;
    }

    public double getSize()
    {
        return Util.map( spanRatio, 1, 0, startSize, endSize ) * worldXpDropsSizeMultiplier * 0.02;
    }

    public float getRotation()
    {
        return (float) Util.map( spanRatio, 1, 0, startRot, endRot );
    }

    public int getColor()
    {
        if( hueColor )
            return Util.hueToRGB( (float) Util.map( spanRatio, 1, 0, startHue, endHue ) % 360, (float) Util.mapCapped( spanRatio, 0, 1, endSaturation, startSaturation ), (float) Util.mapCapped( spanRatio, 0, 1, endBrightness, startBrightness ) );
        else
            return color;
    }

    public enum PresetTextOption
    {
        BLOCK_BREAK( (byte) 1 ),
        BLOCK_PLACE( (byte) 2 );

        private static Map<Byte, PresetTextOption> options = new HashMap<>();
        private final byte value;

        static
        {
            for( PresetTextOption presetTextOption : PresetTextOption.values() )
            {
                options.put( presetTextOption.value, presetTextOption );
            }
        }

        PresetTextOption( byte value )
        {
            this.value = value;
        }

        public CompoundTag getPresetOptions()
        {
            return presetTextOptions.get( this );
        }

        public byte getValue()
        {
            return this.value;
        }

        public static PresetTextOption getPresetOption( byte type )
        {
            return options.get( type );
        }
    }

    public static void init()
    {
        CompoundTag blockBreakPreset = new CompoundTag();
        blockBreakPreset.putFloat( "maxOffset", 0.25F );
        blockBreakPreset.putFloat( "valueDecaySpeed", 1.25F );
        blockBreakPreset.putBoolean( "showValue", true );
        blockBreakPreset.putBoolean( "decayByValue", true );

        presetTextOptions.put( PresetTextOption.BLOCK_BREAK, blockBreakPreset );
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public float getSecondsLifespan()
    {
        return secondsLifespan;
    }

    public void setSecondsLifespan(float secondsLifespan)
    {
        this.secondsLifespan = secondsLifespan;
    }

    public float getMaxOffset()
    {
        return maxOffset;
    }

    public void setMaxOffset( float maxOffset )
    {
        this.maxOffset = maxOffset;
    }

    public void updatePos()
    {
        double xOffset = Math.random()*maxOffset*2 - maxOffset;
        double yOffset = Math.random()*maxOffset*2 - maxOffset;
        double zOffset = Math.random()*maxOffset*2 - maxOffset;
        this.startPos = maxOffset == 0 ? startPos : new Vec3( startPos.x() + xOffset, startPos.y() + yOffset, startPos.z() + zOffset );
        this.endPos = maxOffset == 0 ? endPos : new Vec3( endPos.x() + xOffset, endPos.y() + yOffset, endPos.z() + zOffset );
    }

    public byte getPreset()
    {
        return preset;
    }

    public void setPreset(byte preset)
    {
        this.preset = preset;
    }

    public float getAge()
    {
        return age;
    }

    public void setAge(float age)
    {
        this.age = age;
    }

    public float getSpanRatio() {
        return spanRatio;
    }

    public void setSpanRatio(float spanRatio)
    {
        this.spanRatio = spanRatio;
    }

    public boolean isHueColor()
    {
        return hueColor;
    }

    public void setHueColor(boolean hueColor)
    {
        this.hueColor = hueColor;
    }

    public void setColor(int color)
    {
        this.color = color;
    }

    public float getStartHue()
    {
        return startHue;
    }

    public void setStartHue(float startHue)
    {
        this.startHue = startHue;
    }

    public float getStartSaturation()
    {
        return startSaturation;
    }

    public void setStartSaturation(float startSaturation)
    {
        this.startSaturation = startSaturation;
    }

    public float getStartBrightness()
    {
        return startBrightness;
    }

    public void setStartBrightness(float startBrightness)
    {
        this.startBrightness = startBrightness;
    }

    public float getEndHue()
    {
        return endHue;
    }

    public void setEndHue(float endHue)
    {
        this.endHue = endHue;
    }

    public float getEndSaturation()
    {
        return endSaturation;
    }

    public void setEndSaturation(float endSaturation)
    {
        this.endSaturation = endSaturation;
    }

    public float getEndBrightness()
    {
        return endBrightness;
    }

    public void setEndBrightness(float endBrightness)
    {
        this.endBrightness = endBrightness;
    }

    public boolean isShowValue()
    {
        return showValue;
    }

    public void setShowValue(boolean showValue)
    {
        this.showValue = showValue;
    }

    public float getStartValue()
    {
        return startValue;
    }

    public void setStartValue(float startValue)
    {
        this.startValue = startValue;
    }

    public float getEndValue()
    {
        return endValue;
    }

    public void setEndValue(float endValue)
    {
        this.endValue = endValue;
    }

    public float getValueDecaySpeed()
    {
        return valueDecaySpeed;
    }

    public void setValueDecaySpeed(float valueDecaySpeed)
    {
        this.valueDecaySpeed = valueDecaySpeed;
    }

    public boolean isDecayByValue()
    {
        return decayByValue;
    }

    public void setDecayByValue(boolean decayByValue)
    {
        this.decayByValue = decayByValue;
    }

    public float getValue()
    {
        return value;
    }

    public void setValue(float value)
    {
        this.value = value;
    }

    public float getStartSize()
    {
        return startSize;
    }

    public void setStartSize(float startSize)
    {
        this.startSize = startSize;
    }

    public float getEndSize()
    {
        return endSize;
    }

    public void setEndSize(float endSize)
    {
        this.endSize = endSize;
    }

    public float getStartRot()
    {
        return startRot;
    }

    public void setStartRot(float startRot)
    {
        this.startRot = startRot;
    }

    public float getEndRot()
    {
        return endRot;
    }

    public void setEndRot(float endRot)
    {
        this.endRot = endRot;
    }

    public void setPos( Vec3 pos )
    {
        this.startPos = pos;
        this.endPos = pos;
    }

    public void setPos( Vec3 startPos, Vec3 endPos )
    {
        this.startPos = startPos;
        this.endPos = endPos;
    }
}
