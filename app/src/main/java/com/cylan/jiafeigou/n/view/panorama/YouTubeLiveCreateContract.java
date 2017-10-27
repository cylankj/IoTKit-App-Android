package com.cylan.jiafeigou.n.view.panorama;

import com.cylan.jiafeigou.base.view.JFGPresenter;
import com.cylan.jiafeigou.base.view.JFGView;
import com.cylan.jiafeigou.rtmp.youtube.util.EventData;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by yanzhendong on 2017/9/6.
 */

public interface YouTubeLiveCreateContract {

    interface View extends JFGView {

        void showGooglePlayServicesAvailabilityErrorDialog(int connectionStatusCode);

        void onUserRecoverableAuthIOException(@NotNull UserRecoverableAuthIOException it);

        void onCreateLiveBroadcastSuccess(@Nullable EventData eventData);

        void onCreateLiveBroadcastTimeout();

        void onAuthorizationException();
    }

    interface Presenter extends JFGPresenter {

        void createLiveBroadcast(GoogleAccountCredential credential, String title, String description, long startTime, long endTime);

        void createLiveBroadcast(String title, String description, long startTime, long endTime);
    }

}
