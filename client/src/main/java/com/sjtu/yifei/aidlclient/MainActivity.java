package com.sjtu.yifei.aidlclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.sjtu.yifei.aidl.ISender;
import com.sjtu.yifei.aidl.IReceiver;

public class MainActivity extends AppCompatActivity implements IReceiver, View.OnClickListener {

    private ISender iCallRemote;
    private TextView tv_show_in_message;
    private EditText et_show_out_message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.acquire_info).setOnClickListener(this);
        tv_show_in_message = findViewById(R.id.tv_show_in_message);
        et_show_out_message = findViewById(R.id.et_show_out_message);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.acquire_info) {
            String message = et_show_out_message.getText().toString();
            iCallRemote.sendMessage(message);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void setSender(ISender sender) {
        this.iCallRemote = sender;
    }

    @Override
    public void receiveMessage(String message) {
        tv_show_in_message.setText(message);
    }
}
