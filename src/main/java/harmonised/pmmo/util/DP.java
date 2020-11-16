package harmonised.pmmo.util;

import java.util.Locale;

public class DP
{
	public static String dprefix( Float input )
	{
		return dprefix( input.doubleValue() );
	}

	public static String dprefix( Double input )
	{
		char prefix = ' ';
		if( input >= 10000 && input < 1000000 )
		{
			input /= 1000;
			prefix = 'k';
		}
		else if( input >= 1000000 && input < 1000000000 )
		{
			input /= 1000000;
			prefix = 'm';
		}
		else if( input >= 1000000000 )
		{
			input /= 1000000000;
			prefix = 'b';
		}

		if( prefix == ' ' )
			return String.format( Locale.ENGLISH , "%.2f", input );
		else
			return String.format( Locale.ENGLISH , "%.3f", input ) + prefix;
	}

	public static String dp( Float input )
	{
		return dp( input.doubleValue() );
	}

	public static String dp( Double input )
	{
		return String.format( Locale.ENGLISH , "%.2f", input );
	}

	public static String dpCustom( Double input, int decPlaces )
	{
		return String.format( Locale.ENGLISH , "%." + decPlaces + "f", input );
	}

	public static String dpSoft( Double input )
	{
		if( input % 1 == 0 )
			return String.format( Locale.ENGLISH , "%.0f", input );
		if( (input * 10) % 1 == 0 )
			return String.format( Locale.ENGLISH , "%.1f", input );
		else
			return String.format( Locale.ENGLISH , "%.2f", input );
	}

	public static double mapCapped(double input, double inLow, double inHigh, double outLow, double outHigh )
	{
		if( input < inLow )
			input = inLow;
		if( input > inHigh )
			input = inHigh;

		if( inLow == inHigh || outLow == outHigh )
			return outHigh;
		return ( (input - inLow) / (inHigh - inLow) ) * (outHigh - outLow) + outLow;
	}
}