Award Events are all the different gameplay actions that PMMO watches for and provides XP to the player for.  In all cases, the xp awarded is based on the configuration of the item/block/entity relevant to that event.  Below are all of the events in PMMO.

## Event Configuration 
Event names are used to define when XP is awarded.  Certain events can be blocked/prevented/gated.  For more information on requirements, see [HERE](gating.md). The below table lists the name of the event as it is used in configurations, a description of what the event is, any applicable requirements, and where the xp award configuration is located and how it works.

|Event Key|Description|Requirements|Xp Awarded|
|:---|:---:|:---:|:---:|
|**ANVIL_REPAIR**|When an Item is repaired in an anvil|None|the flat amount from the item's data|
|**BLOCK_BREAK**|When a block is actually broken (not when it is in the process of being broken)|`BREAK`(block)|defined in the block's data, affected by whether the player placed this block via the `pmmo-server.toml`'s `Reuse Penalty` setting.|
|**BREAK_SPEED**|Used by perks to control how fast a block can be broken and also checks requirements in advance of the block being broken|`TOOL`(item) and `BLOCK`(block)|None|
|**BLOCK_PLACE**|When a block is placed in the world|`PLACE`(block)|flat amount from the block's data|
|**BREATH_CHANGE**|when the player's breath increases or decreases|None|defined in `pmmo-server.toml`|
|**BREED**|when animals are bred to make a baby animal|`BREED`(entity)|flat amount from the entity's data|
|**BREW**|when a brewed potion is removed from the brewing stand|None|defined in the item's data (for output item, not the materials used)|
|**CONSUME**|when a potion or food item is consumed|`USE`|flat amount from the item's data|
|**CRAFT**|when an item is crafted|None|flat amount from the item's data|
|**CROUCH**|occurs every 10 ticks while the player is crouching.|None|N/A for Perks only|
|**RECEIVE_DAMAGE**|when the player receives damage|None|defined by the entity that dealt the damage.  This will be the mob itself if melee or the projectile of ranged.  if no entity data exists, `pmmo-server.toml` has a fallback section for damage types.  Note that xp is differentiated by damage type and the value entered is multiplied by the damage value actually received by the player (after damage reduction from armor, enchants, etc)|
|**DEAL_DAMAGE**|when the player deals melee damage|`WEAPON`(item) and `KILL`(entity)|combined from the item data of the player's held item, the entity being damaged, and the projectile if ranged.  if the damage type is not configured for all 3, `pmmo-server.toml` will be used as a fallback.  The Xp value is multiplied by the damage actually dealt after damage reduction effects.|
|**DEATH**| when an entity dies from a player (notice that DEAL_DAMAGE and DEATH will both fire and may have different settings)|`WEAPON`(item) and `KILL`(entity)|defined in the dying entity's data|
|**ENCHANT**|when an item is enchanted in a crafting table|None|from item's data (the config setting will be scaled according to the max enchant level of the enchantment for every enchantment.  So if the config setting is 100 xp and you get mending, which has one level, you get 100 xp.  However, if you get Efficiency 1 which has a max of 3, you get 33 experience instead.  If both enchantments were added, you would get 133)|
|**EFFECT**|Occurs every 10 ticks when the player has any active potion effects|None|defined in the effect's data|
|**FISH**|when an item is obtained from fishing|`TOOL`(item)|each loot item's data is referenced and XP from all loot items is awarded.|
|**SMELT**|when an item is smelted in a vanilla furnace, the player who placed the furnace gets the experience|None|from the ingredient item's data'|
|**GROW**|when a crop's age increases by one, the player who placed the crop gets the experience|None|from block's data|
|**HEALTH_CHANGE**|when the players health increases or decreases|None|defined in `pmmo-server.toml`|
|**JUMP**| when the player performs a regular jump.|None|defined in `pmmo-server.toml`|
|**SPRINT_JUMP**|when the player performs a regular jump while sprinting.|None|defined in `pmmo-server.toml`|
|**CROUCH_JUMP**| when the player performs a regular jump from a crouch.|None|defined in `pmmo-server.toml`|
|**HIT_BLOCK**| occurs when a player hits the block.  This works similarly to the BREAK_SPEED event except that you cannot modify the break speed and also captures creative mode player breaks before they happen.|`INTERACT`(block) and `INTERACT`(item)|defined in the block's data|
|**ACTIVATE_BLOCK**| occurs when a block is interacted with to activate such as pushing a button of flipping a lever.|`INTERACT`(block) and `INTERACT`(item) when using an item on a block|defined in the block's data for the block's behavior and the item's data for the item use on block behavior|
|**ACTIVATE_ITEM**| occurs when an item is right-clicked (or activated).|`USE`(item)|from item's data|
|**ENTITY**| experience for interacting with an entity, such as shearing a sheep, or talking to a villager|`ENTITY_INTERACT`(entity)|from entity's data|
|**RIDING**|occurs every 10 ticks while riding in any vehicle (horses, boats, minecarts, etc)|None (though you can gate mounting the entity with an interact check)|from entity's data|
|**SHIELD_BLOCK**| earn xp when damage is successfully blocked by your shield|`WEAPON`(item)|defined in the attacking entity's data|
|**SKILL_UP**| whenever a player's level increases.  this is not used to award XP but is used by Perks.|
|**SPRINTING**| xp earned every 10 ticks while sprinting|None|defined in `pmmo-server.toml`|
|**SUBMERGED**| xp earned every 10 ticks while underwater. is awarded simultaneously with other applicable swim events|None|defined in `pmmo-server.toml`|
|**SWIMMING**|xp earned every 10 ticks while swimming above water|None|defined in `pmmo-server.toml` and multiplied by travel speed|
|**DIVING**| xp earned every 10 ticks while submerged and sinking deeper|None|defined in `pmmo-server.toml` multiplied by travel speed|
|**SURFACING**| xp earned every 10 ticks while submerged but rising to the surface|None|defined in `pmmo-server.toml` multiplied by travel speed|
|**SWIM_SPRINTING**| xp earned every 10 ticks while submerged and swimming at a sprint|None|defined in `pmmo-server.toml` multiplied by travel speed|
|**TAMING**| awarded for successfully taming an animal.|`TAME`(entity)|defined in the tamed entity's data|