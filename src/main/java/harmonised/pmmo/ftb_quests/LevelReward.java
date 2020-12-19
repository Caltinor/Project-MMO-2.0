package harmonised.pmmo.ftb_quests;

import com.feed_the_beast.ftblib.lib.config.ConfigGroup;
import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.util.misc.NameMap;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.quest.reward.RewardAutoClaim;
import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.skills.Skill;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LevelReward extends Reward
{
    public static RewardType LEVEL_REWARD;
    public String skill = "mining";
    public double amount = 1;
    public boolean ignoreBonuses = false;

    public LevelReward( Quest quest )
    {
        super( quest );
        autoclaim = RewardAutoClaim.INVISIBLE;
    }

    @Override
    public RewardType getType()
    {
        return LEVEL_REWARD;
    }

    @Override
    public void writeData( NBTTagCompound nbt )
    {
        super.writeData( nbt );
        nbt.setString( "skill", skill );
        nbt.setDouble( "amount", amount );
    }

    @Override
    public void readData( NBTTagCompound nbt )
    {
        super.readData( nbt );
        skill = nbt.getString( "skill" );
        amount = nbt.getDouble( "amount" );
    }

    @Override
    public void writeNetData( DataOut data )
    {
        super.writeNetData( data );
        data.writeString( skill );
        data.writeDouble( amount );
    }

    @Override
    public void readNetData( DataIn data )
    {
        super.readNetData( data );
        skill = data.readString();
        amount = data.readDouble();
    }

    @Override
    @SideOnly( Side.CLIENT )
    public void getConfig( ConfigGroup config )
    {
        super.getConfig( config );
        config.addEnum("skill", () -> skill, input -> skill = (String) input, NameMap.create( Skill.MINING.toString(), Skill.getSkills().keySet().toArray() ) );
        config.addDouble( "amount", () -> amount, input -> amount = input, 1, 0.01, FConfig.getConfig("maxLevel" ) );
    }

    @Override
    public void claim(EntityPlayerMP player, boolean notify )
    {
        Skill.addLevel( skill, player.getUniqueID(), amount, "Completing a Quest", !notify, ignoreBonuses );
    }

    @Override
    public String getAltTitle()
    {
        return new TextComponentTranslation( "pmmo." + skill ).setStyle( Skill.getSkillStyle( skill ) ).getFormattedText();
    }
}