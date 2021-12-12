package harmonised.pmmo.ftb_quests;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardAutoClaim;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.XP;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LevelReward extends Reward
{
    public static RewardType LEVEL_REWARD = FTBQHandler.LEVEL_REWARD;
    public String skill = "mining";
    public double amount = 1;
    public boolean ignoreBonuses = false;

    public LevelReward(Quest quest)
    {
        super(quest);
        autoclaim = RewardAutoClaim.INVISIBLE;
    }

    @Override
    public RewardType getType()
    {
        return LEVEL_REWARD;
    }

    @Override
    public void writeData(CompoundNBT nbt)
    {
        super.writeData(nbt);
        nbt.putString("skill", skill);
        nbt.putDouble("amount", amount);
    }

    @Override
    public void readData(CompoundNBT nbt)
    {
        super.readData(nbt);
        skill = nbt.getString("skill");
        amount = nbt.getDouble("amount");
    }

    @Override
    public void writeNetData(PacketBuffer buffer)
    {
        super.writeNetData(buffer);
        buffer.writeString(skill);
        buffer.writeDouble(amount);
    }

    @Override
    public void readNetData(PacketBuffer buffer)
    {
        super.readNetData(buffer);
        skill = buffer.readString();
        amount = buffer.readDouble();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void getConfig(ConfigGroup config)
    {
        super.getConfig(config);
        config.addEnum("skill", skill, input -> skill = (String) input, NameMap.of(Skill.MINING.toString(), Skill.getSkills().keySet().toArray()).create());
        config.addDouble("amount", amount, input -> amount = input, 1, -XP.getMaxLevel(), XP.getMaxLevel());
    }

    @Override
    public void claim(ServerPlayerEntity player, boolean notify)
    {
        Skill.addLevel(skill, player.getUniqueID(), amount, "Completing a Quest", !notify, ignoreBonuses);
    }

    @Override
    public IFormattableTextComponent getAltTitle()
    {
        return new TranslationTextComponent("pmmo." + skill).setStyle(Skill.getSkillStyle(skill));
    }
}