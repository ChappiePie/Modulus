package chappie.modulus.client.model;

import chappie.modulus.Modulus;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;

public class CapeModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Modulus.id("cape"), "main");

	public CapeModel(ModelPart root) {
		super(root.getChild("main"), RenderType::entityTranslucent);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition main = partdefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -24.4F, -0.575F, 8.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		main.addOrReplaceChild("cape", CubeListBuilder.create().texOffs(0, 5).addBox(-7.5F, -1.0F, -0.5F, 14.0F, 24.0F, 1.0F, new CubeDeformation(-0.85F)), PartPose.offset(0.0F, -24.0F, 2.5F));

		return LayerDefinition.create(meshdefinition, 64, 32);
	}
}