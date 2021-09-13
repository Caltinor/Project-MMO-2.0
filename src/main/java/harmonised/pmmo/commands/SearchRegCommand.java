package harmonised.pmmo.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SearchRegCommand
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static void append( String input, StringBuilder listOut, StringBuilder listOutExtra, StringBuilder listOutForBuilder )
    {
        listOut.append(input).append("\n");
        listOutExtra.append("\"").append(input).append("\": { \"info\": value },\n");
        listOutForBuilder.append("addData( \"dataType\", \"").append(input).append("\", { \"info\": value } );\n");
    }

    public static int execute( CommandContext<CommandSourceStack> context ) throws CommandRuntimeException
    {
        String query = StringArgumentType.getString( context, "search query" );
        String type = StringArgumentType.getString( context, "type" );
        StringBuilder listOut = new StringBuilder("PMMO DEBUG SEARCH RESULTS:\n");
        StringBuilder listOutExtra = new StringBuilder("PMMO DEBUG SEARCH RESULTS:\n");
        StringBuilder listOutForBuilder = new StringBuilder();

        switch( type.toLowerCase() )
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

            case "block":
                for( Block item : ForgeRegistries.BLOCKS )
                {
                    String regName = item.getRegistryName().toString();
                    if( regName.contains( query ) )
                    {
                        append( regName, listOut, listOutExtra, listOutForBuilder );
                    }
                }
                break;

            case "biome":
                for( Biome item : ForgeRegistries.BIOMES )
                {
                    String regName = item.getRegistryName().toString();
                    if( regName.contains( query ) )
                    {
                        append( regName, listOut, listOutExtra, listOutForBuilder );
                    }
                }
                break;

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

            case "potioneffect":
                for( Potion item : ForgeRegistries.POTIONS )
                {
                    String regName = item.getRegistryName().toString();
                    if( regName.contains( query ) )
                    {
                        append( regName, listOut, listOutExtra, listOutForBuilder );
                    }
                }
                break;

            case "entity":
                for( EntityType<?> item : ForgeRegistries.ENTITIES )
                {
                    String regName = item.getRegistryName().toString();
                    if( regName.contains( query ) )
                    {
                        append( regName, listOut, listOutExtra, listOutForBuilder );
                    }
                }
                break;
        }

        LOGGER.info( listOut.toString() );
        LOGGER.info( listOutExtra.toString() );
        LOGGER.info( listOutForBuilder.toString() );

        return 1;
    }
}
