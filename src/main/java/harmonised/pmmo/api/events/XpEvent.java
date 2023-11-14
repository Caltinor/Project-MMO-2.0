package harmonised.pmmo.api.events;

import harmonised.pmmo.core.Core;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class XpEvent extends PlayerEvent implements ICancellableEvent {
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

    public int startLevel() {
        return Core.get(this.getEntity().level()).getData().getLevelFromXP(currentSkillXp);
    }

    public int endLevel() {
        return Core.get(this.getEntity().level()).getData().getLevelFromXP(amountAwarded + currentSkillXp);
    }
    
    public boolean isLevelUp() {
    	return startLevel() < endLevel();
    }
    
    public CompoundTag getContext() {
    	return context;
    }
}
