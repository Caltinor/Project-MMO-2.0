package harmonised.pmmo.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import harmonised.pmmo.config.Config;
import harmonised.pmmo.util.DP;
import harmonised.pmmo.util.XP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import com.mojang.math.Matrix4f;
import net.minecraft.world.phys.Vec3;
import com.mojang.math.Vector3f;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

public class WorldRenderHandler
{
    private static long lastTime = System.nanoTime();
    private final static Font fr = Minecraft.getInstance().font;
    private final static Minecraft mc = Minecraft.getInstance();
    private final static MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
    private final static Map<ResourceLocation, List<WorldXpDrop>> xpDrops = new HashMap<>();
    private final static Map<ResourceLocation, List<WorldText>> worldTexts = new HashMap<>();
    private static long currTime;
    private static double d;

    @SubscribeEvent
    public void handleWorldRender(RenderLevelLastEvent event)
    {
        currTime = System.nanoTime();
        d = (currTime - lastTime) / 1000000000D;
        renderWorldText(event);
        renderWorldXpDrops(event);
        if(XP.isPlayerSurvival(mc.player))
            drawBoxHighlights(event);
    }

    public static void drawBoxHighlights(RenderLevelLastEvent event)
    {
        PoseStack stack = event.getPoseStack();
        Vec3 cameraPos = mc.getEntityRenderDispatcher().camera.getPosition();
        stack.pushPose();
        stack.translate(-cameraPos.x(), -cameraPos.y(), -cameraPos.z());
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        VertexConsumer builder = buffer.getBuffer(RenderType.lines());

        if(XPOverlayGUI.getVeinKey() && XPOverlayGUI.getLookingAtBlock() && XPOverlayGUI.getMetToolReq() && XPOverlayGUI.getCanBreak())
        {
            for(BlockPos pos : XPOverlayGUI.veinShapeSet)
            {
                drawBoxHighlight(stack, builder, pos);
//                System.out.println(pos);
            }
        }
        stack.popPose();
        RenderSystem.disableDepthTest();
        buffer.endBatch();
    }

    public static void drawBoxHighlight(PoseStack stack, VertexConsumer builder, BlockPos pos)
    {
        stack.pushPose();
        Matrix4f matrix4f = stack.last().pose();
        int red = 255;
        int green = 0;
        int blue = 255;
        int alpha = 255;

        for(int i = 0; i < 12; i++)
        {
            int mode = i/4, j = i%4;
            float modulus = j%2, divide = j/2;
            switch(mode)
            {
                case 0:
                    builder.vertex(matrix4f, pos.getX() + modulus, pos.getY() + divide, pos.getZ()).color(red, green, blue, alpha).endVertex();
                    builder.vertex(matrix4f, pos.getX() + modulus, pos.getY() + divide, pos.getZ() + 1).color(red, green, blue, alpha).endVertex();
                    break;

                case 1:
                    builder.vertex(matrix4f, pos.getX(), pos.getY() + modulus, pos.getZ() + divide).color(red, green, blue, alpha).endVertex();
                    builder.vertex(matrix4f, pos.getX() + 1, pos.getY() + modulus, pos.getZ() + divide).color(red, green, blue, alpha).endVertex();
                    break;

                case 2:
                    builder.vertex(matrix4f, pos.getX() + divide, pos.getY(), pos.getZ() + modulus).color(red, green, blue, alpha).endVertex();
                    builder.vertex(matrix4f, pos.getX() + divide, pos.getY() + 1, pos.getZ() + modulus).color(red, green, blue, alpha).endVertex();
                    break;
            }
        }
        stack.popPose();
    }

    public static void drawText(PoseStack stack, Vec3 cameraPos, Vec3 textPos, String text, float scale, float rotation, int color)
    {
        stack.pushPose();
        float textWidth = fr.width(text);
        float textOffset = -textWidth/2;
        stack.translate(textPos.x() - cameraPos.x(), textPos.y() - cameraPos.y(), textPos.z() - cameraPos.z());
//            stack.translate(20 - pos.getX(), 4 - pos.getY(), 12 - pos.getZ());
        stack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        stack.mulPose(Vector3f.ZP.rotationDegrees(rotation));
        stack.scale(-scale, -scale, scale);
        FormattedCharSequence reorderingProcessor = FormattedCharSequence.forward(text, XP.getColorStyle(color));
        fr.drawInBatch(reorderingProcessor, textOffset, 0, color, true, stack.last().pose(), buffer, false, color, 225);
//        stack.scale(1, -1, 1);
//        fr.drawInBatch(a, textOffset, 0, 0xffffff, false, stack.getLast().getMatrix(), buffer, false, 0xffffff, 2);
        stack.popPose();
    }

    private static void renderWorldText(RenderLevelLastEvent event)
    {
        //        temp1++;
        Level world = mc.level;
        if(world == null)
            return;
//        ResourceLocation dimResLoc = world.getDimensionType().getEffects();
        ResourceLocation dimResLoc = XP.getDimResLoc(world);
        if(!worldTexts.containsKey(dimResLoc))
            return;
        List<WorldText> dimTexts = worldTexts.get(dimResLoc);
        PoseStack stack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

        stack.pushPose();
        for(int i = dimTexts.size()-1; i >= 0; i--)
        {
            WorldText worldText = dimTexts.get(i);
            if(worldText == null)
                continue;
            if(!worldText.tick(d * (1 + i*0.01)))
            {
                dimTexts.remove(i);
                continue;
            }
            drawText(stack, cameraPos, worldText.getPos(), worldText.getText(), (float) worldText.getSize(), worldText.getRotation(), worldText.getColor());
        }
        stack.popPose();
        buffer.endBatch();
        lastTime = currTime;
    }

    private static void renderWorldXpDrops(RenderLevelLastEvent event)
    {
        //        temp1++;
        Minecraft mc = Minecraft.getInstance();
        Level world = mc.level;
        if(world == null)
            return;
//        ResourceLocation dimResLoc = world.getDimensionType().getEffects();
        ResourceLocation dimResLoc = XP.getDimResLoc(world);
        if(!xpDrops.containsKey(dimResLoc))
            return;
        List<WorldXpDrop> dimXpDrops = xpDrops.get(dimResLoc);
        PoseStack stack = event.getPoseStack();
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

        stack.pushPose();
        for(int i = dimXpDrops.size()-1; i >= 0; i--)
        {
            WorldXpDrop xpDrop = dimXpDrops.get(i);
            if(xpDrop == null)
                continue;
            if(xpDrop.xp <= 0)
            {
                dimXpDrops.remove(i);
                continue;
            }
            float scale = 0.02f * (xpDrop.xp / xpDrop.getStartXp()) * xpDrop.getSize() * WorldText.worldXpDropsSizeMultiplier;
            int color = xpDrop.getColor();
            String text = "+" + DP.dpSoft(xpDrop.xp);
            if(WorldText.worldXpDropsShowSkill)
                text += " " + new TranslatableComponent(xpDrop.getSkill()).getString();
            drawText(stack, cameraPos, xpDrop.getPos(), text, scale, xpDrop.getRotation(), color);
            xpDrop.xp -= Math.max(0.01523, xpDrop.xp * xpDrop.getDecaySpeed() * (1 + i * 0.01)) * WorldText.worldXpDropsDecaySpeedMultiplier * d;
        }
        stack.popPose();
        buffer.endBatch();
        lastTime = currTime;
    }

    public static void addWorldXpDropOffline(WorldXpDrop xpDrop)
    {
        Minecraft mc = Minecraft.getInstance();
//        System.out.println("remote xp drop added at " + xpDrop.getPos());
        Player player = mc.player;
        if(player != null && Config.getPreferencesMap(player).getOrDefault("worldXpDropsEnabled", 1D) != 0)
        {
            ResourceLocation dimResLoc = xpDrop.getWorldResLoc();
            if(!xpDrops.containsKey(dimResLoc))
            {
                xpDrops.put(dimResLoc, new ArrayList<>());
                lastTime = System.nanoTime();
            }
            xpDrops.get(dimResLoc).add(xpDrop);
        }
    }

    public static void addWorldTextOffline(WorldText worldText)
    {
        Minecraft mc = Minecraft.getInstance();
//        System.out.println("remote xp drop added at " + xpDrop.getPos());
        Player player = mc.player;
//        if(player != null && Config.getPreferencesMap(player).getOrDefault("worldXpDropsEnabled", 1D) != 0)
//        {
            ResourceLocation dimResLoc = worldText.getWorldResLoc();
            if(!worldTexts.containsKey(dimResLoc))
            {
                worldTexts.put(dimResLoc, new ArrayList<>());
                lastTime = System.nanoTime();
            }
            worldTexts.get(dimResLoc).add(worldText);
//        }
    }
}