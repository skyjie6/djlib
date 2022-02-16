package com.skyjie.djlib.utils.device;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by dangjie on 16-12-8.
 */
public class SystemUtil {

    /**
     * 扫描系统设置包名
     * @return pkg
     */
    private static Intent scanSettingPkgByAction(String action, Context context) {
        if (action == null) {
            return null;
        }

        ComponentName componentName = null;
        Intent tempIntent = null;
        String pkgName = null;
        String className = null;

//        PreferencesManager	preferencesManager = PreferencesManager.getSharedPreference(context, IPreferencesIds.SYSTEM_SETTING_PKG, Context.MODE_PRIVATE);
//        String component = preferencesManager.getString(action, null);
        String component = null;
        if (component != null) {
            // 有记录
            String[] splitStrings = component.split("/");
            pkgName = splitStrings[0];
            className = splitStrings[1];
        } else {
            if (action.startsWith("com")) {
                pkgName = "com.android.settings";
                className = action;
            } else if (action.startsWith("android")) {
                Intent queryIntent = new Intent(action);
                PackageManager pm = context.getPackageManager();
                List<ResolveInfo> list = pm.queryIntentActivities(queryIntent, 0);
                if (list != null && list.size() > 0) {
                    ResolveInfo info = list.get(0);
                    pkgName = info.activityInfo.packageName;
                    className = info.activityInfo.name;
                }
            }
//            preferencesManager.putString(action, pkgName + "/" + className);
//            preferencesManager.commit();
        }

        if (pkgName != null && className != null) {
            componentName = new ComponentName(pkgName, className);
            tempIntent = new Intent(Intent.ACTION_MAIN);
            tempIntent.setComponent(componentName);
        }

        return tempIntent;
    }

    public static void startSystemSetting(Context context, String action) {
        Intent intent = scanSettingPkgByAction(action, context);
        if (intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (action.equals(Settings.ACTION_DATA_ROAMING_SETTINGS) && (Machine.isNexus() || Machine.isSamsung())) {
                ComponentName cName = new ComponentName("com.android.settings","com.android.settings.Settings$DataUsageSummaryActivity");
                intent.setComponent(cName);
            }
            startActivitySafety(context, intent);
        }
    }

    private static void startActivitySafety(Context context, Intent intent) {
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            // 找不到对应设置项的情况下，先弹出提示，然后跳转到系统设置界面
//            Toast.makeText(context, R.string.system_settings_start_failed_tip, Toast.LENGTH_LONG).show();

            intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Settings.ACTION_SETTINGS);
            context.startActivity(intent);
        }
    }

    public static boolean isSystemAlertPermissionGranted(Context context) {
        final boolean result = !Machine.IS_M || canDrawOverlays(context);
        return result;
    }

    /**
     * 反射API-23 的 Setting.canDrawOverlays(Context)
     * @param context
     * @return
     */
    public static boolean canDrawOverlays(Context context) {
        try {
            Class<?> clazz = Class.forName("android.provider.Settings");
            Method method = clazz.getMethod("canDrawOverlays", Context.class);
            return (boolean) method.invoke(null, context);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * 反射API-23 的 Setting.canDrawOverlays(Context)
     * @return
     */
    public static boolean isCallingPackageAllowedToDrawOverlays(Context context, int uid, String callingPackage, boolean throwException) {
        try {
            Class<?> clazz = Class.forName("android.provider.Settings");
            Method method = clazz.getMethod("isCallingPackageAllowedToDrawOverlays", Context.class, Integer.class, String.class, Boolean.class);
            return (boolean) method.invoke(null, context, uid, callingPackage, throwException);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    @TargetApi(23)
    public static void requestSystemAlertPermission(Activity context, Fragment fragment, int requestCode) {
        if (!Machine.IS_M) {
            return;
        }
        String packageName = context == null ? fragment.getActivity().getPackageName() : context.getPackageName();
        Intent intent = new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse("package:" + packageName));
        if (fragment != null) {
            fragment.startActivityForResult(intent, requestCode);
        } else {
            context.startActivityForResult(intent, requestCode);
        }
    }


    /**
     * 4.4 以上可以直接判断准确
     *
     * 4.4 以下非MIUI直接返回true
     *
     * 4.4 以下MIUI 可 判断 上一次打开app 时 是否开启了悬浮窗权限
     *
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isMiuiFloatWindowOpAllowed(@NonNull Context context) {
        final int version = Build.VERSION.SDK_INT;

        if(!Machine.isFlyme() && !Machine.isMIUI()) {
            return true;
        }

        if (version >= 19) {
            return checkOp(context, 24);  //AppOpsManager.OP_SYSTEM_ALERT_WINDOW = 24;
        } else {
            if(Machine.isMIUI()) {
                if ((context.getApplicationInfo().flags & 1 << 27) == 1 <<27 ) {
                    return true;
                } else {
                    return false;
                }
            }else{
                return true;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean checkOp(Context context, int op) {
        final int version = Build.VERSION.SDK_INT;

        if (version >= 19) {
            AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            try {
                Class managerClass = manager.getClass();
                Method method = managerClass.getDeclaredMethod("checkOp", int.class, int.class, String.class);
                int isAllowNum = (Integer) method.invoke(manager, op, Binder.getCallingUid(), context.getPackageName());

                if (AppOpsManager.MODE_ALLOWED == isAllowNum) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
