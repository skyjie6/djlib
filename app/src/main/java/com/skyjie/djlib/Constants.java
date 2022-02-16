package com.skyjie.djlib;


import android.net.Uri;
import android.os.Environment;

import java.io.File;

/**
 * 静态函数
 */
public class Constants {
    //云通信服务相关配置
    public static final int IMSDK_ACCOUNT_TYPE = 11488;

    private static final int IMSDK_APPID = 1400027299;
    private static final int IMSDK_APPID_TEST = 1400034147;

    public static int sAppID = IMSDK_APPID;
    /**
     * This authority is used for writing to or querying from the clock
     * provider.
     */
    public static final String AUTHORITY = "com.cs.glive";

    public static final String BASE64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAk94Wm6YGOjVIPywRCiAQYlNXKuB5ZEj2rSFGMm3hGHBNYdqHnq53zXz8WbS/NbMCjWAKkCmWrRLFaf0oBjWskg5GVl7J3Iiv10qbQFhACA9O1U7OC7Bh3zTw0Oh59w1gGWEB/0HY5+4e6T3n2jrdsV5lC2dCWionR9Qsm992KVjq9edWBscNDnRXM4aofJimhj1CJzuP98x7obQVo9JNuxyxQdKbBaQNujIQPknXNW04lB2i/1G5Rfta9aYTsjrgC9sENMVh23Zmz1IHddZu1BCt2wybtN/tIE7Kcy5ptxxIz9XOXJIeDZIn/oX9xK3+707Lhs5RMJlVjtdnYiIWtQIDAQAB";


    /**
     * 包名
     */
    public static class Package {

        /**
         * 主程序的包名
         */
        public static final String PACKAGE_NAME = "com.cs.glive";

        /**
         * 　主进程名
         */
        public static final String MAIN_PROCESS_NAME = "com.cs.glive";
    }

    /**
     * 超时常量值
     */
    public static class TimeOut {
        public static final int TIME_OUT_NORMAL = 15;
    }


    /**
     * 文件存放路径
     */
    public static class Path {
        public final static String SDCARD = Environment.getExternalStorageDirectory().getPath();

        public static final String ROOT_PATH = SDCARD + "/GLive/";

        //如果obb文件夹拿不到用SDCard的GLive代替
        public final static String OBB_PATH = "";

        public static final String DOWNLOAD_VERSION_FILE_NAME = "version"; //下载的版本配置文件

        public static final String ACTIVITY_DIR = OBB_PATH + File.separator + "activity/"; // 活动文件路径

        public static final String LANGUAGE_DIR = OBB_PATH + File.separator + "language/"; // 多语言文件路径

        public static final String GIFT_DIR = OBB_PATH + File.separator + "gift/"; // 礼物文件路径

        public static final String STICKER_DIR = OBB_PATH + File.separator + "sticker/"; // 人脸贴图文件路径

        public static final String PIC_STICKER_DIR = OBB_PATH + File.separator + "pic_sticker/"; // 静态贴纸文件路径

        public static final String FILTER_DIR = OBB_PATH + File.separator + "filter/"; // 滤镜文件路径

        public static final String ENTER_ANIM_DIR = OBB_PATH + File.separator + "enter_anim/"; // 进场动画文件路径

        public static final String SCREEN_SHARE_PATH = ROOT_PATH + "screenshots/"; // 截图分享文件路径

        public static final String SCREEN_REPORT_PATH = SCREEN_SHARE_PATH; // 截图举报文件路径

        public static final String SCREEN_RECORD_PATH = ROOT_PATH + "records/"; // 录屏文件路径

        public static final String KINESISHOSE_DIR = OBB_PATH + File.separator + "kinesishose/%s"; // AWS kinesis firehose文件夹路径.

        public static final String LOG_DIR = ROOT_PATH + "logs/";

        public static final String BLOCK_CANARY_PATH = ROOT_PATH + "logs/";

        public static final String NETWORK_DIR = OBB_PATH + File.separator + "monitor/network/"; // 网络检测日志路径

        public static final String VIDEO_DIR =  OBB_PATH + File.separator + "video/"; // 视频路径
    }

    public static class Url {
        public static final String PRIVACY_POLICY = "http://goappdl.goforandroid.com/soft/file/term/1272/golive_privacy.html";
        public static final String TERMS_OF_USE = "http://goappdl.goforandroid.com/soft/file/term/1272/golive_agreement.html";
        public static final String FACEBOOK_HOMEPAGE = "https://www.facebook.com/GO-Live-460606840964280";
    }


    public static final String BD_EXIT_APP = "bd_sxb_exit";

    public static final String VALUE = "value";

    public static final String USER_INFO = "user_info";

    public static final String USER_ID = "user_id";
    public static final String OPEN_ID = "open_id";
    public static final String USER_SIG = "user_sig";
    public static final String USER_SIG_TIME = "user_sig_time";
    public static final String USER_TOKEN = "user_token";
    public static final String USER_ACCOUNT_COUNTRY = "user_account_country";
    public static final String USER_ACCOUNT_TYPE = "user_account_type";
    public static final String USER_ACCOUNT = "user_account";
    public static final String USER_NICK = "user_nick";
    public static final String USER_AVATAR = "user_avatar";
    public static final String USER_LEVEL = "user_level";
    public static final String USER_ANCHOR_LEVEL = "user_anchor_level";
    public static final String USER_EXPERIENCE = "user_experience";

    public static final String USER_ROOM_NUM = "user_room_num";

    public static final String LIVE_ANIMATOR = "live_animator";

    public static final String FIREBASE_TOKEN = "firebase_token";

    public static final String LAST_LOGIN_USER_ID = "last_login_user_id";

    public static final String LOG_LEVEL = "log_level";


    public static final int HOST = 1;

    public static final Uri CONTENT_DOWNLOADS_URI = Uri.parse("content://downloads/my_downloads");

//    public static final int AVIMCMD_MULTI = 0x800;             // 多人互动消息类型
//
//    public static final int AVIMCMD_MUlTI_HOST_INVITE = AVIMCMD_MULTI + 1;         // 邀请互动,
//    public static final int AVIMCMD_MULTI_CANCEL_INTERACT = AVIMCMD_MUlTI_HOST_INVITE + 1;       // 断开互动，
//    public static final int AVIMCMD_MUlTI_JOIN = AVIMCMD_MULTI_CANCEL_INTERACT + 1;       // 同意互动，
//    public static final int AVIMCMD_MUlTI_REFUSE = AVIMCMD_MUlTI_JOIN + 1;      // 拒绝互动，
//
//    public static final int AVIMCMD_MULTI_HOST_ENABLEINTERACTMIC = AVIMCMD_MUlTI_REFUSE + 1;  // 主播打开互动者Mic，
//    public static final int AVIMCMD_MULTI_HOST_DISABLEINTERACTMIC = AVIMCMD_MULTI_HOST_ENABLEINTERACTMIC + 1;// 主播关闭互动者Mic，
//    public static final int AVIMCMD_MULTI_HOST_ENABLEINTERACTCAMERA = AVIMCMD_MULTI_HOST_DISABLEINTERACTMIC + 1; // 主播打开互动者Camera，
//    public static final int AVIMCMD_MULTI_HOST_DISABLEINTERACTCAMERA = AVIMCMD_MULTI_HOST_ENABLEINTERACTCAMERA + 1; // 主播打开互动者Camera
//    public static final int AVIMCMD_MULTI_HOST_CANCELINVITE = AVIMCMD_MULTI_HOST_DISABLEINTERACTCAMERA + 1; //主播让某个互动者下麦
//    public static final int AVIMCMD_MULTI_HOST_CONTROLL_CAMERA = AVIMCMD_MULTI_HOST_CANCELINVITE + 1; //主播控制某个上麦成员摄像头
//    public static final int AVIMCMD_MULTI_HOST_CONTROLL_MIC = AVIMCMD_MULTI_HOST_CONTROLL_CAMERA + 1; //主播控制某个上麦成员MIC
//    public static final int AVIMCMD_MULTI_HOST_SWITCH_CAMERA = AVIMCMD_MULTI_HOST_CONTROLL_MIC+1; ////主播切换某个上麦成员MIC

    public static final int AVIMCMD_TEXT = -1;         // 普通的聊天消息

    public static final int AVIMCMD_NONE = AVIMCMD_TEXT + 1;               // 无事件

    // 以下事件为TCAdapter内部处理的通用事件
    public static final int AVIMCMD_ENTERLIVE = AVIMCMD_NONE + 1;          // 用户加入直播,
    public static final int AVIMCMD_EXITLIVE = AVIMCMD_ENTERLIVE + 1;         // 用户退出直播,
    public static final int AVIMCMD_PRAISE = AVIMCMD_EXITLIVE + 1;           // 点赞消息,
    public static final int AVIMCMD_HOST_LEAVE = AVIMCMD_PRAISE + 1;         // 主播离开,
    public static final int AVIMCMD_HOST_BACK = AVIMCMD_HOST_LEAVE + 1;      // 主播回来,

    public static final String CMD_KEY = "userAction";
    public static final String CMD_PARAM = "actionParam";


//    public static final long HOST_AUTH = AVRoomMulti.AUTH_BITS_DEFAULT;//权限位；TODO：默认值是拥有所有权限。
//    public static final long VIDEO_MEMBER_AUTH = AVRoomMulti.AUTH_BITS_DEFAULT;//权限位；TODO：默认值是拥有所有权限。
//    public static final long NORMAL_MEMBER_AUTH = AVRoomMulti.AUTH_BITS_JOIN_ROOM | AVRoomMulti.AUTH_BITS_RECV_AUDIO | AVRoomMulti.AUTH_BITS_RECV_CAMERA_VIDEO | AVRoomMulti.AUTH_BITS_RECV_SCREEN_VIDEO;


    public static final String ROLE_LIVEMASTER = "LiveMaster"; // 直播主播
    public static final String ROLE_LIVEGUEST = "LiveGuest"; // 直播互动成员
    public static final String ROLE_NOMALGUEST = "Guest"; // 直播普通成员

    public static final String USER_GUIDE = "user_guide";
    public static final String SHARE_GUIDE = "share_guide";
    public static final String SHARE_MESSAGE_TIP_SHOWN = "share_message_tip_shown_%s";//公屏分享引导
    public static final String SHARE_TIP_SHOWN = "share_tip_shown";
    public static final String SHARE_ME_CLICKED = "share_me_clicked_%s";
    public static final String USER_GUIDE_MEMBERS_SHOWN = "members_user_guide_has_shown";
    public static final String USER_GUIDE_ANCHOR_SHOWN = "anchor_user_guide_has_shown";


    //直播自定义消息
//    public static final int ILVLIVE_CMD_PRAISE = ILVLiveConstants.ILVLIVE_CMD_INTERACT_REJECT + 1; // 点赞（爱心）
    public static final int ILVLIVE_CMD_PRAISE = 1; // 点赞（爱心）
    public static final int ILVLIVE_CMD_HOST_LEAVE = ILVLIVE_CMD_PRAISE + 1; // 主播离开
    public static final int ILVLIVE_CMD_HOST_BACK = ILVLIVE_CMD_HOST_LEAVE + 1; //主播回来
    public static final int ILVLIVE_CMD_FOLLOW = ILVLIVE_CMD_HOST_BACK + 1; // 关注主播
    public static final int ILVLIVE_CMD_CANCEL_FOLLOW = ILVLIVE_CMD_FOLLOW + 1; // 取消关注主播
    public static final int ILVLIVE_CMD_SEND_GIFT = ILVLIVE_CMD_CANCEL_FOLLOW + 1; // 送礼物

    public static class Action {
        private static final String ACTION_PREFIX = "com.cs.glive.action.";
        public static final String ALARM_TASK = ACTION_PREFIX + "ALARM_TASK";
        public static final String USER_PROFILE = ACTION_PREFIX + "USER_PROFILE";
        public static final String LIVE_PLAYER = ACTION_PREFIX + "LIVE_PLAYER";
        public static final String STATISTICS = ACTION_PREFIX + "STATISTICS";
        public static final String NOTIFICATION_CANCEL = ACTION_PREFIX + "NOTIFICATION_CANCEL";

    }

    public static final class Extra {
        public static final String NEXT_INTENT = "NEXT_INTENT_STR";
        public static final String EXTRA_FLAG = "EXTRA_FLAG";
        public static final String WEBVIEW_URL = "WEBVIEW_URL";
        public static final String WEBVIEW_IS_SHOW_TITLE = "WEBVIEW_IS_SHOW_TITLE";
        public static final String HOME_TARGET = "target";
        /**
         * 统计入口／分类
         */
        public static final String STATISTICS_ENTRANCE = "entrance";
        /**
         * 统计对象
         */
        public static final String STATISTICS_OBJECT = "object";
        /**
         * 统计位置
         */
        public static final String STATISTICS_POSITION = "position";
        /**
         * 统计关联对象
         */
        public static final String STATISTICS_RELATION = "relation";
        /**
         * 是否通知栏进入
         */
        public static final String FROM_NOTIFICATION = "from_notification";
        public static final String VIDEO_ID = "guide_video";
    }

    /**
     * 内部测试活动的相关变量
     */
    public static final class InternalTesting {
        /**
         * 是否内部测试
         */
        public static final boolean mIsInternalTesting = false;
    }

    /**
     * 屏幕方向
     */
    public static final class ScreenOrientation {
        public static final String LANDSCAPE = "landscape";
        public static final String PORTRAIT = "portrait";
    }

    public static final class LiveUri {
        /**
         * The content:// style URL for this table.
         */
        public static final Uri MESSAGE_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/message");

        public static final Uri FIREBASE_TOPIC_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/firebase_topic");

        public static final Uri USER_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/user_info");
    }
}
