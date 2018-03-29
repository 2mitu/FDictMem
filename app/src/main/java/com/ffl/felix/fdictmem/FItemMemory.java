package com.ffl.felix.fdictmem;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RadioGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.ArrayAdapter;
import android.os.Bundle;
import android.util.Log;

import com.ffl.felix.string.ItemList;

import java.util.ArrayList;

/**
 * Created by PengfeiLin on 2017/12/20.
 */

public class FItemMemory extends Fragment {
    private final static String TAG = "FItemMemory";
    private TextView itemView;
    private TextView itemExplanationView;
    private TextView levelView, listShowProgressView;
    private CheckBox showChinese;
    private Spinner listNameSpinner;
    private ArrayAdapter<String> arr_adapter;
    private ArrayList<String> list_names;
    private ItemList itemList;
    private String listName;
    boolean iKonw;
    FDictMemInterface mCallback;
    int colorChoice;
    private MyResetButtonOnClickListener resetButtonClickListener;
    private MyShowExplanationOnClickListener showExplanationOnClickListener;
    private MyShowNextItemOnClickListener showNextItemOnClickListener;
    private MyShowLevelOnCheckedChangeListener showLevelOnCheckedChangeListener;
    private MyItemListOnItemSelectedListener itemListOnItemSelectedListener;

    private class MyResetButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v){
            Log.d(TAG, "resetB.onClick...");
            itemList.resetItemLevel();
            showCurrentItem();
        }
    }

    private class MyShowNextItemOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v){ showTheNextItem(v);}
    }

    private class MyShowExplanationOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v){ showTheExplanation(v);}
    }

    private class MyShowLevelOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {
        public void onCheckedChanged (RadioGroup group, int checkedId) {
            Log.d(TAG, "levelRadio.onCheckedChanged...");
            int level=2;
            switch(checkedId) {
                case R.id.item_all:
                    level = 0;
                    break;
                case R.id.item_easy:
                    level = 1;
                    break;
                case R.id.item_tough:
                    level = 2;
                    break;
            }
            itemList.setShowLevel(level);
            showCurrentItem();
        }
    }

    private class MyItemListOnItemSelectedListener implements Spinner.OnItemSelectedListener {
        public void onItemSelected (AdapterView < ? > arg0, View arg1,
        int arg2, long arg3){
            Log.d(TAG, "listNameSpinner.onItemSelected...");
            if(listNameSpinner.getSelectedItem().equals(listName)) {
                if(!itemList.isItemListReady()) {
                    changeItemList(list_names.get(arg2));
                }
            }
            else {
                    changeItemList(list_names.get(arg2));
            }
        }

        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }
    }
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach ...");

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (FDictMemInterface) context;
            colorChoice = mCallback.retrieveMemoryPageColorChoice();
            listName = mCallback.retrieveMemoryPageWordList();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnGetItemInfoListener");
        }
    }

    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate...");
        iKonw = true;
        itemList = new ItemList();

        list_names  = itemList.getAllTheItemListName();

        arr_adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, list_names);
        arr_adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice); // select_dialog_singlechoice

        setHasOptionsMenu(true); //Enable fragment's optional menu

        resetButtonClickListener = new MyResetButtonOnClickListener();
        showExplanationOnClickListener = new MyShowExplanationOnClickListener();
        showNextItemOnClickListener = new MyShowNextItemOnClickListener();
        showLevelOnCheckedChangeListener = new MyShowLevelOnCheckedChangeListener();
        itemListOnItemSelectedListener = new MyItemListOnItemSelectedListener();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView start...");
        View memView = inflater.inflate(R.layout.frag_mem, container, false);
        //memView.setBackgroundColor(Color.LTGRAY);
        levelView = memView.findViewById(R.id.item_level);
        listShowProgressView = memView.findViewById(R.id.progress);
        itemView = memView.findViewById(R.id.item_view);
        itemExplanationView = memView.findViewById(R.id.explanation_view);
        itemExplanationView.setMovementMethod(ScrollingMovementMethod.getInstance());

        Button resetB = memView.findViewById(R.id.reset_level);
        resetB.setOnClickListener(resetButtonClickListener);

        Button showB = memView.findViewById(R.id.show_me);
        showB.setOnClickListener(showExplanationOnClickListener);
        showChinese = memView.findViewById(R.id.verify);


        Button knowB = memView.findViewById(R.id.already_know);
        knowB.setOnClickListener(showNextItemOnClickListener);

        RadioGroup levelRadio = memView.findViewById(R.id.chose_level);
        levelRadio.setOnCheckedChangeListener(showLevelOnCheckedChangeListener);

        listNameSpinner  = memView.findViewById(R.id.item_list);
        listNameSpinner.setAdapter(arr_adapter);
        listNameSpinner.setOnItemSelectedListener(itemListOnItemSelectedListener);

        listNameSpinner.setSelection(list_names.indexOf(listName), true);
        changeItemViewBackground(colorChoice);
        return memView;
    }

    public void showTheExplanation(View v) {
        iKonw = false;
        String info = mCallback.getItemInfo(itemView.getText().toString(), showChinese.isChecked());
        itemExplanationView.setText(info);
    }

    public void showTheNextItem(View v) {
        Log.d(TAG, "showTheNextItem...");
        if(!itemList.isItemListReady()) {
            return;
        }
        itemList.setCurrentItemLevel(iKonw);
        iKonw = true;
        itemList.tryToShowNextItem();
        showCurrentItem();
    }

    private void showCurrentItem() {
        Log.d(TAG, "showCurrentItem...");
        itemView.setText(itemList.getCurrentItemToShow());
        levelView.setText(itemList.getCurrentItemLevel());
        listShowProgressView.setText(itemList.getCurrentProgress());
        itemExplanationView.setText("");
    }

    private void changeItemList(String new_list) {
        Log.d(TAG, "changeItemList..." + new_list);
        itemList.openItemListFromFile(new_list);
        listName = new_list;
        showCurrentItem();
    }

    private void changeItemViewBackground(int colorOption){
        int textColor, bkColor;
        switch (colorOption) {
            case 1:
                textColor = R.color.colorItemViewText1;
                bkColor = R.color.colorItemViewBackground1;
                break;
            case 2:
                textColor = R.color.colorItemViewText2;
                bkColor = R.color.colorItemViewBackground2;
                break;
            case 3:
                textColor = R.color.colorItemViewText3;
                bkColor = R.color.colorItemViewBackground3;
                break;
            case 4:
                textColor = R.color.colorItemViewText4;
                bkColor = R.color.colorItemViewBackground4;
                break;
            case 5:
                textColor = R.color.colorItemViewText5;
                bkColor = R.color.colorItemViewBackground5;
                break;
            default:
                textColor = R.color.colorItemViewText3;
                bkColor = R.color.colorItemViewBackground3;
                break;
        }
        itemView.setBackgroundColor(ContextCompat.getColor(getContext(), bkColor));
        itemView.setTextColor(ContextCompat.getColor(getContext(), textColor));
        colorChoice = colorOption;
    }

    public int getColorChoice() {
        return colorChoice;
    }
    public String getWordListName() {
        return listName;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        //super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.memory_setting, menu);
        switch (colorChoice){
            case 1:
                menu.findItem(R.id.color_a).setChecked(true);
                break;
            case 2:
                menu.findItem(R.id.color_b).setChecked(true);
                break;
            case 3:
                menu.findItem(R.id.color_c).setChecked(true);
                break;
            case 4:
                menu.findItem(R.id.color_d).setChecked(true);
                break;
            case 5:
                menu.findItem(R.id.color_e).setChecked(true);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        int color = 3;
        switch(item.getItemId()) {
            case R.id.color_a:
                color = 1;
                break;
            case R.id.color_b:
                color = 2;
                break;
            case R.id.color_c:
                color = 3;
                break;
            case R.id.color_d:
                color = 4;
                break;
            case R.id.color_e:
                color = 5;
                break;
        }
        item.setChecked(true);
        changeItemViewBackground(color);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        Log.d(TAG, "onSaveInstanceState ...");

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView ...");
    }

    @Override
    public void onDestroy () {
        super.onDestroy();
        Log.d(TAG, "onDestroy ...saveCurrentItemListStatus");
        itemList.saveCurrentItemListStatus();
    }
}
