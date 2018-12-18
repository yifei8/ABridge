package com.sjtu.yifei.aidlclient;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.sjtu.yifei.AbridgeMessengerCallBack;
import com.sjtu.yifei.IBridge;

public class TestMessengerActivity extends AppCompatActivity implements View.OnClickListener {

    public final static int ACTIVITYID = 0X0002;
    private TextView tv_show_in_message;
    private EditText et_show_out_message;

    private AbridgeMessengerCallBack callBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);
        findViewById(R.id.acquire_info).setOnClickListener(this);
        tv_show_in_message = findViewById(R.id.tv_show_in_message);
        et_show_out_message = findViewById(R.id.et_show_out_message);

        IBridge.registerMessengerCallBack(callBack = new AbridgeMessengerCallBack() {
            @Override
            public void receiveMessage(Message message) {
                if (message.arg1 == ACTIVITYID) {
                    //客户端接受服务端传来的消息
                    String str = (String) message.getData().get("content");
                    tv_show_in_message.setText(str);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.acquire_info) {
            String messageStr =  "client :" + et_show_out_message.getText().toString();
            Message message = Message.obtain();
            message.arg1 = ACTIVITYID;
            //注意这里，把`Activity`的`Messenger`赋值给了`message`中，当然可能你已经发现这个就是`Service`中我们调用的`msg.replyTo`了。
            Bundle bundle = new Bundle();
            bundle.putString("content", messageStr);
            message.setData(bundle);
            IBridge.sendMessengerMessage(message);
        }
    }

    @Override
    protected void onDestroy() {
        IBridge.uRegisterMessengerCallBack(callBack);
        super.onDestroy();
    }

}
