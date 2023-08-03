# Changelog

## [1.19.2-3.2.4] - 2023-08-03
### Minor version changes
- Removed `EatFoodEvent` which wasn't capturing all CONSUME cases
- Added seagrass to default data, so it doesn't give mining xp
- Fixed default data which used the legacy "KILL" xp event to now use "DEATH"
### Bugfixes / Tweaks
- Added comment to mob-scaling config section to inform users about TOML bug
- Added Compound filtering to NBT configs (eg. "foo{"bar":1}.baz")
- Added config options for inventory stats button positioning
- Fixed pmmo.dat bloat (includes feature to clean up old files)
- Fixed vein marker showing veinable air after using
- Fixed default jump max boost to be actually noticable
- Fixed skill groups not distributing XP to member skills
- Fixed skill groups not being detected by certain events
- Fixed 0% bonuses showing in tooltips 