package cylan.scanbinding.view;

import android.content.Context;

/**
 * Created by hunt on 16-4-7.
 */
public interface BasicView {
    void onStart();

    void onFinish();

    Context getContext();
}
