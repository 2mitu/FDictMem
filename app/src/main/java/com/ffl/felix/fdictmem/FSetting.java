package com.ffl.felix.fdictmem;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.os.Bundle;
/**
 * Created by PengfeiLin on 2017/12/20.
 */

public class FSetting extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.frag_setting, container, false);

        TextView tv = (TextView) v.findViewById(R.id.about);
        tv.setText("Setting1!!");
        return v;
    }

}
