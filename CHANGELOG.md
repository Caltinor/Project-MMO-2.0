# Changelog

## [1.21.1-2.7.31] - 2025-05-08
### Minor Version Changes
- With the addition of MITIGATE_DAMAGE event, the call `APIUtils#registerDamageXpAward` had the `boolean` parameter changed to an `EventType`.
### Bugfixes/Tweaks
- Added `PMMORegistrationEvent` for addons to use when calling `APIUtils#registerXXX`
- Added Rare Drops and Treasure to the glossary
- Fixed default Iron Spellbooks perk typo preventing the perk from functioning
- Added MITIGATE_DAMAGE event which captures damage prevented by armor, absorption, enchantments, and mob_effects.  This works like damage dealt and received events and requires a damage type to specify the xp.  XP can be defined in the server config or on damaging entities.

