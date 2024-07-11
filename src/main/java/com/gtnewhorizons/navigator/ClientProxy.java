package com.gtnewhorizons.navigator;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import com.gtnewhorizons.navigator.api.NavigatorApi;
import com.gtnewhorizons.navigator.config.GeneralConfig;
import com.gtnewhorizons.navigator.impl.DirtyChunkButtonManager;
import com.gtnewhorizons.navigator.impl.DirtyChunkLayerManager;
import com.gtnewhorizons.navigator.impl.journeymap.JMDirtyChunkButton;
import com.gtnewhorizons.navigator.impl.journeymap.JMDirtyChunkRenderer;
import com.gtnewhorizons.navigator.impl.journeymap.JMDirtyChunkWaypointManager;
import com.gtnewhorizons.navigator.impl.xaero.XaeroDirtyChunkButton;
import com.gtnewhorizons.navigator.impl.xaero.XaeroDirtyChunkRenderer;
import com.gtnewhorizons.navigator.impl.xaero.XaeroDirtyChunkWaypointManager;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        Navigator.actionKey = new KeyBinding("navigator.key.action", Keyboard.KEY_DELETE, Navigator.MODNAME);
        ClientRegistry.registerKeyBinding(Navigator.actionKey);
        if (GeneralConfig.enableDebugLayers) {
            // Shared
            NavigatorApi.registerLayerManager(DirtyChunkLayerManager.instance);
            NavigatorApi.registerButtonManager(DirtyChunkButtonManager.instance);

            // Journeymap
            NavigatorApi.registerLayerButton(JMDirtyChunkButton.instance);
            NavigatorApi.registerLayerRenderer(JMDirtyChunkRenderer.instance);
            NavigatorApi.registerWaypointManager(JMDirtyChunkWaypointManager.instance);

            // Xaero's maps
            NavigatorApi.registerLayerButton(XaeroDirtyChunkButton.instance);
            NavigatorApi.registerLayerRenderer(XaeroDirtyChunkRenderer.instance);
            NavigatorApi.registerWaypointManager(XaeroDirtyChunkWaypointManager.instance);
        }
    }

    @Override
    public void init(FMLInitializationEvent event) {}

    @Override
    public void postInit(FMLPostInitializationEvent event) {}

}
