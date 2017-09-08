package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.youtube.model.LiveBroadcast;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by yanzhendong on 2017/9/6.
 */

public interface YouTubeLiveCreateContract {

    interface View extends JFGView {

        void showGooglePlayServicesAvailabilityErrorDialog(int connectionStatusCode);

        void onUserRecoverableAuthIOException(@NotNull UserRecoverableAuthIOException it);

        void onCreateLiveBroadcastSuccess(@Nullable LiveBroadcast liveBroadcast);
    }

    interface Presenter extends JFGPresenter<View> {

        void createLiveBroadcast(GoogleAccountCredential credential, String title, String description, long startTime, long endTime);
    }

}
