package com.cylan.jiafeigou.processor;

import com.cylan.jiafeigou.support.badge.Badge;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
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
import java.util.TreeSet;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Created by hds on 17-6-9.
 */

@AutoService(Processor.class)
public class BadgeProcessor extends AbstractProcessor {
    private boolean run = false;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(Badge.class.getCanonicalName());
        return types;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (!run) {
            run = true;
        } else {
            return run = true;
        }
        collected(roundEnvironment);
        return true;
    }


    /**
     * 检查treeNode是否孤立
     */
    private void verifyTreeNode(Map<String, String> simpleMap) {
        if (simpleMap == null || simpleMap.size() == 0)
            throw new IllegalArgumentException("没有BadgeNode, 出错了?");
        //2.不能有孤立的node
        Iterator<String> iterator = simpleMap.keySet().iterator();
        //检查是否有断裂的节点.
        while (iterator.hasNext()) {
            final String self = iterator.next();
            if (isolated(simpleMap, self))
                throw new IllegalArgumentException("出现孤立的节点? " + self);
        }
    }

    /**
     * 检查点是否孤立
     *
     * @param map
     * @param nodeKey
     * @return
     */
    private boolean isolated(Map<String, String> map, String nodeKey) {
        //check child
        final String nodeParentKey = map.get(nodeKey);
        Map<String, String> tmpMap = new HashMap<>(map);
        tmpMap.remove(nodeKey);
        boolean hasParent = tmpMap.containsKey(nodeParentKey);
        boolean hasChild = false;
        //check parent
        Iterator<String> keyIterator = tmpMap.keySet().iterator();
        while (keyIterator.hasNext()) {
            final String key = keyIterator.next();
            final String value = tmpMap.get(key);
            if (value != null && value.equals(nodeKey)) {
                hasChild = true;
            }
        }
        return !hasChild && !hasParent;
    }


    private void collected(RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Badge.class);
        Map<String, String> simpleMap = new HashMap<>();
        TreeSet<String> asRefreshMap = new TreeSet<>();
        for (Element element : elements) {
            Badge test = element.getAnnotation(Badge.class);
            final String name = element.asType().toString();
            final String s = test.parentTag();
            if (test.asRefresh()) {
                asRefreshMap.add(name.substring(name.lastIndexOf(".") + 1));
                System.out.println("asRefresh?" + name);
            }
            if (simpleMap.containsKey(name))
                throw new IllegalArgumentException("相同的key? " + name);
            System.out.println("result>>>" + name);
            System.out.println("s>>>" + s);
            simpleMap.put(name.substring(name.lastIndexOf(".") + 1), s);
        }
        verifyTreeNode(simpleMap);
//        TypeName wildcard = WildcardTypeName.subtypeOf(Object.class);
//        TypeName classOfAny = ParameterizedTypeName.get(ClassName.get(Class.class), wildcard);
//        TypeName mapOfStringAndClassOfAny = ParameterizedTypeName.get(ClassName.get(Map.class), classOfAny, classOfAny);
//        TypeName hashMapOfStringAndClassOfAny = ParameterizedTypeName.get(ClassName.get(HashMap.class), classOfAny, classOfAny);
//
        ParameterizedTypeName typeName = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(String.class));

        FieldSpec fieldSpec = FieldSpec.builder(typeName, treeMap)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .initializer("new $T<>()", HashMap.class)
                .build();

        FieldSpec fieldSpecSet = FieldSpec.builder(ParameterizedTypeName.get(
                ClassName.get(TreeSet.class),
                ClassName.get(String.class)), treeSet)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .initializer("new $T<>()", TreeSet.class)
                .build();

        TypeSpec fieldImpl = TypeSpec.classBuilder("RawTree")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(fieldSpec)
                .addField(fieldSpecSet)
                .addStaticBlock(getFieldBlock(simpleMap))
                .addStaticBlock(addAsRefreshTree(asRefreshMap))
                .build();
        try {
            JavaFile javaFile = JavaFile.builder("com.cylan.jiafeigou.misc", fieldImpl)
                    .indent("    ")
                    .addFileComment("使用Class<?>,兼容proguard")
                    .build();
            javaFile.writeTo(processingEnv.getFiler());
        } catch (Exception e) {
        }
    }

    private static final String treeMap = "treeMap";
    private static final String treeSet = "asRefreshTreeSet";


    private CodeBlock addAsRefreshTree(Set<String> set) {
        CodeBlock.Builder blockNameId = CodeBlock.builder();
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext()) {
            blockNameId.addStatement(treeSet + ".add($S)", iterator.next());
        }
        return blockNameId.build();
    }

    private CodeBlock getFieldBlock(Map<String, String> nameMap) {
        CodeBlock.Builder blockNameId = CodeBlock.builder();
        Iterator<String> iterator = nameMap.keySet().iterator();
        while (iterator.hasNext()) {
            final String name = iterator.next();
            blockNameId.addStatement(treeMap + ".put($S, $S)", name, nameMap.get(name));
        }
        return blockNameId.build();
    }
}
