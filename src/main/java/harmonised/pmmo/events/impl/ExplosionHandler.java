package harmonised.pmmo.events.impl;

import harmonised.pmmo.api.enums.ReqType;
import harmonised.pmmo.core.Core;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.world.ExplosionEvent;

public class ExplosionHandler {
    public static void handle(ExplosionEvent.Detonate event) {
        if (event.getExplosion().getDamageSource().getEntity() instanceof Player player) {
            Level level = event.getWorld();
            Core core = Core.get(level);
            event.getAffectedBlocks().removeIf(pos -> !core.isActionPermitted(ReqType.BREAK, pos, player));
            event.getAffectedEntities().removeIf(entity -> !core.isActionPermitted(ReqType.KILL, entity, player));
        }
    }
}
