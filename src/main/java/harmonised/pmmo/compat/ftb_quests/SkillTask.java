package harmonised.pmmo.compat.ftb_quests;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.codecs.SkillData;
import harmonised.pmmo.core.Core;
import harmonised.pmmo.core.IDataStorage;
import harmonised.pmmo.setup.datagen.LangProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class SkillTask extends Task
{
    public static TaskType SKILL = FTBQHandler.SKILL;
    public String skill;
    public long requiredLevel;

    public SkillTask(long id, Quest quest)
    {
        super(id, quest);
        skill = "mining";
        requiredLevel = 1;
    }

    @Override
    public TaskType getType()
    {
        return SKILL;
    }

    @Override
    public void writeData(CompoundTag nbt, HolderLookup.Provider provider)
    {
        super.writeData(nbt, provider);
        nbt.putString("skill", skill);
        nbt.putLong("requiredLevel", requiredLevel);
    }

    @Override
    public void readData(CompoundTag nbt, HolderLookup.Provider provider)
    {
        super.readData(nbt, provider);
        skill = nbt.getString("skill");
        requiredLevel = nbt.getLong("requiredLevel");
    }

    @Override
    public void writeNetData(RegistryFriendlyByteBuf buffer)
    {
        super.writeNetData(buffer);
        buffer.writeUtf(skill, Short.MAX_VALUE);
        buffer.writeLong(requiredLevel);
    }

    @Override
    public void readNetData(RegistryFriendlyByteBuf buffer)
    {
        super.readNetData(buffer);
        skill = buffer.readUtf(Short.MAX_VALUE);
        requiredLevel = buffer.readLong();
    }

    @Override
    public void fillConfigGroup(ConfigGroup config)
    {
        super.fillConfigGroup(config);
        config.addEnum("skill", skill, input -> skill = (String) input, NameMap.of("mining", Config.skills().skills().keySet().toArray()).create());
        config.addLong("requiredLevel", requiredLevel, input -> requiredLevel = input, requiredLevel, 1, Config.server().levels().maxLevel());
    }

    @Override
    public int autoSubmitOnPlayerTick()
    {
        return 20;
    }

    @Override
    public void submitTask(TeamData teamData, ServerPlayer player, ItemStack craftedItem)
    {
        if(teamData.isCompleted(this))
            return;

        IDataStorage data = Core.get(player.level()).getData();
        long xp = data.getLevel(skill, player.getUUID());
        SkillData config = Config.skills().get(skill);
        if (config.isSkillGroup() && config.getUseTotalLevels()) {
        	xp = config.groupedSkills().get().entrySet().stream()
                    .map(entry -> (int) (entry.getValue() * data.getLevel(entry.getKey(), player.getUUID())))
                    .mapToInt(Integer::intValue).sum();
        }
        teamData.setProgress(this, xp);
    }

    @Override
    public long getMaxProgress()
    {
        return (long) requiredLevel;
    }

    @Override
    public Component getAltTitle() {
        return LangProvider.FTBQ_SKILL_TITLE.asComponent(requiredLevel, LangProvider.skill(skill));
    }
}
