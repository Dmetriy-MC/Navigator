package com.gtnewhorizons.navigator.mixins.late.xaerosminimap;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.MinecraftForgeClient;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnewhorizons.navigator.Navigator;
import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.api.model.layers.LayerManager;
import com.gtnewhorizons.navigator.api.xaero.renderers.XaeroLayerRenderer;
import com.gtnewhorizons.navigator.api.xaero.rendersteps.XaeroRenderStep;
import com.llamalad7.mixinextras.sugar.Local;

import xaero.common.XaeroMinimapSession;
import xaero.common.minimap.MinimapProcessor;
import xaero.common.minimap.render.MinimapRenderer;

@Mixin(value = MinimapRenderer.class, remap = false)
public abstract class MinimapRendererMixin {

    @Unique
    private boolean navigator$stencilEnabled = true;

    @Shadow
    protected Minecraft mc;

    @Shadow
    protected double zoom;

    /*
     * XaeroMinimapSession minimapSession, MinimapProcessor minimap, int x,
     * int y, int width, int height, int scale, int size, float partial, CallbackInfo ci, ModSettings settings,
     * ArrayList<String> underText, int mapSize, int bufferSize, float minimapScale, float mapScale, float sizeFix,
     * int shape, boolean lockedNorth, double angle, double ps, double pc, boolean useWorldMap, int lightLevel,
     * boolean cave, boolean circleShape, int scaledX, int scaledY, int minimapFrameSize, int circleSides,
     * int frameType, boolean renderFrame, int frameTextureX, int halfFrame, int rightCornerStartX, int specH,
     * boolean safeMode, double playerX, double playerZ
     */

    @Inject(
        method = "renderMinimap",
        at = @At(
            value = "INVOKE",
            target = "Lxaero/common/minimap/waypoints/render/WaypointsGuiRenderer;render(Lxaero/common/XaeroMinimapSession;Lxaero/common/minimap/render/MinimapRendererHelper;DDIIDDFDZFZ)V"))
    private void visualprospecting$injectDraw(XaeroMinimapSession minimapSession, MinimapProcessor minimap, int x,
        int y, int width, int height, int scale, int size, float partial, CallbackInfo ci,
        @Local(name = "circleShape") boolean circleShape, @Local(name = "minimapFrameSize") int minimapFrameSize,
        @Local(name = "angle") double angle) {
        for (LayerManager layerManager : NavigatorApi.layerManagers) {
            if (layerManager.isLayerActive()) {
                if (circleShape) {
                    layerManager.recacheMiniMap((int) mc.thePlayer.posX, (int) mc.thePlayer.posZ, minimapFrameSize * 2);
                } else {
                    layerManager.recacheMiniMap(
                        (int) mc.thePlayer.posX,
                        (int) mc.thePlayer.posZ,
                        minimapFrameSize * 2,
                        minimapFrameSize * 2);
                }
            }
        }

        if (navigator$stencilEnabled) {
            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_STENCIL_TEST);
            GL11.glRotated(Math.toDegrees(angle) - 90, 0.0, 0.0, 1.0);
            GL11.glScaled(zoom, zoom, 0);
            GL11.glStencilFunc(GL11.GL_EQUAL, 1, 1);
            for (XaeroLayerRenderer renderer : NavigatorApi.getXaeroLayerRenderers()) {
                if (renderer.isLayerActive()) {
                    for (XaeroRenderStep renderStep : renderer.getRenderSteps()) {
                        renderStep.draw(null, minimap.mainPlayerX, minimap.mainPlayerZ, scale);
                    }
                }
            }
            GL11.glDisable(GL11.GL_STENCIL_TEST);
            GL11.glPopMatrix();
        }
    }

    @Inject(
        method = "renderMinimap",
        at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glScalef(FFF)V", shift = At.Shift.AFTER),
        slice = @Slice(
            to = @At(
                value = "INVOKE",
                target = "Lxaero/common/minimap/render/MinimapRendererHelper;drawTexturedElipseInsideRectangle(IFFIIFF)V")))
    private void visualprospecting$injectBeginStencil(XaeroMinimapSession minimapSession, MinimapProcessor minimap,
        int x, int y, int width, int height, int scale, int size, float partial, CallbackInfo ci) {
        if (navigator$stencilEnabled && MinecraftForgeClient.getStencilBits() == 0) {
            navigator$stencilEnabled = false;
            Navigator.LOG.warn("Could not enable stencils! Xaero's minimap overlays will not render");
        }
        // if stencil is not enabled, this code will do nothing
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 1);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        GL11.glStencilMask(0xFF);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
    }

    @Inject(
        method = "renderMinimap",
        at = @At(value = "INVOKE", target = "Lxaero/common/minimap/MinimapInterface;usingFBO()Z"),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lxaero/common/minimap/render/MinimapRendererHelper;drawTexturedElipseInsideRectangle(IFFIIFF)V")))
    private void visualprospecting$injectEndStencil(XaeroMinimapSession minimapSession, MinimapProcessor minimap, int x,
        int y, int width, int height, int scale, int size, float partial, CallbackInfo ci) {
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glStencilMask(0x00);
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }
}
