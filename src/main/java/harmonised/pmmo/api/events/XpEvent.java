package harmonised.pmmo.api.events;

import harmonised.pmmo.storage.Experience;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class XpEvent extends PlayerEvent implements ICancellableEvent {
    public String skill;
    public long amountAwarded;
    private Experience ogXp;
    private CompoundTag context;

    public XpEvent(ServerPlayer player, String skill, Experience currentXp, long amountAwarded, CompoundTag context) {
        super(player);
        this.skill = skill;
        this.ogXp = currentXp;
        this.amountAwarded = amountAwarded;
        this.context = context;
    }

    public long startLevel() {
        return ogXp.getLevel().getLevel();
    }

    public long endLevel() {
        Experience newXp = new Experience().merge(ogXp);
        newXp.addXp(amountAwarded);
        return newXp.getLevel().getLevel();
    }
    
    public boolean isLevelUp() {
    	return startLevel() < endLevel();
    }
    public boolean isLevelDown() {return startLevel() > endLevel();}
    
    public CompoundTag getContext() {
    	return context;
    }
}
