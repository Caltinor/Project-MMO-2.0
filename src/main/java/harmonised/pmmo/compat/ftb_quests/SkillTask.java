package harmonised.pmmo.compat.ftb_quests;


/*import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.config.SkillsConfig;
import harmonised.pmmo.core.Core;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
*/
public class SkillTask// extends Task
{/*
    public static TaskType SKILL = FTBQHandler.SKILL;
    public String skill;
    public int requiredLevel;

    public SkillTask(Quest quest)
    {
        super(quest);
        skill = "mining";
        requiredLevel = 1;
    }

    @Override
    public TaskType getType()
    {
        return SKILL;
    }

    @Override
    public void writeData(CompoundTag nbt)
    {
        super.writeData(nbt);
        nbt.putString("skill", skill);
        nbt.putInt("requiredLevel", requiredLevel);
    }

    @Override
    public void readData(CompoundTag nbt)
    {
        super.readData(nbt);
        skill = nbt.getString("skill");
        requiredLevel = nbt.getInt("requiredLevel");
    }

    @Override
    public void writeNetData(FriendlyByteBuf buffer)
    {
        super.writeNetData(buffer);
        buffer.writeUtf(skill, Short.MAX_VALUE);
        buffer.writeInt(requiredLevel);
    }

    @Override
    public void readNetData(FriendlyByteBuf buffer)
    {
        super.readNetData(buffer);
        skill = buffer.readUtf(Short.MAX_VALUE);
        requiredLevel = buffer.readInt();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void getConfig(ConfigGroup config)
    {
        super.getConfig(config);
        config.addEnum("skill", skill, input -> skill = (String) input, NameMap.of("mining", SkillsConfig.SKILLS.get().keySet().toArray()).create());
        config.addInt("requiredLevel", requiredLevel, input -> requiredLevel = input, requiredLevel, 1, Config.MAX_LEVEL.get());
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
        teamData.setProgress(this, Core.get(player.level).getData().getPlayerSkillLevel(skill, player.getUUID()));
    }

    @Override
    public long getMaxProgress()
    {
        return (long) requiredLevel;
    }*/
}
