package com.sjtu.yifei.aidlserver;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.sjtu.yifei.messenger.MessengerReceiver;
import com.sjtu.yifei.messenger.MessengerSender;

public class TestMessengerActivity extends AppCompatActivity implements MessengerReceiver, View.OnClickListener {

    private static final String TAG = "TestMessengerActivity";
    public final static int ACTIVITYID = 0X0002;
    private EditText et_message;
    private TextView tv_show_message;
    private MessengerSender sender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);
        et_message = findViewById(R.id.et_message);
        tv_show_message = findViewById(R.id.tv_show_message);
        findViewById(R.id.btn_add).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_add) {
            String messageStr = "server :" + et_message.getText().toString();
            Message message = Message.obtain();
            message.arg1 = ACTIVITYID;
            //注意这里，把`Activity`的`Messenger`赋值给了`message`中，当然可能你已经发现这个就是`Service`中我们调用的`msg.replyTo`了。
            Bundle bundle = new Bundle();
            bundle.putString("content", messageStr);
            message.setData(bundle);
            sender.sendMessage(message);
        }
    }

    @Override
    public void setSender(MessengerSender sender) {
        this.sender = sender;
    }

    @Override
    public void receiveMessage(Message message) {
        if (message.arg1 == ACTIVITYID) {
            //客户端接受服务端传来的消息
            String str = (String) message.getData().get("content");
            tv_show_message.setText(str);
        }
    }
}
