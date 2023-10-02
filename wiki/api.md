[Home](home.md)

# Project MMO API
Project MMO provides a lot of features to addon makers to add custom logic to many of PMMO's more rigid features.  The API has been expanded as users have expressed specific needs.  If you are developing an addon and need access that you would otherwise only get by accessing internals directly, contact a mod author to discuss having your hook added.

### APIUtils
All Hooks can be found in [APIUtils](../src/main/java/harmonised/pmmo/api/APIUtils.java).  Within this class, you will find methods to:
- get/set levels and xp values
- get xp and requirement skill maps
- set configurations via code (overriding data)
- [register custom logic for reqs, xp gains, and bonuses](api/overrides.md)
- register custom logic for telling pmmo what the player skill level is
- [register nested event listeners that interact with pmmo's logic](api/events.md)
- register [custom perks](api/perks.md)

### harmonised.pmmo.api.enums Package
This package contains objects used in the above calls to specify the game object your custom behavior applies to.

### Custom Forge Events
Project MMO provides 4 custom events that it listens to by default.  These can be used to provide compatibility with pmmo.

| event            | purpose                                                                                 |
|:-----------------|:----------------------------------------------------------------------------------------|
| EnchantEvent     | When an item is enchanted                                                               |
| FurnaceBurnEvent | Pmmo's location-sensitive smelt event                                                   |
| SalvageEvent     | currently unused.                                                                       |
| XpEvent          | Not to be invoked, but listened to.  This is Pmmo's internal event for when XP changes. |

### harmonised.pmmo.api.perks
See the full [Perks API page](api/perks.md) for implementing perks.  For using perks as a user, see the [Perks Configuration page](core/perks.md)

[Home](home.md)