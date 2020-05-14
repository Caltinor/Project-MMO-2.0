package harmonised.pmmo.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
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
    public static int execute( CommandContext<CommandSource> context ) throws CommandException
    {
        String query = StringArgumentType.getString( context, "search query" );
        String type = StringArgumentType.getString( context, "type" );
        StringBuilder listOut = new StringBuilder("PMMO DEBUG SEARCH RESULTS:\n");
        StringBuilder listOutExtra = new StringBuilder("PMMO DEBUG SEARCH RESULTS:\n");

        switch( type )
        {
            case "item":
                for( Item item : ForgeRegistries.ITEMS )
                {
                    String regName = item.getRegistryName().toString();
                    if( regName.contains( query ) )
                    {
                        listOut.append(regName).append("\n");
                        listOutExtra.append("\"").append(regName).append("\": { \"info\": value },\n");
                    }
                }
                break;

            case "biome":
                for( Biome item : ForgeRegistries.BIOMES )
                {
                    String regName = item.getRegistryName().toString();
                    if( regName.contains( query ) )
                    {
                        listOut.append(regName).append("\n");
                        listOutExtra.append("\"").append(regName).append("\": { \"info\": value },\n");
                    }
                }
                break;

            case "enchant":
                for( Enchantment item : ForgeRegistries.ENCHANTMENTS )
                {
                    String regName = item.getRegistryName().toString();
                    if( regName.contains( query ) )
                    {
                        listOut.append(regName).append("\n");
                        listOutExtra.append("\"").append(regName).append("\": { \"info\": value },\n");
                    }
                }
                break;

            case "potionEffect":
                for( Effect item : ForgeRegistries.POTIONS )
                {
                    String regName = item.getRegistryName().toString();
                    if( regName.contains( query ) )
                    {
                        listOut.append(regName).append("\n");
                        listOutExtra.append("\"").append(regName).append("\": { \"info\": value },\n");
                    }
                }
                break;

            case "entity":
                for( EntityType item : ForgeRegistries.ENTITIES )
                {
                    String regName = item.getRegistryName().toString();
                    if( regName.contains( query ) )
                    {
                        listOut.append(regName).append("\n");
                        listOutExtra.append("\"").append(regName).append("\": { \"info\": value },\n");
                    }
                }
                break;
        }

        System.out.println( listOut.toString() );
        System.out.println( listOutExtra.toString() );

        return 1;
    }
}
