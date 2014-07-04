package com.auscultator.app;

/**
 * Created by HongYan on 2014/6/25.
 */
public class ErrorCode {
    public final static int SUCCESS = 0;
    public final static int ERR_NOSDCARD = 1;
    public final static int ERROR_CREATE_FILE = 2;
    public final static int ERR_STATE_RECODING = 3;
    public final static int ERR_UNKOWN = 4;
    public final static int ERR_STATE_PLAYING = 5;
    public final static int ERR_RECORD_DEVICE = 6;
    public final static int ERR_PLAY_DEVICE = 7;
    private final static String error_info_list[] = {
            "执行成功",
            "无法读取SD卡",
            "创建文件失败",
            "正在听诊中......",
            "未知错误",
            "正在播放听诊录音......",
            "录音设备未准备好",
            "播放设备未准备好"
    };

    public static String get_error_msg(int id) {
        return error_info_list[id];
    }
}
