package harmonised.pmmo.ftb_quests;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.data.PlayerData;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.QuestData;
import com.feed_the_beast.ftbquests.quest.task.*;
import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.skills.Skill;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class SkillTask extends Task
{
    public static TaskType SKILL;
    public String skill;
    public double requiredLevel;

    public SkillTask( Quest quest )
    {
        super(quest);
        skill = Skill.MINING.toString();
        requiredLevel = 1;
    }

    @Override
    public TaskType getType()
    {
        return SKILL;
    }

    @Override
    public void writeData(NBTTagCompound nbt)
    {
        super.writeData(nbt);
        nbt.setString( "skill", skill.toString() );
        nbt.setDouble( "requiredLevel", requiredLevel );
    }

    @Override
    public void readData(NBTTagCompound nbt)
    {
        super.readData(nbt);
        skill = nbt.getString( "skill" );
        requiredLevel = nbt.getDouble( "requiredLevel" );
    }

    @Override
    public void writeNetData( DataOut buffer )
    {
        super.writeNetData(buffer);
        buffer.writeString( skill.toString() );
        buffer.writeDouble( requiredLevel );
    }

    @Override
    public void readNetData( DataIn buffer)
    {
        super.readNetData(buffer);
        skill = buffer.readString();
        requiredLevel = buffer.readDouble();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getConfig(ConfigGroup config)
    {
        super.getConfig(config);
        config.addEnum("skill", () -> skill, input -> skill = (String) input, NameMap.create( Skill.MINING.toString(), Skill.getSkills() ));
        config.addDouble( "requiredLevel", () -> requiredLevel, input -> requiredLevel = input, 1, 1, FConfig.getConfig( "maxLevel" ) );

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
    public TaskData createData( QuestData data )
    {
        return new Data(this, data);
    }

    public static class Data extends BooleanTaskData<SkillTask>
    {
        private Data(SkillTask task, QuestData data)
        {
            super(task, data);
        }

        @Override
        public boolean canSubmit( EntityPlayerMP player )
        {
            return Skill.getLevel( task.skill, player ) >= task.requiredLevel;
        }
    }
}