package chappie.modulus.util.model;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.GeoBone;

public interface IChangeableSize {

    default void set(GeoBone bone, Matrix3f worldSpaceNormal, Matrix4f worldSpaceMatrix) {

    }

    void setSize(Vector3f size);

    Vector3f size();
}
