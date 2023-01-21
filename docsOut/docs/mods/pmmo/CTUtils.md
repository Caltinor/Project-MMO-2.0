# CTUtils

## Importing the class

It might be required for you to import the package if you encounter any issues (like casting an Array), so better be safe than sorry and add the import at the very top of the file.
```zenscript
import mods.pmmo.CTUtils;
```


## Static Methods

:::group{name=registerPerk}

Registers a perk for use in pmmo-Perks.toml.

Return Type: void

```zenscript
CTUtils.registerPerk(perkID as ResourceLocation, defaults as MapData, customConditions as CTPerkPredicate, onExecute as CTPerkFunction, onConclude as CTPerkFunction, side as int) as void
```

| Parameter | Type | Description |
|-----------|------|-------------|
| perkID | [ResourceLocation](/vanilla/api/resource/ResourceLocation) | the Perk ID |
| defaults | [MapData](/vanilla/api/data/MapData) | the default settings for your perk.  These <br />  are provided to your execution if no manual user configuration <br />  is present.  This allows you to ignore null checks and  <br />  displays to users what settings are valid for your perk. <br />  <i>(NOTE: you are not required to add settings here for them <br />  to work.  It is permitted to have "hidden" settings.)</i> |
| customConditions | [CTPerkPredicate](/mods/pmmo/CTPerkPredicate) | used to check for conditions in which <br />  a perk is allowed to execute in addition to pmmo's default  <br />  checks.  For example, if the player is the correct dimension. |
| onExecute | [CTPerkFunction](/mods/pmmo/CTPerkFunction) | the logic executed by this perk |
| onConclude | [CTPerkFunction](/mods/pmmo/CTPerkFunction) | if an event has an end state, this executes |
| side | int | which logical side this fires on CLIENT=0, SERVER=1, BOTH=2 |


:::

:::group{name=setBonus}

registers a configuration setting for bonuses to xp gains.

Return Type: void

```zenscript
// CTUtils.setBonus(objectType as invalid, objectID as ResourceLocation, type as invalid, bonus as double?[string]) as void

CTUtils.setBonus(<constant:pmmo:objecttype:value>, <resource:namespace:path>, <constant:pmmo:modifierdatatype:value>, {skillname: 0.0 as double?, otherskillname: 0.0 as double?});
```

| Parameter | Type | Description |
|-----------|------|-------------|
| objectType | **invalid** | a value of [item, block, entity, dimension, or biome] |
| objectID | [ResourceLocation](/vanilla/api/resource/ResourceLocation) | the object linked to the bonus |
| type | **invalid** | the relation to the object which predicates the bonus |
| bonus | double?[string] | a map of skills and multipliers (1.0 = no bonus) |


:::

:::group{name=setEffectXp}

Registers a configuration setting for xp gained from active effects

Return Type: void

```zenscript
CTUtils.setEffectXp(effectID as ResourceLocation, effectLevel as int, xpGains as int?[string]) as void
```

| Parameter | Type | Description |
|-----------|------|-------------|
| effectID | [ResourceLocation](/vanilla/api/resource/ResourceLocation) | the key for the effect |
| effectLevel | int | the level of the effect |
| xpGains | int?[string] | a map of the skills and xp awarded when this effect is active |


:::

:::group{name=setEnchantment}

sets the requirements for a given enchantment and enchantment level

Return Type: void

```zenscript
// CTUtils.setEnchantment(enchantID as ResourceLocation, enchantLevel as int, reqs as int?[string]) as void

CTUtils.setEnchantment(<resource:namespace:path>, 1, {skillname: 00 as int?, otherskillname: 00 as int?});
```

| Parameter | Type | Description |
|-----------|------|-------------|
| enchantID | [ResourceLocation](/vanilla/api/resource/ResourceLocation) | the key for the enchantment |
| enchantLevel | int | the level of the enchantment |
| reqs | int?[string] | a map of the skills and levels needed to use this enchantment |


:::

:::group{name=setMobModifier}

registers a configuration setting for mob modifiers to a biome or dimension. <br />   <br />  Attribute types for the inner map of mob_modifiers can be referenced <br />  using the static strings in this class prefixed with "MOB_"

Return Type: void

```zenscript
CTUtils.setMobModifier(objectType as invalid, locationID as ResourceLocation, mobID as ResourceLocation, modifiers as double?[string]) as void
```

| Parameter | Type | Description |
|-----------|------|-------------|
| objectType | **invalid** | a value of [item, block, entity, dimension, or biome] |
| locationID | [ResourceLocation](/vanilla/api/resource/ResourceLocation) | the biome or dimension key |
| mobID | [ResourceLocation](/vanilla/api/resource/ResourceLocation) | the key for the mob being set |
| modifiers | double?[string] | a map of attributes (health, speed, or damage) and modifiers |


:::

:::group{name=setNegativeEffect}

registers a configuration setting for what status effects should be applied to the player <br />  if they attempt to wear/hold and item they are not skilled enough to use.

Return Type: void

```zenscript
// CTUtils.setNegativeEffect(objectType as invalid, objectID as ResourceLocation, effects as int?[ResourceLocation]) as void

CTUtils.setNegativeEffect(<constant:pmmo:objecttype:value>, <resource:namespace:path>, {<resource:namespace:path>: 00 as int?, <resource:othernamespace:otherpath>: 00 as int?});
```

| Parameter | Type | Description |
|-----------|------|-------------|
| objectType | **invalid** | a value of [item, block, entity, dimension, or biome] |
| objectID | [ResourceLocation](/vanilla/api/resource/ResourceLocation) | No Description Provided |
| effects | int?[[ResourceLocation](/vanilla/api/resource/ResourceLocation)] | a map of effect ids and levels |


:::

:::group{name=setPositiveEffect}

registers a configuration setting for what status effects should be applied to the player <br />  based on their meeting or not meeting the requirements for the specified location. <br />  Note: a "negative" effect on a dimension will have no use in-game

Return Type: void

```zenscript
// CTUtils.setPositiveEffect(objectType as invalid, objectID as ResourceLocation, effects as int?[ResourceLocation]) as void

CTUtils.setPositiveEffect(<constant:pmmo:objecttype:value>, <resource:namespace:path>, {<resource:namespace:path>: 00 as int?, <resource:othernamespace:otherpath>: 00 as int?});
```

| Parameter | Type | Description |
|-----------|------|-------------|
| objectType | **invalid** | a value of [item, block, entity, dimension, or biome] |
| objectID | [ResourceLocation](/vanilla/api/resource/ResourceLocation) | No Description Provided |
| effects | int?[[ResourceLocation](/vanilla/api/resource/ResourceLocation)] | a map of effect ids and levels |


:::

:::group{name=setReq}

sets a configuration setting for a requirement to perform <br />  and action for the specified item.

Return Type: void

```zenscript
// CTUtils.setReq(objectType as invalid, objectID as ResourceLocation, type as invalid, requirements as int?[string]) as void

CTUtils.setReq(<constant:pmmo:objecttype:value>, <resource:namespace:path>, <constant:pmmo:reqtype:value>, {skillname: 00 as int?, otherskillname: 00 as int?});
```

| Parameter | Type | Description |
|-----------|------|-------------|
| objectType | **invalid** | a value of [item, block, entity, dimension, or biome] |
| objectID | [ResourceLocation](/vanilla/api/resource/ResourceLocation) | the key for the item being configured |
| type | **invalid** | the requirement category |
| requirements | int?[string] | a map of skills and levels needed to perform the action |


:::

:::group{name=setSalvage}

registers a configuration setting for items which can be obtained  <br />  via salvage from the item supplied. <br />  This class provides [SalvageBuilder](/mods/pmmo/SalvageBuilder) as a means to construct <br />  the salvage settings for each output object

Return Type: void

```zenscript
// CTUtils.setSalvage(item as ResourceLocation, salvage as SalvageBuilder[ResourceLocation]) as void

CTUtils.setSalvage(<resource:namespace:path>, {<resource:namespace:path>:builderInstance, <resource:othernamespace:otherpath>:otherbuilderInstance});
```

| Parameter | Type | Description |
|-----------|------|-------------|
| item | [ResourceLocation](/vanilla/api/resource/ResourceLocation) | a key for the item to be consumed by the salvage operation |
| salvage | [SalvageBuilder](/mods/pmmo/SalvageBuilder)[[ResourceLocation](/vanilla/api/resource/ResourceLocation)] | a map of output item keys and the conditions for salvage |


:::

:::group{name=setXpAward}

registers a configuration setting for experience that should be awarded <br />  to a player for performing an action with/on a specific object.

Return Type: void

```zenscript
// CTUtils.setXpAward(objectType as invalid, objectID as ResourceLocation, type as invalid, award as long?[string]) as void

CTUtils.setXpAward(<constant:pmmo:objecttype:value>, <resource:namespace:path>, <constant:pmmo:eventtype:value>, {skillname: 00 as long?, otherskillname: 00 as long?});
```

| Parameter | Type | Description |
|-----------|------|-------------|
| objectType | **invalid** | a value of [item, block, entity, dimension, or biome] |
| objectID | [ResourceLocation](/vanilla/api/resource/ResourceLocation) | the key for the item being configured |
| type | **invalid** | the event which awards the experience |
| award | long?[string] | a map of skills and experience values to be awarded |


:::

