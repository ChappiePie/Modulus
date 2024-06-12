package chappie.modulus.client.model;

import chappie.modulus.Modulus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

// Literally player model, but second layer working as child of main parts
public class SuitModel<T extends LivingEntity> extends HumanoidModel<T> {

    public static final ModelLayerLocation SUIT = createLocation("main");
    public static final ModelLayerLocation SUIT_SLIM = createLocation("slim");

    public final ModelPart leftSleeve = this.leftArm.getChild("left_sleeve");
    public final ModelPart rightSleeve = this.rightArm.getChild("right_sleeve");
    public final ModelPart leftPants = this.leftLeg.getChild("left_pants");
    public final ModelPart rightPants = this.rightLeg.getChild("right_pants");
    public final ModelPart jacket = this.body.getChild("jacket");

    public SuitModel(ModelLayerLocation location) {
        this(Minecraft.getInstance().getEntityModels().bakeLayer(location));
    }

    public SuitModel(ModelPart mainPart) {
        this(mainPart, RenderType::entityTranslucent);
    }

    public SuitModel(ModelPart mainPart, Function<ResourceLocation, RenderType> pRenderType) {
        super(mainPart, pRenderType);
    }

    public static LayerDefinition createLayerDefinition(CubeDeformation cubeDeformation, boolean slim) {
        return LayerDefinition.create(SuitModel.createMesh(cubeDeformation, slim), 64, 64);
    }

    public static MeshDefinition createMesh(CubeDeformation size, boolean slim) {
        MeshDefinition mesh = HumanoidModel.createMesh(size, 0.0F);
        PartDefinition parts = mesh.getRoot();
        if (slim) {
            // Left arm and second layer
            parts.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, size), PartPose.offset(5.0F, 2.5F, 0.0F));
            parts.getChild("left_arm").addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, size.extend(0.25F)), PartPose.ZERO);
            // Right arm and second layer
            parts.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, size), PartPose.offset(-5.0F, 2.5F, 0.0F));
            parts.getChild("right_arm").addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, size.extend(0.25F)), PartPose.ZERO);
        } else {
            // Replace left arm to fit position on player skin
            parts.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, size), PartPose.offset(5.0F, 2.0F, 0.0F));
            parts.getChild("left_arm").addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, size.extend(0.25F)), PartPose.ZERO);
            parts.getChild("right_arm").addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, size.extend(0.25F)), PartPose.ZERO);
        }


        parts.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, size), PartPose.offset(1.9F, 12.0F, 0.0F));
        parts.getChild("left_leg").addOrReplaceChild("left_pants", CubeListBuilder.create().texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, size.extend(0.25F)), PartPose.ZERO);
        parts.getChild("right_leg").addOrReplaceChild("right_pants", CubeListBuilder.create().texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, size.extend(0.25F)), PartPose.ZERO);
        parts.getChild("body").addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, size.extend(0.25F)), PartPose.ZERO);
        return mesh;
    }

    private static ModelLayerLocation createLocation(String type) {
        return new ModelLayerLocation(new ResourceLocation(Modulus.MODID, "suit"), type);
    }
}