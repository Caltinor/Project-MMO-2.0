[Home](../home.md)

Project MMO's API allows you to interject custom logic into the requirements, Xp gain, and Bonus logic of the mod.  This allows you to use your mod/addon's custom feature as a prerequisite for an action, as a factor in xp calculations, or in establishing xp bonuses.

## Registering your Logic
During the Forge `ServerStartingEvent` register your custom logic using the respective register call in `APIUtils`

### Registering Requirement Predicates
Depending on whether your object is an item, block, or entity, there is a specific `APIUtils.registerXXXXpredicate` for each respective object.  This function takes the following arguments:
- `ResourceLocation` corresponding to the item applied to
- `ReqType` corresponding to the requirement type this logic applies to.  If you want this to apply to all ReqTypes, register for each individually.
- `BiPredicate<Player, OBJECT>` (where OBJECT is the respective object type) which returns true if the player is permitted to perform the action.

If you are using skill levels as part of your requirement logic, you can also register a function which supplies those values for use in tooltips.  for each object type there is a respective `APIUtils.registerXXXXRequirementTooltipData` function.  The parameters are the same as the predicate registration except for the last, which is a `java.util.function.Function` which returns the requirement map of `Map<String, Integer>`

### Registering XP gain Logic
Like the requirement predicates, there is a function for each object type.  However unlike the requirement predicates you do not need to separately register a tooltip.  Since tooltips read out the exact data being provided as xp gains, the same function covers both use cases.  To register your custom logic, call the respective `APIUtils.registerxxxxxXpGainTooltipData` which takes the following arguments:
- `ResourceLocation` the object ID
- `EventType` the specific event this logic applies to.  If you want this to apply to all EventTypes, register for each individually.
- `Function<OBJECT, Map<String, Long>>`(where OBJECT is the respective object type) which takes the object and returns the map.

### Registering Item Bonus Logic
There is only one call to register custom bonus logic.  To register an item bonus logic call `APIUtils.registerItemBonusData` with parameters:
- `ResourceLocation` the item id
- `ModifierDataType` either HELD or WORN.  if you need both, register once for each
- `Function<ItemStack, Map<String, Double>>` a function which is supplied the itemstack and returns a modifier map.

[Home](../home.md)