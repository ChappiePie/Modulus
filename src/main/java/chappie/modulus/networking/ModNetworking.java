package chappie.modulus.networking;

import chappie.modulus.Modulus;
import chappie.modulus.networking.client.ClientTriggerPlayerAnim;
import chappie.modulus.networking.client.ClientKeyInput;
import chappie.modulus.networking.client.ClientSyncAbility;
import chappie.modulus.networking.client.ClientSyncData;
import chappie.modulus.networking.client.ClientSyncPowerCap;
import chappie.modulus.networking.server.ServerKeyInput;
import chappie.modulus.networking.server.ServerSetData;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworking {

    public static SimpleChannel INSTANCE;
    private static int id = -1;

    public static void registerMessages() {
        INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(Modulus.MODID, "networking"), () -> "1.0", s -> true, s -> true);
        INSTANCE.registerMessage(id++, ClientTriggerPlayerAnim.class, ClientTriggerPlayerAnim::toBytes, ClientTriggerPlayerAnim::new, ClientTriggerPlayerAnim::handle);

        INSTANCE.registerMessage(id++, ClientSyncPowerCap.class, ClientSyncPowerCap::toBytes, ClientSyncPowerCap::new, ClientSyncPowerCap::handle);
        INSTANCE.registerMessage(id++, ClientSyncAbility.class, ClientSyncAbility::toBytes, ClientSyncAbility::new, ClientSyncAbility::handle);
        INSTANCE.registerMessage(id++, ClientSyncData.class, ClientSyncData::toBytes, ClientSyncData::new, ClientSyncData::handle);
        INSTANCE.registerMessage(id++, ClientKeyInput.class, ClientKeyInput::toBytes, ClientKeyInput::new, ClientKeyInput::handle);

        INSTANCE.registerMessage(id++, ServerKeyInput.class, ServerKeyInput::toBytes, ServerKeyInput::new, ServerKeyInput::handle);
        INSTANCE.registerMessage(id++, ServerSetData.class, ServerSetData::toBytes, ServerSetData::new, ServerSetData::handle);
    }
}