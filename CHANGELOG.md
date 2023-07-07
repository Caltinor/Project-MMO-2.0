# Changelog

## [1.20.1-1.0.2] - 2023-07-07
### Bugfixes / Tweaks
- Added CROUCH event (no XP source, only used for perk activation)
- Added defaults for Tetra items
- Added default XP for brewing potions
- Added global vein setting for all players in server config
- Added perk `"pmmo:temp_attribute"` which expires after the event ends
  - nerfed move speed from skill-up default value and added sprinting speed boost
- Updated vein feature to only prevent activation if the tool would result in no drops
- Updated default breath perk to not require a 25-minute cooldown
- Fixed log spam from perks
- Fixed extra drops on tagged blocks giving zero extra
- Fixed xp loss on death cases.  should be consistent now
- Fixed breaking waterlogged insta-break blocks unbreakable water notification
- Fixed admin command not honoring level setting from zero to one
- Fixed cooldown functionality of perks.
- Fixed [Air] showing in salvage 