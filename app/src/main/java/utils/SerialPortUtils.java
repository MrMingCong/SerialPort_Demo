package utils;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;

/**
 * Created by mingcong on 2018/5/15.
 */
public class SerialPortUtils {

    private final String TAG = "SerialPortUtils";
    private int baudrate = 9600;//波特率9600
    public boolean serialPortStatus = false; //是否打开串口标志
    public boolean threadStatus; //线程状态，为了安全终止线程

    public SerialPort serialPort = null;
    public InputStream inputStream = null;
    public OutputStream outputStream = null;
    public ChangeTool changeTool = new ChangeTool();


    /**
     * 打开串口
     *
     * @return serialPort串口对象
     */
    public SerialPort openSerialPort(String path) {

        if (path == null) {

        } else {
            Log.e("path", path);
            try {
                serialPort = new SerialPort(new File(path), baudrate, 0);
                this.serialPortStatus = true;
                threadStatus = false; //线程状态
                //获取打开的串口中的输入输出流，以便于串口数据的收发
                inputStream = serialPort.getInputStream();
                outputStream = serialPort.getOutputStream();


                new ReadThread().start(); //开始线程监控是否有数据要接收
            } catch (IOException e) {
                Log.e(TAG, "openSerialPort: 打开串口异常：" + e.toString());
                return serialPort;
            }
            Log.d(TAG, "openSerialPort: 打开串口");
            return serialPort;
        }
        return null;
    }

    /**
     * 关闭串口
     */
    public void closeSerialPort() {
        if (inputStream != null) {
            try {
                inputStream.close();
                outputStream.close();
                this.serialPortStatus = false;
                this.threadStatus = true; //线程状态
                serialPort.close();
                serialPort = null;
            } catch (IOException e) {
                Log.e(TAG, "closeSerialPort: 关闭串口异常：" + e.toString());
                return;
            }
            Log.d(TAG, "closeSerialPort: 关闭串口成功");
        }

    }


    /**
     * 发送串口指令
     *
     * @param
     */
    public void sendSerialPort(byte[] sendData) {
        Log.d(TAG, "sendSerialPort: 发送数据");
        try {
            if (sendData.length > 0) {
                outputStream.write(sendData);
                Log.d(TAG, "sendSerialPort: 串口数据发送成功" + sendData);
            }
        } catch (IOException e) {
            Log.e(TAG, "sendSerialPort: 串口数据发送失败：" + e.toString());
        }

    }

    public byte[] data;

    public synchronized void doActionSend(byte[] bData) throws FalconException, IOException {
        data = bData;
        if (outputStream == null) {
            Log.e(TAG, "outputstream null");
            throw new FalconException("outputstream null");
        }
        outputStream.write(bData);
        Log.d(TAG, "sendSerialPort: 串口数据发送成功");
    }

    /**
     * 单开一线程，来读数据
     */
    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();

            //判断进程是否在运行，更安全的结束进程
            while (!threadStatus) {
                Log.d(TAG, "进入线程run");
                //64   1024
                byte[] buffer = new byte[64];
                int size; //读取数据的大小
                try {
                    size = inputStream.read(buffer);
                    if (size > 0) {
                        Log.d(TAG, "run: 接收到了数据：" + changeTool.ByteArrToHex(buffer));
                        Log.d(TAG, "run: 接收到了数据大小：" + String.valueOf(size));
                        onDataReceiveListener.onDataReceive(buffer, size);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "run: 数据读取异常：" + e.toString());
                }
            }

        }
    }


    public OnDataReceiveListener onDataReceiveListener = null;

    public static interface OnDataReceiveListener {
        public void onDataReceive(byte[] buffer, int size);
    }

    public void setOnDataReceiveListener(OnDataReceiveListener dataReceiveListener) {
        onDataReceiveListener = dataReceiveListener;
    }

}
