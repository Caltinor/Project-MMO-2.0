package harmonised.pmmo.commands;

import java.util.*;

import javax.annotation.Nullable;

import harmonised.pmmo.config.FConfig;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.network.MessageUpdatePlayerNBT;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.util.DP;

import harmonised.pmmo.util.NBTHelper;
import harmonised.pmmo.util.XP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CheckStatCommand extends CommandBase
{
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public String getName()
    {
        return "checkStat";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if( args.length == 1 )
            return getListOfStringsMatchingLastWord( args, server.getOnlinePlayerNames() );
        else if( args.length == 2 )
            return PmmoCommand.skillCompletions;

        return new ArrayList<>();
    }

    @Override
    public List<String> getAliases()
    {
        return new ArrayList<>();
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return null;
    }

    @Override
    public void execute( MinecraftServer server, ICommandSender sender, String[] args ) throws CommandException
    {
        EntityPlayerMP player = null;
        try
        {
            player = CommandBase.getCommandSenderAsPlayer( sender );
        }
        catch( PlayerNotFoundException e )
        {
            //Not player, it's fine
        }

        if( args.length > 0 )
        {
            EntityPlayerMP target = null;
            try
            {
                target = getPlayer( server, sender, args[0] );
            }
            catch( PlayerNotFoundException e )
            {
                PmmoCommand.reply( player, new TextComponentTranslation( "pmmo.invalidPlayer", args[0] ).setStyle( XP.textStyle.get( "red" ) ) );
                return;
            }

            if( args.length > 1 )
            {
                String skill = args[1];
                double level = 1;

                if( args[1].toLowerCase().equals( "power" ) )
                {
                    level = XP.getPowerLevel( target.getUniqueID() );
                    PmmoCommand.reply( player, new TextComponentTranslation( "pmmo.playerLevelDisplay", target.getDisplayName().getUnformattedText(), (level % 1 == 0 ? (int) Math.floor(level) : DP.dp(level)), new TextComponentTranslation( "pmmo.power" ).setStyle( XP.textStyle.get( "cyan" ) ) ) );
                }
                else
                {
                    level = Skill.getLevelDecimal( skill, target.getUniqueID() );
                    PmmoCommand.reply( player, new TextComponentTranslation( "pmmo.playerLevelDisplay", target.getDisplayName().getUnformattedText(), (level % 1 == 0 ? (int) Math.floor(level) : DP.dp(level)), new TextComponentTranslation( "pmmo." + skill ).setStyle( Skill.getSkillStyle( skill ) ) ) );
                }

                //EXTRA INFO
                switch( skill )
                {
                    case "fishing":
                        double fishPoolBaseChance = FConfig.fishPoolBaseChance;
                        double fishPoolChancePerLevel = FConfig.fishPoolChancePerLevel;
                        double fishPoolMaxChance = FConfig.fishPoolMaxChance;
                        double fishPoolChance = fishPoolBaseChance + fishPoolChancePerLevel * level;
                        if( fishPoolChance > fishPoolMaxChance )
                            fishPoolChance = fishPoolMaxChance;

                        PmmoCommand.reply( player, new TextComponentTranslation( "pmmo.fishPoolChance", DP.dp( fishPoolChance )  ).setStyle( Skill.getSkillStyle( skill ) ) );
                        break;
                }
            }
            else
                PmmoCommand.reply( player, new TextComponentTranslation( "pmmo.missingNextArgument" ).setStyle( XP.textStyle.get( "red" ) ) );
        }
        else
            PmmoCommand.reply( player, new TextComponentTranslation( "pmmo.missingNextArgument" ).setStyle( Skill.getSkillStyle( "red" ) ) );
    }
}
