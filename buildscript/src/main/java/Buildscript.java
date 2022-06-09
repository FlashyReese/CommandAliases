import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import io.github.coolcrabs.brachyura.decompiler.BrachyuraDecompiler;
import io.github.coolcrabs.brachyura.decompiler.fernflower.FernflowerDecompiler;
import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.fabric.FabricContext.ModDependencyCollector;
import io.github.coolcrabs.brachyura.fabric.FabricContext.ModDependencyFlag;
import io.github.coolcrabs.brachyura.fabric.FabricLoader;
import io.github.coolcrabs.brachyura.fabric.FabricMaven;
import io.github.coolcrabs.brachyura.fabric.SimpleFabricProject;
import io.github.coolcrabs.brachyura.fabric.Yarn;
import io.github.coolcrabs.brachyura.maven.Maven;
import io.github.coolcrabs.brachyura.maven.MavenId;
import io.github.coolcrabs.brachyura.minecraft.Minecraft;
import io.github.coolcrabs.brachyura.minecraft.VersionMeta;
import io.github.coolcrabs.brachyura.processing.ProcessorChain;
import io.github.coolcrabs.brachyura.util.AtomicFile;
import io.github.coolcrabs.brachyura.util.Util;
import net.fabricmc.mappingio.tree.MappingTree;

public class Buildscript extends SimpleFabricProject {
    @Override
    public VersionMeta createMcVersion() {
        return Minecraft.getVersion(Versions.MINECRAFT_VERSION);
    }

    @Override
    public MappingTree createMappings() {
        return Yarn.ofMaven(FabricMaven.URL, FabricMaven.yarn(Versions.YARN_MAPPINGS)).tree;
    }

    @Override
    public FabricLoader getLoader() {
        return new FabricLoader(FabricMaven.URL, FabricMaven.loader(Versions.LOADER_VERSION));
    }

    @Override
    public String getModId() {
        return "commandaliases";
    }

    @Override
    public String getVersion() {
        return Versions.MOD_VERSION;
    }

    @Override
    public void getModDependencies(ModDependencyCollector d) {
        // Fabric API
        d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-lifecycle-events-v1", Versions.FABRIC_LIFECYCLE_EVENTS), ModDependencyFlag.RUNTIME, ModDependencyFlag.COMPILE);
        d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-command-api-v1", Versions.FABRIC_COMMANDS_API), ModDependencyFlag.RUNTIME, ModDependencyFlag.COMPILE);
        d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-api-base", Versions.FABRIC_API_BASE), ModDependencyFlag.RUNTIME, ModDependencyFlag.COMPILE);
        // Fabric Permissions API (weird directory structure so needs to be manually done)
        //jij(d.addMaven("https://oss.sonatype.org/content/repositories/snapshots/", new MavenId("me.lucko", "fabric-permissions-api", Versions.PERMISSIONS_API_VERSION), ModDependencyFlag.RUNTIME, ModDependencyFlag.COMPILE));
        try {
            Path target = getLocalBrachyuraPath().resolve("fabric-permissions-api-0.1-20210101.232536-1.jar");
            if (!Files.exists(target)) {
                try (
                    AtomicFile f = new AtomicFile(target);
                    InputStream is = new URL("https://oss.sonatype.org/content/repositories/snapshots/me/lucko/fabric-permissions-api/0.1-SNAPSHOT/fabric-permissions-api-0.1-20210101.232536-1.jar").openStream();
                ) {
                    Files.copy(is, f.tempPath, StandardCopyOption.REPLACE_EXISTING);
                    f.commit();
                }
            }
            jij(d.add(new JavaJarDependency(target, null, null), ModDependencyFlag.RUNTIME, ModDependencyFlag.COMPILE));
        } catch (Exception e) {
            Util.sneak(e);
        }
        // LazyDFU
        d.addMaven("https://api.modrinth.com/maven", new MavenId("maven.modrinth", "lazydfu", Versions.LAZYDFU_VERSION), ModDependencyFlag.RUNTIME);
    }

    @Override
    public int getJavaVersion() {
        return Versions.JAVA_VERSION;
    }

    @Override
    public BrachyuraDecompiler decompiler() {
        return new FernflowerDecompiler(Maven.getMavenJarDep("https://maven.quiltmc.org/repository/release", new MavenId("org.quiltmc", "quiltflower", Versions.QUILTFLOWER_VERSION)));
    };

    @Override
    public Path getBuildJarPath() {
        return getBuildLibsDir().resolve(getModId() + "-" + "mc" + createMcVersion().version + "-" + getVersion() + ".jar");
    }

    @Override
    public ProcessorChain resourcesProcessingChain() {
        return new ProcessorChain(super.resourcesProcessingChain(), new FmjVersionFixer());
    }
}
