package com.cylan.jiafeigou.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import com.cylan.jiafeigou.R;
import cylan.log.DswLog;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

/**
 * Created by hebin on 2015/7/21.
 */
public class SafeChecker {


    private Context mContext;

    public SafeChecker(Context ctx) {
        this.mContext = ctx;
    }


    public boolean isOk() {
        DswLog.e("!checker.isDebuggable()-->" + !isDebuggable() + "------!checker.isOfficialAppName()-->" + !isOfficialAppName() + "-----!checker.isOfficialSignature()--->" + !isOfficialSignature());
        return (!isDebuggable() && (!isOfficialAppName() || !isOfficialSignature()));
    }

    private boolean isOfficialAppName() {
        String names[] = mContext.getString(R.string.test_name).split("\\|");
        boolean isOK = false;
        for (String name : names) {
            if (Utils.getApplicationName(mContext).equals(name)) {
                isOK = true;
                break;
            }

        }
        return isOK;
    }

    private boolean isOfficialSignature() {
        String str = Utils.getSignature(mContext.getPackageName(), mContext);

        boolean isSame = true;
        for (int i = 0; i < getSign().length; i++) {
            if (str != null) {
                if (Character.isDigit(str.charAt(i))) {
                    if (!getSign()[i].equals(Integer.toHexString(str.charAt(i)))) {
                        isSame = false;
                        break;
                    }
                } else {
                    if (!getSign()[i].equals(String.valueOf(str.charAt(i)))) {
                        isSame = false;
                        break;
                    }

                }
            }

        }
        return isSame;
    }

    private final static X500Principal DEBUG_DN = new X500Principal(
            "CN=Android Debug,O=Android,C=US");

    public boolean isDebuggable() {
        boolean debuggable = false;
        try {
            PackageInfo pinfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), PackageManager.GET_SIGNATURES);
            Signature signatures[] = pinfo.signatures;
            for (int i = 0; i < signatures.length; i++) {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                ByteArrayInputStream stream = new ByteArrayInputStream(signatures[i].toByteArray());
                X509Certificate cert = (X509Certificate) cf
                        .generateCertificate(stream);
                debuggable = cert.getSubjectX500Principal().equals(DEBUG_DN);
                if (debuggable)
                    break;
            }

        } catch (CertificateException e) {
        } catch (PackageManager.NameNotFoundException e) {
            DswLog.ex(e.toString());
        }
        return debuggable;
    }


    private String[] getSign() {
        String releaseSign[] = new String[32];
        releaseSign[0] = "38";
        releaseSign[1] = "37";
        releaseSign[2] = "36";
        releaseSign[3] = "31";
        releaseSign[4] = "35";
        releaseSign[5] = "37";
        releaseSign[6] = "30";
        releaseSign[7] = "c";
        releaseSign[8] = "31";
        releaseSign[9] = "33";
        releaseSign[10] = "33";
        releaseSign[11] = "34";
        releaseSign[12] = "b";
        releaseSign[13] = "f";
        releaseSign[14] = "b";
        releaseSign[15] = "39";
        releaseSign[16] = "d";
        releaseSign[17] = "34";
        releaseSign[18] = "b";
        releaseSign[19] = "f";
        releaseSign[20] = "34";
        releaseSign[21] = "35";
        releaseSign[22] = "31";
        releaseSign[23] = "30";
        releaseSign[24] = "30";
        releaseSign[25] = "f";
        releaseSign[26] = "31";
        releaseSign[27] = "37";
        releaseSign[28] = "37";
        releaseSign[29] = "32";
        releaseSign[30] = "37";
        releaseSign[31] = "38";

        return releaseSign;
    }
}
