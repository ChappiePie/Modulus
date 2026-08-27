package chappie.modulus.util;

import chappie.modulus.common.ModConstants;
import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.capability.PowerCap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class CommonUtil {

    public static Collection<Ability> getAbilities(Entity entity) {
        PowerCap cap = PowerCap.getCap(entity);
        if (cap == null) return List.of();

        if (entity instanceof ServerPlayer player && player.isSpectator()) {
            return List.of();
        }

        return cap.getAbilities();
    }

    public static <T extends Ability> Collection<T> getAbilitiesByType(Class<T> type, Entity entity) {
        PowerCap cap = PowerCap.getCap(entity);
        if (cap == null) return List.of();
        if (entity instanceof ServerPlayer player && player.gameMode != null && player.isSpectator()) {
            return List.of();
        }
        return cap.getAbilitiesByType(type);
    }

    public static void spawnParticleForAll(Level world, ParticleOptions particleIn, boolean longDistanceIn, Vec3 posVc3d, Vec3 offsetVc3d, float speedIn, int countIn) {
        for (ServerPlayer player : world.getEntitiesOfClass(ServerPlayer.class, CommonUtil.boxWithRange(posVc3d, ModConstants.PARTICLE_RENDER_DISTANCE))) {
            player.connection.send(new ClientboundLevelParticlesPacket(particleIn, longDistanceIn, true, posVc3d.x, posVc3d.y, posVc3d.z, (float) offsetVc3d.x, (float) offsetVc3d.y, (float) offsetVc3d.z, speedIn, countIn));
        }
    }

    private static final int HTTP_TIMEOUT_MS = 8000;

    public static AABB boxWithRange(Vec3 vec3, double range) {
        return new AABB(vec3, vec3).inflate(range);
    }

    // Pick HitResult from view vector
    public static HitResult pick(Entity entity, double distance) {
        HitResult hitResult = entity.pick(distance, 1F, false); // get block hit result
        Vec3 eyePos = entity.getEyePosition(1F);
        Vec3 viewVector = entity.getViewVector(1.0F).scale(distance);
        AABB aabb = entity.getBoundingBox().expandTowards(viewVector);
        double d = hitResult.getLocation().distanceToSqr(eyePos);
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(entity, eyePos, eyePos.add(viewVector), aabb, (e) -> !e.isSpectator() && e.isPickable(), d);
        // get entity result
        if (entityHitResult != null && !entity.hasPassenger(entityHitResult.getEntity())) {
            // check distance and if hit result is miss - give entity hit result
            if (eyePos.distanceToSqr(entityHitResult.getLocation()) < hitResult.getLocation().distanceToSqr(eyePos)
                    || hitResult.getType() == HitResult.Type.MISS) {
                hitResult = entityHitResult;
            }
        }
        return hitResult;
    }

    public static void setAttribute(LivingEntity entity, Identifier name, Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null || entity.level().isClientSide()) {
            return;
        }

        AttributeModifier modifier = instance.getModifier(name);
        if (modifier != null) {
            if (modifier.amount() == amount && modifier.operation() == operation) {
                return; // No change needed
            }
            instance.removeModifier(name);
        }

        if (amount != 0) {
            instance.addTransientModifier(new AttributeModifier(name, amount, operation));
        }
    }

    public static Supplier<List<String>> getTxtFromLink(String link) {
        List<String> content = new ArrayList<>();
        CompletableFuture.runAsync(() -> {
            try {
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new URL(link).openConnection();
                conn.setConnectTimeout(HTTP_TIMEOUT_MS);
                conn.setReadTimeout(HTTP_TIMEOUT_MS);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    content.add(line);
                }
                bufferedReader.close();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, Util.backgroundExecutor());
        return () -> content;
    }

    public static Supplier<JsonObject> getJsonFromLink(String link) {
        AtomicReference<JsonObject> jsonObject = new AtomicReference<>(new JsonObject());
        AtomicReference<Boolean> loading = new AtomicReference<>(false);
        Runnable load = () -> {
            if (loading.getAndSet(true)) return;
            CompletableFuture.runAsync(() -> {
                try {
                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new URL(link).openConnection();
                    conn.setConnectTimeout(HTTP_TIMEOUT_MS);
                    conn.setReadTimeout(HTTP_TIMEOUT_MS);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    JsonElement root = JsonParser.parseReader(bufferedReader);
                    bufferedReader.close();
                    conn.disconnect();
                    if (root != null) {
                        jsonObject.set(root.getAsJsonObject());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    loading.set(false);
                }
            }, Util.backgroundExecutor());
        };
        load.run();
        return () -> {
            JsonObject result = jsonObject.get();
            if (result.isEmpty() && !loading.get()) {
                load.run(); // retry if failed and not currently loading
            }
            return result;
        };
    }

    public static List<Component> parseDescriptionLines(JsonElement jsonElement) {
        List<Component> lines = new ArrayList<>();

        if (jsonElement != null) {
            if (jsonElement.isJsonArray()) {
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                for (int i = 0; i < jsonArray.size(); i++) {
                    lines.addAll(parseDescriptionLines(jsonArray.get(i)));
                }
            } else if (jsonElement.isJsonObject()) {
                lines.add(ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, jsonElement).result().get());
            } else if (jsonElement.isJsonPrimitive()) {
                lines.add(Component.literal(jsonElement.getAsString()));
            }
        }

        return lines;
    }
}
