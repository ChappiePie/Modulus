package chappie.modulus.util.model;

import org.joml.Vector3f;

public interface IChangeableSize {

    void modulus$setSizeAndPos(Vector3f size, Vector3f pos);

    default void modulus$setSize(Vector3f size) {
        this.modulus$setSizeAndPos(size, new Vector3f());
    }

    default void modulus$setPos(Vector3f pos) {
        this.modulus$setSizeAndPos(new Vector3f(), pos);
    }

    Vector3f modulus$size();

    Vector3f modulus$pos();
}
