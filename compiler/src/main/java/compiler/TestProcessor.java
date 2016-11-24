package compiler;

import com.cylan.annotation.Device;
import com.cylan.annotation.DpAnnotation;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;


/**
 * Created by com.cylan-hunt on 16-11-8.
 */

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class TestProcessor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(DpAnnotation.class.getCanonicalName());
        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        brewIdMap(roundEnv);
        BeanInfoGen.go(Device.CAMERA, processingEnv, roundEnv);
        BeanInfoGen.go(Device.BELL, processingEnv, roundEnv);
        BeanInfoGen.go(Device.CLOUD, processingEnv, roundEnv);
        BeanInfoGen.go(Device.MAG, processingEnv, roundEnv);
        return false;
    }

    private static final String ID_2_CLASS_MAP = "ID_2_CLASS_MAP";
    private static final String NAME_2_ID_MAP = "NAME_2_ID_MAP";
    private static final String ID_2_NAME_MAP = "ID_2_NAME_MAP";

    /**
     * public static final Map<String, Integer> IdMap = new HashMap<>();
     * static {
     * IdMap.put(NET, DP_ID_JFG_BEGIN + 1);
     * IdMap.put(MAC, DP_ID_JFG_BEGIN + 2);
     * }
     *
     * @param roundEnv
     */
    private void brewIdMap(RoundEnvironment roundEnv) {
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(DpAnnotation.class);
        //static block
        Map<Integer, String> mapVerify = new HashMap<>();
        Map<String, Integer> mapVerify_ = new HashMap<>();
        Map<String, Integer> fieldList = new HashMap<>();
        CodeBlock.Builder blockNameId = CodeBlock.builder();
        for (Element element : set) {
            DpAnnotation test = element.getAnnotation(DpAnnotation.class);
            final String name = element.getSimpleName().toString().toLowerCase();
            blockNameId.addStatement(NAME_2_ID_MAP + ".put($S,$L)", name, test.msgId());
            if (mapVerify.containsKey(test.msgId())) {
                throw new IllegalArgumentException("err happen.: 相同的key: " + name);
            }
            mapVerify.put(test.msgId(), name);

            if (mapVerify_.containsKey(name)) {
                throw new IllegalArgumentException("err happen.: 相同的value: " + name);
            }
            mapVerify_.put(name, test.msgId());
            fieldList.put(element.getSimpleName().toString(), test.msgId());
        }
        //static block
        CodeBlock.Builder blockId2NameClass = CodeBlock.builder();
        for (Element element : set) {
            DpAnnotation test = element.getAnnotation(DpAnnotation.class);
            final int msgId = test.msgId();
            blockId2NameClass.addStatement(ID_2_NAME_MAP + ".put($L,$S)", msgId, element.getSimpleName().toString().toLowerCase());
        }


        //static block
        CodeBlock.Builder blockIdClass = CodeBlock.builder();
        for (Element element : set) {
            DpAnnotation test = element.getAnnotation(DpAnnotation.class);
            final int msgId = test.msgId();
            TypeMirror clazzType = null;
            try {
                test.clazz();
            } catch (MirroredTypeException mte) {
                clazzType = mte.getTypeMirror();
            }
            blockIdClass.addStatement(ID_2_CLASS_MAP + ".put($L,$L)", msgId, clazzType + ".class");
        }

        TypeSpec.Builder fileBuilder =
                TypeSpec.classBuilder("DpMsgMap")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addField(getIdClassMap().build())
                        .addField(getNameIdMap().build())
                        .addField(getId2NameMap().build())
                        .addStaticBlock(blockNameId.build())
                        .addStaticBlock(blockId2NameClass.build())
                        .addStaticBlock(blockIdClass.build());
        addFinalStringField(fileBuilder, fieldList);
        addFinalIntField(fileBuilder, fieldList);
        TypeSpec msgIdMap = fileBuilder.build();
//                        .build();
//        package
        JavaFile javaFile = JavaFile.builder("com.cylan.jiafeigou.dp", msgIdMap)
                .build();

        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (Exception e) {
//            e.printStackTrace();
        }

    }

    /**
     * Map<String,Integer></>
     *
     * @return
     */
    private FieldSpec.Builder getNameIdMap() {
//        System.out.println("hunt: " + WildcardTypeName.subtypeOf(Object.class).toString());
        ParameterizedTypeName type = ParameterizedTypeName.get(Map.class,
                String.class,
                Integer.class);
        //field
        FieldSpec.Builder field = FieldSpec.builder(type,
                NAME_2_ID_MAP,
                Modifier.PUBLIC,
                Modifier.STATIC,
                Modifier.FINAL);
        field.initializer("new $T<$T,$T>()", HashMap.class, String.class, Integer.class);
        return field;
    }

    /**
     * Map<Integer,Class<?>></>
     *
     * @return
     */
    private FieldSpec.Builder getIdClassMap() {

        FieldSpec.CodeBlockBuilder blockBuilder = new FieldSpec.CodeBlockBuilder(
                CodeBlock.of(" Map<Integer, Class<?>> " + ID_2_CLASS_MAP
                        + " = new HashMap<Integer, Class<?>>()"));
        blockBuilder.addModifiers(Modifier.PUBLIC);
        blockBuilder.addModifiers(Modifier.STATIC);
        blockBuilder.addModifiers(Modifier.FINAL);
        return blockBuilder;
    }

    private FieldSpec.Builder getId2NameMap() {
        ParameterizedTypeName type = ParameterizedTypeName.get(Map.class,
                Integer.class,
                String.class);
        //field
        FieldSpec.Builder field = FieldSpec.builder(type,
                ID_2_NAME_MAP,
                Modifier.PUBLIC,
                Modifier.STATIC,
                Modifier.FINAL);
        field.initializer("new $T<$T,$T>()", HashMap.class, Integer.class, String.class);
        return field;
    }

    private void addFinalStringField(TypeSpec.Builder typeSpec, Map<String, Integer> fieldMap) {
        Iterator<String> i = fieldMap.keySet().iterator();
        while (i.hasNext()) {
            String name = i.next();
            FieldSpec.Builder field = FieldSpec.builder(String.class,
                    name + "_" + fieldMap.get(name),
                    Modifier.PUBLIC,
                    Modifier.STATIC,
                    Modifier.FINAL);
            field.initializer("$S", name.toLowerCase());
            typeSpec.addField(field.build());
        }
    }

    private void addFinalIntField(TypeSpec.Builder typeSpec, Map<String, Integer> fieldMap) {
        Iterator<String> i = fieldMap.keySet().iterator();
        while (i.hasNext()) {
            String name = i.next();
            FieldSpec.Builder field = FieldSpec.builder(int.class,
                    "ID_" + fieldMap.get(name) + "_" + name,
                    Modifier.PUBLIC,
                    Modifier.STATIC,
                    Modifier.FINAL);
            field.initializer("$L", fieldMap.get(name));
            typeSpec.addField(field.build());
        }
    }
}

