package harmonised.pmmo.events;

import com.mojang.authlib.GameProfile;
import harmonised.pmmo.skills.PMMOFakePlayer;
import harmonised.pmmo.skills.XP;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.BlockEvent;

import java.util.*;

public class WorldTickHandler
{
    private static Map<PlayerEntity, BlockEvent.BreakEvent> scheduledVein;
    private static Map<PlayerEntity, ArrayList<BlockPos>> veinSet;
    private static Map<PlayerEntity, Integer> veinMatches;

    public static void refreshVein()
    {
        scheduledVein = new HashMap<>();
        veinSet = new HashMap<>();
        veinMatches = new HashMap<>();
    }

    public static void handleWorldTick( TickEvent.WorldTickEvent event )
    {
        for( PlayerEntity player : event.world.getServer().getPlayerList().getPlayers() )
        {
            for( int i = 0; i < 1; i++ )
            {
                if( scheduledVein.containsKey( player ) )
                {
                    if( veinSet.get( player ).size() > 0 )
                    {
                        BlockEvent.BreakEvent breakEvent = scheduledVein.get(player);
                        BlockPos veinPos = veinSet.get( player ).get(0);
                        veinSet.get( player ).remove( 0 );

                        World world = breakEvent.getWorld().getWorld();
                        BlockState veinState = world.getBlockState(veinPos);

                        BlockEvent.BreakEvent veinEvent = new BlockEvent.BreakEvent(world, veinPos, veinState, new PMMOFakePlayer((ServerWorld) world, new GameProfile(BlockBrokenHandler.fakePlayerUUID, "PMMOFakePlayer"), player));
                        MinecraftForge.EVENT_BUS.post(veinEvent);
                        if (!veinEvent.isCanceled())
                            world.destroyBlock(veinPos, true);
                    }
                    else
                    {
                        scheduledVein.remove( player );
                        veinSet.remove( player );
                    }
                }
            }
        }
    }

    public static boolean scanBlock( World world, Block block, BlockPos blockPos, int radius )
    {
        for( int i = -radius; i < radius; i++ )
        {
            for( int j = -radius; j < radius; j++ )
            {
                for( int k = -radius; k < radius; k++ )
                {
                    if( world.getBlockState( blockPos.up(i).north(j).east(k) ).getBlock().equals( block ) )
                        return true;
                }
            }
        }

        return false;
    }

    public static boolean hasAdjacentBlock( World world, Block block, BlockPos pos )
    {
        for( int i = -1; i <= 1; i++ )
        {
            for( int j = -1; j <= 1; j++ )
            {
                for( int k = -1; k <= 1; k++ )
                {
                    if( world.getBlockState( pos.up(i).north(j).east(k) ).getBlock().equals( block ) )
                        return true;
                }
            }
        }

        return false;
    }

    public static boolean posIsAdjacent( BlockPos pos1, BlockPos pos2 )
    {
        for( int i = -1; i <= 1; i++ )
        {
            for( int j = -1; j <= 1; j++ )
            {
                for( int k = -1; k <= 1; k++ )
                {
                    if( pos1.up(i).north(j).east(k).equals( pos2 ) )
                        return true;
                }
            }
        }

        return false;
    }

    public static void scheduleVein(PlayerEntity player, BlockEvent.BreakEvent event )
    {
        veinMatches.remove( player );
        veinMatches.put( player, 0 );

        ArrayList<BlockPos> blockPosArrayList = scanNearbyMatchesVein( event );

        if( blockPosArrayList.size() > 0 )
        {
            scheduledVein.put( player, event );
            veinSet.put( player, blockPosArrayList );
        }
    }

    private static boolean addMatch( World world, BlockPos tempPos, Block originBlock, PlayerEntity player, ArrayList<BlockPos> setIn )
    {
        BlockState tempState = world.getBlockState( tempPos );
        if( tempState.getBlock().equals( originBlock ) )
        {
            for( BlockPos checkPos : setIn )
            {
                if( posIsAdjacent( tempPos, checkPos ) )
                {
                    CompoundNBT prefsTag = XP.getPreferencesTag( player );

                    if( prefsTag.getInt( "veinLeft" ) <= 0 )
                        return false;

                    setIn.add( tempPos );
                    veinMatches.replace( player, veinMatches.get( player ) + 1 );
                    prefsTag.putInt( "veinLeft", prefsTag.getInt( "veinLeft" ) - 1 );
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean doX( BlockEvent.BreakEvent event, int offset, ArrayList<BlockPos> setIn )
    {
        boolean matched = false;

        World world = event.getWorld().getWorld();
        BlockPos originPos = event.getPos();
        Block originBlock = event.getState().getBlock();
        BlockPos tempPos;
        PlayerEntity player = event.getPlayer();

        int size = offset;
        if( size < 0 )
            size = -size;

        for( int i = 0; i <= size; i++ )
        {
            for( int j = 0; j < size; j++ )
            {
                tempPos = originPos.east(offset).north(i).up(j);
                if( addMatch( world, tempPos, originBlock, player, setIn ) )
                {
                    veinMatches.replace( player, veinMatches.get( player ) );
                    matched = true;
                }
            }
            for( int j = 0; j < size; j++ )
            {
                tempPos = originPos.east(offset).north(i).up(-j);
                if( addMatch( world, tempPos, originBlock, player, setIn ) )
                {
                    veinMatches.replace( player, veinMatches.get( player ) );
                    matched = true;
                }
            }
        }
        for( int i = 0; i <= size; i++ )
        {
            for( int j = 0; j < size; j++ )
            {
                tempPos = originPos.east(offset).north(-i).up(j);
                if( addMatch( world, tempPos, originBlock, player, setIn ) )
                {
                    veinMatches.replace( player, veinMatches.get( player ) );
                    matched = true;
                }
            }
            for( int j = 0; j < size; j++ )
            {
                tempPos = originPos.east(offset).north(-i).up(-j);
                if( addMatch( world, tempPos, originBlock, player, setIn ) )
                {
                    veinMatches.replace( player, veinMatches.get( player ) );
                    matched = true;
                }
            }
        }

        return false;
    }

    private static boolean doY( BlockEvent.BreakEvent event, int offset, ArrayList<BlockPos> setIn )
    {
        boolean matched = false;

        World world = event.getWorld().getWorld();
        BlockPos originPos = event.getPos();
        Block originBlock = event.getState().getBlock();
        BlockPos tempPos;
        PlayerEntity player = event.getPlayer();

        int size = offset;
        if( size < 0 )
            size = -size;

        for( int i = 0; i <= size; i++ )
        {
            for( int j = 0; j <= size; j++ )
            {
                tempPos = originPos.up(offset).north(i).east(j);
                if( addMatch( world, tempPos, originBlock, player, setIn ) )
                    matched = true;
            }
            for( int j = 0; j <= size; j++ )
            {
                tempPos = originPos.up(offset).north(i).east(-j);
                if( addMatch( world, tempPos, originBlock, player, setIn ) )
                    matched = true;
            }
        }
        for( int i = 0; i <= size; i++ )
        {
            for( int j = 0; j <= size; j++ )
            {
                tempPos = originPos.up(offset).north(-i).east(j);
                if( addMatch( world, tempPos, originBlock, player, setIn ) )
                    matched = true;
            }
            for( int j = 0; j <= size; j++ )
            {
                tempPos = originPos.up(offset).north(-i).east(-j);
                if( addMatch( world, tempPos, originBlock, player, setIn ) )
                    matched = true;
            }
        }
        return matched;
    }

    private static boolean doZ( BlockEvent.BreakEvent event, int offset, ArrayList<BlockPos> setIn )
    {
        boolean matched = false;

        World world = event.getWorld().getWorld();
        BlockPos originPos = event.getPos();
        Block originBlock = event.getState().getBlock();
        BlockPos tempPos;
        PlayerEntity player = event.getPlayer();

        int size = offset;
        if( size < 0 )
            size = -size;

        for( int i = 0; i <= size; i++ )
        {
            for( int j = 0; j < size; j++ )
            {
                tempPos = originPos.up(i).north(offset).east(j);
                if( addMatch( world, tempPos, originBlock, player, setIn ) )
                    matched = true;
            }
            for( int j = 0; j < size; j++ )
            {
                tempPos = originPos.up(i).north(offset).east(-j);
                if( addMatch( world, tempPos, originBlock, player, setIn ) )
                    matched = true;
            }
        }
        for( int i = 0; i <= size; i++ )
        {
            for( int j = 0; j < size; j++ )
            {
                tempPos = originPos.up(-i).north(offset).east(j);
                if( addMatch( world, tempPos, originBlock, player, setIn ) )
                    matched = true;
            }
            for( int j = 0; j < size; j++ )
            {
                tempPos = originPos.up(-i).north(offset).east(-j);
                if( addMatch( world, tempPos, originBlock, player, setIn ) )
                    matched = true;
            }
        }
        return matched;
    }

    private static ArrayList<BlockPos> scanNearbyMatchesVein( BlockEvent.BreakEvent event )
    {
        ArrayList<BlockPos> matches = new ArrayList<>();
        matches.add( event.getPos() );
        int maxSize = 3;
        boolean x1, x2, y1, y2, z1, z2;
        x1 = x2 = y1 = y2 = z1 = z2 = true;

        for( int offset = 1; offset <= maxSize; offset++ )
        {
            //y+

            if( !doY( event, offset, matches ) )
                y1 = false;
            //

            //x+
            if( !doX( event, offset, matches ) )
                x1 = false;
            //

            //z+
            if( !doZ( event, offset, matches ) )
                z1 = false;
            //

            //x-
            if( !doX( event, -offset, matches ) )
                x2 = false;
            //

            //z-
            if( !doZ( event, -offset, matches ) )
                z2 = false;
            //

            //y-
            if( !doY( event, -offset, matches ) )
                y2 = false;
            //

            if( !x1 && !x2 && !y1 && !y2 && !z1 && !z2 )
                break;
        }

        return matches;
    }
}
