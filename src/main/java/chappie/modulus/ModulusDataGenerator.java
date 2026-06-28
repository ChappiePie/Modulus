package chappie.modulus;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = Modulus.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModulusDataGenerator {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new ModulusLangGen(output, lookupProvider));
    }

    static class ModulusLangGen extends LanguageProvider {
        public ModulusLangGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
            super(output, Modulus.MODID, "en_us");
        }

        @Override
        protected void addTranslations() {
            add("gui.modulus.mainScreen", "Modulus Screen");
            add("narrator.modulus.button", "Modulus Menu");

            add("key.categories.modulus", "Modulus");
            for (int i = 0; i < 5; i++) {
                add("key.categories.modulus.ability.%s".formatted(i), "Ability key %s".formatted(i + 1));
            }

            add("screen.modulus.tab.settings", "Settings");
            add("screen.modulus.tab.mods", "Mods");
            add("screen.modulus.tab.about", "About");

            add("screen.modulus.modEntry.version", "Version, %s");

            add("screen.modulus.creator", "Creator");
            add("screen.modulus.socials", "Socials");
            add("screen.modulus.modEntry.download", "Download");
            add("commands.modulus.superpower.set.single", "%s superpower was given to %s");
            add("commands.modulus.superpower.set.multiple", "%s superpower was given to %s entities");
            add("commands.modulus.superpower.removed", "%s superpowers have been removed");
            add("commands.modulus.superpower.removed.multiple", "Superpowers of %s entities have been removed");
            add("commands.modulus.DidntExist", "Didn't exist");
        }
    }
}
