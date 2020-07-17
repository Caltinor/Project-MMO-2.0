https://www.curseforge.com/minecraft/mc-mods/project-mmo

I am a new mod dev, so please let me know if the following is not correct:

///Craft Tweaker Support

Example of Craft Tweaker use with PMMO

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
	var metLevelsSpecified = Levels.checkLevels( new MapData( {"agility": 10 as IData, "farming": 50 as IData} ), event.getPlayer() );
}));
`

//Award the player levels
`
CTEventManager.register(new MCUseHoeEvent(event =>
{
	Levels.awardLevels( new MapData( {"agility": 10 as IData, "farming": 3 as IData} ), event.getPlayer() );
}));
`

//Award the player Xp (the extra boolean is ignoreBonuses, such as xp boosting items, or biomes)
`
CTEventManager.register(new MCUseHoeEvent(event =>
{
	Levels.awardXp( new MapData( {"agility": 10 as IData, "farming": 3 as IData} ), event.getPlayer(), true );
}));
`

//Set player levels
`
CTEventManager.register(new MCUseHoeEvent(event =>
{
	Levels.setLevels( new MapData( {"agility": 10 as IData, "farming": 3 as IData} ), event.getPlayer() );
}));
`

//Set player xp
`
CTEventManager.register(new MCUseHoeEvent(event =>
{
	Levels.setXp( new MapData( {"agility": 10 as IData, "farming": 3 as IData} ), event.getPlayer() );
}));
`

This script (.zs) will check for level 10 agility, and level 50 farming.
If the player has all of the stats specified, it will return true
Else, it will return false if any 1 is not met.

///API

I believe I have API support for my mod! You should be able to access my mod's Levels by the methods

(If you want to use my API with customizable-by-user values, please contact me! I have an idea of how that could be done, and that is a new method that takes in an ENUM I provide to specify what you want done, with a Registry Name that you provide)

Skill.SKILLNAME.getLevel( player ) returns level int
Skill.SKILLNAME.getXp( player ) returns xp double
Skill.SKILLNAME.setLevel( player ) sets level double
Skill.SKILLNAME.setXp( player ) sets xp double
Skill.SKILLNAME.addLevel( player ) rewards level double
Skill.SKILLNAME.addXp( player ) rewards xp double

XP.getLevelDecimal (takes in skillName + player, returns level as double, works on client/server)
XP.xpAtLevel (takes in a level as int, returns double xp value)
XP.xpAtLevelDecimal (takes in a level as double, returns double xp value)
XP.getSkillsTag (takes in player, returns PMMO Skills CompoundNBT of all the player's xp values, as doubles)

To use the API, include these two lines in your gradle.build

repositories { maven { url "http://dvs1.progwml6.com/files/maven/" } }
dependencies { compileOnly fg.deobf("curse.maven:project-mmo:${version}") }

If you have issues with this, or know what could be wrong, please contact me on Discord, or otherwise!