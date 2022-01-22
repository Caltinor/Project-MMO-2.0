package harmonised.pmmo.client.utils;

import java.util.Locale;

public class DP
{	
	public static String dprefix(long input)
	{
		char prefix = ' ';

		if(input >= 1E+3)
		{
			if(input >= 1E+6)
			{
				if(input >= 1E+9)
				{
					if(input >= 1E+12)
					{
						if(input >= 1E+15)
						{
							if(input >= 1E+18)
							{
								if(input >= 1E+21)
								{
									if(input >= 1E+24)
									{
										if(input >= 1E+27)
										{
											return String.valueOf(input);
										}
										else
										{
											input /= 1E+24;
											prefix = 'y';
										}
									}
									else
									{
										input /= 1E+21;
										prefix = 'z';
									}
								}
								else
								{
									input /= 1E+18;
									prefix = 'e';
								}
							}
							else
							{
								input /= 1E+15;
								prefix = 'p';
							}
						}
						else
						{
							input /= 1E+12;
							prefix = 't';
						}
					}
					else
					{
						input /= 1E+9;
						prefix = 'g';
					}
				}
				else
				{
					input /= 1E+6;
					prefix = 'm';
				}
			}
			else
			{
				input /= 1E+3;
				prefix = 'k';
			}
		}
		
		if(prefix == ' ')
			return String.format(Locale.ENGLISH, "%.2f", input);
		else
			return String.format(Locale.ENGLISH, "%.3f", input) + prefix;
	}
	
	public static String dp(Float input)
	{
		return dp(input.doubleValue());
	}
	
	public static String dp(Double input)
	{
		return String.format(Locale.ENGLISH, "%.2f", input);
	}

	public static String dpCustom(Double input, int decPlaces)
	{
		return String.format(Locale.ENGLISH, "%." + decPlaces + "f", input);
	}

	public static String dpSoft(double input)
	{
		if(input % 1 == 0)
			return String.format(Locale.ENGLISH, "%.0f", input);
		if((input * 10) % 1 == 0)
			return String.format(Locale.ENGLISH, "%.1f", input);
		else
			return String.format(Locale.ENGLISH, "%.2f", input);
	}

	public static String dpSoft(float input)
	{
		if(input % 1 == 0)
			return String.format(Locale.ENGLISH, "%.0f", input);
		if((input * 10) % 1 == 0)
			return String.format(Locale.ENGLISH, "%.1f", input);
		else
			return String.format(Locale.ENGLISH, "%.2f", input);
	}
}
