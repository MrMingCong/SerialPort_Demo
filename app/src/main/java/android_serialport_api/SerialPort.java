package android_serialport_api;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPort {

    private static final String TAG = "SerialPort";
    public FileDescriptor mFd=null;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    public SerialPort(File device, int baudrate, int flags) throws SecurityException, IOException {

        //检查访问权限，如果没有读写权限，进行文件操作，修改文件访问权限
        if (!device.canRead() || !device.canWrite()) {

            try {
                //通过挂载到linux的方式，修改文件的操作权限
                Process su = Runtime.getRuntime().exec("/system/xbin/su");
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n" + "exit\n";
                su.getOutputStream().write(cmd.getBytes());

                if ((su.waitFor() != 0) || !device.canRead() || !device.canWrite()) {
                    Log.e("SerialPort", "SerialPort: 没有权限");
                    throw new SecurityException();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new SecurityException();
            }
        }

        mFd = open(device.getAbsolutePath(), baudrate, flags);

        if (mFd == null) {
            Log.e(TAG, "native open returns null");
            throw new IOException();
        }

        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }

    // Getters and setters
    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }


    // JNI(调用java本地接口，实现串口的打开和关闭)
    private native static FileDescriptor open(String path, int baudrate, int flags);

    public native void close();

    static {//加载jni下的C文件库
        Log.d(TAG, "本地库加载中");
        System.loadLibrary("serial_port");
    }
}
