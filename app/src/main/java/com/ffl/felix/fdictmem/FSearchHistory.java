package com.ffl.felix.fdictmem;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.NullPointerException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by PengfeiLin on 2017/12/28.
 */

public class FSearchHistory extends Fragment {
    private final static String TAG = "FSearchHistory";
    private final static String wordListDir = "/Android/data/com.ffl.felix.fdictmem/wordlist/";
    private ListView historyView;
    private Button saveButton;
    private ArrayAdapter<String> searchListAdapter;
    private FDictMemInterface activityInterface;
    private MyMultiChoiceModeListener multiChoiceModeListener;
    private MyOnItemClickListener itemClickListener;
    private MyOnClickButtonListener saveButtonClickListener;
    private ActionMode mActionMode;

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
                    + " must implement FDictMemInterface");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate...");
        initializeHistoryListFromFile(); //Initialize searchListAdapter
        multiChoiceModeListener = new MyMultiChoiceModeListener();
        itemClickListener = new MyOnItemClickListener();
        saveButtonClickListener = new MyOnClickButtonListener();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView start...");
        View hView = inflater.inflate(R.layout.frag_history, container, false);
        historyView = hView.findViewById(R.id.history_list);
        historyView.setAdapter(searchListAdapter);
        historyView.setMultiChoiceModeListener(multiChoiceModeListener);
        historyView.setOnItemClickListener(itemClickListener);
        saveButton = hView.findViewById(R.id.history_save);
        saveButton.setOnClickListener(saveButtonClickListener);
        return hView;
    }

    private class MyOnClickButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v){
            saveHistoryToWordList();
        }
    }

    private class MyOnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            activityInterface.showItemInfoInDictionaryPage(searchListAdapter.getItem(position).toString());
            Log.d(TAG, "setOnItemClickListener..." + position);
        }
    }

    private class MyMultiChoiceModeListener implements ListView.MultiChoiceModeListener {

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            //添加列表项被点击后的响应
            searchListAdapter.notifyDataSetChanged();
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            Log.d(TAG, "onCreateActionMode ...");
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.history_manage, menu);
            mode.setTitle(R.string.history_select);
            mActionMode = mode;
            //这里返回true
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            //这里返回true
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            boolean noitemselected = false;
            switch (item.getItemId()) {
                case R.id.select_all:
                    for(int i= 0; i< searchListAdapter.getCount(); i++) {
                        historyView.setItemChecked(i, true);
                    }
                    break;
                case R.id.select_reverse:
                    boolean itemchecked;
                    for(int i= 0; i< searchListAdapter.getCount(); i++) {
                        itemchecked = historyView.isItemChecked(i);
                        historyView.setItemChecked(i, !itemchecked);
                    }
                    break;
                case R.id.select_clear:
                    historyView.clearChoices();
                    searchListAdapter.notifyDataSetChanged();
                    break;
                case R.id.select_delete:
                    noitemselected = true;
                    SparseBooleanArray selecteditemarray = historyView.getCheckedItemPositions();
                    Log.d(TAG, "Checked count: " + historyView.getCheckedItemCount());
                    for(int i = selecteditemarray.size() - 1; i >= 0; i-- ) {
                        if(selecteditemarray.valueAt(i)) {
                            String deleteitem = searchListAdapter.getItem(selecteditemarray.keyAt(i)).toString();
                            Log.d(TAG, "Delete: " + deleteitem);
                            searchListAdapter.remove(deleteitem);
                        }
                    }
                    break;
                default:
                    break;
            }
            if(noitemselected){
                mode.finish();
            }

            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            Log.d(TAG, "onDestroyActionMode...");
            mActionMode = null;
        }
    }

    private void initializeHistoryListFromFile(){
        searchListAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_multiple_choice);
        Log.d(TAG, "initializeHistoryListFromFile...");
        String fileName = Environment.getExternalStorageDirectory().toString() + wordListDir + "fdictmem.history";
        try {
            FileInputStream fin = new FileInputStream(fileName);
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(fin, "UTF-8"));
            String hItem, hItem1;
            //get the Memory Page color choice & lists name
            hItem = bufReader.readLine();
            hItem1 = bufReader.readLine();
            if(hItem != null && hItem1 != null) {
                activityInterface.restoreMemoryPageSetting(Integer.decode(hItem), hItem1);
                Log.d(TAG, "restoreMemoryPageSetting" + hItem + hItem1);
            }

            while ((hItem = bufReader.readLine()) != null) {
                searchListAdapter.add(hItem);
            }
            bufReader.close();
            fin.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "initializeHistoryListFromFile: FileNotFound" + fileName);
        } catch (IOException e) {
            Log.d(TAG, "initializeHistoryListFromFile: IOException");
        }
    }

    public void addItemToHistoryList(String item) {
        searchListAdapter.add(item);
    }

    private void saveHistoryItemToFile() {
        Log.d(TAG,"saveHistoryItemToFile...");
        FileOutputStream fileOut;
        String itemStr;
        String fileName = Environment.getExternalStorageDirectory().toString() + wordListDir + "fdictmem.history";
        try{
            fileOut = new FileOutputStream(fileName);
            //Save memory page setting
            itemStr = activityInterface.getMemoryPageColorChoice() + "\n";
            fileOut.write(itemStr.getBytes("UTF-8"));
            Log.d(TAG, itemStr);

            itemStr = activityInterface.getMemoryPageWordList() + "\n";
            fileOut.write(itemStr.getBytes("UTF-8"));
            Log.d(TAG, itemStr);

            for (int i=0; i< searchListAdapter.getCount(); i++) {
                itemStr = searchListAdapter.getItem(i).toString() + "\n";
                Log.d(TAG, itemStr);
                fileOut.write(itemStr.getBytes("UTF-8"));
            }
            fileOut.flush();
            fileOut.close();
        }
        catch (FileNotFoundException e){
            Log.d(TAG,"saveHistoryItemToFile: FileNotFound" + fileName);
        }
        catch (IOException e) {
            Log.d(TAG,"saveHistoryItemToFile: IOException");
        }
        catch (NullPointerException e) {
            Log.d(TAG,"saveHistoryItemToFile: searchListAdapter.getItem(i).toString() NullPointerException");
        }
    }

    public void saveHistoryToWordList() {
        Log.d(TAG,"saveHistoryToWordList...");
        FileOutputStream fout;
        String itemStr;
        String fileName = Environment.getExternalStorageDirectory().toString() + wordListDir;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");
        String nowStr=format.format(new Date());
        fileName += nowStr + ".txt";
        try{
            fout = new FileOutputStream(fileName);
            for (int i=0; i< searchListAdapter.getCount(); i++) {
                itemStr = String.format("2\t%s\n", searchListAdapter.getItem(i).toString());
                fout.write(itemStr.getBytes("UTF-8"));
            }
            fout.flush();
            fout.close();
            searchListAdapter.clear();
        }
        catch (FileNotFoundException e){
            Log.d(TAG,"saveHistoryToWordList: FileNotFound" + fileName);
        }
        catch (IOException e) {
            Log.d(TAG,"saveHistoryToWordList: IOException");
        }
        catch (NullPointerException e) {
            Log.d(TAG,"saveHistoryToWordList: searchListAdapter.getItem(i).toString() NullPointerException");
        }

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(TAG, "onHiddenChanged ..." + hidden);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        Log.d(TAG, "setUserVisibleHint ...");
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser && mActionMode != null) {
            mActionMode.finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause ...");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop ...");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView ...");
    }

    @Override
    public void onDestroy () {
        super.onDestroy();
        Log.d(TAG, "onDestroy ...");
        saveHistoryItemToFile();
    }
}
