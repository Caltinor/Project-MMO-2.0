package harmonised.pmmo.commands;

import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.biome.Biome;

import java.util.Map;

public class CheckBiomeCommand
{
    public static int execute(CommandContext<CommandSourceStack> context) throws CommandRuntimeException
    {
        Player sender = (Player) context.getSource().getEntity();
        Biome biome = sender.level.getBiome(new BlockPos(sender.position())).value();
        ResourceLocation biomeResLoc = XP.getBiomeResLoc(sender.level, biome);
        String biomeKey = biomeResLoc.toString();
        String transKey = Util.makeDescriptionId("biome", biomeResLoc);
        Map<String, Double> theMap = JsonConfig.data.get(JType.BIOME_MOB_MULTIPLIER).get(biomeKey);

        String damageBonus = "100";
        String hpBonus = "100";
        String speedBonus = "100";

        if(theMap != null)
        {
            if(theMap.containsKey("damageBonus"))
                damageBonus = DP.dp(theMap.get("damageBonus") * 100);
            if(theMap.containsKey("hpBonus"))
                hpBonus = DP.dp(theMap.get("hpBonus") * 100);
            if(theMap.containsKey("damageBonus"))
                speedBonus = DP.dp(theMap.get("damageBonus") * 100);
        }

        sender.displayClientMessage(new TranslatableComponent("pmmo.mobDamageBoost", new TranslatableComponent(damageBonus).setStyle(XP.textStyle.get("grey")), new TranslatableComponent(transKey).setStyle(XP.textStyle.get("grey"))), false);
        sender.displayClientMessage(new TranslatableComponent("pmmo.mobHpBoost", new TranslatableComponent(hpBonus).setStyle(XP.textStyle.get("grey")), new TranslatableComponent(transKey).setStyle(XP.textStyle.get("grey"))), false);
        sender.displayClientMessage(new TranslatableComponent("pmmo.mobSpeedBoost", new TranslatableComponent(speedBonus).setStyle(XP.textStyle.get("grey")), new TranslatableComponent(transKey).setStyle(XP.textStyle.get("grey"))), false);

        return 1;
    }
}
