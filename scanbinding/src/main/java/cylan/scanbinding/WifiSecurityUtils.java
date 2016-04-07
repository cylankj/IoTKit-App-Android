package cylan.scanbinding;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.util.Log;

/**
 * Created by hunt on 15-8-27.
 */
public class WifiSecurityUtils {
    private static final String NONE = "NONE";
    private static final String[] WEP = {"WEP", "WEP"};
    private static final String[] EAP = {"802.1x", "802.1x EAP"};
    private static final String[] WPA = {"WPA", "WPA PSK"};
    private static final String[] WPA2 = {"WPA2", "WPA2 PSK"};
    private static final String[] WPA_WPA2 = {"WPA/WPA2", "WPA/WPA2 PSK"};

    /**
     * These values are matched in string arrays -- changes must be kept in sync
     */
    static final int SECURITY_NONE = 0;
    static final int SECURITY_WEP = 1;
    static final int SECURITY_PSK = 2;
    static final int SECURITY_EAP = 3;

    enum PskType {
        UNKNOWN,
        WPA,
        WPA2,
        WPA_WPA2
    }

    public static int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            return SECURITY_PSK;
        }
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) ||
                config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
            return SECURITY_EAP;
        }
        return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
    }

    public static int getSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_PSK;
        } else if (result.capabilities.contains("EAP")) {
            return SECURITY_EAP;
        }
        return SECURITY_NONE;
    }

    public static String getSecurityString(ScanResult result, boolean concise) {
        int security = getSecurity(result);
        switch (security) {
            case SECURITY_EAP:
                return concise ? EAP[0] : EAP[1];
            case SECURITY_PSK:
                PskType pskType = getPskType(result);
                switch (pskType) {
                    case WPA:
                        return concise ? WPA[0] : WPA[1];
                    case WPA2:
                        return concise ? WPA2[0] : WPA2[1];
                    case WPA_WPA2:
                        return concise ? WPA_WPA2[0] : WPA_WPA2[1];
                    case UNKNOWN:
                    default:
                        return concise ? WPA_WPA2[0] : WPA_WPA2[1];
                }
            case SECURITY_WEP:
                return concise ? WEP[0] : WEP[1];
            case SECURITY_NONE:
            default:
                return concise ? "" : NONE;
        }
    }

    private static PskType getPskType(ScanResult result) {
        boolean wpa = result.capabilities.contains("WPA-PSK");
        boolean wpa2 = result.capabilities.contains("WPA2-PSK");
        if (wpa2 && wpa) {
            return PskType.WPA_WPA2;
        } else if (wpa2) {
            return PskType.WPA2;
        } else if (wpa) {
            return PskType.WPA;
        } else {
            Log.w("", "Received abnormal flag string: " + result.capabilities);
            return PskType.UNKNOWN;
        }
    }

}
