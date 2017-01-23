package compiler;

import com.cylan.annotation.DpAnnotation;
import com.google.auto.service.AutoService;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;


/**
 * Created by com.cylan-hunt on 16-11-8.
 */

@AutoService(Processor.class)
public class MapProcessor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(DpAnnotation.class.getCanonicalName());
        return types;
    }

    private IdMap idMap;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (idMap == null) {
            idMap = new IdMap();
            idMap.brewIdMap(roundEnv, processingEnv);
        }
        return true;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}

