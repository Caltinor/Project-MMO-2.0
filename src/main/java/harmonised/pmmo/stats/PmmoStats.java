//package harmonised.pmmo.stats;
//
//import harmonised.pmmo.skills.Skill;
//import harmonised.pmmo.util.Reference;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.stats.IStatFormatter;
//import net.minecraft.stats.Stats;
//import net.minecraft.util.ResourceLocation;
//import net.minecraft.util.registry.Registry;
//
//public class PmmoStats
//{
//    public static void registerStats()
//    {
//        registerStat( new ResourceLocation( Reference.MOD_ID, "treasure_excavated" ) );
//        registerStat( new ResourceLocation( Reference.MOD_ID, "items_salvaged" ) );
//        registerStat( new ResourceLocation( Reference.MOD_ID, "fish_pool_rewards" ) );
//        registerStat( new ResourceLocation( Reference.MOD_ID, "fall_damage_saved" ) );
//        registerStat( new ResourceLocation( Reference.MOD_ID, "extra_mined" ) );
//        registerStat( new ResourceLocation( Reference.MOD_ID, "extra_chopped" ) );
//        registerStat( new ResourceLocation( Reference.MOD_ID, "extra_harvested" ) );
//        registerStat( new ResourceLocation( Reference.MOD_ID, "extra_smelted" ) );
//        registerStat( new ResourceLocation( Reference.MOD_ID, "extra_cooked" ) );
//        registerStat( new ResourceLocation( Reference.MOD_ID, "extra_brewed" ) );
//        registerStat( new ResourceLocation( Reference.MOD_ID, "blocks_veined" ) );
//        registerStat( new ResourceLocation( Reference.MOD_ID, "rare_drops" ) );
//    }
//
//    public static void registerStat( ResourceLocation resLoc )
//    {
//        Registry.register( Registry.CUSTOM_STAT, resLoc.getPath(), resLoc );
//        Stats.CUSTOM.get( resLoc, IStatFormatter.DEFAULT );
//    }
//}
