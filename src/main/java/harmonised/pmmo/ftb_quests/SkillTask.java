package harmonised.pmmo.ftb_quests;

import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.task.*;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigDouble;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.NameMap;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.skills.Skill;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


public class SkillTask extends Task
{
    public static TaskType SKILL;
    public Skill skill;
    public double requiredLevel;

    public SkillTask(Quest quest)
    {
        super(quest);
        skill = Skill.MINING;
        requiredLevel = 1;
    }

    @Override
    public TaskType getType()
    {
        return SKILL;
    }

    @Override
    public void writeData(CompoundNBT nbt)
    {
        super.writeData(nbt);
        nbt.putString( "skill", skill.toString() );
        nbt.putDouble( "requiredLevel", requiredLevel );
    }

    @Override
    public void readData(CompoundNBT nbt)
    {
        super.readData(nbt);
        skill = Skill.getSkill( nbt.getString( "skill" ) );
        requiredLevel = nbt.getDouble( "requiredLevel" );
    }

    @Override
    public void writeNetData(PacketBuffer buffer)
    {
        super.writeNetData(buffer);
        buffer.writeString( skill.toString(), Short.MAX_VALUE );
        buffer.writeDouble( requiredLevel );
    }

    @Override
    public void readNetData(PacketBuffer buffer)
    {
        super.readNetData(buffer);
        skill = Skill.getSkill( buffer.readString( Short.MAX_VALUE ) );
        requiredLevel = buffer.readDouble();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void getConfig(ConfigGroup config)
    {
        super.getConfig(config);
        config.addEnum("skill", skill, input -> skill = input, NameMap.of( Skill.INVALID_SKILL, Skill.valuesArray ).create());
        config.addDouble( "requiredLevel", requiredLevel, input -> requiredLevel = input, 1, 1, Config.getConfig( "maxLevel" ) );

    }

    @Override
    public String getAltTitle()
    {
//        return I18n.format("ftbquests.task.ftbquests.skill") + ": " + TextFormatting.DARK_GREEN + skill;
        return TextFormatting.DARK_GREEN + skill.toString();
    }

    @Override
    public int autoSubmitOnPlayerTick()
    {
        return 20;
    }

    @Override
    public TaskData createData(PlayerData data)
    {
        return new Data(this, data);
    }

    public static class Data extends BooleanTaskData<SkillTask>
    {
        private Data(SkillTask task, PlayerData data)
        {
            super(task, data);
        }

        @Override
        public boolean canSubmit( ServerPlayerEntity player )
        {
            return task.skill.getLevel( player ) >= task.requiredLevel;
        }
    }
}
