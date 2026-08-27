package chappie.modulus;

import chappie.modulus.common.command.SuperpowerCommand;
import chappie.modulus.networking.ModNetworking;
import chappie.modulus.util.ModRegistries;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Modulus implements ModInitializer {
    public static final String MODID = "modulus";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static Identifier id(String id) {
        return Identifier.fromNamespaceAndPath(MODID, id);
    }

    @Override
    public void onInitialize() {
        ModRegistries.init();
        ModNetworking.registerMessages();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> SuperpowerCommand.register(dispatcher));

        // Dev test superpowers — remove or disable for production builds
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            chappie.modulus.test.ModulusTestSuperpowers.init();
        }
    }
}