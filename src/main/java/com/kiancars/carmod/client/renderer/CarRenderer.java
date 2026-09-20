package com.kiancars.carmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.kiancars.carmod.CarMod;
import com.kiancars.carmod.entity.CarEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Placeholder renderer: draws nothing fancy yet, just resolves per-variant
 * texture paths under {@code assets/carmod/textures/entity/car/<id>.png}
 * (not yet supplied — point these at real textures once art exists). Model
 * itself is a plain box for now.
 * <p>
 * NOTE: verify {@code EntityRenderer}'s constructor / render() signature
 * against 1.21.11 before relying on this — entity rendering has a fair
 * amount of version churn (PoseStack vs. matrix helpers, model layer
 * registration via EntityRendererProvider.Context, etc.).
 */
public class CarRenderer extends EntityRenderer<CarEntity> {

    public CarRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.6F;
    }

    @Override
    public ResourceLocation getTextureLocation(CarEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(CarMod.MOD_ID,
                "textures/entity/car/" + entity.getCarType().getId() + ".png");
    }

    @Override
    public void render(CarEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                        MultiBufferSource buffer, int packedLight) {
        // TODO: actual box/geo model + texture mapping. Left minimal so the
        // entity at least exists and is trackable/ridable before art lands.
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
