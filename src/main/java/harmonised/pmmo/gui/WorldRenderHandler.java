package harmonised.pmmo.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.gui.WorldXpDrop;
import harmonised.pmmo.network.MessageWorldXp;
import harmonised.pmmo.network.NetworkHandler;
import harmonised.pmmo.pmmo_saved_data.PmmoSavedData;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.XP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

public class WorldRenderHandler
{
    private static long lastTime = System.nanoTime();
    private final static List<WorldXpDrop> xpDrops = new ArrayList<>();

    public static float worldXpDropsSizeMultiplier = (float) ( 0f + Config.forgeConfig.worldXpDropsSizeMultiplier.get() );
    public static float worldXpDropsDecaySpeedMultiplier = (float) ( 0f + Config.forgeConfig.worldXpDropsDecaySpeedMultiplier.get() );
    public static boolean worldXpDropsShowSkill =  Config.forgeConfig.worldXpDropsShowSkill.get();

    @SubscribeEvent
    public void handleWorldRender( RenderWorldLastEvent event )
    {
//        temp1++;
        long currTime = System.nanoTime();
        double d = (currTime - lastTime) / 1000000000D;
        Minecraft mc = Minecraft.getInstance();
        FontRenderer fr = mc.getRenderManager().getFontRenderer();
        MatrixStack stack = event.getMatrixStack();
        IRenderTypeBuffer.Impl buffer = mc.getRenderTypeBuffers().getBufferSource();
        Vector3d pos = mc.gameRenderer.getActiveRenderInfo().getProjectedView();

        stack.push();
        for( int i = xpDrops.size()-1; i >= 0; i-- )
        {
            WorldXpDrop xpDrop = xpDrops.get( i );
            if( xpDrop == null )
                continue;
            if( xpDrop.xp <= 0 )
            {
                xpDrops.remove( i );
                continue;
            }
            stack.push();
            float scale = 0.02f * (float) ( xpDrop.xp / xpDrop.getStartXp() ) * xpDrop.getSize() * worldXpDropsSizeMultiplier;
            Vector3d xpDropPos = xpDrop.getPos();
            int color = xpDrop.getColor();
            String text = "+" + DP.dpSoft( xpDrop.xp );
            if( Config.forgeConfig.worldXpDropsShowSkill.get() )
                text += " " + xpDrop.getSkill();
            float textWidth = fr.getStringWidth( text );
            float textOffset = -textWidth/2;
            stack.translate( xpDropPos.getX() - pos.getX(), xpDropPos.getY() - pos.getY(), xpDropPos.getZ() - pos.getZ() );
//            stack.translate( 20 - pos.getX(), 4 - pos.getY(), 12 - pos.getZ() );
            stack.rotate( mc.getRenderManager().getCameraOrientation() );
            stack.rotate( Vector3f.ZP.rotationDegrees( xpDrop.getRotation() ) );
            stack.scale( -scale, -scale, scale );
            IReorderingProcessor reorderingProcessor = IReorderingProcessor.fromString( text, XP.getColorStyle( color ) );
            fr.func_238416_a_( reorderingProcessor, textOffset, 0, color, true, stack.getLast().getMatrix(), buffer, false, color, 225 );
//        stack.scale( 1, -1, 1 );
//        fr.func_238416_a_( a, textOffset, 0, 0xffffff, false, stack.getLast().getMatrix(), buffer, false, 0xffffff, 2 );
            xpDrop.xp -= Math.max( 0.01523, xpDrop.xp * xpDrop.getDecaySpeed() * ( 1 + i * 0.01 ) ) * worldXpDropsDecaySpeedMultiplier * d;
            stack.pop();
        }
        stack.pop();
        buffer.finish();
        lastTime = currTime;
    }

    public static void addWorldXpDropOffline( WorldXpDrop xpDrop )
    {
//        System.out.println( "remote xp drop added at " + xpDrop.getPos() );
        PlayerEntity player = Minecraft.getInstance().player;
        if( player != null && Config.getPreferencesMap( Minecraft.getInstance().player ).getOrDefault( "worldXpDropsEnabled", 1D ) != 0 )
            WorldRenderHandler.xpDrops.add( xpDrop );
    }
}