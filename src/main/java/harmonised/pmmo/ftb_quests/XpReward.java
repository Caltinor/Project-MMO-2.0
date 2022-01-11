package harmonised.pmmo.ftb_quests;


import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardAutoClaim;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.skills.Skill;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class XpReward extends Reward
{
    public static RewardType XP_REWARD = FTBQHandler.XP_REWARD;
    public String skill = "mining";
    public double amount = 83;
    public boolean ignoreBonuses = false;

    public XpReward(Quest quest)
    {
        super(quest);
        autoclaim = RewardAutoClaim.INVISIBLE;
    }

    @Override
    public RewardType getType()
    {
        return XP_REWARD;
    }

    @Override
    public void writeData(CompoundTag nbt)
    {
        super.writeData(nbt);
        nbt.putString("skill", skill);
        nbt.putDouble("amount", amount);
        nbt.putBoolean("ignoreBonuses", ignoreBonuses);
    }

    @Override
    public void readData(CompoundTag nbt)
    {
        super.readData(nbt);
        skill = nbt.getString("skill");
        amount = nbt.getDouble("amount");
        ignoreBonuses = nbt.getBoolean("ignoreBonuses");
    }

    @Override
    public void writeNetData(FriendlyByteBuf buffer)
    {
        super.writeNetData(buffer);
        buffer.writeUtf(skill);
        buffer.writeDouble(amount);
        buffer.writeBoolean(ignoreBonuses);
    }

    @Override
    public void readNetData(FriendlyByteBuf buffer)
    {
        super.readNetData(buffer);
        skill = buffer.readUtf();
        amount = buffer.readDouble();
        ignoreBonuses = buffer.readBoolean();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void getConfig(ConfigGroup config)
    {
        super.getConfig(config);
        config.addEnum("skill", skill, input -> skill = (String) input, NameMap.of(Skill.MINING.toString(), Skill.getSkills().keySet().toArray()).create());
        config.addDouble("amount", amount, input -> amount = input, 1, -Config.getConfig("maxXp"), Config.getConfig("maxXp"));
        config.addBool("ignoreBonuses", ignoreBonuses, v -> ignoreBonuses = v, false).setNameKey("pmmo.ignoreBonuses");
    }

    @Override
    public void claim(ServerPlayer player, boolean notify)
    {
        Skill.addXp(skill, player.getUUID(), amount, "Completing a Quest", !notify, ignoreBonuses);
    }

//    @Override
//    public IFormattableTextComponent getAltTitle()
//    {
//        return new TranslatableComponent("pmmo." + skill).setStyle(Skill.getSkillStyle(skill));
//    }
}