package cylan.scanbinding.presenter.impl;

import android.net.wifi.ScanResult;

import java.util.List;

import cylan.scanbinding.ScanConfigure;
import cylan.scanbinding.presenter.Presenter;


/**
 * Created by hunt on 16-4-6.
 */
public class ScanPresenter implements Presenter {

    private Presenter presenter;
    ScanConfigure.Builder builder;

    public ScanPresenter(Presenter presenter) {

    }

    public void setConfigure(ScanConfigure configure) {
        this.builder = configure.getBuilder();
    }

    @Override
    public void onScan(boolean actionResult) {
        if (presenter != null) presenter.onScan(actionResult);
    }

    @Override
    public void onResult(List<ScanResult> scanResultList) {
        if (presenter != null) presenter.onResult(scanResultList);
    }
}
