package com.ffl.felix.fdictmem;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.os.Bundle;
import android.util.Log;

import com.ffl.felix.widget.ClearableAutoCompleteTextView;
import com.ffl.felix.widget.FelixArrayAdapter;

/**
 * Created by PengfeiLin on 2017/12/20.
 */

public class FDictSearch extends Fragment implements OnItemClickListener{
    private final static String TAG = "FDictSearch";
    View dictView;
    private ClearableAutoCompleteTextView inputEditBox;
    private TextView infoView;
    private FelixArrayAdapter arr_adapter;
    FDictMemInterface activityInterface;
    private int filter_type;
    private int dict_type;
    private boolean viewReady = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach ...");

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            activityInterface = (FDictMemInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnGetItemInfoListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate...");
        setHasOptionsMenu(true); //Enable fragment's optional menu
        filter_type = 1;
        dict_type = 0;
        //arr_adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1);
        arr_adapter = new FelixArrayAdapter(getContext(), android.R.layout.simple_list_item_1);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView start...");

        dictView = inflater.inflate(R.layout.frag_dict, container, false);
        inputEditBox = dictView.findViewById(R.id.item_input);
        inputEditBox.setAdapter(arr_adapter);
        inputEditBox.setOnItemClickListener(this);

        infoView = dictView.findViewById(R.id.item_info);
        infoView.setText("\n\nWelcome\n\n" + activityInterface.getDictionaryInfo());
        infoView.setMovementMethod(ScrollingMovementMethod.getInstance());
        viewReady = true;
        return dictView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "OnActivityCreated ...");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        //super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu, menu);
        switch (filter_type) {
            case 0:
                menu.findItem(R.id.filter_contains).setChecked(true);
                break;
            case 1:
                menu.findItem(R.id.filter_starts_with).setChecked(true);
                break;
            case 2:
                menu.findItem(R.id.filter_ends_with).setChecked(true);
                break;
            case 3:
                menu.findItem(R.id.filter_inner_it).setChecked(true);
                break;
        }

        switch (dict_type) {
            case 0:
                menu.findItem(R.id.dict_oxford).setChecked(true);
                break;
            case 1:
                menu.findItem(R.id.dict_langdao).setChecked(true);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch(item.getItemId()) {
            case R.id.filter_contains:
                filter_type = 0;
                arr_adapter.setFilterType(0);
                item.setChecked(true);
                break;
            case R.id.filter_starts_with:
                filter_type = 1;
                arr_adapter.setFilterType(1);
                item.setChecked(true);
                break;
            case R.id.filter_ends_with:
                filter_type = 2;
                arr_adapter.setFilterType(2);
                item.setChecked(true);
                break;
            case R.id.filter_inner_it:
                filter_type = 3;
                arr_adapter.setFilterType(3);
                item.setChecked(true);
                break;
            case R.id.about_info:
                infoView.setText("\n\nMany a little\nmakes\na mickle!\n\n\nuKnow v1.1 \nGet in " +
                        "touch with me:\n 2mitu@163.com");
                break;
            case R.id.dict_oxford:
                if(activityInterface.getDictionaryStatus(0)) {
                    dict_type = 0;
                    activityInterface.changeDictionary(0);
                    item.setChecked(true);
                }
                break;
            case R.id.dict_langdao:
                if(activityInterface.getDictionaryStatus(1)) {
                    dict_type = 1;
                    activityInterface.changeDictionary(1);
                    item.setChecked(true);
                }
                break;
        }
        //item.setChecked(beChecked);
        return super.onOptionsItemSelected(item);
    }

    public static void hideSoftKeyboard(EditText editText, Context context) {
        if (editText != null && context != null) {
            InputMethodManager imm = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    public void showItemInfo(String item, boolean save, boolean updateEditBox) {
        Log.d(TAG, "showItemInfo...");
        hideSoftKeyboard(inputEditBox, getContext());
        infoView.setText(activityInterface.getItemInfo(item, false));
        infoView.scrollTo(0,0);

        if(updateEditBox) {
            inputEditBox.setText(item);
            inputEditBox.setSelection(item.length());
            inputEditBox.dismissDropDown();
        }

        if(save){
            activityInterface.rememberJustSearchedItem(item);
        }
    }

    public void initializeDictionaryItemAdapter(String[] itemNames, String[] lcItemNames) {
        arr_adapter.updateOriginalValues(itemNames, lcItemNames);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "Save Instance State...");
        super.onSaveInstanceState(outState);
        outState.putString("TestString", "Useless!");
    }

    @Override
    public void onDestroy () {
        super.onDestroy();
    }

    //----Implement AdapterView.OnItemClickListener
    // OnItemSelectedListener does not work
    //One item is chosen by user from the suggestions list
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "suggestions item chosen...");
        showItemInfo(inputEditBox.getText().toString(), true, false);
    }

    public void notifyDictionaryInfo(String dictInfo) {
        if(viewReady) {
            infoView.setText("\n\n......\n\n" + dictInfo);
        }
    }
}
