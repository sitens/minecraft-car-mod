package com.kiancars.carmod.client.renderer;

import com.kiancars.carmod.CarMod;
import com.kiancars.carmod.client.model.HoesacBossModel;
import com.kiancars.carmod.entity.HoesacBossEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class HoesacBossRenderer extends MobRenderer<HoesacBossEntity, HoesacBossRenderState, HoesacBossModel> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(CarMod.MOD_ID, "textures/entity/hoesac_final_boss.png");

    public HoesacBossRenderer(EntityRendererProvider.Context context) {
        super(context, new HoesacBossModel(context.bakeLayer(HoesacBossModel.LAYER)), 3.5F);
    }

    @Override
    public HoesacBossRenderState createRenderState() {
        return new HoesacBossRenderState();
    }

    @Override
    public void extractRenderState(HoesacBossEntity entity, HoesacBossRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.rollAngle = Mth.lerp(partialTick, entity.rollAngleO, entity.rollAngle);
    }

    @Override
    protected void scale(HoesacBossRenderState state, PoseStack poseStack) {
        poseStack.scale(HoesacBossModel.SCALE, HoesacBossModel.SCALE, HoesacBossModel.SCALE);
    }

    @Override
    protected boolean shouldShowName(HoesacBossEntity entity, double distanceSq) {
        return false;
    }

    @Override
    public Identifier getTextureLocation(HoesacBossRenderState state) {
        return TEXTURE;
    }
}
