package harmonised.pmmo.ct;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.data.MapData;
import com.blamejared.crafttweaker.impl.entity.player.MCPlayerEntity;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.LogHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import org.openzen.zencode.java.ZenCodeType;

@ZenCodeType.Name("mods.pmmo.ct.Levels")
@ZenRegister
public class Levels
{
    @ZenCodeType.Method("checkLevels")
    public static boolean checkLevels( MapData args, MCPlayerEntity mcPlayer )
    {
        PlayerEntity player = mcPlayer.getInternal();
        CompoundNBT reqLevels = args.getInternal();
        Skill skill;
        for( String key : reqLevels.keySet() )
        {
            skill = Skill.getSkill( key );
            if( skill.equals( Skill.INVALID_SKILL ) )
                LogHandler.LOGGER.error( "ZenScript -> PMMO -> checkLevels -> Invalid Skill Provided! \"" + key + "\"" );
            if( reqLevels.getDouble( key ) > skill.getLevelDecimal( player ) )
                return false;
        }
        return true;
    }
}
