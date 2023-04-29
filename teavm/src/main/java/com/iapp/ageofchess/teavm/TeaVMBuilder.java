package com.iapp.ageofchess.teavm;

import com.github.xpenatan.gdx.backends.teavm.TeaBuildConfiguration;
import com.github.xpenatan.gdx.backends.teavm.TeaBuilder;
import com.github.xpenatan.gdx.backends.teavm.gen.SkipClass;
import org.teavm.tooling.TeaVMTool;

import java.io.File;
import java.io.IOException;

// TODO https://github.com/xpenatan/gdx-teavm
/** Builds the TeaVM/HTML application. */
@SkipClass
public class TeaVMBuilder {
    public static void main(String[] args) throws IOException {
        TeaBuildConfiguration teaBuildConfiguration = new TeaBuildConfiguration();
        teaBuildConfiguration.assetsPath.add(new File("../assets"));
        teaBuildConfiguration.webappPath = new File("build/dist").getCanonicalPath();
        // You can switch this setting during development:
        //teaBuildConfiguration.obfuscate = true;

        // Register any extra classpath assets here:
        // teaBuildConfiguration.additionalAssetsClasspathFiles.add("com/iapp/ageofchess/asset.extension");

        // Register any classes or packages that require reflection here:
        // TeaReflectionSupplier.addReflectionClass("com.iapp.ageofchess.reflect");

        TeaVMTool tool = TeaBuilder.config(teaBuildConfiguration);
        tool.setMainClass(TeaVMLauncher.class.getName());
        TeaBuilder.build(tool);
    }
}
