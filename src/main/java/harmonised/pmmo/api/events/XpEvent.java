package harmonised.pmmo.api.events;

import harmonised.pmmo.core.XpUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class XpEvent extends PlayerEvent
{
    public String skill;
    public long amountAwarded; 
    private long currentSkillXp;
    private CompoundTag context;

    public XpEvent(ServerPlayer player, String skill, long currentSkillXp, long amountAwarded, CompoundTag context) {
        super(player);
        this.skill = skill;
        this.currentSkillXp = currentSkillXp;
        this.amountAwarded = amountAwarded;
        this.context = context;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }

    public int startLevel() {
        return XpUtils.getLevelFromXP(currentSkillXp);
    }

    public int endLevel() {
        return XpUtils.getLevelFromXP(amountAwarded + currentSkillXp);
    }
    
    public boolean isLevelUp() {
    	return startLevel() < endLevel();
    }
    
    public CompoundTag getContext() {
    	return context;
    }
}
