package harmonised.pmmo.api.events;

import harmonised.pmmo.core.XpUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class XpEvent extends PlayerEvent
{
    public String skill;
    public long amount;
    private CompoundTag context;

    public XpEvent(ServerPlayer player, String skill, long amount, CompoundTag context) {
        super(player);
        this.skill = skill;
        this.amount = amount;
        this.context = context;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }

    public int startLevel() {
        return XpUtils.getLevelFromXP(XpUtils.getPlayerXpRaw(this.getPlayer().getUUID(), skill));
    }

    public int endLevel() {
        return XpUtils.getLevelFromXP(amount + XpUtils.getPlayerXpRaw(this.getPlayer().getUUID(), skill));
    }
    
    public CompoundTag getContext() {
    	return context;
    }
}
