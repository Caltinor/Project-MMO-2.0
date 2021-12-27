package harmonised.pmmo.events;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.api.perks.PerkRegistry;
import harmonised.pmmo.api.perks.PerkTrigger;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.gui.ScreenshotHandler;
import harmonised.pmmo.gui.WorldXpDrop;
import harmonised.pmmo.gui.XPOverlayGUI;
import harmonised.pmmo.party.PartyPendingSystem;
import harmonised.pmmo.proxy.ClientHandler;
import harmonised.pmmo.skills.CheeseTracker;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class PlayerTickHandler
{
    private final static Map<UUID, Long> lastAward = new HashMap<>();
    private final static Map<UUID, Long> lastVeinAward = new HashMap<>();
    private final static Map<UUID, Long> lastCheeseUpdate = new HashMap<>();
    private final static Map<UUID, Long> hpRegen = new HashMap<>();
    private final static Map<UUID, Long> sync = new HashMap<>();
    private final static Map<UUID, Integer> sneakCounter = new HashMap<>();
    private final static Map<UUID, Boolean> sneakTracker = new HashMap<>();
    public static boolean syncPrefs = false;

    public static void handlePlayerTick(TickEvent.PlayerTickEvent event)
    {
        if(!event.player.level.isClientSide && XP.isPlayerSurvival(event.player) && event.player.isAlive())
        {
        	ServerPlayer player = (ServerPlayer) event.player;
            UUID uuid = player.getUUID();

            if(player.isSprinting()) {
            	for (Map.Entry<String, Integer> skill : Skill.getSkills().entrySet()) {
            		int skillLevel = APIUtils.getLevel(skill.getKey(), player);
            		PerkRegistry.executePerk(PerkTrigger.SPRINTING, (ServerPlayer)player, skillLevel);
            	}
            }
            else {
            	for (Map.Entry<String, Integer> skill : Skill.getSkills().entrySet()) {
            		int skillLevel = APIUtils.getLevel(skill.getKey(), player);
            		PerkRegistry.terminatePerk(PerkTrigger.SPRINTING, (ServerPlayer)player, skillLevel);
            	}
            }

            if(!player.level.isClientSide && player.level.getServer().getTickCount() % 200 == 200)
            {
            	WorldTickHandler.updateVein(player, 0);
            }

            if(!lastAward.containsKey(uuid))
                lastAward.put(uuid, System.nanoTime());

            if(!lastVeinAward.containsKey(uuid))
                lastVeinAward.put(uuid, System.nanoTime());
            if(!lastCheeseUpdate.containsKey(uuid))
                lastCheeseUpdate.put(uuid, System.nanoTime());
            if(!hpRegen.containsKey(uuid))
                hpRegen.put(uuid, System.nanoTime());
            if(!sync.containsKey(uuid))
                sync.put(uuid, System.nanoTime());

            //Sneak
            if(!sneakCounter.containsKey(uuid))
            {
                sneakTracker.put(uuid, player.isShiftKeyDown());
                sneakCounter.put(uuid, sneakTracker.get(uuid) ? 1 : 0);
            }
            int sneakCount = sneakCounter.get(uuid);
            if(player.isShiftKeyDown() && !sneakTracker.get(uuid))
            {
                sneakCount++;
                if(!player.level.isClientSide)
                {
                    double roll = Math.random();
                    double chance = 0.01 * (sneakCount - 250) / 5;
                    if(sneakCount > 250 && roll < chance)
                    {
                        player.hurt(DamageSource.WITHER, (float) Math.max(1, chance*2.5));
                        System.out.println(chance);
                        if(player.getHealth() <= 0)
                            sneakCount = 0;
                    }
                    if(sneakCount > 50)
                    {
                        double award = (sneakCount - 50) / 200D;
                        if(award > 0)
                            XP.awardXp(player, Skill.ENDURANCE.toString(), "twerking", award, true, false, false);
                    }
                }
            }
            sneakCounter.put(uuid, sneakCount);
            sneakTracker.put(uuid, player.isShiftKeyDown());
            //End of sneak

            double veinGap      = ((System.nanoTime() - lastVeinAward.get     (uuid)) / 1000000000D);
            double cheeseGap    = ((System.nanoTime() - lastCheeseUpdate.get  (uuid)) / 1000000000D);
            double hpRegenGap   = ((System.nanoTime() - hpRegen.get           (uuid)) / 1000000000D);
            double syncGap      = ((System.nanoTime() - sync.get              (uuid)) / 1000000000D);

            if(veinGap > 0.25)
            {
                sneakCounter.put(uuid, Math.max(0, sneakCount - 1));
                WorldTickHandler.updateVein(player, veinGap);
                lastVeinAward.put(uuid, System.nanoTime());

                if(Config.forgeConfig.antiCheeseEnabled.get() && cheeseGap > Config.forgeConfig.cheeseCheckFrequency.get())
//                if(Config.forgeConfig.antiCheeseEnabled.get() && cheeseGap > 0.1)
                {
                    CheeseTracker.trackCheese(player);
                    lastCheeseUpdate.put(uuid, System.nanoTime());
                }

                if(hpRegenGap > getHpRegenTime(player))
                {
                    float startHp = player.getHealth();
                    player.heal(1f);
                    XP.awardXp(player, Skill.ENDURANCE.toString(), "Regeneration", (60 / getHpRegenTime(player)) * Config.forgeConfig.hpRegenXpMultiplier.get() * (player.getHealth() - startHp), true, false, false);
                    hpRegen.put(uuid, System.nanoTime());
                }
            }

            if(syncGap > 2.5)
            {
                PartyPendingSystem.sendPlayerOfflineData((ServerPlayer) player);

                sync.put(uuid, System.nanoTime());
            }

            double gap          = ((System.nanoTime() - lastAward.get         (uuid)) / 1000000000D);

            if(gap > 0.5)
            {
                int swimLevel = APIUtils.getLevel(Skill.SWIMMING.toString(), player);
                int flyLevel = APIUtils.getLevel(Skill.FLYING.toString(), player);
                int agilityLevel = APIUtils.getLevel(Skill.AGILITY.toString(), player);
                float swimAmp = EnchantmentHelper.getDepthStrider(player);
                float speedAmp = 0;
                Inventory inv = player.getInventory();

                XP.checkBiomeLevelReq(player);

                /*if(Curios.isLoaded())
                {
                    Curios.getCurios(player).forEach(value ->
                    {
                        for (int i = 0; i < value.getSlots(); i++)
                        {
                            XP.applyWornPenalty(player, value.getStacks().getStackInSlot(i));
                        }
                    });
                }*/

                if(!inv.getItem(39).isEmpty())	//Helm
                    XP.applyWornPenalty(player, player.getInventory().getItem(39));
                if(!inv.getItem(38).isEmpty())	//Chest
                    XP.applyWornPenalty(player, player.getInventory().getItem(38));
                if(!inv.getItem(37).isEmpty())	//Legs
                    XP.applyWornPenalty(player, player.getInventory().getItem(37));
                if(!inv.getItem(36).isEmpty())	//Boots
                    XP.applyWornPenalty(player, player.getInventory().getItem(36));
                if(!player.getMainHandItem().isEmpty())
                    XP.applyEnchantmentUsePenalty(player, player.getMainHandItem());
                if(!player.getOffhandItem().isEmpty())
                {
                    XP.applyWornPenalty(player, player.getOffhandItem());
                    XP.applyEnchantmentUsePenalty(player, player.getOffhandItem());
                }
                
////////////////////////////////////////////XP_STUFF//////////////////////////////////////////

                if(player.hasEffect(MobEffects.MOVEMENT_SPEED))
                    speedAmp = player.getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier() + 1;

                double swimAward = (3D + swimLevel    / 10.00D) * gap * (1D + swimAmp / 4D);
                double flyAward  = (1D + flyLevel     / 30.77D) * gap ;
                double runAward  = (1D + agilityLevel / 30.77D) * gap * (1D + speedAmp / 4D);

                if(!player.hurtMarked)
                    swimAward *= 0.1d;

                lastAward.replace(uuid, System.nanoTime());
                Block waterBlock = Blocks.WATER;
                Block tallSeagrassBlock = Blocks.TALL_SEAGRASS;
                Block kelpBlock = Blocks.KELP_PLANT;
                BlockPos playerPos = XP.vecToBlock(player.position());
                Block currBlock;
                boolean waterBelow = true;

                for(int i = -1; i <= 1; i++)
                {
                    for(int j = -1; j <= 1; j++)
                    {
                        currBlock = player.getCommandSenderWorld().getBlockState(playerPos.below().east(i).north(j)).getBlock();
                        if(!(currBlock.equals(waterBlock) || currBlock.equals(tallSeagrassBlock) || currBlock.equals(kelpBlock)))
                            waterBelow = false;
                    }
                }
                boolean waterAbove = player.getCommandSenderWorld().getBlockState(playerPos.above()  ).getBlock().equals(waterBlock);
                boolean nightVisionPref = Config.getPreferencesMap(player).getOrDefault("underwaterNightVision", 1D) == 1;

                if(nightVisionPref && XP.isNightvisionUnlocked(player) && XP.isNightvisionUnlocked(player) && player.isInWater() && waterAbove)
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, false, false));

                ServerLevel world = player.getLevel();
                Vec3 xpDropPos = player.position();
                if(player.isSprinting())
                {
                    if(player.isInWater() && (waterAbove || waterBelow))
                    {
                        WorldXpDrop xpDrop = WorldXpDrop.fromXYZ(XP.getDimResLoc(world), xpDropPos.x(), xpDropPos.y(), xpDropPos.z(), 0.35, swimAward, Skill.SWIMMING.toString());
                        XP.addWorldXpDrop(xpDrop, player);
                        XP.awardXp(player, Skill.SWIMMING.toString(), "swimming fast", swimAward * 1.25f, true, false, false);
                    }
                    else
                    {
                        WorldXpDrop xpDrop = WorldXpDrop.fromXYZ(XP.getDimResLoc(world), xpDropPos.x(), xpDropPos.y() + 0.523, xpDropPos.z(), 0.15, runAward, Skill.AGILITY.toString());
                        XP.addWorldXpDrop(xpDrop, player);
                        XP.awardXp(player, Skill.AGILITY.toString(), "running", runAward, true, false, false);
                    }
                }

                if(player.isInWater() && (waterAbove || waterBelow || player.isEyeInFluid(FluidTags.WATER)))
                {
                    if(!player.isSprinting())
                    {
                        WorldXpDrop xpDrop = WorldXpDrop.fromXYZ(XP.getDimResLoc(world), xpDropPos.x(), xpDropPos.y(), xpDropPos.z(), 0.35, swimAward, Skill.SWIMMING.toString());
                        XP.addWorldXpDrop(xpDrop, player);
                        XP.awardXp(player, Skill.SWIMMING.toString(), "swimming", swimAward, true, false, false);
                    }
                }
                else if(player.isFallFlying())
                {
                    WorldXpDrop xpDrop = WorldXpDrop.fromXYZ(XP.getDimResLoc(world), xpDropPos.x(), xpDropPos.y(), xpDropPos.z(), 0.35, flyAward, Skill.FLYING.toString());
                    XP.addWorldXpDrop(xpDrop, player);
                    XP.awardXp(player, Skill.FLYING.toString(), "flying", flyAward, true, false, false);
                }

                if((player.getVehicle() instanceof Boat) && waterBelow)
                {
                    if(!player.isSprinting())
                        swimAward *= 1.5;

                    WorldXpDrop xpDrop = WorldXpDrop.fromXYZ(XP.getDimResLoc(world), xpDropPos.x(), xpDropPos.y(), xpDropPos.z(), 0.35, swimAward, Skill.SWIMMING.toString());
                    XP.addWorldXpDrop(xpDrop, player);
                    XP.awardXp(player, Skill.SAILING.toString(), "Sailing", swimAward, true, false, false);
                }
               
////////////////////////////////////////////ABILITIES//////////////////////////////////////////
            }
        }

        if(XPOverlayGUI.screenshots.size() > 0)
        {
            for(String key : new HashSet<>(XPOverlayGUI.screenshots))
            {
                ScreenshotHandler.takeScreenshot(key, "levelup");
                XPOverlayGUI.screenshots.remove(key);
                XPOverlayGUI.listOn = XPOverlayGUI.listWasOn;
            }
        }

        if(syncPrefs)
        {
            ClientHandler.syncPrefsToServer();
            syncPrefs = false;
        }
    }

    public static double getHpRegenTime(Player player)
    {
        double dividend = Config.getConfig("hpRegenPerMinuteBase") + APIUtils.getLevel(Skill.ENDURANCE.toString(), player) * Config.getConfig("hpRegenPerMinuteBoostPerLevel");
        return dividend <= 0 ? Double.POSITIVE_INFINITY : 60 / dividend;
    }
}