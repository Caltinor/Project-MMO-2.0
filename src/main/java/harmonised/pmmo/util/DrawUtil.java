package harmonised.pmmo.util;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DrawUtil
{
    public static Set<BlockPos> getCircle(int r)
    {
        Set<BlockPos> posSet = new HashSet<>();
        int x = 0;
        int d = 1-r;
        int y = r;

        do
        {
            posSet.add(new BlockPos(+x, 0, +y));
            posSet.add(new BlockPos(+x, 0, -y));
            posSet.add(new BlockPos(-x, 0, +y));
            posSet.add(new BlockPos(-x, 0, -y));

            posSet.add(new BlockPos(+y, 0, +x));
            posSet.add(new BlockPos(+y, 0, -x));
            posSet.add(new BlockPos(-y, 0, +x));
            posSet.add(new BlockPos(-y, 0, -x));

            x++;
            if(d < 0)
            {
                d += 2*x+2;
            }
            else
            {
                y--;
                d += 2*(x-y)+1;
            }
        }
        while(x < y);

        posSet.add(new BlockPos(+x, 0, +y));
        posSet.add(new BlockPos(+x, 0, -y));
        posSet.add(new BlockPos(-x, 0, +y));
        posSet.add(new BlockPos(-x, 0, -y));
        return posSet;
    }

    public static Set<BlockPos> getCircleSolid(int r)
    {
        Set<BlockPos> posSet = new HashSet<>();
        List<Integer> lengths = getCircleLengths(r);
        int line = 0;
        for(int length : lengths)
        {
            for(int z = 0; z <= length; z++)
            {
                posSet.add(new BlockPos(+line, 0, +z));
                posSet.add(new BlockPos(+line, 0, -z));
                posSet.add(new BlockPos(-line, 0, +z));
                posSet.add(new BlockPos(-line, 0, -z));

                posSet.add(new BlockPos(+z, 0, +line));
                posSet.add(new BlockPos(+z, 0, -line));
                posSet.add(new BlockPos(-z, 0, +line));
                posSet.add(new BlockPos(-z, 0, -line));
            }
            line++;
        }
        return posSet;
    }

    public static Set<BlockPos> getSphereSolid(int r)
    {
        Set<BlockPos> posSet = new HashSet<>();
        int i = 0;
        List<Integer> sphereRadii = getSphereRadii(r);
        System.out.println(sphereRadii);
        for(int radius : sphereRadii)
        {
            for(BlockPos pos : getCircleSolid(radius))
            {
                posSet.add(pos.down(r - i));
            }
            i++;
        }
        return posSet;
    }

    public static List<Integer> getSphereRadii(int r)
    {
        List<Integer> fourthRadii = new ArrayList<>();
        List<Integer> allY = new ArrayList<>();
        int x = 0;
        int d = 1-r;
        int y = r;

        int oldY = y;
        boolean corner = false;

        do
        {
            x++;
            if(d < 0)
            {
                d += 2*x+2;
            }
            else
            {
                y--;
                d += 2*(x-y)+1;
            }

            if(y != oldY)
                fourthRadii.add(x-1);
            oldY = y;
            if(x == y)
                corner = true;
            else
                allY.add(y);
        }
        while(x < y);
        if(corner)
            fourthRadii.add(++x);

        List<Integer> reversedAllY = Lists.reverse(allY);
        reversedAllY.add(reversedAllY.get(reversedAllY.size()-1));
        fourthRadii.addAll(reversedAllY);

        return fourthRadii;
    }

    public static List<Integer> getCircleLengths(int r)
    {
        List<Integer> halfLengths = new ArrayList<>();
        int x = 0;
        int d = 1-r;
        int y = r;

        do
        {
            halfLengths.add(y);
            x++;
            if(d < 0)
            {
                d += 2*x+2;
            }
            else
            {
                y--;
                d += 2*(x-y)+1;
            }
        }
        while(x < y);
        return halfLengths;
    }

    public static void drawToWorld(World world, BlockPos offset, Set<BlockPos> posSet, BlockState state)
    {
        for(BlockPos pos : posSet)
        {
            world.setBlockState(new BlockPos(offset.getX() + pos.getX(), offset.getY() + pos.getY(), offset.getZ() + pos.getZ()), state);
        }
    }
    public static void drawToWorld(World world, BlockPos offset, Set<BlockPos> posSet, BlockState state, Direction direction)
    {
        switch(direction)
        {
            case UP:
            case DOWN:
                drawToWorld(world, offset, posSet, state);
                break;

            case EAST:
            case WEST:

                break;

            case NORTH:
            case SOUTH:

                break;
        }
    }
}
