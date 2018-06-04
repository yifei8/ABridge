package com.sjtu.yifei.aidl;

/**
 * [向跨进程发送信息]
 * author: yifei
 * created at 18/6/2 下午11:51
 */

public interface ISender {
    void sendMessage(String json);
}
