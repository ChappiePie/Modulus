package chappie.modulus.util.data;

import net.minecraft.nbt.*;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public record DataAccessor<T>(String key, DataSerializer<T> serializer) {

    public record DataSerializer<T>(Function<T, Tag> toTag, BiFunction<CompoundTag, String, T> fromTag) {
        public static final DataSerializer<Integer> INT = new DataSerializer<>(IntTag::valueOf, CompoundTag::getInt);
        public static final DataSerializer<Float> FLOAT = new DataSerializer<>(FloatTag::valueOf, CompoundTag::getFloat);
        public static final DataSerializer<Double> DOUBLE = new DataSerializer<>(DoubleTag::valueOf, CompoundTag::getDouble);
        public static final DataSerializer<Boolean> BOOLEAN = new DataSerializer<>(ByteTag::valueOf, CompoundTag::getBoolean);
        public static final DataSerializer<String> STRING = new DataSerializer<>(StringTag::valueOf, CompoundTag::getString);
        public static final DataSerializer<CompoundTag> TAG = new DataSerializer<>(tag -> tag, CompoundTag::getCompound);
        public static final DataSerializer<Color> COLOR = new DataSerializer<>(color -> IntTag.valueOf(color.getRGB()), (compoundTag, pKey) -> new Color(compoundTag.getInt(pKey)));
        public static final DataSerializer<Vec3> VEC_3 = new DataSerializer<>(vec3 -> {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("x", vec3.x);
            tag.putDouble("y", vec3.y);
            tag.putDouble("z", vec3.z);
            return tag;
        }, (compoundTag, pKey) -> {
            CompoundTag tag = compoundTag.getCompound(pKey);
            return new Vec3(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
        });
    }
}
