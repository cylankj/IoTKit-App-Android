package com.cylan.compiler;

import com.cylan.annotation.DPTarget;
import com.squareup.javapoet.JavaFile;

import java.util.List;

import javax.lang.model.element.Element;

/**
 * Created by yzd on 17-1-14.
 */

public interface Generator {
    String MODULE_PACKAGE = "com.cylan.jiafeigou.base.module";
    String TYPE_DEFINE_PACKAGE = "com.cylan.jiafeigou.dp";
    String TYPE_DEFINE_NAME = "DpMsgDefine";

    JavaFile generator(List<Element> fields) throws Exception;

    void doProcessChain(ProcessChain chain);

    void setTarget(DPTarget target);
}
