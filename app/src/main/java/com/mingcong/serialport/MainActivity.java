package com.mingcong.serialport;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;
import utils.FalconException;
import utils.InstructionsTool;
import utils.SerialPortUtils;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    private Button button_open;
    private Button button_close;
    private Button button_selected;
    private Button button_send;
    private TextView textView_status;
    private Button button_status;
    private String path;
    private SerialPortUtils serialPortUtils = new SerialPortUtils();
    private SerialPort serialPort;

    private Handler handler;
    private byte[] mBuffer;
    private SerialPortFinder mSerialPortFinder;
    private Button button_send_white, button_send_blue, button_send_red;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSerialPortFinder = new SerialPortFinder();
        handler = new Handler(); //创建主线程的handler  用于更新UI
        button_open = (Button) findViewById(R.id.button_open);
        button_close = (Button) findViewById(R.id.button_close);
        button_send = (Button) findViewById(R.id.button_send);
        textView_status = (TextView) findViewById(R.id.textView_status);
        button_status = (Button) findViewById(R.id.button_status);
        button_selected = (Button) findViewById(R.id.button_selected);
        button_send_white = (Button) findViewById(R.id.button_send_white);
        button_send_blue = (Button) findViewById(R.id.button_send_blue);
        button_send_red = (Button) findViewById(R.id.button_send_red);


        button_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serialPort = serialPortUtils.openSerialPort(path);
                if (serialPort == null) {
                    Log.e(TAG, "串口打开失败");
                    Toast.makeText(MainActivity.this, "串口打开失败", Toast.LENGTH_SHORT).show();
                    return;
                }
                textView_status.setText("串口已打开");
                Toast.makeText(MainActivity.this, "串口已打开", Toast.LENGTH_SHORT).show();

            }
        });
        button_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serialPortUtils.closeSerialPort();
                textView_status.setText("串口已关闭");
                Toast.makeText(MainActivity.this, "串口关闭成功", Toast.LENGTH_SHORT).show();
            }
        });
        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send(InstructionsTool.GREEN);
            }
        });
        button_send_red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send(InstructionsTool.RED);
            }
        });
        button_send_blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send(InstructionsTool.BLUE);
            }
        });
        button_send_white.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send(InstructionsTool.WHITE);
            }
        });
        button_selected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String> list = getAllDevicesPath();
                if (list == null || list.size() <= 0) {
                    Toast.makeText(MainActivity.this, "没有串口设备", Toast.LENGTH_SHORT).show();
                    return;
                }
                SerialPortPathSelectDialog dialog = new SerialPortPathSelectDialog(MainActivity.this, list, new SerialPortPathSelectDialog.SelectListener() {
                    @Override
                    public void selected(int position, String value) {
                        button_selected.setText(value);
                        path = value;
                    }
                });
                dialog.show();
            }
        });
        button_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (serialPort != null) {
                    FileDescriptor fileDescriptor = serialPort.mFd;
                    String result = fileDescriptor.toString();
                    textView_status.setText(result);
                }
            }
        });
        //串口数据监听事件
        serialPortUtils.setOnDataReceiveListener(new SerialPortUtils.OnDataReceiveListener() {
            @Override
            public void onDataReceive(byte[] buffer, int size) {
                Log.d(TAG, "进入数据监听事件中。。。" + new String(buffer));
                //在线程中直接操作UI会报异常：ViewRootImpl$CalledFromWrongThreadException
                //解决方法：handler
                //
                mBuffer = buffer;
                handler.post(runnable);
            }

            //开线程更新UI
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    textView_status.setText("size：" + String.valueOf(mBuffer.length) + "数据监听：" + new String(mBuffer));
                }
            };
        });
    }

    /**
     * 获取全部串口地址
     *
     * @return
     */
    public List<String> getAllDevicesPath() {
        return Arrays.asList(mSerialPortFinder.getAllDevicesPath());
    }

    private void send(byte[] a) {
        try {
            serialPortUtils.doActionSend(a);
        } catch (FalconException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
