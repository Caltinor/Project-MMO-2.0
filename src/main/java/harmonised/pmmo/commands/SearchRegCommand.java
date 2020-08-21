package harmonised.pmmo.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import harmonised.pmmo.util.LogHandler;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.potion.Effect;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;

public class SearchRegCommand
{
    public static void append( String input, StringBuilder listOut, StringBuilder listOutExtra, StringBuilder listOutForBuilder )
    {
        listOut.append(input).append("\n");
        listOutExtra.append("\"").append(input).append("\": { \"info\": value },\n");
        listOutForBuilder.append("addData( \"dataType\", \"").append(input).append("\", { \"info\": value } );\n");
    }

    public static int execute( CommandContext<CommandSource> context ) throws CommandException
    {
        String query = StringArgumentType.getString( context, "search query" );
        String type = StringArgumentType.getString( context, "type" );
        StringBuilder listOut = new StringBuilder("PMMO DEBUG SEARCH RESULTS:\n");
        StringBuilder listOutExtra = new StringBuilder("PMMO DEBUG SEARCH RESULTS:\n");
        StringBuilder listOutForBuilder = new StringBuilder();

        switch( type )
        {
            case "item":
                for( Item item : ForgeRegistries.ITEMS )
                {
                    String regName = item.getRegistryName().toString();
                    if( regName.contains( query ) )
                    {
                        append( regName, listOut, listOutExtra, listOutForBuilder );
                    }
                }
                break;

//            case "biome":
//                for( Biome item : ForgeRegistries.BIOMES )
//                {
//                    String regName = item.getRegistryName().toString();
//                    if( regName.contains( query ) )
//                    {
//                        append( regName, listOut, listOutExtra, listOutForBuilder );
//                    }
//                }
//                break;

            case "enchant":
                for( Enchantment item : ForgeRegistries.ENCHANTMENTS )
                {
                    String regName = item.getRegistryName().toString();
                    if( regName.contains( query ) )
                    {
                        append( regName, listOut, listOutExtra, listOutForBuilder );
                    }
                }
                break;

            case "potionEffect":
                for( Effect item : ForgeRegistries.POTIONS )
                {
                    String regName = item.getRegistryName().toString();
                    if( regName.contains( query ) )
                    {
                        append( regName, listOut, listOutExtra, listOutForBuilder );
                    }
                }
                break;

            case "entity":
                for( EntityType item : ForgeRegistries.ENTITIES )
                {
                    String regName = item.getRegistryName().toString();
                    if( regName.contains( query ) )
                    {
                        append( regName, listOut, listOutExtra, listOutForBuilder );
                    }
                }
                break;
        }

        LogHandler.LOGGER.info( listOut.toString() );
        LogHandler.LOGGER.info( listOutExtra.toString() );
        LogHandler.LOGGER.info( listOutForBuilder.toString() );

        return 1;
    }
}
