package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.events.BlockBrokenHandler;
import harmonised.pmmo.events.FishedHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.Util;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.Reference;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class ListScreen extends Screen
{
    private final List<GuiEventListener> children = Lists.newArrayList();
    private final ResourceLocation box = XP.getResLoc(Reference.MOD_ID, "textures/gui/screenboxy.png");
    private static final Style greenColor = XP.textStyle.get("green");
    private static Button exitButton;

    Minecraft minecraft = Minecraft.getInstance();
    Window sr = minecraft.getWindow();
    Font font = minecraft.font;
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x, y, scrollX, scrollY, buttonX, buttonY, accumulativeHeight, buttonsSize, buttonsLoaded, futureHeight, minCount, maxCount;
    private ListScrollPanel scrollPanel;
    private EditBox filterTextField;
    private String filterText = "";

    private final Player player;
    private final JType jType;
    private final double baseXp = Config.getConfig("baseXp");
    private ArrayList<ListButton> listButtons = new ArrayList<>();
    private ArrayList<ListButton> activeListButtons = new ArrayList<>();
    private UUID uuid;
    private Component title;
    private String type;

    public ListScreen(UUID uuid, Component titleIn, String type, JType jType, Player player)
    {
        super(titleIn);
        this.title = titleIn;
        this.player = player;
        this.jType = jType;
        this.uuid = uuid;
        this.type = type;
    }

//    @Override
//    public boolean isPauseScreen()
//    {
//        return false;
//    }

    @Override
    public void init()
    {
        ArrayList<String> keyWords = new ArrayList<>();
        keyWords.add("helmet");
        keyWords.add("chestplate");
        keyWords.add("leggings");
        keyWords.add("boots");
        keyWords.add("pickaxe");
        keyWords.add("axe");
        keyWords.add("shovel");
        keyWords.add("hoe");
        keyWords.add("sword");

        x = (sr.getGuiScaledWidth() / 2) - (boxWidth / 2);
        y = (sr.getGuiScaledHeight() / 2) - (boxHeight / 2);
        scrollX = x + 16;
        scrollY = y + 10;
        buttonX = scrollX + 4;
        String skill;

        exitButton = new TileButton(x + boxWidth - 24, y - 8, 7, 0, "", JType.NONE, (button) ->
        {
            switch(jType)
            {
                case SKILLS:
                case STATS:
                case HISCORE:
                    Minecraft.getInstance().setScreen(new MainScreen(uuid, getTransComp("pmmo.potato")));
                    break;

                default:
                    Minecraft.getInstance().setScreen(new GlossaryScreen(uuid, getTransComp("pmmo.skills"), false));
                    break;
            }
        });

        Map<String, Map<String, Double>> reqMap = JsonConfig.data.get(jType);
        Map<String, Map<String, Map<String, Double>>> reqMap2 = JsonConfig.data2.get(jType);

        ArrayList<ListButton> tempList = new ArrayList<>();
        listButtons = new ArrayList<>();

        switch(jType)      //How it's made: Buttons!
        {
            case REQ_BIOME:
            {
                Map<String, Map<String, Double>> bonusMap = JsonConfig.data.get(JType.XP_BONUS_BIOME);
                Map<String, Map<String, Double>> scaleMap = JsonConfig.data.get(JType.BIOME_MOB_MULTIPLIER);
                List<String> biomesToAdd = new ArrayList<>();

                if(reqMap != null)
                {
                    for(Map.Entry<String, Map<String, Double>> entry : reqMap.entrySet())
                    {
                        if(!biomesToAdd.contains(entry.getKey()))
                            biomesToAdd.add(entry.getKey());
                    }
                }

                if(bonusMap != null)
                {
                    for(Map.Entry<String, Map<String, Double>> entry : bonusMap.entrySet())
                    {
                        if(!biomesToAdd.contains(entry.getKey()))
                            biomesToAdd.add(entry.getKey());
                    }
                }

                if(scaleMap != null)
                {
                    for(Map.Entry<String, Map<String, Double>> entry : scaleMap.entrySet())
                    {
                        if(!biomesToAdd.contains(entry.getKey()))
                            biomesToAdd.add(entry.getKey());
                    }
                }

                biomesToAdd.sort(Comparator.comparingInt(b -> getReqCount(b, JType.REQ_BIOME)));

                for(String regKey : biomesToAdd)
                {
                    if (ForgeRegistries.BIOMES.getValue(XP.getResLoc(regKey)) != null)
                    {
                        tempList.add(new ListButton(0, 0, 3, 8, regKey, jType, "", button -> ((ListButton) button).clickActionGlossary()));
                    }
                }
            }
                break;

            case DIMENSION:
            {
                Map<String, Map<String, Double>> veinBlacklist = JsonConfig.data.get(JType.VEIN_BLACKLIST);
                if(veinBlacklist == null)
                    break;

                if(veinBlacklist.containsKey("all_dimensions"))
                {
                    tempList.add(new ListButton(0, 0, 3, 8, "all_dimensions", jType, "", button -> ((ListButton) button).clickActionGlossary()));
                }


                if(veinBlacklist.containsKey("minecraft:overworld"))
                {
                    tempList.add(new ListButton(0, 0, 3, 8, "minecraft:overworld", jType, "", button -> ((ListButton) button).clickActionGlossary()));
                }

                if(veinBlacklist.containsKey("minecraft:the_nether"))
                {
                    tempList.add(new ListButton(0, 0, 3, 8, "minecraft:the_nether", jType, "", button -> ((ListButton) button).clickActionGlossary()));
                }

                if(veinBlacklist.containsKey("minecraft:the_end"))
                {
                    tempList.add(new ListButton(0, 0, 3, 8, "minecraft:the_end", jType, "", button -> ((ListButton) button).clickActionGlossary()));
                }

//                for(Map.Entry<String, Map<String, Double>> entry : veinBlacklist.entrySet())
//                {
//                    if (ForgeRegistries.MOD_DIMENSIONS.getValue(XP.getResLoc(entry.getKey())) != null)
//                    {
//                        tempList.add(new ListButton(0, 0, 3, 8, entry.getKey(), jType, "", button -> ((ListButton) button).clickAction()));
//                    }
//                }
                //COUT
            }
                break;

            case REQ_KILL:
            {
                Map<String, Map<String, Double>> killXpMap = JsonConfig.data.get(JType.XP_VALUE_KILL);
                Map<String, Map<String, Double>> rareDropMap = JsonConfig.data.get(JType.MOB_RARE_DROP);
                ArrayList<String> mobsToAdd = new ArrayList<>();

                if(reqMap != null)
                {
                    for(Map.Entry<String, Map<String, Double>> entry : reqMap.entrySet())
                    {
                        if(!mobsToAdd.contains(entry.getKey()))
                            mobsToAdd.add(entry.getKey());
                    }
                }

                if(killXpMap != null)
                {
                    for(Map.Entry<String, Map<String, Double>> entry : killXpMap.entrySet())
                    {
                        if(!mobsToAdd.contains(entry.getKey()))
                            mobsToAdd.add(entry.getKey());
                    }
                }

                if(rareDropMap != null)
                {
                    for(Map.Entry<String, Map<String, Double>> entry : rareDropMap.entrySet())
                    {
                        if(!mobsToAdd.contains(entry.getKey()))
                            mobsToAdd.add(entry.getKey());
                    }
                }

                for(String regKey : mobsToAdd)
                {
                    if(ForgeRegistries.ENTITIES.containsKey(XP.getResLoc(regKey)))
                    {
                        tempList.add(new ListButton(0, 0, 3, 0, regKey, jType, "", button -> ((ListButton) button).clickActionGlossary()));
                    }
                }
            }
                break;

            case XP_VALUE_BREED:
            case XP_VALUE_TAME:
            {
                if(reqMap == null)
                    break;

                for(Map.Entry<String, Map<String, Double>> entry : reqMap.entrySet())
                {
                    if(ForgeRegistries.ENTITIES.containsKey(XP.getResLoc(entry.getKey())))
                    {
                        tempList.add(new ListButton(0, 0, 3, 0, entry.getKey(), jType, "", button -> ((ListButton) button).clickActionGlossary()));
                    }
                }
            }
                break;

            case FISH_ENCHANT_POOL:
            {
                if(reqMap == null)
                    break;

                for(Map.Entry<String, Map<String, Double>> entry : reqMap.entrySet())
                {
                    if(ForgeRegistries.ENCHANTMENTS.containsKey(XP.getResLoc(entry.getKey())))
                    {
                        tempList.add(new ListButton(0, 0, 3, 25, entry.getKey(), jType, "", button -> ((ListButton) button).clickActionGlossary()));
                    }
                }
                break;
            }

            case SKILLS:
            {
                Set<String> skills = XP.getOfflineXpMap(uuid).keySet();
                List<ListButton> buttonsToAdd = new ArrayList<>();
                tempList.add(new ListButton(0, 0, 3, 6, "totalLevel", jType, "", button -> ((ListButton) button).clickActionSkills()));
                for(String theSkill : skills)
                {
                    buttonsToAdd.add(new ListButton(0, 0, 3, 6, theSkill, jType, "", button -> ((ListButton) button).clickActionSkills()));
                }
                buttonsToAdd.sort(Comparator.comparingDouble(b -> XP.getOfflineXp(((ListButton) b).regKey, uuid)).reversed());
                tempList.addAll(buttonsToAdd);
            }
            break;

            case HISCORE:
            {
                String theSkill = type;
                List<ListButton> buttonsToAdd = new ArrayList<>();

                for(Map.Entry<UUID, String> entry : XP.playerNames.entrySet())
                {
                    buttonsToAdd.add(new ListButton(0, 0, 3, 6, entry.getValue(), jType, "", button -> ((ListButton) button).clickActionSkills()));
                }
                if(type.equals("totalLevel"))
                    buttonsToAdd.sort(Comparator.comparingDouble(b -> XP.getTotalLevelFromMap(XP.getOfflineXpMap(XP.playerUUIDs.get(((ListButton) b).regKey)))).reversed());
                else
                    buttonsToAdd.sort(Comparator.comparingDouble(b -> XP.getOfflineXp(theSkill, XP.playerUUIDs.get(((ListButton) b).regKey))).reversed());
                tempList.addAll(buttonsToAdd);
            }
            break;

            case SALVAGE:
            case SALVAGE_FROM:
            case TREASURE:
            case TREASURE_FROM:
            {
                if(reqMap2 == null)
                    break;
                for(Map.Entry<String, Map<String, Map<String, Double>>> salvageFromItemEntry : reqMap2.entrySet())
                {
                    tempList.add(new ListButton(0, 0, 3, 0, salvageFromItemEntry.getKey(), jType, "", button -> ((ListButton) button).clickActionGlossary()));
                }
            }
                break;

            default:
            {

                if(reqMap == null)
                    return;

                for(Map.Entry<String, Map<String, Double>> entry : reqMap.entrySet())
                {
                    if(XP.getItem(entry.getKey()) != Items.AIR)
                    {
                        tempList.add(new ListButton(0, 0, 3, 0, entry.getKey(), jType, "", button -> ((ListButton) button).clickActionGlossary()));
                    }
                }
            }
                break;
        }

        for(String keyWord : keyWords)
        {
            for(ListButton button : tempList)
            {
                if(button.regKey.contains(keyWord))
                {
                    if(!listButtons.contains(button))
                        listButtons.add(button);
                }
            }
        }

        for(ListButton button : tempList)
        {
            if(!listButtons.contains(button))
                listButtons.add(button);
        }

        for(ListButton button : tempList)
        {
            if(JsonConfig.levelJTypes.contains(jType) && !Util.mapIsAnyAbove1String(reqMap, button.regKey))
                listButtons.remove(button);
        }

        for(ListButton button : listButtons)
        {
            List<Component> skillText = new ArrayList<>();
            List<Component> scaleText = new ArrayList<>();
            List<Component> negativeEffectText = new ArrayList<>();
            List<Component> positiveEffectText = new ArrayList<>();

            switch (jType)   //Individual Button Handling
            {
                case DIMENSION:
                {
                    Map<String, Map<String, Double>> veinBlacklist = JsonConfig.data.get(JType.VEIN_BLACKLIST);
                    Map<String, Double> dimensionBonusMap = JsonConfig.data.get(JType.XP_BONUS_DIMENSION).get(button.regKey);

                    if(veinBlacklist != null)
                    {
                        button.text.add(new TextComponent(""));
                        button.text.add(getTransComp("pmmo.veinBlacklist").setStyle(XP.textStyle.get("red")));
                        for (Map.Entry<String, Double> entry : veinBlacklist.get(button.regKey).entrySet())
                        {
                            button.text.add(new TextComponent(" " + getTransComp(XP.getItem(entry.getKey()).getDescriptionId()).getString()).setStyle(XP.textStyle.get("red")));
                        }
                    }

                    if (dimensionBonusMap != null)
                    {
                        for (Map.Entry<String, Double> entry : dimensionBonusMap.entrySet())
                        {
                            if (entry.getValue() > 0)
                                skillText.add(new TextComponent(" " + getTransComp("pmmo.levelDisplay", getTransComp("pmmo." + entry.getKey()), "+" + entry.getValue() + "%").getString()).setStyle(Skill.getSkillStyle(entry.getKey())));
                            if (entry.getValue() < 0)
                                skillText.add(new TextComponent(" " + getTransComp("pmmo.levelDisplay", getTransComp("pmmo." + entry.getKey()), entry.getValue() + "%").getString()).setStyle(Skill.getSkillStyle(entry.getKey())));
                        }
                    }
                }
                break;

                case REQ_BIOME:
                {
                    button.text.add(new TextComponent(""));

                    if (reqMap.containsKey(button.regKey))
                        addLevelsToButton(button, reqMap.get(button.regKey), player, false);

                    Map<String, Double> biomeBonusMap = JsonConfig.data.get(JType.XP_BONUS_BIOME).get(button.regKey);
                    Map<String, Double> biomeMobMultiplierMap = JsonConfig.data.get(JType.BIOME_MOB_MULTIPLIER).get(button.regKey);
                    Map<String, Double> biomeNegativeEffectsMap = JsonConfig.data.get(JType.BIOME_EFFECT_NEGATIVE).get(button.regKey);
                    Map<String, Double> biomePositiveEffectsMap = JsonConfig.data.get(JType.BIOME_EFFECT_POSITIVE).get(button.regKey);

                    if (biomeBonusMap != null)
                    {
                        for (Map.Entry<String, Double> entry : biomeBonusMap.entrySet())
                        {
                            if (entry.getValue() > 0)
                                skillText.add(new TextComponent(" " + getTransComp("pmmo.levelDisplay", getTransComp("pmmo." + entry.getKey()), "+" + entry.getValue() + "%").getString()).setStyle(Skill.getSkillStyle(entry.getKey())));
                            if (entry.getValue() < 0)
                                skillText.add(new TextComponent(" " + getTransComp("pmmo.levelDisplay", getTransComp("pmmo." + entry.getKey()), entry.getValue() + "%").getString()).setStyle(Skill.getSkillStyle(entry.getKey())));
                        }
                    }

                    if (biomeMobMultiplierMap != null)
                    {
                        for (Map.Entry<String, Double> entry : biomeMobMultiplierMap.entrySet())
                        {
                            Style styleColor = Style.EMPTY;

                            if (entry.getValue() > 1)
                                styleColor = XP.textStyle.get("red");
                            else if (entry.getValue() < 1)
                                styleColor = XP.textStyle.get("green");

                            switch (entry.getKey())
                            {
                                case "damageBonus":
                                    scaleText.add(new TextComponent(" " + getTransComp("pmmo.enemyScaleDamage", DP.dp(entry.getValue() * 100)).getString()).setStyle(styleColor));
                                    break;

                                case "hpBonus":
                                    scaleText.add(new TextComponent(" " + getTransComp("pmmo.enemyScaleHp", DP.dp(entry.getValue() * 100)).getString()).setStyle(styleColor));
                                    break;

                                case "speedBonus":
                                    scaleText.add(new TextComponent(" " + getTransComp("pmmo.enemyScaleSpeed", DP.dp(entry.getValue() * 100)).getString()).setStyle(styleColor));
                                    break;
                            }
                        }
                    }

                    if (biomeNegativeEffectsMap != null)
                    {
                        for (Map.Entry<String, Double> entry : biomeNegativeEffectsMap.entrySet())
                        {
                            if (ForgeRegistries.POTIONS.containsKey(XP.getResLoc(entry.getKey())))
                            {
                                Potion effect = ForgeRegistries.POTIONS.getValue(XP.getResLoc(entry.getKey()));
                                if (effect != null)
                                    negativeEffectText.add(new TextComponent(" " + getTransComp(effect.getRegistryName().toString() + " " + (int) (entry.getValue() + 1)).getString()).setStyle(XP.textStyle.get("red")));
                            }
                        }
                    }

                    if (biomePositiveEffectsMap != null)
                    {
                        for (Map.Entry<String, Double> entry : biomePositiveEffectsMap.entrySet())
                        {
                            if (ForgeRegistries.POTIONS.containsKey(XP.getResLoc(entry.getKey())))
                            {
                                Potion effect = ForgeRegistries.POTIONS.getValue(XP.getResLoc(entry.getKey()));
                                if (effect != null)
                                    positiveEffectText.add(new TextComponent(" " + getTransComp(effect.getRegistryName().toString() + " " + (int) (entry.getValue() + 1)).getString()).setStyle(XP.textStyle.get("green")));
                            }
                        }
                    }
                }
                    break;

                case INFO_ORE:
                case INFO_LOG:
                case INFO_PLANT:
                case INFO_SMELT:
                case INFO_COOK:
                case INFO_BREW:
                {
                    button.text.add(new TextComponent(""));
                    Map<String, Double> breakMap = JsonConfig.data.get(JType.REQ_BREAK).get(button.regKey);
                    Map<String, Double> infoMap = XP.getJsonMap(button.regKey, jType);
                    List<Component> infoText = new ArrayList<>();
                    String transKey = "pmmo." + jType.toString().replace("info_", "") + "ExtraDrop";
                    if(!infoMap.containsKey("extraChance"))
                        continue;
                    double extraDroppedPerLevel = infoMap.get("extraChance") / 100;
                    double extraDropped = XP.getExtraChance(player.getUUID(), button.regKey, jType, true) / 100;

                    if (extraDropped <= 0)
                        infoText.add(getTransComp(transKey, DP.dp(extraDropped)).setStyle(XP.textStyle.get("red")));
                    else
                        infoText.add(getTransComp(transKey, DP.dp(extraDropped)).setStyle(XP.textStyle.get("green")));

                    if (extraDroppedPerLevel <= 0)
                        infoText.add(getTransComp("pmmo.extraPerLevel", DP.dpCustom(extraDroppedPerLevel, 4)).setStyle(XP.textStyle.get("red")));
                    else
                        infoText.add(getTransComp("pmmo.extraPerLevel", DP.dpCustom(extraDroppedPerLevel, 4)).setStyle(XP.textStyle.get("green")));

                    if (infoText.size() > 0)
                        button.text.addAll(infoText);

                    if (breakMap != null && (jType.equals(JType.INFO_ORE) || jType.equals(JType.INFO_LOG) || jType.equals(JType.INFO_PLANT)))
                    {
                        if (XP.checkReq(player, button.regKey, JType.REQ_BREAK))
                            button.text.add(getTransComp("pmmo.break").setStyle(XP.textStyle.get("green")));
                        else
                            button.text.add(getTransComp("pmmo.break").setStyle(XP.textStyle.get("red")));
                        addLevelsToButton(button, breakMap, player, false);
                    }
                }
                    break;

                case XP_BONUS_WORN:
                {
                    button.text.add(new TextComponent(""));
                    addPercentageToButton(button, reqMap.get(button.regKey), XP.checkReq(player, button.regKey, JType.REQ_WEAR));
                }
                    break;

                case XP_BONUS_HELD:
                {
                    button.text.add(new TextComponent(""));
                    addPercentageToButton(button, reqMap.get(button.regKey), true);
                }
                    break;

                case XP_VALUE_BREED:
                case XP_VALUE_TAME:
                case XP_VALUE_SMELT:
                case XP_VALUE_COOK:
                case XP_VALUE_BREW:
                {
                    addXpToButton(button, reqMap.get(button.regKey));
                }
                    break;

                case XP_VALUE_BREAK:
                {
                    addXpToButton(button, reqMap.get(button.regKey), JType.REQ_BREAK, player);
                }
                    break;

                case XP_VALUE_CRAFT:
                {
                    addXpToButton(button, reqMap.get(button.regKey), JType.REQ_CRAFT, player);
                }
                    break;

                case XP_VALUE_GROW:
                {
                    addXpToButton(button, reqMap.get(button.regKey), JType.REQ_PLACE, player);
                }
                    break;

                case FISH_ENCHANT_POOL:
                {
                    Map<String, Double> enchantMap = reqMap.get(button.regKey);

                    double fishLevel = Skill.getLevelDecimal(Skill.FISHING.toString(), player);

                    double levelReq = enchantMap.get("levelReq");
                    double chancePerLevel = enchantMap.get("chancePerLevel");
                    double maxChance = enchantMap.get("maxChance");
                    double maxLevel = (int) (double) enchantMap.get("maxLevel");
                    double levelsPerTier = enchantMap.get("levelPerLevel");
                    double maxLevelAvailable;
                    if(levelsPerTier == 0)
                        maxLevelAvailable = maxLevel;
                    else
                        maxLevelAvailable = Math.floor((fishLevel - levelReq) / levelsPerTier);
                    if(maxLevelAvailable < 0)
                        maxLevelAvailable = 0;
                    if(maxLevelAvailable > maxLevel)
                        maxLevelAvailable = maxLevel;

                    double curChance = (fishLevel - levelReq) * chancePerLevel;
                    if(curChance > maxChance)
                        curChance = maxChance;
                    if(curChance < 0)
                        curChance = 0;

                    button.unlocked = maxLevelAvailable > 0;
                    Style color = XP.textStyle.get(button.unlocked ? "green" : "red");

                    button.text.add(new TextComponent(""));

                    button.text.add(new TextComponent(" " + getTransComp("pmmo.currentChance", DP.dpSoft(curChance)).getString()).setStyle(color));
                    button.text.add(new TextComponent(" " + getTransComp("pmmo.startLevel", DP.dpSoft(levelReq)).getString()).setStyle(color));
                    button.text.add(new TextComponent(" " + getTransComp("pmmo.maxEnchantLevel", (int) maxLevelAvailable).getString()).setStyle(color));

                    button.text.add(new TextComponent(""));
                    button.text.add(new TextComponent(" " + getTransComp("pmmo.chancePerLevel", DP.dpSoft(chancePerLevel)).getString()));
                    if(maxLevel > 1)
                        button.text.add(new TextComponent(" " + getTransComp("pmmo.levelsPerTier", DP.dpSoft(levelsPerTier)).getString()));
                    button.text.add(new TextComponent(" " + getTransComp("pmmo.maxEnchantLevel", (int) maxLevel).getString()));
                }
                    break;

                case REQ_KILL:
                {
                    Map<String, Double> killXpMap = JsonConfig.data.get(JType.XP_VALUE_KILL).get(button.regKey);
                    Map<String, Double> rareDropMap = JsonConfig.data.get(JType.MOB_RARE_DROP).get(button.regKey);
                    button.unlocked = XP.checkReq(player, button.regKey, jType);
                    Style color = XP.textStyle.get(button.unlocked ? "green" : "red");

                    if (reqMap.containsKey(button.regKey))
                    {
                        button.text.add(new TextComponent(""));
                        button.text.add(getTransComp("pmmo.toHarm").setStyle(color));
                        addLevelsToButton(button, reqMap.get(button.regKey), player, false);
                    }

                    button.text.add(new TextComponent(""));
                    button.text.add(getTransComp("pmmo.xpValue").setStyle(color));

                    if (killXpMap != null)
                        addXpToButton(button, killXpMap, jType, player);
                    else
                    {
                        if(button.entity instanceof Animal)
                            button.text.add(new TextComponent(" " + getTransComp("pmmo.xpDisplay", getTransComp("pmmo.hunter"), DP.dpSoft(Config.forgeConfig.passiveMobHunterXp.get())).getString()).setStyle(color));
                        else if(button.entity instanceof Mob)
                            button.text.add(new TextComponent(" " + getTransComp("pmmo.xpDisplay", getTransComp("pmmo.slayer"), DP.dpSoft(Config.forgeConfig.aggresiveMobSlayerXp.get())).getString()).setStyle(color));
                    }

                    if (rareDropMap != null)
                    {
                        button.text.add(new TextComponent(""));
                        button.text.add(getTransComp("pmmo.rareDrops").setStyle(color));
                        for(Map.Entry<String, Double> entry : rareDropMap.entrySet())
                        {
                            button.text.add(new TextComponent(getTransComp(XP.getItem(entry.getKey()).getDescriptionId()).getString() + ": " + getTransComp("pmmo.dropChance", DP.dpSoft(entry.getValue())).getString()).setStyle(color));
                        }
                    }
                }
                    break;

                case FISH_POOL:
                {
                    Map<String, Double> fishPoolMap = reqMap.get(button.regKey);

                    double level = Skill.getLevelDecimal(Skill.FISHING.toString(), player);
                    double weight = FishedHandler.getFishPoolWeight(level, fishPoolMap);
                    double chance = weight / FishedHandler.getTotalFishPoolWeight(level);
                    button.unlocked = weight > 0;
                    Style color = XP.textStyle.get(button.unlocked ? "green" : "red");

                    minCount = (int) (double) fishPoolMap.get("minCount");
                    maxCount = (int) (double) fishPoolMap.get("maxCount");

                    button.text.add(new TextComponent(""));
                    button.text.add(new TextComponent(" " + getTransComp("pmmo.currentWeight", DP.dpSoft(weight)).getString()).setStyle(color));
                    button.text.add(new TextComponent(" " + getTransComp("pmmo.currentChance", DP.dpCustom(chance, 5)).getString()).setStyle(color));
                    button.text.add(new TextComponent(" " + getTransComp("pmmo.dropChance", DP.dpSoft(1/chance)).getString()).setStyle(color));

                    if (minCount == maxCount)
                        button.text.add(new TextComponent(" " + getTransComp("pmmo.caughtAmount", minCount).getString()).setStyle(color));
                    else
                        button.text.add(new TextComponent(" " + getTransComp("pmmo.caughtAmountRange", minCount, maxCount).getString()).setStyle(color));

                    button.text.add(new TextComponent(" " + getTransComp("pmmo.xpEach", DP.dpSoft(fishPoolMap.get("xp"))).getString()).setStyle(color));

                    if (button.itemStack.isEnchantable())
                    {
                        if(fishPoolMap.get("enchantLevelReq") <= level && button.unlocked)
                            button.text.add(new TextComponent(" " + getTransComp("pmmo.enchantLevelReq", DP.dpSoft(fishPoolMap.get("enchantLevelReq"))).getString()).setStyle(XP.textStyle.get("green")));
                        else
                            button.text.add(new TextComponent(" " + getTransComp("pmmo.enchantLevelReq", DP.dpSoft(fishPoolMap.get("enchantLevelReq"))).getString()).setStyle(XP.textStyle.get("red")));
                    }

                    button.text.add(new TextComponent(""));
//                    button.text.add(getTransComp("pmmo.info"));
                    button.text.add(new TextComponent(" " + getTransComp("pmmo.startWeight", DP.dpSoft(fishPoolMap.get("startWeight"))).getString()));
                    button.text.add(new TextComponent(" " + getTransComp("pmmo.startLevel", DP.dpSoft(fishPoolMap.get("startLevel"))).getString()));
                    button.text.add(new TextComponent(" " + getTransComp("pmmo.endWeight", DP.dpSoft(fishPoolMap.get("endWeight"))).getString()));
                    button.text.add(new TextComponent(" " + getTransComp("pmmo.endLevel", DP.dpSoft(fishPoolMap.get("endLevel"))).getString()));
                }
                    break;

                case REQ_WEAR:
                case REQ_TOOL:
                case REQ_WEAPON:
                case REQ_USE:
                case REQ_BREAK:
                case REQ_CRAFT:
                case REQ_PLACE:
                {
                    button.text.add(new TextComponent(""));
                    button.text.add(getTransComp("pmmo." + jType.toString().replace("req_", "")).setStyle(XP.textStyle.get(XP.checkReq(player, button.regKey, jType) ? "green" : "red")));
                    addLevelsToButton(button, reqMap.get(button.regKey), player, false);
                }
                    break;

                case HISCORE:
                    skill = type;
                    String playerName = button.regKey;
                    UUID playerUUID = XP.playerUUIDs.get(playerName);
                    double xp;

                    if(type.equals("totalLevel"))
                    {
                        button.text.add(new TextComponent(" " + XP.getTotalLevelFromMap(XP.getOfflineXpMap(playerUUID))));
                        button.text.add(new TranslatableComponent("pmmo.xpX", " " + XP.getTotalXpFromMap(XP.getOfflineXpMap(playerUUID))));
                    }
                    else
                    {
                        Map<String, Double> skillsMap;
                        Style color = XP.getColorStyle(0x53f953);
                        skillsMap = XP.getOfflineXpMap(playerUUID);
                        if(skillsMap.containsKey(skill))
                        {
                            xp = skillsMap.get(skill);
                            button.text.add(getTransComp("pmmo.levelX", DP.dpSoft(XP.levelAtXpDecimal(xp))).setStyle(color));
                            button.text.add(getTransComp("pmmo.xpX", DP.dpSoft(xp)).setStyle(color));
                        }
                    }
                    break;

                case SALVAGE:
                case SALVAGE_FROM:
                {
                    if(reqMap2 == null)
                        return;
                    int smithLevel = (int) Skill.getLevelDecimal(Skill.SMITHING.toString(), player);
                    String outputName;
                    double chance, levelReq, salvageMax, baseChance, chancePerLevel, maxChance, xpPerItem;
                    Map<String, Double> salvageToItemMap;
                    Style color;
                    button.unlocked = false;
                    int i = 0;
                    List<String> toItemsList = new ArrayList<>(reqMap2.get(button.regKey).keySet());
                    toItemsList.sort(Comparator.comparingInt(key -> (int) (double) reqMap2.get(button.regKey).get(key).get("levelReq")));

                    for(String salvageToItemKey : toItemsList)
                    {
                        salvageToItemMap = reqMap2.get(button.regKey).get(salvageToItemKey);
                        outputName       = getTransComp(XP.getItem(salvageToItemKey).getDescriptionId()).getString();
                        levelReq         = salvageToItemMap.get("levelReq");
                        salvageMax       = salvageToItemMap.get("salvageMax");
                        baseChance       = salvageToItemMap.get("baseChance");
                        chancePerLevel   = salvageToItemMap.get("chancePerLevel");
                        maxChance        = salvageToItemMap.get("maxChance");
                        xpPerItem        = salvageToItemMap.get("xpPerItem");

                        chance = baseChance + (chancePerLevel * (smithLevel - levelReq));

                        if(chance < 0)
                            chance = 0;
                        if(chance > maxChance)
                            chance = maxChance;

                        color = XP.textStyle.get(chance > 0 ? "green" : "red");

                        if(chance > 0 && smithLevel >= levelReq)
                            button.unlocked = true;

                        if(i++ == 0)
                            button.text.add(new TranslatableComponent(jType == JType.SALVAGE ? "pmmo.salvagesInto" : "pmmo.canBeSalvagedFrom"));
                        button.text.add(new TextComponent("____________________________"));
                        button.text.add(new TextComponent(""));
                        button.text.add(new TextComponent(getTransComp(jType == JType.SALVAGE ? "pmmo.valueValue" : "pmmo.valueFromValue", DP.dpSoft(salvageMax), outputName).getString()).setStyle(color));
                        button.text.add(getTransComp("pmmo.canBeSalvagedFromLevel", DP.dpSoft(levelReq)).setStyle(color));
                        button.text.add(new TextComponent(""));
                        button.text.add(getTransComp("pmmo.xpPerItem", DP.dpSoft(xpPerItem)).setStyle(color));
                        button.text.add(getTransComp("pmmo.chancePerItem", DP.dpSoft(chance)).setStyle(color));
                        button.text.add(new TextComponent(""));
                        button.text.add(getTransComp("pmmo.baseChance", DP.dpSoft(baseChance)).setStyle(color));
                        button.text.add(getTransComp("pmmo.chancePerLevel", DP.dpSoft(chancePerLevel)).setStyle(color));
                        button.text.add(getTransComp("pmmo.maxChancePerItem", DP.dpSoft(maxChance)).setStyle(color));
                    }
                }
                    break;

                case TREASURE:
                case TREASURE_FROM:
                {
                    if(reqMap2 == null)
                        return;
                    int excavationLevel = (int) Skill.getLevelDecimal(Skill.EXCAVATION.toString(), player);
                    int startLevel, endLevel, minCount, maxCount;
                    String outputName;
                    double chance, startChance, endChance, xpPerItem;
                    Map<String, Double> treasureToItemMap;
                    Style color;
                    button.unlocked = false;
                    int i = 0;
                    List<String> toItemsList = new ArrayList<>(reqMap2.get(button.regKey).keySet());
                    toItemsList.sort(Comparator.comparingDouble(key -> BlockBrokenHandler.getTreasureItemChance(excavationLevel, reqMap2.get(button.regKey).get(key))));

                    for(String treasureToItemKey : toItemsList)
                    {
                        treasureToItemMap = reqMap2.get(button.regKey).get(treasureToItemKey);
                        outputName      = getTransComp(XP.getItem(treasureToItemKey).getDescriptionId()).getString();
                        startLevel      = (int) (double) treasureToItemMap.get("startLevel");
                        endLevel        = (int) (double) treasureToItemMap.get("endLevel");
                        startChance     = treasureToItemMap.get("startChance");
                        endChance       = treasureToItemMap.get("endChance");
                        xpPerItem       = treasureToItemMap.get("xpPerItem");
                        minCount        = (int) (double) treasureToItemMap.get("minCount");
                        maxCount        = (int) (double) treasureToItemMap.get("maxCount");

                        chance = BlockBrokenHandler.getTreasureItemChance(excavationLevel, treasureToItemMap);

                        color = XP.textStyle.get(chance > 0 ? "green" : "red");

                        if(chance > 0 )
                            button.unlocked = true;

                        if(i++ == 0)
                            button.text.add(new TranslatableComponent(jType == JType.TREASURE ? "pmmo.containsTreasure" : "pmmo.treasureFrom"));
                        button.text.add(new TextComponent("____________________________"));
                        button.text.add(new TextComponent(""));
                        button.text.add(new TextComponent(outputName).setStyle(color));
                        button.text.add(getTransComp("pmmo.xpPerItem", DP.dpSoft(xpPerItem)).setStyle(color));
                        button.text.add(getTransComp("pmmo.chancePerItem", DP.dpSoft(chance)).setStyle(color));
                        button.text.add(new TextComponent(""));
                        button.text.add(getTransComp("pmmo.minCount", minCount).setStyle(color));
                        button.text.add(getTransComp("pmmo.maxCount", maxCount).setStyle(color));
                        button.text.add(new TextComponent(""));
                        button.text.add(getTransComp("pmmo.startChance", startChance).setStyle(color));
                        button.text.add(getTransComp("pmmo.startLevel", startLevel).setStyle(color));
                        button.text.add(getTransComp("pmmo.endChance", endChance).setStyle(color));
                        button.text.add(getTransComp("pmmo.endLevel", endLevel).setStyle(color));
                    }
                }
                break;

                case SKILLS:
                {
                    if(button.regKey.equals("totalLevel"))
                    {
                        button.title = "" + getTransComp("pmmo.totalLevel").getString();
                        button.text.add(new TextComponent(" " + XP.getTotalLevelFromMap(XP.getOfflineXpMap(uuid))));
                        button.text.add(new TranslatableComponent("pmmo.xpX", " " + XP.getTotalXpFromMap(XP.getOfflineXpMap(uuid))));
                    }
                    else
                    {
                        skill = button.regKey;

                        double curXp = XP.getOfflineXp(skill, uuid);
                        double nextXp = XP.xpAtLevel(XP.levelAtXp(curXp) + 1);

                        button.title = getTransComp("pmmo.levelDisplay", getTransComp("pmmo." + button.regKey), DP.dpSoft(XP.levelAtXpDecimal(curXp))).setStyle(Skill.getSkillStyle(button.regKey)).getString();

                        button.text.add(new TextComponent(" " + getTransComp("pmmo.currentXp", DP.dpSoft(curXp)).getString()));
                        if(Skill.getLevel(skill, player) != XP.getMaxLevel())
                        {
                            button.text.add(new TextComponent(" " + getTransComp("pmmo.nextLevelXp", DP.dpSoft(nextXp)).getString()));
                            button.text.add(new TextComponent(" " + getTransComp("pmmo.RemainderXp", DP.dpSoft(nextXp - curXp)).getString()));
                        }
                    }
                }
                    break;

                default:
                    break;
            }

            if(skillText.size() > 0)
            {
                button.text.add(new TextComponent(""));
                skillText.sort(Comparator.comparingInt(textComp -> getTextInt(((Component) textComp).getString())).reversed());
                button.text.add(getTransComp("pmmo.xpModifiers"));
                button.text.addAll(skillText);
            }

            if(scaleText.size() > 0)
            {
                if(skillText.size() > 0)
                    button.text.add(new TextComponent(""));

                scaleText.sort(Comparator.comparingInt(textComp -> getTextInt(((Component) textComp).getString())).reversed());
                button.text.add(getTransComp("pmmo.enemyScaling"));
                button.text.addAll(scaleText);
            }

            if(negativeEffectText.size() > 0)
            {
                if(skillText.size() > 0 || scaleText.size() > 0)
                    button.text.add(new TextComponent(""));

                negativeEffectText.sort(Comparator.comparingInt(textComp -> getTextInt(((Component) textComp).getString())).reversed());
                button.text.add(getTransComp("pmmo.negativeBiomeEffects").setStyle(XP.textStyle.get("red")));
                button.text.addAll(negativeEffectText);
            }

            if(positiveEffectText.size() > 0)
            {
                if(skillText.size() > 0 || scaleText.size() > 0)
                    button.text.add(new TextComponent(""));

                positiveEffectText.sort(Comparator.comparingInt(textComp -> getTextInt(((Component) textComp).getString())).reversed());
                button.text.add(getTransComp("pmmo.positiveBiomeEffects").setStyle(XP.textStyle.get("green")));
                button.text.addAll(positiveEffectText);
            }

            switch(jType)  //Unlock/Lock buttons if necessary
            {
                case INFO_ORE:
                case INFO_LOG:
                case INFO_PLANT:
                case XP_VALUE_BREAK:
                        button.unlocked = XP.checkReq(player, button.regKey, JType.REQ_BREAK);
                    break;

                case XP_BONUS_WORN:
                        button.unlocked = XP.checkReq(player, button.regKey, JType.REQ_WEAR);
                    break;

                case FISH_POOL:

                    break;

                case REQ_WEAR:
                case REQ_TOOL:
                case REQ_WEAPON:
                case REQ_USE:
                case REQ_BREAK:
                case REQ_CRAFT:
                case REQ_PLACE:
                case REQ_BIOME:
                    button.unlocked = XP.checkReq(player, button.regKey, jType);
                    break;

                case XP_VALUE_CRAFT:
                    button.unlocked = XP.checkReq(player, button.regKey, JType.REQ_CRAFT);
                    break;

                case XP_VALUE_GROW:
                    button.unlocked = XP.checkReq(player, button.regKey, JType.REQ_PLACE);
                    break;

                default:
                    break;
            }
//            children.add(button);
        }

        //SORT BUTTONS

        switch(jType)
        {
            case INFO_ORE:
            case INFO_LOG:
            case INFO_PLANT:
            case XP_VALUE_BREAK:
                listButtons.sort(Comparator.comparingInt(b -> XP.getHighestReq(b.regKey, JType.REQ_BREAK)));
                break;

            case REQ_WEAR:
                listButtons.sort(Comparator.comparingInt(b -> XP.getHighestReq(b.regKey, JType.REQ_WEAR)));
                break;

            case SALVAGE:
            case SALVAGE_FROM:
                listButtons.sort(Comparator.comparingDouble(b -> (double) getLowestSalvageReq(reqMap2.get(b.regKey))));
                break;

            default:
                listButtons.sort(Comparator.comparingInt(b -> XP.getHighestReq(b.regKey, jType)));
                break;
        }

        switch(jType) //default buttons
        {
            case XP_VALUE_BREED:
                ListButton otherAnimalsBreedButton = new ListButton(0, 0, 3, 20, "pmmo.otherAnimals", jType, "", button -> ((ListButton) button).clickActionGlossary());
                otherAnimalsBreedButton.text.add(new TextComponent(" " + getTransComp("pmmo.xpDisplay", getTransComp("pmmo.farming"), DP.dpSoft(Config.forgeConfig.defaultBreedingXp.get())).getString()).setStyle(greenColor));
                listButtons.add(otherAnimalsBreedButton);
                break;

            case XP_VALUE_TAME:
            {
                ListButton otherAnimalsTameButton = new ListButton(0, 0, 3, 21, "pmmo.otherAnimals", jType, "", button -> ((ListButton) button).clickActionGlossary());
                otherAnimalsTameButton.text.add(new TextComponent(" " + getTransComp("pmmo.xpDisplay", getTransComp("pmmo.taming"), DP.dpSoft(Config.forgeConfig.defaultTamingXp.get())).getString()).setStyle(greenColor));
                listButtons.add(otherAnimalsTameButton);
            }
                break;

            case REQ_KILL:
            {
                ListButton otherAggresiveMobsButton = new ListButton(0, 0, 3, 26, "pmmo.otherAggresiveMobs", jType, "", button -> ((ListButton) button).clickActionGlossary());
                otherAggresiveMobsButton.text.add(new TextComponent(" " + getTransComp("pmmo.xpDisplay", getTransComp("pmmo.slayer"), DP.dpSoft(Config.forgeConfig.aggresiveMobSlayerXp.get())).getString()).setStyle(greenColor));
                listButtons.add(otherAggresiveMobsButton);

                ListButton otherPassiveMobsButton = new ListButton(0, 0, 3, 26, "pmmo.otherPassiveMobs", jType, "", button -> ((ListButton) button).clickActionGlossary());
                otherPassiveMobsButton.text.add(new TextComponent(" " + getTransComp("pmmo.xpDisplay", getTransComp("pmmo.hunter"), DP.dpSoft(Config.forgeConfig.passiveMobHunterXp.get())).getString()).setStyle(greenColor));
                listButtons.add(otherPassiveMobsButton);
            }
                break;

            case XP_VALUE_CRAFT:
            {
                ListButton otherCraftsButton = new ListButton(0, 0, 3, 22, "pmmo.otherCrafts", jType, "", button -> ((ListButton) button).clickActionGlossary());
                otherCraftsButton.text.add(new TextComponent(" " + getTransComp("pmmo.xpDisplay", getTransComp("pmmo.crafting"), DP.dpSoft(Config.forgeConfig.defaultCraftingXp.get())).getString()).setStyle(greenColor));
                listButtons.add(otherCraftsButton);
            }
                break;
        }

        scrollPanel = new ListScrollPanel(Minecraft.getInstance(), boxWidth - 40, boxHeight - 21, scrollY, scrollX, jType, player, listButtons);
        if(!MainScreen.scrollAmounts.containsKey(jType))
            MainScreen.scrollAmounts.put(jType, 0);
        scrollPanel.setScroll(MainScreen.scrollAmounts.get(jType));
        children.add(scrollPanel);
        filterTextField = new EditBox(font, x, y + boxHeight - 10, boxWidth, 16, new TranslatableComponent(""));
        filterTextField.setValue(filterText);
        updateListFilter();
        filterTextField.setFocus(true);
        children.add(filterTextField);
        addWidget(exitButton);
    }

    private static void addLevelsToButton(ListButton button, Map<String, Double> map, Player player, boolean ignoreReq)
    {
        List<Component> levelsToAdd = new ArrayList<>();

        for(Map.Entry<String, Double> inEntry : map.entrySet())
        {
            if(inEntry.getValue() <= 1)
                continue;
            if(!ignoreReq && Skill.getLevelDecimal(inEntry.getKey(), player) < inEntry.getValue())
                levelsToAdd.add(new TextComponent(" " + getTransComp("pmmo.levelDisplay", getTransComp("pmmo." + inEntry.getKey()), DP.dpSoft(inEntry.getValue())).getString()).setStyle(XP.textStyle.get("red")));
            else
                levelsToAdd.add(new TextComponent(" " + getTransComp("pmmo.levelDisplay", getTransComp("pmmo." + inEntry.getKey()), DP.dpSoft(inEntry.getValue())).getString()).setStyle(XP.textStyle.get("green")));
        }

        levelsToAdd.sort(Comparator.comparingInt(textComp -> getTextInt(((Component) textComp).getString())).reversed());

        button.text.addAll(levelsToAdd);
    }

    private static void addXpToButton(ListButton button, Map<String, Double> map)
    {
        List<Component> xpToAdd = new ArrayList<>();

        for(Map.Entry<String, Double> inEntry : map.entrySet())
        {
            xpToAdd.add(new TextComponent(" " + getTransComp("pmmo.xpDisplay", getTransComp("pmmo." + inEntry.getKey()), DP.dpSoft(inEntry.getValue())).getString()).setStyle(XP.textStyle.get("green")));
        }

        xpToAdd.sort(Comparator.comparingInt(textComp -> getTextInt(((Component) textComp).getString())).reversed());

        button.text.addAll(xpToAdd);
    }

    private static void addXpToButton(ListButton button, Map<String, Double> map, JType jType, Player player)
    {
        List<Component> xpToAdd = new ArrayList<>();

        for(Map.Entry<String, Double> inEntry : map.entrySet())
        {
            xpToAdd.add(new TextComponent(" " + getTransComp("pmmo.xpDisplay", getTransComp("pmmo." + inEntry.getKey()), DP.dpSoft(inEntry.getValue())).getString()).setStyle(XP.textStyle.get(XP.checkReq(player, button.regKey, jType) ? "green" : "red")));
        }

        xpToAdd.sort(Comparator.comparingInt(textComp -> getTextInt(((Component) textComp).getString())).reversed());

        button.text.addAll(xpToAdd);
    }

    private static void addPercentageToButton(ListButton button, Map<String, Double> map, boolean metReq)
    {
        List<Component> levelsToAdd = new ArrayList<>();

        for(Map.Entry<String, Double> inEntry : map.entrySet())
        {
            double value = inEntry.getValue();

            if(metReq)
            {
                if(value > 0)
                    levelsToAdd.add(new TextComponent(" " + getTransComp("pmmo.levelDisplay", getTransComp("pmmo." + inEntry.getKey()), "+" + value + "%").getString()).setStyle(XP.textStyle.get("green")));
                else if(value < 0)
                    levelsToAdd.add(new TextComponent(" " + getTransComp("pmmo.levelDisplay", getTransComp("pmmo." + inEntry.getKey()), value + "%").getString()).setStyle(XP.textStyle.get("red")));
            }
            else
                levelsToAdd.add(new TextComponent(" " + getTransComp("pmmo.levelDisplay", getTransComp("pmmo." + inEntry.getKey()), value + "%").getString()).setStyle(XP.textStyle.get("red")));
        }

        levelsToAdd.sort(Comparator.comparingInt(textComp -> getTextInt(((Component) textComp).getString())).reversed());

        button.text.addAll(levelsToAdd);
    }

    private static int getTextInt(String comp)
    {
        String number = comp.replaceAll("\\D+","");

        if(number.length() > 0 && !Double.isNaN(Double.parseDouble(number)))
            return (int) Double.parseDouble(number);
        else
            return 0;
    }

    private static double getTextDouble(String comp)
    {
        String number = comp.replaceAll("\\D+","");

        if(number.length() > 0 && !Double.isNaN(Double.parseDouble(number)))
            return Double.parseDouble(number);
        else
            return 0;
    }

    private static int getReqCount(String regKey, JType jType)
    {
        Map<String, Double> map = XP.getJsonMap(regKey, jType);

        if(map == null)
            return 0;
        else
            return map.size();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks)
    {
        renderBackground(stack,  1);

        if(jType.equals(JType.SKILLS))
            title = getTransComp("pmmo.playerStats", XP.playerNames.get(uuid));
        else if(jType.equals(JType.HISCORE))
            title = getTransComp("pmmo.skillHiscores", getTransComp("pmmo." + type)).setStyle(Skill.getSkillStyle(type));

        if(font.width(title.getString()) > 220)
            drawCenteredString(stack, font, title, sr.getGuiScaledWidth() / 2, y - 10, 0xffffff);
        else
            drawCenteredString(stack, font, title, sr.getGuiScaledWidth() / 2, y - 5, 0xffffff);

        x = ((sr.getGuiScaledWidth() / 2) - (boxWidth / 2));
        y = ((sr.getGuiScaledHeight() / 2) - (boxHeight / 2));

        scrollPanel.render(stack, mouseX, mouseY, partialTicks);
        filterTextField.render(stack, mouseX, mouseY, partialTicks);

        accumulativeHeight = 0;
        buttonsSize = listButtons.size();

        super.render(stack, mouseX, mouseY, partialTicks);

        for(ListButton button : activeListButtons)
        {
            buttonX = mouseX - button.x;
            buttonY = mouseY - button.y;

            if(mouseY >= scrollPanel.getTop() && mouseY <= scrollPanel.getBottom() && buttonX >= 0 && buttonX < 32 && buttonY >= 0 && buttonY < 32)
            {
                if(jType.equals(JType.REQ_BIOME) || jType.equals(JType.REQ_KILL) || jType.equals(JType.XP_VALUE_BREED) || jType.equals(JType.XP_VALUE_TAME) || jType.equals(JType.DIMENSION) || jType.equals(JType.FISH_ENCHANT_POOL) || jType.equals(JType.SKILLS) || jType.equals(JType.HISCORE) || button.regKey.equals("pmmo.otherCrafts"))
                    renderTooltip(stack, new TranslatableComponent(button.title), mouseX, mouseY);
                else if(button.itemStack != null)
                    renderTooltip(stack, button.itemStack, mouseX, mouseY);
            }

            accumulativeHeight += button.getHeight();
        }

        MainScreen.scrollAmounts.replace(jType, scrollPanel.getScroll());
    }

    public static double getLowestSalvageReq(Map<String, Map<String, Double>> map)
    {
        double lowestReq = XP.getMaxLevel();

        for(Map.Entry<String, Map<String, Double>> entry : map.entrySet())
        {
            lowestReq = Math.min(lowestReq, (int) (double) entry.getValue().get("levelReq"));
        }

        return lowestReq;
    }

    @Override
    public void renderBackground(PoseStack stack, int p_renderBackground_1_)
    {
        if (this.minecraft != null)
        {
            this.fillGradient(stack, 0, 0, this.width, this.height, 0x66222222, 0x66333333);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new ScreenEvent.BackgroundDrawnEvent(this, stack));
        }

        boxHeight = 256;
        boxWidth = 256;
        Minecraft.getInstance().getTextureManager().bindForSetup(box);

        this.blit(stack,  x, y, 0, 0,  boxWidth, boxHeight);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
    {
        accumulativeHeight = 0;
        for(ListButton listButton : listButtons)
        {
            accumulativeHeight += listButton.getHeight();
            if(accumulativeHeight > scrollPanel.getBottom() - scrollPanel.getTop())
            {
                scrollPanel.mouseScrolled(mouseX, mouseY, scroll);
                break;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        filterTextField.keyPressed(keyCode, scanCode, modifiers);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers)
    {
        filterTextField.keyReleased(keyCode, scanCode, modifiers);
        filterText = filterTextField.getValue();
        if(filterTextField.isFocused())
            updateListFilter();
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers)
    {
        filterTextField.charTyped(codePoint, modifiers);
        return false;
    }

    public void updateListFilter()
    {
        activeListButtons = new ArrayList<>();

        for(ListButton button : listButtons)
        {
            if(button.title.toLowerCase().contains(filterText.toLowerCase()))
            {
                activeListButtons.add(button);
                continue;
            }
            for(Component textComp : button.text)
            {
                if(textComp.getString().toLowerCase().contains(filterText.trim().toLowerCase()))
                {
                    activeListButtons.add(button);
                    break;
                }
            }
        }

        scrollPanel.setButtons(activeListButtons);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(button == 1)
        {
            exitButton.onPress();
            return true;
        }

        for(ListButton a : listButtons)
        {
            int buttonX = (int) mouseX - a.x;
            int buttonY = (int) mouseY- a.y;

            if(mouseY >= scrollPanel.getTop() && mouseY <= scrollPanel.getBottom() && buttonX >= 0 && buttonX < 32 && buttonY >= 0 && buttonY < 32)
            {
                a.onClick(mouseX, mouseY);
            }
        }
        scrollPanel.mouseClicked(mouseX, mouseY, button);
        filterTextField.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        scrollPanel.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        scrollPanel.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    public static TranslatableComponent getTransComp(String translationKey, Object... args)
    {
        return new TranslatableComponent(translationKey, args);
    }

}