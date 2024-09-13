package chappie.modulus.client.model.anim;

import chappie.modulus.Modulus;
import chappie.modulus.common.capability.anim.PlayerAnimCap;
import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.PlayerPart;
import chappie.modulus.util.model.IHasModelProperties;
import chappie.modulus.util.model.ModelProperties;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.molang.LazyVariable;
import software.bernie.geckolib.core.molang.MolangParser;
import software.bernie.geckolib.core.object.DataTicket;
import software.bernie.geckolib.model.DefaultedGeoModel;

public class PlayerGeoModel extends DefaultedGeoModel<PlayerAnimCap> {

    @SuppressWarnings("rawtypes")
    public static final DataTicket<PlayerModel> PLAYER_MODEL_DATA = new DataTicket<>("player_model_data", PlayerModel.class);

    @Nullable
    private PlayerModel<?> model;

    public PlayerGeoModel() {
        super(Modulus.id("player"));
    }

    @Override
    protected String subtype() {
        return "entity";
    }

    @Override
    public ResourceLocation getTextureResource(PlayerAnimCap o) {
        return ((LocalPlayer) o.player).getSkin().texture();
    }

    @Override
    public ResourceLocation getAnimationResource(PlayerAnimCap o) {
        return Modulus.id("animations/player.animation.json");
    }

    @Override
    public void setCustomAnimations(PlayerAnimCap entity, long instanceId, AnimationState<PlayerAnimCap> customPredicate) {
        if (customPredicate != null) {
            this.model = customPredicate.getData(PLAYER_MODEL_DATA);
        }
        super.setCustomAnimations(entity, instanceId, customPredicate);
    }

    @Override
    public boolean crashIfBoneMissing() {
        return false;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void applyMolangQueries(PlayerAnimCap animatable, double animTime) {
        super.applyMolangQueries(animatable, animTime);
        MolangParser parser = MolangParser.INSTANCE;
        if (animatable != null && this.model != null) {
            for (PlayerPart part : PlayerPart.bodyParts()) {
                String name = part == PlayerPart.CHEST ? "body" : part.name().toLowerCase();
                ModelPart renderer = part.initialModelPart(this.model);
                parser.setMemoizedValue(String.format("player.%s.x_rot", name), () -> renderer.xRot / Math.PI * 180.0);
                parser.setMemoizedValue(String.format("player.%s.y_rot", name), () -> renderer.yRot / Math.PI * 180.0);
                parser.setMemoizedValue(String.format("player.%s.z_rot", name), () -> renderer.zRot / Math.PI * 180.0);

                parser.setMemoizedValue(String.format("player.%s.x", name), () -> switch (part) {
                    case RIGHT_ARM -> renderer.x + 5;
                    case LEFT_ARM -> renderer.x - 5;
                    case RIGHT_LEG -> renderer.x + 2;
                    case LEFT_LEG -> renderer.x - 2;
                    default -> renderer.x;
                });
                parser.setMemoizedValue(String.format("player.%s.y", name), () -> switch (part) {
                    case RIGHT_ARM, LEFT_ARM -> 2 - renderer.y;
                    case RIGHT_LEG, LEFT_LEG -> 12 - renderer.y;
                    default -> -renderer.y;
                });
                parser.setMemoizedValue(String.format("player.%s.z", name), () -> renderer.z);
            }
            parser.setMemoizedValue("player.leftIsMainArm", () -> animatable.player.getMainArm() == HumanoidArm.LEFT ? 1 : 0);
            parser.setMemoizedValue("player.x_rot", animatable.player::getXRot);
            parser.setMemoizedValue("player.y_rot", animatable.player::getYRot);
            if (this.model instanceof IHasModelProperties iHasProps) {
                ModelProperties properties = iHasProps.modelProperties();
                parser.setMemoizedValue("player.limbSwing", properties::limbSwing);
                parser.setMemoizedValue("player.limbSwingAmount", properties::limbSwingAmount);
                parser.setMemoizedValue("player.ageInTicks", properties::ageInTicks);
                parser.setMemoizedValue("player.headPitch", properties::headPitch);
                parser.setMemoizedValue("player.netHeadYaw", properties::netHeadYaw);
            }


        }
    }

    public static void registerMolangQueries() {
        MolangParser parser = MolangParser.INSTANCE;

        for (PlayerPart part : PlayerPart.bodyParts()) {
            String name = part.name().toLowerCase();
            parser.register(new LazyVariable(String.format("player.%s.x_rot", name), 0));
            parser.register(new LazyVariable(String.format("player.%s.y_rot", name), 0));
            parser.register(new LazyVariable(String.format("player.%s.z_rot", name), 0));

            parser.register(new LazyVariable(String.format("player.%s.x", name), 0));
            parser.register(new LazyVariable(String.format("player.%s.y", name), 0));
            parser.register(new LazyVariable(String.format("player.%s.z", name), 0));
        }

        parser.register(new LazyVariable("player.leftIsMainArm", 0));
        parser.register(new LazyVariable("player.x_rot", 0));
        parser.register(new LazyVariable("player.y_rot", 0));
        parser.register(new LazyVariable("player.limbSwing", 0));
        parser.register(new LazyVariable("player.limbSwingAmount", 0));
        parser.register(new LazyVariable("player.ageInTicks", 0));
        parser.register(new LazyVariable("player.headPitch", 0));
        parser.register(new LazyVariable("player.netHeadYaw", 0));
    }

    public static void setupPlayerBones(GeoBone bone, ModelPart modelPart, boolean changePos) {
        //Rotation
        modelPart.setRotation(-bone.getRotX(), -bone.getRotY(), bone.getRotZ());

        //Position
        if (changePos) {
            modelPart.x = -bone.getPivotX() + bone.getPosX();
            modelPart.y = (24 - bone.getPivotY()) - bone.getPosY();
            modelPart.z = bone.getPivotZ() + bone.getPosZ();
        }

        //Scale
        ClientUtil.modified(modelPart).setSize(new Vector3f(bone.getScaleZ() - 1.0F, bone.getScaleY() - 1.0F, bone.getScaleZ() - 1.0F));
    }
}
