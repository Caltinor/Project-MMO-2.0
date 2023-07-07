# Changelog

## [1.18.2-6.1.2] - 2023-07-07
### Bugfixes / Tweaks
- Added CROUCH event (no XP source, only used for perk activation)
- Added defaults for Tetra items
- Added default XP for brewing potions
- Added global vein setting for all players in server config
- Updated vein feature to only prevent activation if the tool would result in no drops
- Fixed log spam from perks
- Fixed extra drops on tagged blocks giving zero extra
- Fixed xp loss on death cases.  should be consistent now
- Fixed breaking waterlogged insta-break blocks unbreakable water notification
- Fixed admin command not honoring level setting from zero to one
- Fixed cooldown functionality of perks.
- Fixed [Air] showing in salvage 
- Added `modFilter` option to `genData` to generate files for specific mods
  - supports multiple calls to add multiple mods to the filter
  - note: player filters always had this repeat calling behavior
- Fixed excessive dig speed
- Fixed vein "correct tool" check being arbitrarily restrictive
- Fixed crash from childless breeding

