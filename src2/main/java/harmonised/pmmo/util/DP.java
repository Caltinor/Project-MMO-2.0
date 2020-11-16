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
}
