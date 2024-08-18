package net.minestom.codegen.entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.javapoet.*;
import net.minestom.codegen.MinestomCodeGenerator;
import net.minestom.codegen.util.GenerationHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static net.minestom.codegen.util.GenerationHelper.ADVENTURE_KEY;
import static net.minestom.codegen.util.GenerationHelper.TO_STRING;

@ApiStatus.NonExtendable
@ApiStatus.Internal
public final class VillagerTypeGenerator extends MinestomCodeGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(VillagerTypeGenerator.class);
    private final InputStream villagerTypesFile;
    private final File outputFolder;

    public VillagerTypeGenerator(@Nullable InputStream villagerTypesFile, @NotNull File outputFolder) {
        this.villagerTypesFile = villagerTypesFile;
        this.outputFolder = outputFolder;
    }

    @Override
    public void generate() {
        if (villagerTypesFile == null) {
            LOGGER.error("Failed to find villager_types.json.");
            LOGGER.error("Stopped code generation for villager types.");
            return;
        }
        if (!outputFolder.exists() && !outputFolder.mkdirs()) {
            LOGGER.error("Output folder for code generation does not exist and could not be created.");
            return;
        }
        // Important classes we use alot
        JsonArray villagerTypes = GSON.fromJson(new InputStreamReader(villagerTypesFile), JsonArray.class);
        ClassName villagerTypeClassName = ClassName.get("net.minestom.server.entity.metadata.villager", "VillagerType");

        // Particle
        TypeSpec.Builder villagerTypeClass = TypeSpec.classBuilder(villagerTypeClassName)
                .addSuperinterface(KEYORI_ADVENTURE_KEY)
                .addModifiers(Modifier.PUBLIC).addJavadoc("AUTOGENERATED by " + getClass().getSimpleName());
        villagerTypeClass.addField(
                FieldSpec.builder(NAMESPACE_ID_CLASS, "id")
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL).addAnnotation(NotNull.class).build()
        );
        villagerTypeClass.addMethod(
                MethodSpec.constructorBuilder()
                        .addParameter(ParameterSpec.builder(NAMESPACE_ID_CLASS, "id").addAnnotation(NotNull.class).build())
                        .addStatement("this.id = id")
                        .addModifiers(Modifier.PROTECTED)
                        .build()
        );
        // Override key method (adventure)
        villagerTypeClass.addMethod(GenerationHelper.ID_GETTER);
        // getId method
        villagerTypeClass.addMethod(
                MethodSpec.methodBuilder("getId")
                        .returns(NAMESPACE_ID_CLASS)
                        .addAnnotation(NotNull.class)
                        .addStatement("return this.id")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // getNumericalId
        villagerTypeClass.addMethod(
                MethodSpec.methodBuilder("getNumericalId")
                        .returns(TypeName.INT)
                        .addStatement("return $T.VILLAGER_TYPE_REGISTRY.getId(this)", REGISTRY_CLASS)
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // fromId Method
        villagerTypeClass.addMethod(
                MethodSpec.methodBuilder("fromId")
                        .returns(villagerTypeClassName)
                        .addAnnotation(Nullable.class)
                        .addParameter(TypeName.INT, "id")
                        .addStatement("return $T.VILLAGER_TYPE_REGISTRY.get((short) id)", REGISTRY_CLASS)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .build()
        );
        // fromId Method
        villagerTypeClass.addMethod(
                MethodSpec.methodBuilder("fromId")
                        .returns(villagerTypeClassName)
                        .addAnnotation(NotNull.class)
                        .addParameter(ADVENTURE_KEY, "id")
                        .addStatement("return $T.VILLAGER_TYPE_REGISTRY.get(id)", REGISTRY_CLASS)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .build()
        );
        // toString method
        villagerTypeClass.addMethod(TO_STRING);
        // values method
        villagerTypeClass.addMethod(
                MethodSpec.methodBuilder("values")
                        .addAnnotation(NotNull.class)
                        .returns(ParameterizedTypeName.get(ClassName.get(List.class), villagerTypeClassName))
                        .addStatement("return $T.VILLAGER_TYPE_REGISTRY.values()", REGISTRY_CLASS)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .build()
        );
        CodeBlock.Builder staticBlock = CodeBlock.builder();
        // Use data
        for (JsonElement vp : villagerTypes) {
            JsonObject villagerProfession = vp.getAsJsonObject();

            String villagerProfessionName = villagerProfession.get("name").getAsString();

            villagerTypeClass.addField(
                    FieldSpec.builder(
                            villagerTypeClassName,
                            villagerProfessionName
                    ).initializer(
                            "new $T($T.from($S))",
                            villagerTypeClassName,
                            NAMESPACE_ID_CLASS,
                            villagerProfession.get("id").getAsString()
                    ).addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).build()
            );
            // Add to static init.
            staticBlock.addStatement("$T.VILLAGER_TYPE_REGISTRY.register($N)", REGISTRY_CLASS, villagerProfessionName);
        }

        villagerTypeClass.addStaticBlock(staticBlock.build());

        // Write files to outputFolder
        writeFiles(
                List.of(
                        JavaFile.builder("net.minestom.server.entity.metadata.villager", villagerTypeClass.build())
                                .indent(DEFAULT_INDENT)
                                .skipJavaLangImports(true)
                                .build()
                ),
                outputFolder
        );
    }
}
