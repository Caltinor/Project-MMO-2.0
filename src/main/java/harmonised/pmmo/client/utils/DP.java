package harmonised.pmmo.client.utils;

import java.util.Locale;

public class DP
{	
	private static char[] prefix = {' ','k','m','g','t','p','e','z','y'};
	public static String dprefix(long input)
	{
		int length = String.valueOf(input).length();
		int prefixId = length / 3;
		prefixId = length % 3 == 0 ? prefixId - 1 : prefixId;
		input /= Math.pow(10, (length - 3));
		Double output = Double.valueOf(input);	
		int decimalPlaces = 3 - (length - (length/3) * 3);
		decimalPlaces = decimalPlaces == 3 ? 0 : decimalPlaces;
		output /= Math.pow(10, decimalPlaces);
		
		return String.format(Locale.ENGLISH, "%."+decimalPlaces+"f", output) + prefix[prefixId];
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
