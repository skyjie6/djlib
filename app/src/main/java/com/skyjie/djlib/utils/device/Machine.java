package com.skyjie.djlib.utils.device;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

// CHECKSTYLE:OFF
public class Machine {
	public static String sSimOperator;
	public static final int CHINA_MOBILE = 0;
	public static final int CHINA_UNICOM = 1;
	public static final int CHINA_TELECOM = 2;
	//imei
	private static String sCurDeviceId = null;
	
	public static int LEPHONE_ICON_SIZE = 72;
	private static boolean sCheckTablet = false;
	private static boolean sIsTablet = false;

	// 硬件加速
	public static int LAYER_TYPE_NONE = 0x00000000;
	public static int LAYER_TYPE_SOFTWARE = 0x00000001;
	public static int LAYER_TYPE_HARDWARE = 0x00000002;
	public static boolean IS_FROYO = Build.VERSION.SDK_INT >= 8;
	public static boolean IS_HONEYCOMB = Build.VERSION.SDK_INT >= 11;
	public static boolean IS_HONEYCOMB_MR1 = Build.VERSION.SDK_INT >= 12;
	public static boolean IS_ICS = Build.VERSION.SDK_INT >= 14;
	public static boolean IS_ICS_15 = Build.VERSION.SDK_INT >= 15;
	public static boolean IS_ICS_MR1 = IS_ICS_15 && Build.VERSION.RELEASE.equals("4.0.4");// HTC oneX 4.0.4系统
	public static boolean IS_JELLY_BEAN = Build.VERSION.SDK_INT >= 16;
	public static boolean IS_JELLY_BEAN_2 = Build.VERSION.SDK_INT >= 17;  //4.2
	public static final boolean IS_JELLY_BEAN_3 = Build.VERSION.SDK_INT >= 18; //4.3
	public static final boolean IS_SDK_ABOVE_KITKAT = Build.VERSION.SDK_INT >= 19; //sdk是否4.4或以上
	public static final boolean IS_LOLLIPOP = Build.VERSION.SDK_INT >= 21;
	public static final boolean IS_LOLLIPOP_2 = Build.VERSION.SDK_INT >= 22;
	public static final boolean IS_M = Build.VERSION.SDK_INT >= 23;
	public static boolean sLevelUnder3 = Build.VERSION.SDK_INT < 11;// 版本小于3.0
	public static boolean UNDER_ICS= Build.VERSION.SDK_INT < 14;
	private static Method sAcceleratedMethod = null;

	private final static String LEPHONEMODEL[] = { "3GW100", "3GW101", "3GC100", "3GC101" };
	private final static String MEIZUBOARD[] = { "m9", "mx", "mx2", "mx3", "mx4", "m1note" };
	private final static String M9BOARD[] = { "m9", "M9" };
	private final static String ONE_X_MODEL[] = { "HTC One X", "HTC One S", "HTC Butterfly",
			"HTC One XL", "htc one xl", "HTC Droid Incredible 4G LTE", "HTC 802w"};
	private final static String[] F100 = { "I_SKT" };
	private final static String USE_3DCORE_DEVICE_MODEL[] = { "GT-I9300",
			"GT-N7000", "GT-I9100", "m0", "d2att", "d2spr", "d2vzw", "d2tmo",
			"SGH-T989", "SHW-M250S", "c1skt", "c1ktt", "SHV-E160S", "SPH-D710",
			"c1lgt", "d2can", "SHW-M250K", "gt-i9300", "m0skt", "s2vep",
			"SHV-E160K", "s2ve", "GT-I9100T", "SHV-E120S", "SGH-I717",
			"SHW-M250L", "SHV-E120L", "d2dcm", "d2ltetmo", "d2usc", "GT-I9103",
			"d2mtr", "SGH-I777", "SHV-E120K", "d2cri", "SCH-R760", "galaxy s3",
			"SC-03E", "d2vmu", "SC-02C", "SC-05D", "SGH-T989D", "SC-03D" }; // 三星S3、三星Note、三星S2
	private static final String XIAOMI_UI = "miui";
	//imei
	public static final String DEFAULT_RANDOM_DEVICE_ID = "0000000000000000"; // 默认随机IMEI
	public static final String RANDOM_DEVICE_ID = "random_device_id"; // IMEI存入sharedPreference中的key
	public static final String SHAREDPREFERENCES_RANDOM_DEVICE_ID = "randomdeviceid"; // 保存IMEI的sharedPreference文件名
	private final static String KITKAT_WITHOUT_NAVBAR[] = {"xt1030", "HUAWEI MT2-L01", "HUAWEI P7-L00", "H60-L01"}; // 部分不想透明操作栏的手机机型
	private final static String COOLPAD8705 = "Coolpad 8705";
	//对以下机型，老用户升级后，如果之前是2D模式，自动转成3D模式
	private final static String RECOMMEND_2D_TO_3D_MODEL[] = { "GT-I9100", "GT-I9100T", "GT-I9103",
			"GT-I9210", "SC-02C", "SC-03D", "SGH-I727R", "SGH-I777", "SGH-T989", "SGH-T989D",
			"SHV-E120K", "SHV-E120L", "SHV-E120S", "SHW-M250K", "SHW-M250L", "SHW-M250S",
			"SPH-D710", "ISW11SC", "SCH-R760", "SGH-I757M", "logandsdtv", "s2ve", "s2vep",
			"SGH-I727" }; //三星S2
	
	private final static String PAY_NOT_BY_GETJER_COUNTRY[] = {"us","gb","de","ru","jp","au","fr","it",
		"ca","br","es","se","tw","mx","nl","no","kr","cn"}; //不通过亚太付费规则购买付费功能的国家
	
	private static boolean sSupportGLES20 = false;
	private static boolean sDetectedDevice = false;
	
	// 用于判断设备是否支持绑定widget
	private static boolean sSupportBindWidget = false;
	// 是否已经进行过绑定widget的判断
	private static boolean sDetectedBindWidget = false;

	public final static String[] S5360_MODEL = { "GT-S5360" };
	
	public static String sInstallDate = null; //apk安装时间,缓存起来，避免每次IO访问
	
	public static boolean sDetectedSupportAPITransparentStatusBar;
	public static boolean sIsSupportAPITransparentStatusBar;
	
	public static boolean isLephone() {
		final String model = Build.MODEL;
		if (model == null) {
			return false;
		}
		final int size = LEPHONEMODEL.length;
		for (int i = 0; i < size; i++) {
			if (model.equals(LEPHONEMODEL[i])) {
				return true;
			}
		}
		return false;
	}

	public static boolean isM9() {
		return isPhone(M9BOARD);
	}

	public static boolean isCoolpad_8705() {
		final String model = Build.MODEL;
		if (model == null) {
			return false;
		}

		if(model.equalsIgnoreCase(COOLPAD8705)) {
			return true;
		}

		return false;
	}

	public static boolean isF100() {
		return isPhone(F100);
	}

	public static boolean isMeizu() {
		return isPhone(MEIZUBOARD);
	}
	public static boolean isONE_X() {
		return isModel(ONE_X_MODEL);
	}

	/**
	 * 检查机型是否需要默认开启3DCore
	 * @return bool
	 */
//	public static boolean needToOpen3DCore() {
//		//V4.05开始默认全部开放3DCore
////		return true;//checkModel(USE_3DCORE_DEVICE_MODEL) || IS_JELLY_BEAN;
////		return Build.VERSION.SDK_INT >= 14;
//		// modified by liulixia 2013-11-25 新用户全开启3D插件
//		return true;
//	}

	/**
	 * 检查是否符合特定机型，打开2D转3D的引导通知
	 */
	public static boolean recommendOpen3DCore() {
		return isModel(RECOMMEND_2D_TO_3D_MODEL);
	}

	private static boolean isPhone(String[] boards) {
		final String board = Build.BOARD;
		if (board == null) {
			return false;
		}
		final int size = boards.length;
		for (int i = 0; i < size; i++) {
			if (board.toLowerCase().equals(boards[i])) {
				return true;
			}
		}
		return false;
	}

	public static boolean isModel(String[] models) {
		boolean ret = false;
		final String board = Build.MODEL;  //H60-L01
		if (board == null) {
			return ret;
		}
		final int size = models.length;
		try {
			for (int i = 0; i < size; i++) {
				if (board.equals(models[i])
						|| board.equals(models[i].toLowerCase())
						|| board.equals(models[i].toUpperCase())) {
					ret = true;
				}
			}
		} catch (Exception e) {
		}
		return ret;
	}

	/**
	 * 判断是否为非中国大陆用户 针对GOStore而起的判断，必须要在拿到SIM的情况下，而且移动国家号不属于中国大陆
	 *
	 * @return
	 */
	public static boolean isNotCnUser(Context context) {
		return !isCnUser(context);
	}


	public static boolean isHaveSim(Context context) {
		boolean result = true;

		if (context != null) {
			if (sSimOperator == null) {
				getSubscriberId(context);
			}

			// 从系统服务上获取了当前网络的MCC(移动国家号)，进而确定所处的国家和地区
			TelephonyManager manager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);

			// SIM卡状态
			boolean simCardUnable = manager.getSimState() != TelephonyManager.SIM_STATE_READY;
			String simOperator = manager.getSimOperator();

			if (simCardUnable || TextUtils.isEmpty(simOperator)) {
				// 如果没有SIM卡的话simOperator为null，然后获取本地信息进行判断处理
				// 获取当前国家或地区，如果当前手机设置为简体中文-中国，则使用此方法返回CN
				result = false;
			}
		}

		return result;
	}

	/**
	 * 因为主题2.0新起进程，无法获取GoLauncher.getContext()， 所以重载此方法，以便主题2.0调用
	 *
	 * @param context
	 * @return
	 */
	public static boolean isCnUser(Context context) {
		boolean result = false;

		if (context != null) {
			if (sSimOperator == null) {
				getSubscriberId(context);
			}
			// 从系统服务上获取了当前网络的MCC(移动国家号)，进而确定所处的国家和地区
			TelephonyManager manager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);

			// SIM卡状态
			boolean simCardUnable = manager.getSimState() != TelephonyManager.SIM_STATE_READY;
			String simOperator = manager.getSimOperator();

			if (simCardUnable || TextUtils.isEmpty(simOperator)) {
				// 如果没有SIM卡的话simOperator为null，然后获取本地信息进行判断处理
				// 获取当前国家或地区，如果当前手机设置为简体中文-中国，则使用此方法返回CN
				String curCountry = Locale.getDefault().getCountry();
				if (curCountry != null && curCountry.contains("CN")) {
					// 如果获取的国家信息是CN，则返回TRUE
					result = true;
				} else {
					// 如果获取不到国家信息，或者国家信息不是CN
					result = false;
				}
			} else if (simOperator.startsWith("460")) {
				// 如果有SIM卡，并且获取到simOperator信息。
				/**
				 * 中国大陆的前5位是(46000) 中国移动：46000、46002 中国联通：46001 中国电信：46003
				 */
				result = true;
			}
		}

		return result;
	}

	public static int getOperator(Context context) {
		int result = -1;
		if (context != null) {
			if (sSimOperator == null) {
				getSubscriberId(context);
			}

			if (!TextUtils.isEmpty(sSimOperator)) {
				// 如果没有SIM卡的话simOperator为null，然后获取本地信息进行判断处理

				if (sSimOperator.startsWith("46000") || sSimOperator.startsWith("46002")) {
					// 如果有SIM卡，并且获取到simOperator信息。
					result = CHINA_MOBILE;
				} else if (sSimOperator.startsWith("46001")) {
					result = CHINA_UNICOM;
				} else if (sSimOperator.startsWith("46003")) {
					result = CHINA_TELECOM;
				}
			}
		}
		return result;
	}

	public static String getSubscriberId(Context context) {
		if (TextUtils.isEmpty(sSimOperator)) {
			TelephonyManager manager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);

			// SIM卡状态
			sSimOperator = manager.getSubscriberId();

		}
		return sSimOperator;
	}

	public static String getSimOperator(Context context) {
		String simOperator = "000";
		try {
			if (context != null) {
				TelephonyManager manager = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
				simOperator = manager.getSimOperator();
			}
		} catch (Throwable e) {
		}
		return simOperator;
	}

	/**
	 * get IMSI
	 *
	 * @param context
	 * @return the IMSI, 000 when error
	 */
	public static String getIMSI(Context context) {
		String imsi = null;
		try {
			if (context != null) {
				// 从系统服务上获取了当前网络的MCC(移动国家号)，进而确定所处的国家和地区
				TelephonyManager manager = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
				imsi = manager.getSubscriberId();
				imsi = (imsi == null) ? "000" : imsi;
			}
		} catch (Throwable e) {
		}

		return imsi;
	}

//	/**
//	 * 是否国外用户或国内的有电子市场的用户，true for yes，or false for no
//	 *
//	 * @author huyong
//	 * @param context
//	 * @return
//	 */
//	public static boolean isOverSeaOrExistMarket(Context context) {
//		boolean result = false;
//		boolean isCnUser = isCnUser(context);
//		// 全部的国外用户 + 有电子市场的国内用户
//		if (isCnUser) {
//			// 是国内用户，则进一步判断是否有电子市场
//			result = AppUtils.isMarketExist(context);
//		} else {
//			// 是国外用户
//			result = true;
//		}
//		return result;
//	}

	/**
	 * 判断当前运营商是否在指定数组内
	 *
	 * @param areaArray
	 * @return
	 */
	public static boolean isLocalAreaCodeMatch(String[] areaArray,Context context) {
		if (null == areaArray || areaArray.length == 0) {
			return false;
		}
		// 从系统服务上获取了当前网络的MCC(移动国家号)，进而确定所处的国家和地区
		TelephonyManager manager = (TelephonyManager) context.getSystemService(
				Context.TELEPHONY_SERVICE);
		String simOperator = manager.getSimOperator();
		if (null == simOperator) {
			return false;
		}

		boolean ret = false;
		for (String areastring : areaArray) {
			if (null == areastring || areastring.length() == 0
					|| areastring.length() > simOperator.length()) {
				continue;
			} else {
				String subString = simOperator.substring(0, areastring.length());
				if (subString.equals(areastring)) {
					ret = true;
					break;
				} else {
					continue;
				}
			}
		}
		return ret;
	}

	// 根据系统版本号判断时候为华为2.2 or 2.2.1, Y 则catch
	public static boolean isHuaweiAndOS2_2_1() {
		boolean resault = false;
		String androidVersion = Build.VERSION.RELEASE;// os版本号
		String brand = Build.BRAND;// 商标
		if (androidVersion == null || brand == null) {
			return resault;
		}
		if (brand.equalsIgnoreCase("Huawei")
				&& (androidVersion.equals("2.2") || androidVersion.equals("2.2.2")
						|| androidVersion.equals("2.2.1") || androidVersion.equals("2.2.0"))) {
			resault = true;
		}
		return resault;
	}

	public static boolean isHuaweiAndOS() {
		boolean resault = false;
		String brand = Build.BRAND;// 商标
		if (brand.equalsIgnoreCase("Huawei")) {
			resault = true;
		}
		return resault;
	}


	public static boolean isHuawei() {
		boolean result = false;
		String brand = Build.BRAND;// 商标
		if (brand == null) {
			return result;
		}
		if (brand.equalsIgnoreCase("Huawei")) {
			result = true;
		}
		return result;
	}

	// 判断当前设备是否为平板
	private static boolean isPad(Context context) {
//		if (DrawUtils.sDensity >= 1.5 || DrawUtils.sDensity <= 0) {
//			return false;
//		}
//		if (DrawUtils.sWidthPixels < DrawUtils.sHeightPixels) {
//			if (DrawUtils.sWidthPixels > 480 && DrawUtils.sHeightPixels > 800) {
//				return true;
//			}
//		} else {
//			if (DrawUtils.sWidthPixels > 800 && DrawUtils.sHeightPixels > 480) {
//				return true;
//			}
//		}
//		return false;
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	public static boolean isTablet(Context context) {
		if (sCheckTablet == true) {
			return sIsTablet;
		}
		sCheckTablet = true;
		sIsTablet = isPad(context);
		return sIsTablet;
	}

	/**
	 * 判断当前网络是否可以使用
	 *
	 * @author huyong
	 * @param context
	 * @return
	 */
	public static boolean isNetworkOK(Context context) {
		boolean result = false;
		if (context != null) {
			try {
				ConnectivityManager cm = (ConnectivityManager) context
						.getSystemService(Context.CONNECTIVITY_SERVICE);
				if (cm != null) {
					NetworkInfo networkInfo = cm.getActiveNetworkInfo();
					if (networkInfo != null && networkInfo.isConnected()) {
						result = true;
					}
				}
			} catch (NoSuchFieldError e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	public static String getNetworkState(Context context) {
		NetworkInfo mobNetInfo = ((ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if (mobNetInfo != null) {
			if (mobNetInfo.isAvailable()) {
				int netType = mobNetInfo.getType();
				if (netType == ConnectivityManager.TYPE_WIFI) {
					return "wifi";
				} else if (netType == ConnectivityManager.TYPE_MOBILE) {
					if (mobNetInfo.getExtraInfo() != null) {
						String netMode = mobNetInfo.getExtraInfo().toLowerCase();
						if (netMode.equals("cmwap")) {
							return "cmwap";
						} else {
							return "cmnet";
						}
					}
				}
			}
		}
		return "other";
	}

	public static String getNetworkState2(Context context) {
		NetworkInfo mobNetInfo = ((ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
		if (mobNetInfo != null) {
			if (mobNetInfo.isAvailable()) {
				int netType = mobNetInfo.getType();
				if (netType == ConnectivityManager.TYPE_WIFI) {
					return "wifi";
				} else if (netType == ConnectivityManager.TYPE_MOBILE) {
					 int subType = mobNetInfo.getSubtype();
				        if (subType == TelephonyManager.NETWORK_TYPE_CDMA || subType == TelephonyManager.NETWORK_TYPE_GPRS
				                || subType == TelephonyManager.NETWORK_TYPE_EDGE) {
				        	return "2g";
				        } else if (subType == TelephonyManager.NETWORK_TYPE_UMTS || subType == TelephonyManager.NETWORK_TYPE_HSDPA
				                || subType == TelephonyManager.NETWORK_TYPE_EVDO_A || subType == TelephonyManager.NETWORK_TYPE_EVDO_0
				                || subType == TelephonyManager.NETWORK_TYPE_EVDO_B || subType == TelephonyManager.NETWORK_TYPE_HSPAP
				                || subType == TelephonyManager.NETWORK_TYPE_EHRPD) {
				        	return "3g";
				        } else if (subType == TelephonyManager.NETWORK_TYPE_LTE) {// LTE是3g到4g的过渡，是3.9G的全球标准
				        	return "4g";
				        }
				}
			}
		}
		return "other";
	}

	/**
	 * 设置硬件加速
	 *
	 * @param view
	 */
	public static void setHardwareAccelerated(View view, int mode) {
		if (sLevelUnder3) {
			return;
		}
		try {
			if (null == sAcceleratedMethod) {
				sAcceleratedMethod = View.class.getMethod("setLayerType", new Class[] {
						Integer.TYPE, Paint.class });
			}
			sAcceleratedMethod.invoke(view, new Object[] { Integer.valueOf(mode), null });
		} catch (Throwable e) {
			sLevelUnder3 = true;
		}
	}

	public static boolean isIceCreamSandwichOrHigherSdk() {
		return Build.VERSION.SDK_INT >= 14;
	}

	/**
	 * 获取Android中的Linux内核版本号
	 *
	 */
	public static String getLinuxKernel() {
		Process process = null;
		try {
			process = Runtime.getRuntime().exec("cat /proc/version");
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (null == process) {
			return null;
		}

		// get the output line
		InputStream outs = process.getInputStream();
		InputStreamReader isrout = new InputStreamReader(outs);
		BufferedReader brout = new BufferedReader(isrout, 8 * 1024);
		String result = "";
		String line;

		// get the whole standard output string
		try {
			while ((line = brout.readLine()) != null) {
				result += line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (result.equals("")) {
			String Keyword = "version ";
			int index = result.indexOf(Keyword);
			line = result.substring(index + Keyword.length());
			if (null != line) {
				index = line.indexOf(" ");
				return line.substring(0, index);
			}
		}
		return null;
	}

	/**
	 * 获得手机内存的可用空间大小
	 *
	 * @author kingyang
	 */
	public static long getAvailableInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	/**
	 * 获得手机内存的总空间大小
	 *
	 * @author kingyang
	 */
	public static long getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	/**
	 * 获得手机sdcard的可用空间大小
	 *
	 * @author kingyang
	 */
	public static long getAvailableExternalMemorySize() {
		File path = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	/**
	 * 获得手机sdcard的总空间大小
	 *
	 * @author kingyang
	 */
	public static long getTotalExternalMemorySize() {
		File path = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	/**
	 * 是否存在SDCard
	 *
	 * @author chenguanyu
	 * @return
	 */
	public static boolean isSDCardExist() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 获取当前的语言
	 *
	 * @author zhoujun
	 * @param context
	 * @return
	 */
	public static String getLanguage(Context context) {
		String language = context.getResources().getConfiguration().locale.getLanguage();
		return language;
	}

	/**
	 * 判断应用软件是否运行在前台
	 *
	 * @param context
	 * @param packageName
	 *            应用软件的包名
	 * @return
	 */
	public static boolean isTopActivity(Context context, String packageName) {
		return isTopActivity(context, packageName, null);
	}

	/**
	 * <br>功能简述: 获取真实的imei号。
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * 判断某一Activity是否运行在前台
	 *
	 * @param context
	 * @param packageName
	 *            应用软件的包名
	 * @return
	 */
	public static boolean isTopActivity(Context context, String packageName, String className) {
		try {
			ActivityManager activityManager = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
			if (tasksInfo.size() > 0) {
				// Activity位于堆栈的顶层,如果Activity的类为空则判断的是当前应用是否在前台
				if (packageName.equals(tasksInfo.get(0).topActivity.getPackageName())
						&& (className == null || className.equals(tasksInfo.get(0).topActivity
								.getClassName()))) {
					return true;
				}
			}
		} catch (Exception e) {
		}
		return false;
	}

	/**
	 * <br>功能简述:获取versionCode
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @return
	 */
	public static String getVersionCode(Context context) {
		String versionCode = null;
		try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(
					context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
			versionCode = pInfo.versionCode + "";
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return versionCode;
	}

	/**
	 * <br>功能简述:获取versionCode
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @return
	 */
	public static String getVersionName(Context context) {
		String versionCode = null;
		try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(
					context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
			versionCode = pInfo.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return versionCode;
	}

//	/**
//	 * @return
//	 */
//	public static String getIMEI(Context context) {
//		TelephonyManager telephonyManager = (TelephonyManager) context
//				.getSystemService(Context.TELEPHONY_SERVICE);
//		String imei = telephonyManager.getDeviceId();
//		return imei;
//	}
	public static String getImei(Context context) {
		try {
			return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
					.getDeviceId();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取真实的imei号
	 */
	public static String getIMEI(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getDeviceId();
	}

	/**
	 * <br>功能简述:获取Android ID的方法
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public static String getAndroidId(Context context) {
		String androidId = null;
		if (context != null) {
			androidId = Settings.Secure.getString(context.getContentResolver(),
					Settings.Secure.ANDROID_ID);
		}
		return androidId;
	}

	/**
	 * 获取本地mac地址
	 * @return
	 */
	public static String getLocalMacAddress(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }

	/**
	 * 获取国家
	 * @param context
	 * @return
	 */
	public static String getCountry(Context context) {
		String ret = null;

		try {
			TelephonyManager telManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			if (telManager != null) {
				ret = telManager.getSimCountryIso().toLowerCase();
			}
		} catch (Throwable e) {
			//			 e.printStackTrace();
		}
		if (ret == null || ret.equals("")) {
			ret = Locale.getDefault().getCountry().toLowerCase();
		}
		return ret;
	}


	/**
	 * 是否支持OpenGL2.0
	 * @param context
	 * @return
	 */
	public static boolean isSupportGLES20(Context context) {
		if (!sDetectedDevice) {
			ActivityManager am = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
			ConfigurationInfo info = am.getDeviceConfigurationInfo();
			sSupportGLES20 = info.reqGlEsVersion >= 0x20000;
			sDetectedDevice = true;
		}
		return sSupportGLES20;
	}

	/**
	 * 判断是否为韩国用户
	 * @return
	 */
	public static boolean isKorea(Context context) {
		boolean isKorea = false;

		String country = getCountry(context);
		if (country.equals("kr")) {
			isKorea = true;
		}

		return isKorea;
	}

	public static boolean canHideNavBar() {
		if (isModel(KITKAT_WITHOUT_NAVBAR)) {
			return false;
		}
		return true;
	}

	public static boolean isSupportBindWidget(Context context) {
		if (!sDetectedBindWidget) {
			sSupportBindWidget = false;
			if (Build.VERSION.SDK_INT >= 16) {
				try {
					// 在某些设备上，没有支持"android.appwidget.action.APPWIDGET_BIND"的activity
					Intent intent = new Intent("android.appwidget.action.APPWIDGET_BIND");
					PackageManager packageManager = context.getPackageManager();
					List<ResolveInfo> list = packageManager.queryIntentActivities(intent, 0);
					if (list == null || list.size() <= 0) {
						sSupportBindWidget = false;
					} else {
						// 假如有支持上述action的activity，还需要判断是否已经进行了授权创建widget
						AppWidgetManager.class.getMethod("bindAppWidgetIdIfAllowed", int.class,
								ComponentName.class);
						sSupportBindWidget = true;
					}
				} catch (NoSuchMethodException e) { // 虽然是4.1以上系统，但是不支持绑定权限，仍按列表方式添加系统widget
					e.printStackTrace();
				}
			}
			sDetectedBindWidget = true;
		}
		return sSupportBindWidget;
	}

	/**
	 * 申请权限获取手机的IMEI号
	 *
	 * @author dingzijian
	 * @param context
	 * @return
	 */
	public static String getVirtualIMEI(Context context) {
		//如果已保存IMEI或者存在旧的虚拟IMEI号则直接使用。
		String imei = getDeviceIdFromSharedpreference(context);
		// 获取手机的IMEI，并保存下来
		if (context != null) {
			if (null == imei || imei.equals(DEFAULT_RANDOM_DEVICE_ID)) {
				try {
					imei = getIMEI(context);
					saveDeviceIdToSharedpreference(context, imei);
				} catch (Exception e) {

				}
			}
		}
		return imei;
	}


	/**
	 * 获取随机生成的IMEI的方法
	 *
	 * @author dingzijian
	 * @return
	 */
	private static String getDeviceIdFromSharedpreference(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPREFERENCES_RANDOM_DEVICE_ID, Context.MODE_PRIVATE);
		return sharedPreferences.getString(RANDOM_DEVICE_ID, DEFAULT_RANDOM_DEVICE_ID);
	}
	/**
	 * 保存随机生成的IMEI的方法
	 *
	 * @param context
	 * @param deviceId
	 */
	private static void saveDeviceIdToSharedpreference(Context context, String deviceId) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(SHAREDPREFERENCES_RANDOM_DEVICE_ID, Context.MODE_PRIVATE);
		sharedPreferences.edit().putString(RANDOM_DEVICE_ID, deviceId).commit();
	}

	// 判断是不是米UI V5及以上系统
	public static boolean isMIUIV5() {
		boolean result = false;

		// 修改判断条件，针对rom而不是机器生产商
		if (IS_JELLY_BEAN && isMIUI()) {
			result = true;
		}
		return result;
	}

//	private static boolean isMIUI() {
//		String host = android.os.Build.HOST;
//		if (host != null && host.toLowerCase() != null
//				&& host.toLowerCase().contains(XIAOMI_UI)) {
//			return true;
//		}
//		return false;
//	}

	private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
	private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
	private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";

	public static boolean isMIUI() {
		try {
			final BuildProperties prop = BuildProperties.newInstance();
			return prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
					|| prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
					|| prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null;
		} catch (final IOException e) {
			return false;
		}
	}



	public static boolean isFlyme() {
		int version = Build.VERSION.SDK_INT;
		if (version > 21) {
			return isMeizuFlymeOS();
		} else {
			try {
				// Invoke Build.hasSmartBar()
				final Method method = Build.class.getMethod("hasSmartBar");
				return method != null;
			} catch (final Exception e) {
				return false;
			}
		}
	}

	public static boolean isMeizuFlymeOS() {
	/* 获取魅族系统操作版本标识*/
		String meizuFlymeOSFlag  = getSystemProperty("ro.build.display.id","");
		if (meizuFlymeOSFlag == null || meizuFlymeOSFlag.equals("")){
			return false;
		}else if (meizuFlymeOSFlag.contains("flyme") || meizuFlymeOSFlag.toLowerCase().contains("flyme")){
			return  true;
		}else {
			return false;
		}
	}

	private static String getSystemProperty(String key, String defaultValue) {
		try {
			Class<?> clz = Class.forName("android.os.SystemProperties");
			Method get = clz.getMethod("get", String.class, String.class);
			return (String)get.invoke(clz, key, defaultValue);
		} catch (Exception e) {
			return null;
		}
	}

	public static void setDefaultLauncher(Context context) {
		if (isMIUIV5()) {
			Intent mIntent = new Intent(Intent.ACTION_MAIN);
			// 小米v5 系统,不支持弹出设置默认launcher,跳到显示页面
			mIntent.setComponent(new ComponentName("com.android.settings",
					"com.android.settings.Settings$DisplaySettingsActivity"));
			try {
				context.startActivity(mIntent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} /*else {
			mIntent.addCategory("android.intent.category.HOME");
		}*/

	}


	/**
	 * 检测手机WIFI有没有打开的方法
	 *
	 * @param context
	 * @return
	 */
	public static boolean isWifiEnable(Context context) {
		boolean result = false;
		try {
			if (context != null) {
				ConnectivityManager connectivityManager = (ConnectivityManager) context
						.getSystemService(Context.CONNECTIVITY_SERVICE);
				if (connectivityManager != null) {
					NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
					if (networkInfo != null && networkInfo.isConnected()
							&& networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
						result = true;
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static String getCurProcessName(Context context) {
		int pid = android.os.Process.myPid();
		ActivityManager mActivityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
//			Thread.yield();
			if (appProcess.pid == pid) {
				return appProcess.processName;
			}
		}
		return null;
	}

	/**
	 * 获取SD卡剩余空间
	 * @return
	 */
	public static long getSDFreeSize() {
		// 取得SD卡文件路径
		File path = Environment.getExternalStorageDirectory();
		StatFs sf = new StatFs(path.getPath());
		long blockSize = sf.getBlockSize();
		// 空闲的数据块的数量
		long freeBlocks = sf.getAvailableBlocks();
		// 返回SD卡空闲大小
		return freeBlocks * blockSize; // 单位K
	}

	public static String getFrontActivity(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);

		// get the info from the currently running task
		List< RunningTaskInfo > taskInfo = am.getRunningTasks(1);

		Log.d("topActivity", "CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());

		ComponentName componentInfo = taskInfo.get(0).topActivity;
		componentInfo.getPackageName();
		
		return componentInfo.getPackageName();
	}
	
	// 获取当前wifi状态
	public static Boolean getWifiState(WifiManager sWifiManager) {
		if (sWifiManager.isWifiEnabled() == true) {
			return true;
		} else {
			return false;
		}
	}
		
	// 检测GPRS是否打开
	public static boolean gprsIsOpenMethod(ConnectivityManager mCm,
			String methodName) {
		Class mCmClass = mCm.getClass();
		Class[] argClasses = null;
		Object[] argObject = null;
		Boolean isOpen = false;
		try {

			Method method = mCmClass.getMethod(methodName, argClasses);
			isOpen = (Boolean) method.invoke(mCm, argObject);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return isOpen;
	}


	/**
	 * <br>功能简述:判断指定包名的进程是否运行 
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	*/
	public static boolean isRunning(Context context,String packageName){
	    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    List<RunningAppProcessInfo> infos = am.getRunningAppProcesses();
	    for(RunningAppProcessInfo rapi : infos){
	        if(rapi.processName.equals(packageName)) {
				return true;
			}
	        }
	    return false;
	}
	
	
	/**
	 * 是否存在google电子市场
	 * 
	 * @author huyong
	 * @param context
	 * @return
	 */
	public static boolean isExistGoogleMarket(Context context) {
		String googleMarketPkgName = "com.android.vending";
		return isApplicationExsit(context, googleMarketPkgName);
	}
	
	/**
	 * 通过应用程序包名判断程序是否安装的方法
	 * 
	 * @param packageName
	 *            应用程序包名
	 * @return 程序已安装返回TRUE，否则返回FALSE
	 */
	public static boolean isApplicationExsit(Context context, String packageName) {
		boolean result = false;
		if (context != null && packageName != null) {
			try {
				// context.createPackageContext(packageName,
				// Context.CONTEXT_IGNORE_SECURITY);
				context.getPackageManager().getPackageInfo(packageName,
						PackageManager.GET_SHARED_LIBRARY_FILES);
				result = true;
			} catch (Exception e) {
				// Log.i("store", "ThemeStoreUtil.isApplicationExsit for " +
				// packageName + " is exception");
			}
		}
		return result;
	}


	/**
	 * 判断是否nexus
	 * @return
	 */
	public static Boolean isNexus() {
		if (Build.MODEL.toLowerCase().contains("nexus")) {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否三星
	 * @return
	 */
	public static Boolean isSamsung() {
		if (Build.BRAND.contains("samsung")) {
			return true;
		}
		return false;
	}
}
