package com.tw.marquee.demo;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.tw.marquee.lib.MarqueeView;

public class MainActivity extends AppCompatActivity {

    private MarqueeView mV3;
    private Button btStop,bt_control_continue,bt_control_add,bt_control_flicker,bt_control_reversal;
    private EditText ed_text;
    private Spinner bt_control_color,bt_control_size,bt_control_speed,bt_control_from,bt_control_alpha,bt_control_count,bt_control_repeat_count,bt_control_space;
    private boolean flag,flagReversal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
        initClickListenser();
    }

    private void initData() {

    }



    private void initClickListenser() {
        btStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mV3.stopRoll();
            }
        });
        bt_control_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mV3.continueRoll();
            }
        });
        bt_control_flicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag=!flag;
                mV3.setFlicker(flag);
            }
        });
        bt_control_reversal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flagReversal=!flagReversal;
                mV3.setReversalble(flagReversal);
            }
        });
        bt_control_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mV3.setContent(ed_text.getText().toString());
            }
        });

        //颜色
        bt_control_color.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                String str=parent.getItemAtPosition(position).toString();
                if(position>0)mV3.setTextColorByString(str);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
        //透明度
        bt_control_alpha.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                String str=parent.getItemAtPosition(position).toString();
                if(position>0)mV3.setTextAlpha(Integer.parseInt(str));//0-255
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
        //方向
        bt_control_from.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                String str=parent.getItemAtPosition(position).toString();
                if(position>0)mV3.setTextAngle(Integer.parseInt(str));//0-255
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
        //循环方式
        bt_control_count.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                String str=parent.getItemAtPosition(position).toString();
                if(position>0)mV3.setRepetType(Integer.parseInt(str));//0-255
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
        //循环次数
        bt_control_repeat_count.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                String str=parent.getItemAtPosition(position).toString();
                if(position>0)mV3.setRepetCounts(Integer.parseInt(str));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
        //设置文字大小
        bt_control_size.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                String str=parent.getItemAtPosition(position).toString();
                if(position>0)mV3.setTextSize(Integer.parseInt(str));//0-255
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
        //设置文字速度
        bt_control_speed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                String str=parent.getItemAtPosition(position).toString();
                if(position>0){

                    mV3.setTextTimeSpeed(Integer.parseInt(str));//0-255
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
        //设置文字间距
        bt_control_space.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                String str=parent.getItemAtPosition(position).toString();
                if(position>0)mV3.setTextDistance(Integer.parseInt(str));//0-255
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

    }

    private void initView() {
        ed_text = findViewById(R.id.ed_text);
        bt_control_add = findViewById(R.id.bt_control_add);
        bt_control_flicker = findViewById(R.id.bt_control_flicker);
        bt_control_reversal = findViewById(R.id.bt_control_reversal);
        bt_control_repeat_count = findViewById(R.id.bt_control_repeat_count);
        bt_control_color = findViewById(R.id.bt_control_color);
        bt_control_size = findViewById(R.id.bt_control_size);
        bt_control_speed = findViewById(R.id.bt_control_speed);
        bt_control_from = findViewById(R.id.bt_control_from);
        bt_control_alpha = findViewById(R.id.bt_control_alpha);
        bt_control_count = findViewById(R.id.bt_control_count);
        bt_control_space = findViewById(R.id.bt_control_space);
        bt_control_continue = findViewById(R.id.bt_control_continue);
        btStop = findViewById(R.id.bt_control_stop);
        mV3 = ((MarqueeView) findViewById(R.id.mv_main3));
    }
}
