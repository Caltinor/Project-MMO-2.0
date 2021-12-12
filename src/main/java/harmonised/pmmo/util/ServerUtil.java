package harmonised.pmmo.util;

import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;

import java.util.*;

public class ServerUtil
{
    private static boolean serverSide = false;
    private static MinecraftServer server;
    private static final Map<ResourceLocation, ServerLevel> worlds = new HashMap<>();
    private static long overworldTime = 0;

    public static ServerLevel overworld;
    public static ServerLevel nether;
    public static ServerLevel end;

    public static void init(MinecraftServer server)
    {
        serverSide = true;
        ServerUtil.server = server;
        worlds.clear();
        for(ServerLevel world : server.getAllLevels())
        {
            worlds.put(XP.getDimResLoc(world), world);
        }
        overworld = getWorld(ServerLevel.OVERWORLD.location());
        nether = getWorld(ServerLevel.NETHER.location());
        end = getWorld(ServerLevel.END.location());
    }

    public static void tick(TickEvent.WorldTickEvent event)
    {
        overworldTime = event.world.getGameTime();
    }

    public static long getGameTime()
    {
        return overworldTime;
    }

    public static List<ServerPlayer> getPlayers()
    {
        return server == null ? Collections.emptyList() : server.getPlayerList().getPlayers();
    }

    public static ServerLevel getWorld(ResourceLocation dimResLoc)
    {
        return worlds.get(dimResLoc);
    }

    public static void sendMsgToAll(Component msg, ChatType type)
    {
        sendMsg(server.getPlayerList().getPlayers(), msg, type);
    }

    public static void sendMsg(Collection<ServerPlayer> players, Component msg, ChatType type)
    {
        for(ServerPlayer player : players)
            player.sendMessage(msg, type, player.getUUID());
    }
}
