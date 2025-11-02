package chappie.modulus.util.data;

import net.minecraft.nbt.*;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public record DataAccessor<T>(String key, DataSerializer<T> serializer) {

    public record DataSerializer<T>(Function<T, Tag> toTag, BiFunction<CompoundTag, String, T> fromTag) {
        public static final DataSerializer<Integer> INT = new DataSerializer<>(IntTag::valueOf, (compoundTag, key) -> compoundTag.getInt(key).orElse(0));
        public static final DataSerializer<Float> FLOAT = new DataSerializer<>(FloatTag::valueOf, (compoundTag, key) -> compoundTag.getFloat(key).orElse(0.0F));
        public static final DataSerializer<Double> DOUBLE = new DataSerializer<>(DoubleTag::valueOf, (compoundTag, key) -> compoundTag.getDouble(key).orElse(0.0D));
        public static final DataSerializer<Boolean> BOOLEAN = new DataSerializer<>(ByteTag::valueOf, (compoundTag, key) -> compoundTag.getBoolean(key).orElse(false));
        public static final DataSerializer<String> STRING = new DataSerializer<>(StringTag::valueOf, (compoundTag, key) -> compoundTag.getString(key).orElse(""));
        public static final DataSerializer<CompoundTag> TAG = new DataSerializer<>(tag -> tag, (compoundTag, key) -> compoundTag.getCompound(key).orElseGet(CompoundTag::new));
        public static final DataSerializer<Color> COLOR = new DataSerializer<>(color -> IntTag.valueOf(color.getRGB()), (compoundTag, key) -> new Color(compoundTag.getInt(key).orElse(0)));
        public static final DataSerializer<Vec3> VEC_3 = new DataSerializer<>(vec3 -> {
            CompoundTag tag = new CompoundTag();
            tag.putDouble("x", vec3.x);
            tag.putDouble("y", vec3.y);
            tag.putDouble("z", vec3.z);
            return tag;
        }, (compoundTag, pKey) -> {
            CompoundTag tag = compoundTag.getCompound(pKey).orElseGet(CompoundTag::new);
            double x = tag.getDouble("x").orElse(0.0D);
            double y = tag.getDouble("y").orElse(0.0D);
            double z = tag.getDouble("z").orElse(0.0D);
            return new Vec3(x, y, z);
        });
    }
}
