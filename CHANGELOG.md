# Changelog

## [1.20.1-1.2.11] - 2023-12-17
### Bugfixes / Tweaks
- Added Trade events.  Items now give XP based on whether they are given or received
- Added campfires to smelt event
- Updated xp modifier config comments to explain mutual exclusivity
- Updated skill xp modifier to use a dummy skill by default
- Updated effect perk to include visibility property
- Updated default Perks config to not include lvl 3 speed boost with sprinting
- Updated all perks with level scaling to respect `max_level` property
- Updated damage reduce perk to use damage tags
- Updated gains list to use a fade effect (removed the cap in the config)
- Fixed autovalues on food not scaling based on nutrition
- Fixed effect perk jerking effect at level zero
- Fixed Tetra/SG/Tinkers not being able to vein mine