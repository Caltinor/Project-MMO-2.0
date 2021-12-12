https://www.curseforge.com/minecraft/mc-mods/project-mmo

///Craft Tweaker Support

Example of Craft Tweaker use with PMMO (May or may not be available for either Minecraft version, you may ask in our Discord if you'd like to make sure of the current state)

`
import crafttweaker.api.data.MapData;
import crafttweaker.api.data.IData;
import mods.pmmo.ct.Levels;
import crafttweaker.api.events.CTEventManager;
import crafttweaker.api.event.entity.player.MCUseHoeEvent;
`

//Check if the player has met the required stats
`
CTEventManager.register(new MCUseHoeEvent(event =>
{
	var metLevelsSpecified = Levels.checkLevels(new MapData({"agility": 10 as IData, "farming": 50 as IData}), event.getPlayer());
}));
`

//Award the player levels
`
CTEventManager.register(new MCUseHoeEvent(event =>
{
	Levels.awardLevels(new MapData({"agility": 10 as IData, "farming": 3 as IData}), event.getPlayer());
}));
`

//Award the player Xp (the extra boolean is ignoreBonuses, such as xp boosting items, or biomes)
`
CTEventManager.register(new MCUseHoeEvent(event =>
{
	Levels.awardXp(new MapData({"agility": 10 as IData, "farming": 3 as IData}), event.getPlayer(), true);
}));
`

//Set player levels
`
CTEventManager.register(new MCUseHoeEvent(event =>
{
	Levels.setLevels(new MapData({"agility": 10 as IData, "farming": 3 as IData}), event.getPlayer());
}));
`

//Set player xp
`
CTEventManager.register(new MCUseHoeEvent(event =>
{
	Levels.setXp(new MapData({"agility": 10 as IData, "farming": 3 as IData}), event.getPlayer());
}));
`

This script (.zs) will check for level 10 agility, and level 50 farming.
If the player has all of the stats specified, it will return true
Else, it will return false if any 1 is not met.

///API

Xp.awardXpTrigger is the most flexible way to award people experience, made specifically for API
The xp values can be configured by anyone inside data.json, by the use of a given key, example: "doomweapon.consume.invisible" inside the "trigger_xp" entry of data.json will determine how much xp, and in what skills this action will award when the xp award is triggered from an API

Others:

Skill.getLevel returns level int
Skill.getXp returns xp double
Skill.setLevel sets level double
Skill.setXp sets xp double
Skill.addLevel rewards level double
Skill.addXp rewards xp double
XP.getXp(ResourceLocation item, JType type) returns a Map<String, Double> of the xp/level values stored for that item, in that type

To use the API, include these two lines in your gradle.build

repositories { maven { url "http://dvs1.progwml6.com/files/maven/" } }
dependencies { compileOnly fg.deobf("curse.maven:project-mmo:${version}") }

If you have issues with this, or know what could be wrong, please contact me on Discord, or otherwise!
