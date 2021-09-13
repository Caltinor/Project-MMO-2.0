package harmonised.pmmo.api.events;

import harmonised.pmmo.api.APIUtils;
import harmonised.pmmo.util.XP;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class XpEvent extends PlayerEvent
{
    private String skill;
    private double amount;
    private String sourceName;
    private boolean skip, ignoreBonuses, causedByParty;

    public XpEvent( Player player, String skill, String sourceName, double amount, boolean skip, boolean ignoreBonuses, boolean causedByParty )
    {
        super( player );
        this.skill = skill;
        this.amount = amount;
        this.sourceName = sourceName;
        this.skip = skip;
        this.ignoreBonuses = ignoreBonuses;
        this.causedByParty = causedByParty;
    }

    @Override
    public boolean isCancelable()
    {
        return true;
    }

    public int startLevel()
    {
        return XP.levelAtXp( amount );
    }

    public int endLevel()
    {
        return XP.levelAtXp( amount + APIUtils.getXp( skill, getPlayer() ) );
    }

    public String getSkill()
    {
        return skill;
    }

    public void setSkill(String skill)
    {
        this.skill = skill;
    }

    public double getAmount()
    {
        return amount;
    }

    public void setAmount(double amount)
    {
        this.amount = amount;
    }

    public String getSourceName()
    {
        return sourceName;
    }

    public void setSourceName(String sourceName)
    {
        this.sourceName = sourceName;
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip)
    {
        this.skip = skip;
    }

    public boolean isIgnoreBonuses() {
        return ignoreBonuses;
    }

    public void setIgnoreBonuses(boolean ignoreBonuses)
    {
        this.ignoreBonuses = ignoreBonuses;
    }

    public boolean isCausedByParty()
    {
        return causedByParty;
    }

    public void setCausedByParty(boolean causedByParty)
    {
        this.causedByParty = causedByParty;
    }
}
