package com.kiancars.carmod.client.renderer;

import com.kiancars.carmod.CarMod;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;

/** hOesaC uses the zombie body shape with his own orange, cheese-dusted skin. */
public class HoesacRenderer extends ZombieRenderer {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(CarMod.MOD_ID, "textures/entity/hoesac.png");

    public HoesacRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Identifier getTextureLocation(ZombieRenderState state) {
        return TEXTURE;
    }
}
