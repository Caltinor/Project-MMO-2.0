# Changelog

## [1.18.2-6.2.5] - 2023-10-23
### Hotfix
- Fix qualified compound edge case

## [1.18.2-6.2.4] - 2023-10-18
### Bugfixes / Tweaks
- Added admin command to ignore reqs for targeted player
- Added hover behavior to inventory skill list to show xp to next level
- Added API hooks for obtaining a player's full xp map
- Added potion tracking toggle.  this makes stacking potions work for compat sake at the expense of exploitable potion xp from brewing stands.
- Updated `HEALTH_CHANGED` to have sub-events `HEALTH_INCREASED` and `HEALTH_DECREASED`, which fire in addition to the base event but for their respective HP change direction.
- Updated `pmmo:command` to accept a skill like fireworks do to only fire the command for the specified skill.  only works on the `SKILL_UP` event
- Fixed explosions not respecting BREAK and KILL reqs
- Fixed compound qualifiers not working with paths that included colons
- Fixed `genData withDefaults` not including data from non-items

