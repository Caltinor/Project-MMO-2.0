# Changelog

## [1.21.1-2.8.37] - 2026-01-20
This update completely replaces and revamps the Glossary.  Here is the breakdown of changes:

# Improvements
- All entries now display on one screen removing the need to change filters to cycle through object or feature types
- images, colors, and spacers are used to break up the text blob and make it easier on the eyes
- Glossary is now full screen and has a collapsible filter panel to improve readability on all GUI scales
- Glossary now Caches on first load so as not to lag machines when opening the glossary repeatedly
  - *Note: data reloads and opening with targeted blocks/entities/locations will reset this caching*
- Replaced PMMO button in inventory with a left-side collapsible panel containing the same skill information.

# Additions
- Player-relevant information from the server configs is now shown in the glossary
    - [Server]: salvage block, xp from player sources, level formula and max level, skill modifiers, party settings, mob scaling ratios, and global vein settings
    - [Skills] max level, skill groupings, afk exemption
    - [AntiCheese] settings per skill and anti-cheese type
- Search bar to type in names/IDs of desired objects

# Removed
- PMMO button from inventory (see above detail about side panel)

### Minor Version Changes
- Perk implementations have changed
  - registration parameters for description and status lines have been removed
  - a separate API call now registers a `PerkRenderer` for custom displays.  *Note: this is an optional call since there is a default implementation.  However, this only shows common elements, not specific perk behavior*
  - 

