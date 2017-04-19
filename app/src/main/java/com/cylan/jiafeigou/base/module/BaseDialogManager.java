package com.cylan.jiafeigou.base.module;

import com.cylan.jiafeigou.base.view.IDialogManager;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by yanzhendong on 2017/4/19.
 */

//弹出框管理器,包括 Alert, Dialog,DialogFragment
@Singleton
public class BaseDialogManager implements IDialogManager {

    @Inject
    public BaseDialogManager() {
    }


    @Override
    public <T extends IDialogBuilder> T getDialogBuilder(int dialogType) {
        return null;
    }
}
