package com.kiancars.carmod.client.renderer;

import com.kiancars.carmod.entity.CarEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Placeholder renderer. Confirmed against the real 1.21.11 sources: entity
 * rendering was overhauled to a render-state pattern —
 * {@code EntityRenderer<T extends Entity, S extends EntityRenderState>} no
 * longer has a {@code render(...)} or {@code getTextureLocation(...)} method
 * to override; per-frame drawing happens in {@code submit(S, ...)} against a
 * plain data snapshot ({@code S}), not the live entity.
 * <p>
 * This leaves the default (no-op body/model, nametag only) behavior in
 * place — cars are trackable and rideable but invisible until a real model
 * is built. That's an intentional placeholder, not a bug: see
 * {@code README.md}'s "Status" section.
 */
public class CarRenderer extends EntityRenderer<CarEntity, EntityRenderState> {

    public CarRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.6F;
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
