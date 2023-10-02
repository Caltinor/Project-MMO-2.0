[Home](../home.md)

Event Triggers are an extension of PMMO and the Forge Event system.  They allow you to register event logic just as you would for a Forge event, but within Project MMO.  The primary goal of this system is to expose pmmo's events to addons and allow some control back to the event they came from.

### Advantages of Event Triggers
Event triggers are different from forge events for two reasons. The first is that they execute only if any PMMO prerequisites have been met.  for example if a damage event triggers and the player does not have the requirement for that weapon to do so, the event is cancelled and any registered event triggers are never called.  The second benefit is the ability to apply special logic within a PMMO event that can affect the outcome of said event.  Because this logic is running within the PMMO event, you do not have to worry about prioritization of mod call order to ensure your changes are reflected in PMMO's logic.  You also have the ability to pass information back to the event which impacts subsequent logic such as perks, and awarded experience.

### Registering a listener
The event system works by calling `harmonised.pmmo.api.APIUtils.registerListener()` after mod initialization.  Server loading is the safest place to call, but there is no restriction on when a listener can be registered.  This is no way to unregister though.  At this time, all listeners are server-side.  the method takes three parameters

| parameter                                                            | purpose                                                                                            |
|:---------------------------------------------------------------------|:---------------------------------------------------------------------------------------------------|
| `ResourceLocation listenerID`                                        | a custom ID used for debugging                                                                     |
| EventType eventType                                                  | The specific PMMO event this is to be called within                                                |
| BiFunction<? super Event, CompoundTag, CompoundTag> executeOnTrigger | consumes the event and pmmo's input data and returns an output that is used by perks and xp awards |

### Events with Inputs
WIP

### Events with Outputs
WIP

### Cancellable Events
Any event which has the ability to be cancelled can be done so from an event Trigger.  To notify PMMO that you would like to cancel the event, add an element that uses `APIUtils.IS_CANCELLED` as a key in your output CompoundTag.  Note that PMMO is not looking for a value, only the existence of the key.  do not pass `"is_cancelled":false` back to PMMO, it will be treated as true.  there are also `DENY_ITEM_USE` and `DENY_BLOCK_USE` which are used in the player interact events and allow you to specifically cancel an aspect of the interaction, but not the entire event.  These work the same as the event cancellation in that only the presence of the key matters.

### Modifying XP Awards
Event Triggers have the ability to supply an award output to the event instance for which it was triggered.

To provide an award map, include a tag in your return tag using the key `APIUtils.SERIALIZED_AWARD_MAP` and the value using `APIUtils.serializeAwardMap(yourMap)` where "yourMap" is an instance of `Map<String, Long>` representing a map of skills and xp values.

[Home](../home.md)