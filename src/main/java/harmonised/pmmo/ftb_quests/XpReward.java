package harmonised.pmmo.ftb_quests;



public class XpReward //extends Reward
{
    /*public static RewardType XP_REWARD = FTBQHandler.XP_REWARD;
    public String skill = "mining";
    public double amount = 83;
    public boolean ignoreBonuses = false;

    public XpReward( Quest quest )
    {
        super( quest );
        autoclaim = RewardAutoClaim.INVISIBLE;
    }

    @Override
    public RewardType getType()
    {
        return XP_REWARD;
    }

    @Override
    public void writeData( CompoundNBT nbt )
    {
        super.writeData( nbt );
        nbt.putString( "skill", skill );
        nbt.putDouble( "amount", amount );
        nbt.putBoolean( "ignoreBonuses", ignoreBonuses );
    }

    @Override
    public void readData( CompoundNBT nbt )
    {
        super.readData( nbt );
        skill = nbt.getString( "skill" );
        amount = nbt.getDouble( "amount" );
        ignoreBonuses = nbt.getBoolean( "ignoreBonuses" );
    }

    @Override
    public void writeNetData( PacketBuffer buffer )
    {
        super.writeNetData(buffer );
        buffer.writeString( skill );
        buffer.writeDouble( amount );
        buffer.writeBoolean( ignoreBonuses );
    }

    @Override
    public void readNetData( PacketBuffer buffer )
    {
        super.readNetData(buffer );
        skill = buffer.readString();
        amount = buffer.readDouble();
        ignoreBonuses = buffer.readBoolean();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void getConfig( ConfigGroup config )
    {
        super.getConfig( config );
        config.addEnum("skill", skill, input -> skill = (String) input, NameMap.of( Skill.MINING.toString(), Skill.getSkills().keySet().toArray() ).create() );
        config.addDouble( "amount", amount, input -> amount = input, 1, -Config.getConfig("maxXp" ), Config.getConfig("maxXp" ) );
        config.addBool( "ignoreBonuses", ignoreBonuses, v -> ignoreBonuses = v, false ).setNameKey( "pmmo.ignoreBonuses" );
    }

    @Override
    public void claim( ServerPlayerEntity player, boolean notify )
    {
        Skill.addXp( skill, player.getUniqueID(), amount, "Completing a Quest", !notify, ignoreBonuses );
    }

    @Override
    public IFormattableTextComponent getAltTitle()
    {
        return new TranslationTextComponent( "pmmo." + skill ).setStyle( Skill.getSkillStyle( skill ) );
    }*/
}