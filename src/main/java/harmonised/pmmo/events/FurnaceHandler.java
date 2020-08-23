package harmonised.pmmo.events;

import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.network.MessageDoubleTranslation;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.Map;
import java.util.UUID;

public class FurnaceHandler
{
    public static void handleSmelted(IRecipe<?> recipe, NonNullList<ItemStack> items, World world )
    {
        ItemStack input = items.get(0);

        if( input.getTag() != null && input.getTag().hasUniqueId( "lastOwner" ) )
        {
            UUID uuid = input.getTag().getUniqueId( "lastOwner" );
            PlayerEntity player = world.getServer().getPlayerList().getPlayerByUUID( uuid );
            double extraChance = XP.getExtraChance( player, input.getItem().getRegistryName(), JType.INFO_SMELT ) / 100D;

            int guaranteedDrop = (int) extraChance;
            int extraDrop;

            if( XP.rollChance( extraChance % 1 ) )
                extraDrop = 1;
            else
                extraDrop = 0;

            int totalExtraDrops = guaranteedDrop + extraDrop;

            ItemStack output = items.get(2);
            output.grow( totalExtraDrops );

            XP.awardXpMapDouble( player, XP.multiplyMap( XP.getXp( input.getItem().getRegistryName(), JType.XP_VALUE_SMELT ), 1 + totalExtraDrops ), "Smelting", true, false );
//            if( totalExtraDrops > 0 )
//                NetworkHandler.sendToPlayer( new MessageDoubleTranslation( "pmmo.smeltExtraDrop", "" + totalExtraDrops, output.getTranslationKey(), true, 1 ), (ServerPlayerEntity) player );
        }
    }
}
