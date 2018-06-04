package com.sjtu.yifei.aidlserver;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.sjtu.yifei.aidl.ISender;
import com.sjtu.yifei.aidl.IReceiver;
import com.sjtu.yifei.messenger.MessengerReceiver;
import com.sjtu.yifei.messenger.MessengerSender;

public class MainActivity extends AppCompatActivity implements MessengerReceiver, View.OnClickListener {

    private static final String TAG = "MainActivity";
    private EditText tv_name;
    private EditText tv_age;
    private EditText tv_user;
    private MessengerSender remoteCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_name = findViewById(R.id.tv_name);
        tv_age = findViewById(R.id.tv_age);
        tv_user = findViewById(R.id.tv_user);
        findViewById(R.id.btn_add).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_add) {
            String messageStr = "姓名:" + tv_name.getText().toString() + "，年龄：" + tv_age.getText().toString();
            Message message = Message.obtain();
            message.arg1 = ACTIVITYID;
            //注意这里，把`Activity`的`Messenger`赋值给了`message`中，当然可能你已经发现这个就是`Service`中我们调用的`msg.replyTo`了。
            Bundle bundle = new Bundle();
            bundle.putString("content", messageStr);
            message.setData(bundle);
            remoteCall.sendMessage(message);
        }
    }

    @Override
    public void setSender(MessengerSender sender) {
        this.remoteCall = sender;
    }

    public final static int ACTIVITYID = 0X0002;
    @Override
    public void receiveMessage(Message message) {
        if (message.arg1 == ACTIVITYID) {
            //客户端接受服务端传来的消息
            String str = (String) message.getData().get("content");
            tv_user.setText(str);
        }
    }
}
