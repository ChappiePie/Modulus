package chappie.modulus.util;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.capability.PowerCap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.Util;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CommonUtil {

    public static List<Ability> getAbilities(Entity entity) {
        List<Ability> list = new ArrayList<>();
        PowerCap cap = PowerCap.getCap(entity);
        if (cap == null) return list;
        if (entity instanceof ServerPlayer player && player.gameMode != null) {
            if (!player.isSpectator()) {
                list.addAll(cap.getAbilities());
            }
        } else {
            list.addAll(cap.getAbilities());
        }
        return list;
    }

    public static <T> List<T> listOfType(Class<T> type, Collection<?> list) {
        return list.stream().filter(x -> type.isAssignableFrom(x.getClass())).map(type::cast).collect(Collectors.toList());
    }

    public static boolean smallArms(Entity entity) {
        if (entity instanceof AbstractClientPlayer) {
            return ((AbstractClientPlayer) entity).getSkin().model().equals(PlayerSkin.Model.SLIM);
        }
        return false;
    }

    public static void spawnParticleForAll(Level world, ParticleOptions particleIn, boolean longDistanceIn, Vec3 posVc3d, Vec3 offsetVc3d, float speedIn, int countIn) {
        for (ServerPlayer player : world.getEntitiesOfClass(ServerPlayer.class, CommonUtil.boxWithRange(posVc3d, 20))) {
            player.connection.send(new ClientboundLevelParticlesPacket(particleIn, longDistanceIn, true, posVc3d.x, posVc3d.y, posVc3d.z, (float) offsetVc3d.x, (float) offsetVc3d.y, (float) offsetVc3d.z, speedIn, countIn));
        }
    }

    public static void setAttribute(LivingEntity entity, ResourceLocation name, Attribute attribute, double amount, AttributeModifier.Operation operation) {
        Holder<Attribute> attributeHolder = Holder.direct(attribute);
        AttributeInstance instance = entity.getAttribute(attributeHolder);
        if (instance == null || entity.getCommandSenderWorld().isClientSide) {
            return;
        }

        AttributeModifier modifier = instance.getModifier(name);
        if (modifier != null && (amount == 0 || (modifier.amount() != amount || modifier.operation() != operation))) {
            instance.removeModifier(name);
            return;
        }

        if (modifier == null && amount != 0) {
            instance.addTransientModifier(new AttributeModifier(name, amount, operation));
        }
    }

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

    public static Supplier<List<String>> getTxtFromLink(String link) {
        List<String> content = new ArrayList<>();
        CompletableFuture.runAsync(() -> {
            try {
                URL url = new URL(link);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    content.add(line);
                }

                bufferedReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, Util.backgroundExecutor());
        return () -> content;
    }

    public static Supplier<JsonObject> getJsonFromLink(String link) {
        AtomicReference<JsonObject> jsonObject = new AtomicReference<>(new JsonObject());
        CompletableFuture.runAsync(() -> {
            try (InputStream url = new URL(link).openStream()) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url));
                JsonElement root = JsonParser.parseReader(bufferedReader);
                bufferedReader.close();
                assert root != null;
                jsonObject.set(root.getAsJsonObject());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return jsonObject::get;
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
                lines.add(Component.Serializer.fromJson(jsonElement, RegistryAccess.EMPTY));
            } else if (jsonElement.isJsonPrimitive()) {
                lines.add(Component.literal(jsonElement.getAsString()));
            }
        }

        return lines;
    }
}
