package com.sjtu.yifei.aidlclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.sjtu.yifei.AbridgeCallBack;
import com.sjtu.yifei.IBridge;

public class TestAIDLActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_show_in_message;
    private EditText et_show_out_message;
    private AbridgeCallBack callBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aidl);
        findViewById(R.id.acquire_info).setOnClickListener(this);
        tv_show_in_message = findViewById(R.id.tv_show_in_message);
        et_show_out_message = findViewById(R.id.et_show_out_message);
        IBridge.registerAIDLCallBack(callBack = new AbridgeCallBack() {
            @Override
            public void receiveMessage(String message) {
                tv_show_in_message.setText(message);
            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.acquire_info) {
            String message = et_show_out_message.getText().toString();
            IBridge.sendAIDLMessage(message);
        }
    }

    @Override
    protected void onDestroy() {
        IBridge.uRegisterAIDLCallBack(callBack);
        super.onDestroy();
    }

}
