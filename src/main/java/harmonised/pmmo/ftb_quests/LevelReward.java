package harmonised.pmmo.ftb_quests;



public class LevelReward //extends Reward
{
    /*public static RewardType LEVEL_REWARD = FTBQHandler.LEVEL_REWARD;
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
        config.addDouble( "amount", amount, input -> amount = input, 1, -XP.getMaxLevel(), XP.getMaxLevel() );
    }

    @Override
    public void claim( ServerPlayerEntity player, boolean notify )
    {
        Skill.addLevel( skill, player.getUniqueID(), amount, "Completing a Quest", !notify, ignoreBonuses );
    }

    @Override
    public IFormattableTextComponent getAltTitle()
    {
        return new TranslationTextComponent( "pmmo." + skill ).setStyle( Skill.getSkillStyle( skill ) );
    }*/
}