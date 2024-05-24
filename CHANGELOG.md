# Changelog

## [1.20.1-1.3.22] - 2024-05-24
### Bugfixes / Tweaks
- Fixed 5+ players breaking vein/perks on servers
- Fixed tags in damage boost perks crashing the glossary
- Fixed `pmmo:damage_boost` perks not stacking with each other
- Added ability to use an empty list in `applies_to` for universal damage boosts
- Added optional `chance_message` property to all perks which notifies users when a perk activates on chance
- Updated levelup unlocks to cache on first attempt to eliminate level-up lag