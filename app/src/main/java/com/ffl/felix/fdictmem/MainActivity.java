package com.ffl.felix.fdictmem;

import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import com.ffl.felix.dictionary.FDictionary;
import com.ffl.felix.string.FString;

import android.util.Log;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity
        implements ViewPager.OnPageChangeListener,FDictMemInterface {
    private final static String TAG = "FDictMem";
    private final static String appFileFolder = "/Android/data/com.ffl.felix.fdictmem/dict/";
    private final String[] dictNames = {"oxford", "langdao"};
    private long mExitTime;//退出时的时间
    private FDictionary[] dictionaries;
    private FDictionary myDict;
    private boolean dictReady[] = {false, false};
    private int dictNum;
    LoadDictionaryTask loadTask0, loadTask1;
    private String dictInfo = "";

    //3 fragments
    private FDictSearch searchPage;
    private FItemMemory memoryPage;
    private FSearchHistory historyPage;

    //
    private int memoryPage_colorChoice;
    private String memoryPage_wordList;

    private ViewPager mViewPager;
    BottomNavigationView navigation;
    private MyOnNavigationItemSelectedListener navItemSelectedListener;
    private MyFragmentPagerAdapter fragmentPagerAdapter;

    private class MyFragmentPagerAdapter extends FragmentPagerAdapter{
        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return searchPage;
                case 1:
                    return historyPage;
                case 2:
                    return memoryPage;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    private class MyOnNavigationItemSelectedListener implements BottomNavigationView.OnNavigationItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getOrder()) {
                case 0:
                    mViewPager.setCurrentItem(0);
                    break;
                case 1:
                    mViewPager.setCurrentItem(1);
                    break;
                case 2:
                    mViewPager.setCurrentItem(2);
                    break;
            }
            return false;
        }
    };

    private void fieldsInitialization() {
        mExitTime = 0;
        memoryPage_colorChoice = 3;
        memoryPage_wordList = "Nothing";
        dictionaries = new FDictionary[2] ;

        historyPage = new FSearchHistory(); //Memory Page setting will be restored
        searchPage = new FDictSearch();
        memoryPage = new FItemMemory();

        navItemSelectedListener = new MyOnNavigationItemSelectedListener();
        fragmentPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());

        dictInfo = "正在加载字典......";
        Log.d(TAG, dictInfo);
        //加载oxford
        loadTask0 = new LoadDictionaryTask();
        loadTask0.execute(0);
        //加载langdao
        loadTask1 = new LoadDictionaryTask();
        loadTask1.execute(1);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate ...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        dictNum = -1;
        fieldsInitialization();

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(navItemSelectedListener);

        mViewPager = findViewById(R.id.main_view);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setAdapter(fragmentPagerAdapter);
    }

    //Following 3 methods implements ViewPager.OnPageChangeListener
    @Override
    public void onPageSelected(int position) {
        navigation.getMenu().getItem(position).setChecked(true);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    private class LoadDictionaryTask extends AsyncTask<Integer, Integer, Boolean> {
        private String dictDirStr;
        private boolean dictAvailable = true;
        private int dID;

        protected void onPreExecute() {
            dictDirStr = Environment.getExternalStorageDirectory().toString() + appFileFolder;
            File listDir = new File(dictDirStr);
            if (!listDir.exists()) {
                dictAvailable = false;
                if (listDir.mkdirs()) {
                    dictInfo = "请将词典文件放入该目录中:\n" + listDir;
                } else {
                    dictInfo = "不存在目录：\n" + listDir + "\n";
                    dictInfo += "请检查APP是否获得SD卡读写权限！！\n";
                }
            }
        }

        protected Boolean doInBackground(Integer... dictNo) {
            if(!dictAvailable) {
                return false;
            }

            dID = dictNo[0];
            dictionaries[dID] = new FDictionary(dictDirStr, dictNames[dID]);
            dictReady[dID] = dictionaries[dID].isReady();
            dictInfo = dictNames[dID] + " 加载";
            dictInfo += dictReady[dID] ? "完毕！":"失败！";
            Log.d(TAG, dictInfo);
            if(dictNum == -1 && dictReady[dID]) {
                dictNum = dID;
                myDict = dictionaries[dID];
                searchPage.initializeDictionaryItemAdapter(myDict.getAllItemName(), myDict.getAllItemNameInLowerCase());
            }
            return dictReady[dID];
        }

        protected void onProgressUpdate(Integer... progress) {
            dictInfo = "当前加载：" + progress[0] + "%";
        }

        protected void onPostExecute(Boolean result) {
            searchPage.notifyDictionaryInfo(dictInfo);
        }
    }

    //interface FDictMemInterface implementation -- BEGIN
    public String getDictionaryInfo() {
        return dictInfo;
    }

    public String getItemInfo(String item, boolean noExp) {
        String itemInfo;
        if(dictReady[dictNum]) {
            itemInfo =  myDict.getItemInfoFromDictionary(item);
            if(noExp) {
                itemInfo = FString.hideChineseCharacters('口', itemInfo);
            }
        }
        else {
            itemInfo = "Dictionary is not ready!!!";
        }
        return itemInfo;
    }

    public void rememberJustSearchedItem(String item) {
        historyPage.addItemToHistoryList(item);
    }

    public void changeDictionary(int dict) {
        if(dictNum != dict && dictReady[dict]) {
            Log.d(TAG, "changeDictionary: " + dict);
            dictNum = dict;
            myDict = dictionaries[dictNum];
            searchPage.initializeDictionaryItemAdapter(myDict.getAllItemName(), myDict.getAllItemNameInLowerCase());
        }
    }

    public boolean getDictionaryStatus(int dict) {
        Log.d(TAG,"getDictionaryStatus..." + dictReady[dict]);
        return dictReady[dict];
    }

    public void showItemInfoInDictionaryPage(String item) {
        searchPage.showItemInfo(item, false, true);
        mViewPager.setCurrentItem(0);
    }

    public int getMemoryPageColorChoice() {
        return memoryPage.getColorChoice();
    }
    public String getMemoryPageWordList() {
        return memoryPage.getWordListName();
    }
    public void restoreMemoryPageSetting(int color, String wordList) {
        memoryPage_colorChoice = color;
        memoryPage_wordList = wordList;
    }

    public int retrieveMemoryPageColorChoice() {
        return memoryPage_colorChoice;
    }
    public String retrieveMemoryPageWordList() {
        return memoryPage_wordList;
    }

    //interface FDictMemInterface implementation -- END

    //对返回键进行监听
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(MainActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy () {
        Log.d(TAG,"onDestroy...Cancelling the loadTask...");
        loadTask0.cancel(true);
        loadTask1.cancel(true);
        super.onDestroy();
    }
}
