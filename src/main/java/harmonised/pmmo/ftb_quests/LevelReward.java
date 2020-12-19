package harmonised.pmmo.ftb_quests;

import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import com.feed_the_beast.ftbquests.quest.reward.RewardAutoClaim;
import com.feed_the_beast.ftbquests.quest.reward.RewardType;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.NameMap;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.skills.Skill;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
    public void writeData( CompoundNBT nbt )
    {
        super.writeData( nbt );
        nbt.putString( "skill", skill );
        nbt.putDouble( "amount", amount );
    }

    @Override
    public void readData( CompoundNBT nbt )
    {
        super.readData( nbt );
        skill = nbt.getString( "skill" );
        amount = nbt.getDouble( "amount" );
    }

    @Override
    public void writeNetData( PacketBuffer buffer )
    {
        super.writeNetData(buffer );
        buffer.writeString( skill );
        buffer.writeDouble( amount );
    }

    @Override
    public void readNetData( PacketBuffer buffer )
    {
        super.readNetData(buffer );
        skill = buffer.readString();
        amount = buffer.readDouble();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void getConfig( ConfigGroup config )
    {
        super.getConfig( config );
        config.addEnum("skill", skill, input -> skill = (String) input, NameMap.of( Skill.MINING.toString(), Skill.getSkills().keySet().toArray() ).create() );
        config.addDouble( "amount", 1, input -> amount = input, 1, 0.01, Config.getConfig("maxLevel" ) );
    }

    @Override
    public void claim( ServerPlayerEntity player, boolean notify )
    {
        Skill.addLevel( skill, player.getUniqueID(), amount, "Completing a Quest", !notify, ignoreBonuses );
    }

    @Override
    public String getAltTitle()
    {
        return new TranslationTextComponent( "pmmo." + skill ).setStyle( Skill.getSkillStyle( skill ) ).getString();
    }
}