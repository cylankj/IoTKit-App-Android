package cylan.scanbinding;

import android.text.TextUtils;

import java.util.List;

/**
 * Created by hunt on 16-4-6.
 */
class Utils {
    public static String removeDoubleQuotes(String string) {
        if (TextUtils.isEmpty(string)) return "";
        int length = string.length();
        if ((length > 1) && (string.charAt(0) == '"')
                && (string.charAt(length - 1) == '"')) {
            return string.substring(1, length - 1);
        }
        return string;
    }

    public static boolean contains(List<String> list, String target) {
        final int count = list == null ? 0 : list.size();
        if (TextUtils.isEmpty(target))
            return false;
        for (int i = 0; i < count; i++) {
            final String str = list.get(i);
            if (!TextUtils.isEmpty(str) && target.contains(str))
                return true;
        }
        return false;
    }
}
