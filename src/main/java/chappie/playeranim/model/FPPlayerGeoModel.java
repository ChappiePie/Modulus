package chappie.playeranim.model;

import chappie.playeranim.capability.PlayerAnimCap;
import net.minecraft.resources.ResourceLocation;

public class FPPlayerGeoModel extends PlayerGeoModel {

    @Override
    public ResourceLocation getModelResource(PlayerAnimCap animatable) {
        ResourceLocation location = super.getModelResource(animatable);
        return location.withPath(location.getPath().replace(".geo.json", "_first_person.geo.json"));
    }
}