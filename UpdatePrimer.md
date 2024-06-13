# Changelog

## [1.21.0-2.2.2] - 2024-02-01
### Major Changes
- Block Placed History and Vein Data moved to Data Attachments (see below)
- XP formulas have been revamped (see below)

### Minor Changes
- Default Data moved to optional Feature Packs (see below)
- Item NBT structures have changed, so your NBT paths will need to be updated.

### Bugfixes / Tweaks
- Added TOOL_BREAKING event which fires when an item with durability breaks.


### Block Placed History and Vein Data moved to Data Attachments
If you want to update a world from 1.20 to 1.21, you will lose all stored data related to which players placed blocks and their current vein.  This means furnaces and crops that are awarding XP for players will stop doing so.  If your vein charges are relatively fast, you won't notice the loss, but if you configured vein to be a rare ability, this may be an inconvenience to your players.

This was caused by NeoForge's revamp of the capability system.

### XP formulas have been revamped
Internally, XP now has a separate value for XP and Level.  This means nothing to end users, except for this now makes it possible to use the 9 Quadrillion total XP limit from 1.20 as the limit for a single level and makes that same value an option for max level.  In short, it is practically impossible now to have a max level that pmmo caps.  

The formula also changed.  
<u>Old Formula</u>
`base_xp * power_base ^ (per_level * level)`
<u>New Formula</u>
`xp_min + xp_base ^ (per_lvl * level)`

With this the toggles for Linear and exponential were also removed.  to achieve linear XP, set `xp_base` to zero and `xp_min` to your per-level amount.

### Default Data moved to optional Feature Packs
The built-in default data was somewhat annoying to work around.  The disabler pack was helpful, but added the additional problem of having to make sure your pack loaded before other packs with pmmo data.  

To solve this problem, all default data was moved to "feature packs", which Mojang uses for things like Bundles, Villager update, Sniffers, etc.  By default the "default" pack is enabled.  When you create a new world, go to the datapacks menu.  You will see you now have the option to toggle this datapack off.  Additionally, you can enable either the Easy or Hardcore pack in its place.  If you would like pmmo to provide no default data, disable all 3.  Note that pmmo still populates its internal tags, but all data and treasure are now compartmentalized into these feature packs.

### Item NBT structures have changed, so your NBT paths will need to be updated.
Mojang changed how NBT is stored on itemstacks.  This means that your old NBT paths will no longer be correct.  Please look at the new NBT format for your configured items and update your paths.  In most cases, the path should be the only thing that broke.  NBT for blocks and entities is unaffected.


**TECHNICAL NOTE: admin set level does not trigger perks.  use add level if you want perks to trigger.**