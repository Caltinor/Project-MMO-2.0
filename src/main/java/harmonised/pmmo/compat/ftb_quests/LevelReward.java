package harmonised.pmmo.compat.ftb_quests;


//import dev.ftb.mods.ftblibrary.config.ConfigGroup;
//import dev.ftb.mods.ftblibrary.config.NameMap;
//import dev.ftb.mods.ftbquests.quest.Quest;
//import dev.ftb.mods.ftbquests.quest.reward.*;
//import harmonised.pmmo.config.Config;
//import harmonised.pmmo.config.SkillsConfig;
//import harmonised.pmmo.core.Core;
//import harmonised.pmmo.setup.datagen.LangProvider;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.network.chat.Component;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.api.distmarker.OnlyIn;

public class LevelReward //extends Reward
{
//    public static RewardType LEVEL_REWARD = FTBQHandler.LEVEL_REWARD;
//    public String skill = "mining";
//    public int amount = 1;
//    public boolean ignoreBonuses = false;
//
//    public LevelReward(long id, Quest quest)
//    {
//        super(id, quest);
//        autoclaim = RewardAutoClaim.INVISIBLE;
//    }
//
//    @Override
//    public RewardType getType()
//    {
//        return LEVEL_REWARD;
//    }
//
//    @Override
//    public void writeData(CompoundTag nbt)
//    {
//        super.writeData(nbt);
//        nbt.putString("skill", skill);
//        nbt.putInt("amount", amount);
//    }
//
//    @Override
//    public void readData(CompoundTag nbt)
//    {
//        super.readData(nbt);
//        skill = nbt.getString("skill");
//        amount = nbt.getInt("amount");
//    }
//
//    @Override
//    public void writeNetData(FriendlyByteBuf buffer)
//    {
//        super.writeNetData(buffer);
//        buffer.writeUtf(skill);
//        buffer.writeInt(amount);
//    }
//
//    @Override
//    public void readNetData(FriendlyByteBuf buffer)
//    {
//        super.readNetData(buffer);
//        skill = buffer.readUtf();
//        amount = buffer.readInt();
//    }
//
//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public void fillConfigGroup(ConfigGroup config)
//    {
//        super.fillConfigGroup(config);
//        config.addEnum("skill", skill, input -> skill = (String) input, NameMap.of("mining", SkillsConfig.SKILLS.get().keySet().toArray()).create());
//        config.addInt("amount", amount, input -> amount = input, 1, -Config.MAX_LEVEL.get(), Config.MAX_LEVEL.get());
//    }
//
//    @Override
//    public void claim(ServerPlayer player, boolean notify)
//    {
//        Core.get(player.level()).getData().changePlayerSkillLevel(skill, player.getUUID(), amount);
//    }
//
//    @Override
//    public Component getAltTitle() {
//        return LangProvider.FTBQ_LVL_TITLE.asComponent(amount, LangProvider.skill(skill));
//    }
}