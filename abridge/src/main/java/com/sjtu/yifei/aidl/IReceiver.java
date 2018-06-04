package com.sjtu.yifei.aidl;

/**
 * [接受远程发送过来的消息]
 * author: yifei
 * created at 18/6/2 下午11:52
 */
public interface IReceiver {
    void setSender(ISender sender);
    void receiveMessage(String message);
}
