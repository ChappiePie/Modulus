package chappie.modulus.networking;

import chappie.modulus.Modulus;
import chappie.modulus.networking.client.*;
import chappie.modulus.networking.server.ServerKeyInput;
import chappie.modulus.networking.server.ServerSetData;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;
import net.minecraftforge.network.packets.OpenContainer;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class ModNetworking {

    private static final Marker MARKER = MarkerManager.getMarker("FORGE_NETWORK");
    public static final ResourceLocation NAME = new ResourceLocation(Modulus.MODID, "networking");

    public static void registerMessages() {
        INSTANCE.messageBuilder(ClientTriggerPlayerAnim.class, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ClientTriggerPlayerAnim::new)
                .encoder(ClientTriggerPlayerAnim::toBytes)
                .consumerMainThread(ClientTriggerPlayerAnim::handle)
                .add()

                .messageBuilder(ClientSyncPowerCap.class, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ClientSyncPowerCap::new)
                .encoder(ClientSyncPowerCap::toBytes)
                .consumerMainThread(ClientSyncPowerCap::handle)
                .add()

                .messageBuilder(ClientSyncAbility.class, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ClientSyncAbility::new)
                .encoder(ClientSyncAbility::toBytes)
                .consumerMainThread(ClientSyncAbility::handle)
                .add()

                .messageBuilder(ClientSyncData.class, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ClientSyncData::new)
                .encoder(ClientSyncData::toBytes)
                .consumerMainThread(ClientSyncData::handle)
                .add()

                .messageBuilder(ClientKeyInput.class, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ClientKeyInput::new)
                .encoder(ClientKeyInput::toBytes)
                .consumerMainThread(ClientKeyInput::handle)
                .add()

                .messageBuilder(ServerKeyInput.class, NetworkDirection.PLAY_TO_SERVER)
                .decoder(ServerKeyInput::new)
                .encoder(ServerKeyInput::toBytes)
                .consumerMainThread(ServerKeyInput::handle)
                .add()

                .messageBuilder(ServerSetData.class, NetworkDirection.PLAY_TO_SERVER)
                .decoder(ServerSetData::new)
                .encoder(ServerSetData::toBytes)
                .consumerMainThread(ServerSetData::handle)
                .add()

                .messageBuilder(OpenContainer.class)
                .decoder(OpenContainer::decode)
                .encoder(OpenContainer::encode)
                .consumerMainThread(OpenContainer::handle)
                .add();

        Modulus.LOGGER.debug(MARKER, "Registering Network {} v{}", INSTANCE.getName(), INSTANCE.getProtocolVersion());
    }

    public static SimpleChannel INSTANCE = ChannelBuilder.named(NAME).simpleChannel();
}