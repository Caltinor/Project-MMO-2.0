# Changelog

## [1.21.1-2.7.32] - 2025-05-08
### Minor Version Changes
- With the addition of MITIGATE_DAMAGE event, the call `APIUtils#registerDamageXpAward` had the `boolean` parameter changed to an `EventType`.
### Bugfixes/Tweaks
- Added `PMMORegistrationEvent` for addons to use when calling `APIUtils#registerXXX`
- Added Rare Drops and Treasure to the glossary
- Added commands `/pmmo admin <player> attribute <refresh/clear` to force attribute perks to reapply or to remove attributes applied by perks.
- Added hint to `admin clear` command to provide skill suggestions
- Fixed default Iron Spellbooks perk typo preventing the perk from functioning
- Added MITIGATE_DAMAGE event which captures damage prevented by armor, absorption, enchantments, and mob_effects.  This works like damage dealt and received events and requires a damage type to specify the xp.  XP can be defined in the server config or on damaging entities.

