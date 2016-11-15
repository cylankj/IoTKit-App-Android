package compiler;

import com.cylan.annotation.DpAnnotation;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;


/**
 * Created by com.cylan-hunt on 16-11-8.
 */

@AutoService(Processor.class)
public class TestProcessor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(DpAnnotation.class.getCanonicalName());
        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> set = roundEnv.getElementsAnnotatedWith(DpAnnotation.class);
        brewIdMap(set);
        return false;
    }

    private static final String ID_2_CLASS_MAP = "ID_2_CLASS_MAP";
    private static final String NAME_2_ID_MAP = "NAME_2_ID_MAP";

    /**
     * public static final Map<String, Integer> IdMap = new HashMap<>();
     * static {
     * IdMap.put(NET, DP_ID_JFG_BEGIN + 1);
     * IdMap.put(MAC, DP_ID_JFG_BEGIN + 2);
     * }
     *
     * @param set
     */
    private void brewIdMap(Set<? extends Element> set) {
        //static block
        Map<Integer, String> mapVerify = new HashMap<>();
        Map<String, Integer> mapVerify_ = new HashMap<>();
        CodeBlock.Builder blockNameId = CodeBlock.builder();
        for (Element element : set) {
            DpAnnotation test = element.getAnnotation(DpAnnotation.class);
            blockNameId.addStatement(NAME_2_ID_MAP + ".put($S,$L)", element.getSimpleName(), test.msgId());
            if (mapVerify.containsKey(test.msgId())) {
                throw new IllegalArgumentException("err happen.: 相同的key: " + element.getSimpleName());
            }
            mapVerify.put(test.msgId(), element.getSimpleName().toString());

            if (mapVerify_.containsKey(element.getSimpleName().toString())) {
                throw new IllegalArgumentException("err happen.: 相同的value: " + element.getSimpleName());
            }
            mapVerify_.put(element.getSimpleName().toString(), test.msgId());
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

        TypeSpec msgIdMap =
                TypeSpec.classBuilder("DpMsgMap")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addField(getIdClassMap().build())
                        .addField(getNameIdMap().build())
                        .addStaticBlock(blockNameId.build())
                        .addStaticBlock(blockIdClass.build())
                        .build();
//        package
        JavaFile javaFile = JavaFile.builder("com.cylan.jiafeigou.dp", msgIdMap)
                .build();

        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Map<String,Integer></>
     *
     * @return
     */
    private FieldSpec.Builder getNameIdMap() {
        System.out.println("hunt: " + WildcardTypeName.subtypeOf(Object.class).toString());
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
}

