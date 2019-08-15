package fr.neamar.kiss.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.UserManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import fr.neamar.kiss.pojo.ShortcutsPojo;
import fr.neamar.kiss.shortcut.SaveOreoShortcutAsync;

import static android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC;
import static android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST;
import static android.content.pm.LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED;

public class ShortcutUtil {

    final static private String TAG = "ShortcutUtil";

    public static boolean areShortcutsEnabled(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                prefs.getBoolean("enable-shortcuts", true);

    }

    public static void buildShortcuts(Context context){
        if (areShortcutsEnabled(context)) {
                new SaveOreoShortcutAsync(context).execute();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static List<ShortcutInfo> getAllShortcuts(Context context) {
        return getShortcut(context, null);
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static List<ShortcutInfo> getShortcut(Context context, String packageName) {
        List<ShortcutInfo> shortcutInfoList = new ArrayList<>();

        UserManager manager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        LauncherApps launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);

        LauncherApps.ShortcutQuery shortcutQuery = new LauncherApps.ShortcutQuery();
        shortcutQuery.setQueryFlags(FLAG_MATCH_DYNAMIC | FLAG_MATCH_MANIFEST | FLAG_MATCH_PINNED);

        if(!TextUtils.isEmpty(packageName)){
            shortcutQuery.setPackage(packageName);
        }

        for (android.os.UserHandle profile : manager.getUserProfiles()) {
            shortcutInfoList.addAll(launcherApps.getShortcuts(shortcutQuery, profile));
        }

        return shortcutInfoList;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static ShortcutsPojo createShortcutPojo(Context context, ShortcutInfo shortcutInfo){

        LauncherApps launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        // id isn't used after being saved in the DB.
        String id = ShortcutsPojo.SCHEME + ShortcutsPojo.OREO_PREFIX + shortcutInfo.getId();

        final Drawable iconDrawable = launcherApps.getShortcutIconDrawable(shortcutInfo, 0);
        ShortcutsPojo pojo = new ShortcutsPojo(id, shortcutInfo.getPackage(), shortcutInfo.getId(),
                DrawableUtils.drawableToBitmap(iconDrawable));

        if (shortcutInfo.getShortLabel() != null) {
            pojo.setName(shortcutInfo.getShortLabel().toString());
        } else if (shortcutInfo.getLongLabel() != null) {
            pojo.setName(shortcutInfo.getLongLabel().toString());
        } else {
            Log.d(TAG, "Invalid shortcut " + pojo.id + ", ignoring");
            return null;
        }

        return pojo;
    }

}
