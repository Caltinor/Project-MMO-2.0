package harmonised.pmmo.ct;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.impl.data.MapData;
import com.blamejared.crafttweaker.impl.entity.player.MCPlayerEntity;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.PlayerEntity;
import org.openzen.zencode.java.ZenCodeType;

@ZenCodeType.Name("mods.pmmo.ct.Levels")
@ZenRegister
public class Levels
{
    @ZenCodeType.Method("checkLevels")
    public static boolean checkLevels( MapData args, MCPlayerEntity player )
    {
        System.out.println(XP.getSkillsTag( player.getInternal()) );

        return true;
    }
}
