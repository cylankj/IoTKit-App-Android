package cylan.mta;

import android.content.Context;
import android.util.Log;

import com.tencent.stat.StatConfig;

/**
 * Created by hunt on 16-4-5.
 */
public class MtaManager {

    public static void init(Context context, boolean debug) {
        Log.d("MtaManager", "MtaManager: " + StatConfig.getAppKey(context));
    }
}
