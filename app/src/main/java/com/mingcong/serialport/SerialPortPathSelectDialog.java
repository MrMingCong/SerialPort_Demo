package com.mingcong.serialport;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.List;


public class SerialPortPathSelectDialog extends Dialog {

    private ListView lv_path;
    private Context conext;
    private List<String> mList;
    private SelectListener listener;

    public SerialPortPathSelectDialog(Context context, List<String> mList, SelectListener listener) {
        super(context, R.style.MyDialogStyle);
        this.conext = context;
        this.mList = mList;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.serial_port_select_layout);


        lv_path = (ListView) findViewById(R.id.lv_path);
        lv_path.setAdapter(new SerialPortPathAdapter(conext, mList));
        lv_path.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                listener.selected(position, mList.get(position));
                dismiss();
            }
        });
    }


    public interface SelectListener {
        void selected(int position, String value);
    }


}
