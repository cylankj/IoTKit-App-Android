package com.cylan.jiafeigou.misc;

import android.content.Context;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.cylan.jiafeigou.support.log.AppLogger;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by hds on 17-6-28.
 */

public class CheckServerTrustedWebViewClient extends WebViewClient {

    private static final String TAG = "TrustedWebViewClient";

    private TrustManagerFactory tmf = null;
    private Context context;

    public CheckServerTrustedWebViewClient(Context context) throws
            CertificateException,
            NoSuchAlgorithmException,
            KeyStoreException,
            IOException {
        this.context = context;
        initTrustStore();
    }

    private void initTrustStore() throws java.security.cert.CertificateException, FileNotFoundException,
            IOException, KeyStoreException, NoSuchAlgorithmException {

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore trustedKeyStore = KeyStore.getInstance(keyStoreType);
        trustedKeyStore.load(null, null);

        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        InputStream caInput = new BufferedInputStream(context.getResources().getAssets().open("jfg_ca.crt"));
        Certificate ca;
        try {
            ca = cf.generateCertificate(caInput);
            Log.d(TAG, "ca-root DN=" + ((X509Certificate) ca).getSubjectDN());
        } finally {
            caInput.close();
        }
        trustedKeyStore.setCertificateEntry("ca", ca);

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(trustedKeyStore);

    }

    @Override
    public void onReceivedSslError(WebView view, final SslErrorHandler handler,
                                   SslError error) {
        AppLogger.d("onReceivedSslError");
        boolean passVerify = false;
        if (error.getPrimaryError() == SslError.SSL_UNTRUSTED) {
            SslCertificate cert = error.getCertificate();
            String subjectDN = cert.getIssuedTo().getDName();
            Log.d(TAG, "subjectDN: " + subjectDN);
            try {
                Field f = cert.getClass().getDeclaredField("mX509Certificate");
                f.setAccessible(true);
                X509Certificate x509 = (X509Certificate) f.get(cert);
                X509Certificate[] chain = {x509};
                for (TrustManager trustManager : tmf.getTrustManagers()) {
                    if (trustManager instanceof X509TrustManager) {
                        X509TrustManager x509TrustManager = (X509TrustManager) trustManager;
                        try {
                            x509TrustManager.checkServerTrusted(chain, "generic");
                            passVerify = true;
                            break;
                        } catch (Exception e) {
                            Log.e(TAG, "verify trustManager failed", e);
                            passVerify = false;
                        }
                    }
                }
                Log.d(TAG, "passVerify: " + passVerify);
            } catch (Exception e) {
                Log.e(TAG, "verify cert fail", e);
            }
        }
        if (passVerify) {
            handler.proceed();
        } else {
            handler.cancel();
        }
    }
}
