package com.example.createaddon.client.ponder;

import com.example.createaddon.CreateAddon;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class GuidancePonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return CreateAddon.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        GuidancePonderScenes.register(helper);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        GuidancePonderTags.register(helper);
    }
}
