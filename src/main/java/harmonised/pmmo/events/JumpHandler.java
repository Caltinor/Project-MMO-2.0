package harmonised.pmmo.events;

import harmonised.pmmo.api.perks.PerkRegistry;
import harmonised.pmmo.api.perks.PerkTrigger;
import harmonised.pmmo.gui.WorldXpDrop;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingEvent;

public class JumpHandler
{
    public static void handleJump(LivingEvent.LivingJumpEvent event)
    {
        if(!event.getEntityLiving().level.isClientSide 
        		&& event.getEntityLiving() instanceof Player 
        		&& !(event.getEntityLiving() instanceof FakePlayer))
        {
            ServerPlayer player = (ServerPlayer) event.getEntityLiving();

            if(XP.isPlayerSurvival(player))
            {
            	CompoundTag perkOutput = new CompoundTag();
                if (player.isCrouching())
                	perkOutput = PerkRegistry.executePerk(PerkTrigger.CROUCH_JUMP, player);
                else if (player.isSprinting())
                	perkOutput = PerkRegistry.executePerk(PerkTrigger.SPRINT_JUMP, player);
                else if (!player.isCrouching() && ! player.isSprinting()) 
                	perkOutput = PerkRegistry.executePerk(PerkTrigger.JUMPING, player);                

                if (!player.isInWater())
                {
                    Vec3 xpDropPos = player.position();
                    double award = perkOutput.contains("power") ? Math.max(1, perkOutput.getInt("power")) : 1;
                    WorldXpDrop xpDrop = WorldXpDrop.fromXYZ(XP.getDimResLoc(player.getCommandSenderWorld()), xpDropPos.x(), xpDropPos.y() + 0.523, xpDropPos.z(), 0.15, award, Skill.AGILITY.toString());
                    XP.addWorldXpDrop(xpDrop, (ServerPlayer) player);
                    XP.awardXp((ServerPlayer) player, Skill.AGILITY.toString(), "jumping", award, true, false, false);
                }
            }
        }
    }
}