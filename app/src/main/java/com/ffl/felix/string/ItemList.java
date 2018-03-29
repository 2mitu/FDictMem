package com.ffl.felix.string;

/**
 * Created by PengfeiLin on 2017/12/27.
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.lang.NumberFormatException;

import android.os.Environment;

public class ItemList {
    private final static String TAG = "ItemList";
    private final static String listFileFolder = "/Android/data/com.ffl.felix.fdictmem/wordlist/";
    private ArrayList<String> wordListNames;

    private String listName;
    private boolean itemListReady;
    private int showLevel;

    private int show_number;
    private int showed_number;

    private ArrayList<String> itemList;
    private ArrayList<Integer> levelList;
    private ArrayList<Integer> showList;

    public ItemList(){
        itemListReady = false;
        showLevel = 2;
        wordListNames = new ArrayList<String>();
        itemList = new ArrayList<String>();
        levelList = new ArrayList<Integer>();
        showList = new ArrayList<Integer>();
    }

    public ArrayList<String> getAllTheItemListName() {
        wordListNames.clear();
        String notExist = "";

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String listDirStr = Environment.getExternalStorageDirectory().toString() + listFileFolder;
            File listDir = new File(listDirStr);

            if(!listDir.exists())
            {
                if(listDir.mkdirs()) {
                    notExist = "请将单词列表文件放入该目录中:\n" + listFileFolder;
                }
                else {
                    notExist = "不存在目录：\n" + listFileFolder + "\n";
                    notExist += "请检查APP是否获得SD卡读写权限！！\n";
                }
            }
            else {
                //Word list file name suffix is ".txt"
                SuffixFilenameFilter filter = new SuffixFilenameFilter(".txt");
                String[] files = listDir.list(filter);
                //wordListNames.addAll(Arrays.asList(files));
                for(String name:files) {
                    wordListNames.add(name.substring(0, name.length() - 4));
                }
                notExist = "Done!";
                if(wordListNames.size() == 0){
                    notExist = "No files here:\n" + listFileFolder;
                }
            }
        } else {
            notExist = "--->SD not found!";
        }
        notExist = "getAllTheItemListName..." + notExist;
        Log.d(TAG, notExist);

        return wordListNames;
    }

    public Boolean openItemListFromFile(String name){
        if(listName == name && itemListReady){
            return itemListReady;
        }
        if(itemListReady) {
            saveCurrentItemListStatus();
        }
        Log.d(TAG, "openItemListFromFile...");
        String fileName = Environment.getExternalStorageDirectory().toString() + listFileFolder;
        fileName += name + ".txt";
        try {
            levelList.clear();
            itemList.clear();
            showList.clear();
            showed_number = 0;
            show_number = 0;

            FileInputStream fin = new FileInputStream(fileName);
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(fin, "UTF-8"));

            String str = null;
            while ((str = bufReader.readLine()) != null) {
                Log.d(TAG, str);
                String[] tabStr = str.split("\\t", 2);
                levelList.add(Integer.decode(tabStr[0]));
                itemList.add(tabStr[1]);
            }
            listName = name;
            itemListReady = true;
            bufReader.close();
            fin.close();
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
            itemListReady = false;
        }
        catch(IOException e) {
            e.printStackTrace();
            itemListReady = false;
        }
        catch(NumberFormatException e) {
            e.printStackTrace();
            itemListReady = false;
        }

        checkWhichItemShouldBeShown();
        return itemListReady;
    }

    public boolean isItemListReady() {
        return itemListReady;
    }

    public void setShowLevel(int level) {
        Log.d(TAG, "setShowLevel...");
        showLevel = level;
        checkWhichItemShouldBeShown();
    }

    public void resetItemLevel() {
        if( itemListReady && !levelList.isEmpty()){
            for (int i = 0; i < levelList.size(); i++) {
                levelList.set(i, new Integer(0));
            }
            checkWhichItemShouldBeShown();
        }
    }

    public boolean tryToShowNextItem() {
        boolean hasNext = false;
        if(show_number > 0 ) {
            if(showed_number+1 < show_number) {
                showed_number++;
                hasNext = true;
            }
            else if(showed_number+1 == show_number) {
                checkWhichItemShouldBeShown();
                if(show_number > 0 ) {
                    hasNext = true;
                }
            }
        }
        return hasNext;
    }

    public String getCurrentItemToShow() {
        if(show_number > 0 ) {
            return itemList.get(showList.get(showed_number));
        }
        else {
            return "No More";
        }
    }

    public String getCurrentProgress() {
        if(show_number > 0 ) {
            return showed_number+1 + "/" + show_number;
        }
        else {
            return "-/-";
        }
    }

    public String getCurrentItemLevel() {
        if(show_number > 0) {
            return "L" + levelList.get(showList.get(showed_number));
        }
        else {
            return "L";
        }
    }

    public void setCurrentItemLevel(boolean uKnow) {
        if(show_number <= 0 || showed_number+1 > show_number){
            return;
        }

        Integer level = levelList.get(showList.get(showed_number));
        if(uKnow) {
            level -= 1;
        }
        else {
            level += 2;
        }

        if(!uKnow && level < 0) {
            level = 0;
        }

        if(level < -20) {
            level = -20;
        }

        if(level > 20) {
            level = 20;
        }

        levelList.set(showList.get(showed_number), new Integer(level));
        Log.d(TAG, "setCurrentItemLevel...");
    }

    public void saveCurrentItemListStatus() {
        if(!itemListReady){
            return;
        }

        FileOutputStream fout;
        String itemStr;
        String fileName = Environment.getExternalStorageDirectory().toString() + listFileFolder;
        fileName += listName + ".txt";
        try{
            fout = new FileOutputStream(fileName);
            for (int i=0; i< itemList.size(); i++) {
                itemStr = String.format("%d\t%s\n", levelList.get(i), itemList.get(i));
                fout.write(itemStr.getBytes("UTF-8"));
            }
            fout.flush();
            fout.close();
        }
        catch (FileNotFoundException e){
            Log.d(TAG,"saveCurrentItemListStatus: FileNotFound" + listName);
        }
        catch (IOException e) {
            Log.d(TAG,"saveCurrentItemListStatus: IOException");
        }
    }

    private void checkWhichItemShouldBeShown() {
        if(!itemListReady) {
            return;
        }
        Log.d(TAG, "checkWhichItemShouldBeShown...");
        showList.clear();
        for( int i = 0; i < itemList.size(); i++) {
            switch(showLevel) {
                case 0: // All
                    showList.add(new Integer(i));
                    break;
                case 1: // Easy
                    if (levelList.get(i) < 0) {
                        showList.add(new Integer(i));
                    }
                    break;
                case 2: // Tough
                    if (levelList.get(i) >= 0) {
                        showList.add(new Integer(i));
                    }
                    break;
            }
        }
        Collections.shuffle(showList);
        showed_number = 0;
        show_number = showList.size();
    }
}
