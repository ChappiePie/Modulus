package chappie.modulus;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

public class ModulusDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();

        pack.addProvider(ModulusLangGen::new);
    }

    static class ModulusLangGen extends FabricLanguageProvider {
        protected ModulusLangGen(FabricDataOutput dataOutput) {
            super(dataOutput);
        }

        @Override
        public void generateTranslations(TranslationBuilder translationBuilder) {
            translationBuilder.add("gui.modulus.mainScreen", "Modulus Screen");
            translationBuilder.add("narrator.modulus.button", "Modulus Menu");

            translationBuilder.add("key.categories.modulus", "Modulus");
            for (int i = 0; i < 5; i++) {
                translationBuilder.add("key.categories.modulus.ability.%s".formatted(i), "Ability key %s".formatted(i + 1));
            }

            translationBuilder.add("screen.modulus.tab.settings", "Settings");
            translationBuilder.add("screen.modulus.tab.mods", "Mods");
            translationBuilder.add("screen.modulus.tab.about", "About");

            translationBuilder.add("screen.modulus.modEntry.version", "Version, %s");

            translationBuilder.add("screen.modulus.creator", "Creator");
            translationBuilder.add("screen.modulus.socials", "Socials");
            translationBuilder.add("screen.modulus.modEntry.download", "Download");
            translationBuilder.add("commands.modulus.superpower.set.single", "%s superpower was given to %s");
            translationBuilder.add("commands.modulus.superpower.set.multiple", "%s superpower was given to %s entities");
            translationBuilder.add("commands.modulus.superpower.removed", "%s superpowers have been removed");
            translationBuilder.add("commands.modulus.superpower.removed.multiple", "Superpowers of %s entities have been removed");
            translationBuilder.add("commands.modulus.DidntExist", "Didn't exist");
        }
    }
}