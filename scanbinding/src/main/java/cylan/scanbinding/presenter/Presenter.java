package cylan.scanbinding.presenter;

import android.net.wifi.ScanResult;

import java.util.List;

/**
 * Created by hunt on 16-4-6.
 */
public interface Presenter {
    /**
     * means start to scan ,so you can do some ui update .
     */
    void onScan(final boolean actionResult);

    /**
     * @param scanResultList : when get result list
     */
    void onResult(List<ScanResult> scanResultList);
}
