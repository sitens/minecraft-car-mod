package com.kiancars.carmod.client.model;

import com.kiancars.carmod.CarMod;
import com.kiancars.carmod.client.renderer.HoesacBossRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;

/**
 * The boss is drawn small (32 model units wide) and scaled up 8x by the
 * renderer, so the picture can stay chunky and low-resolution. Everything
 * hangs off one "roller" part whose pivot is the center of the body, so
 * turning it makes the whole blob roll.
 */
public class HoesacBossModel extends EntityModel<HoesacBossRenderState> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(Identifier.fromNamespaceAndPath(CarMod.MOD_ID, "hoesac_final_boss"), "main");

    /** How many times the renderer blows the model up. 32 units * 8 / 16 = 16 blocks. */
    public static final float SCALE = 8.0F;

    private final ModelPart roller;

    public HoesacBossModel(ModelPart root) {
        super(root);
        this.roller = root.getChild("roller");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Pivot = body center. Ground is at y=24, so the body (y 3..21) centers on y=12.
        PartDefinition roller = root.addOrReplaceChild("roller", CubeListBuilder.create(), PartPose.offset(0.0F, 12.0F, 0.0F));

        // Big round body: a wide middle block with stacked, narrower layers on top to soften the corners.
        roller.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 30).addBox(-13.0F, -9.0F, -13.0F, 26.0F, 18.0F, 26.0F), PartPose.ZERO);
        roller.addOrReplaceChild("dome",
                CubeListBuilder.create().texOffs(0, 76).addBox(-11.0F, -11.0F, -11.0F, 22.0F, 2.0F, 22.0F), PartPose.ZERO);
        roller.addOrReplaceChild("belly_bottom",
                CubeListBuilder.create().texOffs(0, 104).addBox(-10.0F, 9.0F, -10.0F, 20.0F, 1.0F, 20.0F), PartPose.ZERO);

        // Medium head on top, with the face on its front.
        roller.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -20.0F, -6.0F, 12.0F, 9.0F, 12.0F), PartPose.ZERO);

        // Tiny arms and legs.
        roller.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(76, 104).addBox(13.0F, -4.0F, -1.5F, 3.0F, 10.0F, 3.0F), PartPose.ZERO);
        roller.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(76, 104).mirror().addBox(-16.0F, -4.0F, -1.5F, 3.0F, 10.0F, 3.0F), PartPose.ZERO);
        roller.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(100, 104).addBox(6.0F, 10.0F, -2.0F, 4.0F, 2.0F, 4.0F), PartPose.ZERO);
        roller.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(100, 104).mirror().addBox(-10.0F, 10.0F, -2.0F, 4.0F, 2.0F, 4.0F), PartPose.ZERO);

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(HoesacBossRenderState state) {
        super.setupAnim(state);
        this.roller.xRot = state.rollAngle;
    }
}
