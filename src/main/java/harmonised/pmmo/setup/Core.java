package harmonised.pmmo.setup;

import java.util.Map;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import harmonised.pmmo.config.DataConfig;
import harmonised.pmmo.core.SkillGates;
import harmonised.pmmo.core.XpUtils;
import harmonised.pmmo.impl.EventTriggerRegistry;
import harmonised.pmmo.impl.PerkRegistry;
import harmonised.pmmo.impl.PredicateRegistry;
import harmonised.pmmo.impl.TooltipRegistry;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.LogicalSide;

public class Core {
	  private static final Map<LogicalSide, Supplier<Core>> INSTANCES = Map.of(LogicalSide.CLIENT, Suppliers.memoize(Core::new)::get, LogicalSide.SERVER, Suppliers.memoize(Core::new)::get);
	  private final XpUtils xp;
	  private final SkillGates gates;
	  private final DataConfig config;
	  private final PredicateRegistry predicates;
	  private final EventTriggerRegistry eventReg;
	  private final TooltipRegistry tooltips;
	  private final PerkRegistry perks;
	  
	  private Core() {
	    this.xp = new XpUtils();
	    this.gates = new SkillGates();
	    this.config = new DataConfig();
	    this.predicates = new PredicateRegistry();
	    this.eventReg = new EventTriggerRegistry();
	    this.tooltips = new TooltipRegistry();
	    this.perks = new PerkRegistry();
	  }
	  
	  public static Core get(final LogicalSide side) {
	    return INSTANCES.get(side).get();
	  }
	  public static Core get(final Level level) {
	    return get(level.isClientSide()? LogicalSide.CLIENT : LogicalSide.SERVER);
	  }
	  
	  public XpUtils getXpUtils() {return xp;}
	  public SkillGates getSkillGates() {return gates;}
	  public DataConfig getDataConfig() {return config;}
	  public PredicateRegistry getPredicateRegistry() {return predicates;}
	  public EventTriggerRegistry getEventTriggerRegistry() {return eventReg;}
	  public TooltipRegistry getTooltipRegistry() {return tooltips;}
	  public PerkRegistry getPerkRegistry() {return perks;}
}
