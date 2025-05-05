package harmonised.pmmo.api.events;


import net.neoforged.bus.api.Event;

/**This event acts as the appropriate endpoint for calling {@link harmonised.pmmo.api.APIUtils APIUtils}'s
 * various registration methods.  Both defaults and overrides should be called during this event.
 */
public class PMMORegistrationEvent extends Event {
    public PMMORegistrationEvent() {}
}
