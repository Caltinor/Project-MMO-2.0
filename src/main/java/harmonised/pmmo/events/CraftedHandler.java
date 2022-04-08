package harmonised.pmmo.events;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.gui.WorldXpDrop;
import harmonised.pmmo.util.XP;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.apache.logging.log4j.*;

import java.util.HashMap;
import java.util.Map;

public class CraftedHandler
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static void handleCrafted(PlayerEvent.ItemCraftedEvent event)
    {
    	if (event.getPlayer().level.isClientSide) return;
        try
        {
            Player player = event.getPlayer();
            ItemStack itemStack = event.getCrafting();
            
            if (!XP.checkReq(player, itemStack.getItem().getRegistryName(), JType.REQ_CRAFT))
            		return;
            
            Vec3 pos = player.position();
            double defaultCraftingXp = Config.forgeConfig.defaultCraftingXp.get();
            double durabilityMultiplier = 1;

            Map<String, Double> xpValue = XP.getXp(itemStack, JType.XP_VALUE_CRAFT);

            Map<String, Double> award = new HashMap<>();
            if(xpValue.size() == 0)
            {
                if(itemStack.getItem() instanceof BlockItem)
                    award.put("crafting", (double) ((BlockItem) itemStack.getItem()).getBlock().defaultBlockState().getDestroySpeed(null, null));
                else
                    award.put("crafting", defaultCraftingXp);
            }
            else
                XP.addMapsAnyDouble(award, xpValue);

            if(itemStack.isDamageableItem())
                durabilityMultiplier = (double) (itemStack.getMaxDamage() - itemStack.getDamageValue()) / (double) itemStack.getMaxDamage();

//            XP.multiplyMap(award, itemStack.getCount());
            XP.multiplyMapAnyDouble(award, durabilityMultiplier);

            for(String awardSkillName : award.keySet())
            {
            	if (player instanceof ServerPlayer) {
            		WorldXpDrop xpDrop = WorldXpDrop.fromXYZ(XP.getDimResLoc(player.getLevel()), pos.x(), pos.y() + player.getEyeHeight() + 0.523, pos.z(), 1.523, award.get(awardSkillName), awardSkillName);
                	XP.addWorldXpDrop(xpDrop, (ServerPlayer) player);
            	}
                APIUtils.addXp(awardSkillName, player.getUUID(), award.get(awardSkillName), "crafting", false, false);
            }
        }
        catch(Exception e)
        {
            LOGGER.error("PMMO error while crafting", e);
        }
    }
}