[Home](../home.md)

This page details the configuration details for jsons placed under `/data/namespace/advancements/`.

## Advancements

# Datapack JSON Information

PMMO adds a new trigger that advancements can utilize to give the player advancements for leveling up their skills.
This new trigger is `pmmo:skill_up`, and it can be used like this:

```json
"criteria": {
    "requirement": {
        "trigger": "pmmo:skill_up",
        "conditions": {
        "player": [
            {
                "condition":"pmmo:skill_level",
                "skill":"agility",
                "level_min": 5,
                "level_max": 20
            }
        ]
        }
    }
},
```

This criteria attached to an advancement would trigger when the player reaches level 5 in agility. You can also have multiple skill requirements in order for the advancement to activate:

```json
"criteria": {
        "requirement": {
            "trigger": "pmmo:skill_up",
            "conditions": {
            "player": [
                {"condition":"pmmo:skill_level",
                "skill":"agility",
                "level_min": 3,
                "level_max": 20
                },
                {"condition":"pmmo:skill_level",
                "skill":"swimming",
                "level_min": 3,
                "level_max": 20
                }
            ]
            }
        }
    }
```

This criteria requires the player to be level 3 in agility and swimming in order to trigger. For more information on creating advancements refer to the [minecraft wiki](https://minecraft.fandom.com/wiki/Advancement/JSON_format). There is also [this](https://misode.github.io/advancement/) tool that will do a lot of the setup for you for all your datapack needs, advancements included.

# What this can do.

MMO games are heavily achievement based and putting in long hours and dedication into your character progression is a great place to give some rewards.

At a baseline an advancement would allow a player to feel a sense of accomplishment in unlocking the plaque or advancement itself. This is enough of a reason for many achievement hunter style players, which are common place in MMO's. Further this allows you to give rewards as well, like unlocking recipes or giving the player loot or experience. You could also do things like give special colored armor to your players that can't be obtained any other way.

Unlocking recipes based on certain skill level advancements can be an interesting community and economy builder, encouraging the community to come together to obtain items. If only a higher level builder can craft specific decorations or functional pieces, the players would need to come to them for their services. A high level mage might be able to craft a better version of a wand, or an engineer able to build attachments for a rocket or upgrades for a backpack. Gating some of the higher level items behind players who have achieved skills high enough to craft them can make for interesting community interaction and drive economy.

The real special sauce here is in using functions as a reward. Functions can integrate into other mods and vanilla minecraft itself in extremely interesting and almost limitless ways. Things like:

-Enabling or disabling features (ex: Parcool parkour abilities can be enabled and disabled through advancement functions, tying the unlocking of new parkour abilities through an agility skill level up on top of the pmmo perks is just awesome and rewarding).
-Unlocking titles for players to equip in a title mod (I'm actually not sure if this exists but I'd bet so) So they can equip that they've mastered a skill.
-Changing stages using game stages.

to name just a very few. Advancements with functions that can modify the game or player in countless ways and opens up a lot of options for you to customize your game.

[Home](../home.md)