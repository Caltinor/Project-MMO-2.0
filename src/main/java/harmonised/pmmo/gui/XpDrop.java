package harmonised.pmmo.gui;

import harmonised.pmmo.skills.Skill;

public class XpDrop
{
	public int age;
	public String skill;
	public double Y, startXp, gainedXp;
	public boolean skip;
	
	XpDrop( int age, double Y, String skill, double startXp, double gainedXp, boolean skip )
	{
		this.age = age;
		this.Y = Y;
		this.skill = skill;
		this.startXp = startXp;
		this.gainedXp = gainedXp;
		this.skip = skip;
	}
}
