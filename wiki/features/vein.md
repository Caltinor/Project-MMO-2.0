Vein Mining is a feature of PMMO that allows the player to break multiple blocks of the same kind all at once.  In previous versions of PMMO this ability scaled with your player skill level.  In this version of PMMO you gain vein charge and capacity based on the items equipped.

## How to Vein Mine
Vein mining occurs when a player breaks a "marked" block.  Players mark blocks by pressing the "`/~" (default) key while looking at a block.  If the player breaks an unmarked block, they will mine normally, even if the block is adjacent or the same as the marked block.

You can unmark a block by marking it again, or by marking a different block.  a player can only have one marked block at a time.

When a marked block is looked at by the player, the veinable blocks will show a purple outline around each block that will be veined.

## Equipped Items and Vein Charge/Capacity
When an item is worn as an armor or curio or held in either hand, and it has vein attributes, it will add to the player's current vein ability.  Items with vein attributes have two attributes:
- Vein Capacity: The maximum charge this item can provide
- Vein Charge Rate: The rate this item recovers charge after use

A player's vein ability is determined by how much charge they have when breaking a block.  The player's charge regenerates based on the charge rate of the items up to the capacity added from all items.  Using this, the player can swap items to change charge rate and/or the max capacity.

If a player has more charge than the items equipped allow, the player is "overcharged".  the player will not gain any more charge until they use the excess charge, or re-equip an item that has a large charge capacity.  Overcharge is still considered charge and can be used like regular charge.

## Block Consumption
When blocks are broken they consume vein charge per block based on their configured consume amount.  If a block does not have a manual configuration, it uses the consume amount set in `pmmo-server.toml`.  You can also set the consume amount in the block's datapack json, which will override this setting.  for example, if the setting in `pmmo-server.toml` is set to 4, and you have 100 charge, a default block vein would break 25 blocks.  If that same block had a configured setting of 2, veining would break 50 blocks.

