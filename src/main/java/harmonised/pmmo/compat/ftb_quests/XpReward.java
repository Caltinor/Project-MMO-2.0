package harmonised.pmmo.compat.ftb_quests;


//import dev.ftb.mods.ftblibrary.config.ConfigGroup;
//import dev.ftb.mods.ftblibrary.config.NameMap;
//import dev.ftb.mods.ftbquests.quest.Quest;
//import dev.ftb.mods.ftbquests.quest.reward.Reward;
//import dev.ftb.mods.ftbquests.quest.reward.RewardAutoClaim;
//import dev.ftb.mods.ftbquests.quest.reward.RewardType;
//import harmonised.pmmo.config.Config;
//import harmonised.pmmo.core.Core;
//import harmonised.pmmo.setup.datagen.LangProvider;
//import net.minecraft.core.HolderLookup;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.network.RegistryFriendlyByteBuf;
//import net.minecraft.network.chat.Component;
//import net.minecraft.server.level.ServerPlayer;

public class XpReward// extends Reward
{
//    public static RewardType XP_REWARD = FTBQHandler.XP_REWARD;
//    public String skill = "mining";
//    public long amount = 83;
//    public boolean ignoreBonuses = false;
//
//    public XpReward(long id, Quest quest)
//    {
//        super(id, quest);
//        autoclaim = RewardAutoClaim.INVISIBLE;
//    }
//
//    @Override
//    public RewardType getType()
//    {
//        return XP_REWARD;
//    }
//
//    @Override
//    public void writeData(CompoundTag nbt, HolderLookup.Provider provider)
//    {
//        super.writeData(nbt, provider);
//        nbt.putString("skill", skill);
//        nbt.putLong("amount", amount);
//        nbt.putBoolean("ignoreBonuses", ignoreBonuses);
//    }
//
//    @Override
//    public void readData(CompoundTag nbt, HolderLookup.Provider provider)
//    {
//        super.readData(nbt, provider);
//        skill = nbt.getString("skill");
//        amount = nbt.getLong("amount");
//        ignoreBonuses = nbt.getBoolean("ignoreBonuses");
//    }
//
//    @Override
//    public void writeNetData(RegistryFriendlyByteBuf buffer)
//    {
//        super.writeNetData(buffer);
//        buffer.writeUtf(skill);
//        buffer.writeLong(amount);
//        buffer.writeBoolean(ignoreBonuses);
//    }
//
//    @Override
//    public void readNetData(RegistryFriendlyByteBuf buffer)
//    {
//        super.readNetData(buffer);
//        skill = buffer.readUtf();
//        amount = buffer.readLong();
//        ignoreBonuses = buffer.readBoolean();
//    }
//
//    @Override
//    public void fillConfigGroup(ConfigGroup config)
//    {
//        super.fillConfigGroup(config);
//        config.addEnum("skill", skill, input -> skill = (String) input, NameMap.of("mining", Config.skills().skills().keySet().toArray()).create());
//        config.addLong("amount", amount, input -> amount = input, 1, Long.MIN_VALUE, Long.MAX_VALUE);
//        config.addBool("ignoreBonuses", ignoreBonuses, v -> ignoreBonuses = v, false).setNameKey("pmmo.ignoreBonuses");
//    }
//
//    @Override
//    public void claim(ServerPlayer player, boolean notify)
//    {
//        Core.get(player.level()).getData().addXp(player.getUUID(), skill, amount);
//    }
//
//    @Override
//    public Component getAltTitle() {
//        return LangProvider.FTBQ_XP_TITLE.asComponent(amount, LangProvider.skill(skill));
//    }
}