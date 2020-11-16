package harmonised.pmmo.gui;

public class XpDrop
{
	public int age;
	public String name;
	public double Y, startXp, gainedXp;
	public boolean skip;
	
	XpDrop( int age, double Y, String name, double startXp, double gainedXp, boolean skip )
	{
		this.age = age;
		this.Y = Y;
		this.name = name;
		this.startXp = startXp;
		this.gainedXp = gainedXp;
		this.skip = skip;
	}
}
