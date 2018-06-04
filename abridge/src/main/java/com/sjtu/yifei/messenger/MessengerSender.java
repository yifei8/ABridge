package com.sjtu.yifei.messenger;

import android.os.Message;

/**
 * [向跨进程发送信息]
 * author: yifei
 * created at 18/6/2 下午11:51
 */

public interface MessengerSender {
    void sendMessage(Message message);
}
