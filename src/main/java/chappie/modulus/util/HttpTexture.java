package chappie.modulus.util;

import chappie.modulus.Modulus;
import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Environment(EnvType.CLIENT)
public class HttpTexture implements AutoCloseable {
    private final TextureManager textureManager;
    private final ResourceLocation textureLocation;
    private final ResourceLocation withoutInternet;
    private final String urlString;
    private final @Nullable File file;
    @Nullable
    private DynamicTexture texture;
    private boolean closed;

    private HttpTexture(TextureManager textureManager, @Nullable File file, String urlString, ResourceLocation textureLocation, ResourceLocation withoutInternet) {
        this.textureManager = textureManager;
        this.file = file;
        this.urlString = urlString;
        this.textureLocation = textureLocation;
        this.withoutInternet = withoutInternet == null ? Modulus.id("textures/gui/white.png") : withoutInternet;
    }

    public static ResourceLocation byUrl(@Nullable File file, String urlString, ResourceLocation textureLocation, @Nullable ResourceLocation withoutInternet) {
        HttpTexture texture = new HttpTexture(Minecraft.getInstance().getTextureManager(), file, urlString, textureLocation, withoutInternet);
        texture.loadIcon();
        return texture.textureLocation;
    }

    public void upload(NativeImage image) {
        if (image.getWidth() > 0 && image.getHeight() > 0) {
            try {
                this.checkOpen();
                if (this.texture == null) {
                    this.texture = new DynamicTexture(image);
                } else {
                    this.texture.setPixels(image);
                    this.texture.upload();
                }

                this.textureManager.register(this.textureLocation, this.texture);
            } catch (Throwable var3) {
                image.close();
                this.clear();
                throw var3;
            }
        } else {
            image.close();
            throw new IllegalArgumentException("Icon must be 64x64, but was " + image.getWidth() + "x" + image.getHeight());
        }
    }

    public void clear() {
        this.checkOpen();
        if (this.texture != null) {
            this.textureManager.release(this.textureLocation);
            this.texture.close();
            this.texture = null;
        }
    }

    public ResourceLocation textureLocation() {
        return this.texture != null ? this.textureLocation : this.withoutInternet;
    }

    public void close() {
        this.clear();
        this.closed = true;
    }

    private void checkOpen() {
        if (this.closed) {
            throw new IllegalStateException("Texture already closed");
        }
    }

    private void loadIcon() {
        try {
            InputStream inputStream = null;
            if (this.file != null && this.file.isFile()) {
                Modulus.LOGGER.debug("Loading http texture from local cache ({})", this.file);
                inputStream = new FileInputStream(this.file);
            } else {
                HttpURLConnection httpURLConnection = null;
                Modulus.LOGGER.debug("Downloading http texture from {} to {}", this.urlString, this.file);

                try {
                    httpURLConnection = (HttpURLConnection) new URL(this.urlString).openConnection(Minecraft.getInstance().getProxy());
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setDoOutput(false);
                    httpURLConnection.connect();
                    if (httpURLConnection.getResponseCode() / 100 == 2) {
                        if (this.file != null) {
                            FileUtils.copyInputStreamToFile(httpURLConnection.getInputStream(), this.file);
                            inputStream = new FileInputStream(this.file);
                        } else {
                            inputStream = httpURLConnection.getInputStream();
                        }
                    }
                } catch (Exception var6) {
                    Modulus.LOGGER.error("Couldn't download http texture", var6);
                } finally {
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                }
            }

            try {
                this.upload(NativeImage.read(inputStream));
            } catch (Throwable var6) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable var5) {
                        var6.addSuppressed(var5);
                    }
                }

                throw var6;
            }

            inputStream.close();
        } catch (Throwable var7) {
            Modulus.LOGGER.error("Invalid texture for url {}", this.urlString, var7);
        }
    }
}
