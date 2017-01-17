package com.cylan.jiafeigou;

import com.squareup.javapoet.TypeSpec;

/**
 * Created by yzd on 17-1-16.
 */

public interface ProcessChain {

    void doProcessChain(TypeSpec.Builder builder, String clazzName, String parentName);
}
