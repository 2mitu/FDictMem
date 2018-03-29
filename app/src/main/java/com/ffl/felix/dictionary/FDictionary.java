package com.ffl.felix.dictionary;

/**
 * Created by PengfeiLin on 2017/12/20.
 */
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import com.ffl.felix.math.NumberUtil;

import java.util.Calendar;
import java.util.regex.*;
import java.lang.Integer;

public class FDictionary {
    private final static String TAG = "FDictionary";
    final static int itemSizeByteNumber;
    final static int itemOffsetByteNumber;
    //for item loading from Index file
    byte[] itemOffsetBytes;
    byte[] itemSizeBytes;
    byte[] itemBytes;
    byte byteNumber;
    int itemOffset, itemSize;
    //Dictionary files
    String dictDir;
    String dictPrefix;
    String infoFile, indexFile, dictFile;
    FileInputStream fin, fdict;
    //Dictionary information
    public String[] items;
    public String[] lcItems;
    private int[] infoOffset;
    private int[] infoSize;

    private String dictName;
    private int totalItems;
    private int itemMaxByteNumber;
    boolean dictReady;

    // class initializer.
    static {
        itemOffsetByteNumber = 4;
        itemSizeByteNumber = 4;
    }

    // Instance/object initializer
    {
        itemSizeBytes = new byte[itemSizeByteNumber];
        itemOffsetBytes = new byte[itemOffsetByteNumber];
        dictReady = false;
    }

    public FDictionary(String dir, String dictName){
        boolean dictAvailable = true;
        dictDir = dir;
        switch(dictName.toLowerCase()){
            case "oxford": dictPrefix = "oxford-gb";
                break;
            case "langdao": dictPrefix = "langdao-ec-gb";
                break;
            default: dictPrefix = "";
                dictAvailable = false;
                break;
        }

        if(dictAvailable) {
            infoFile = dictDir + dictPrefix + ".ifo";
            indexFile = dictDir + dictPrefix + "-F.idx";
            dictFile = dictDir + dictPrefix + ".dict";

            long time_lasted = Calendar.getInstance().getTimeInMillis();
            if(readDictionaryGeneralInfo()) {
                loadDictionaryItemInfoFromIndexFile();
            }
            time_lasted = Calendar.getInstance().getTimeInMillis() - time_lasted;
            Log.d(TAG, "loadDictionary, time consumed: " + time_lasted + "ms");
        }
        else {
            Log.d(TAG, "\n%s is not supported!" + dictName);
        }
    }

    private void loadDictionaryItemInfoFromIndexFile() {
        //加入了单词长度的Index文件， 依次存储：
        //  4Bytes单词信息的其实位置（用于查单词）
        //  4Bytes单词信息的长度（用于查单词）
        //  1Bytes单词长度（用于读取Index文件，Felix加入，为了更高效）
        //  ？Bytes单词本身（用于读取Index文件，按照上一个长度值来读取）
        itemBytes = new byte[itemMaxByteNumber];
        int itemCount = 0;

        items = new String[totalItems];
        lcItems = new String[totalItems];
        infoOffset = new int[totalItems];
        infoSize = new int[totalItems];

        boolean readMore = true;
        try{
            fin = new FileInputStream(indexFile);
            while(readMore) {
                if( fin.read(itemBytes, 0, 9) == 9) {
                    //Read item "infoOffset": 4 bytes
                    itemOffset = NumberUtil.byte4ToInt(itemBytes, 0, true);
                    //Read item "infoSize": 4 bytes
                    itemSize = NumberUtil.byte4ToInt(itemBytes, 4, true);
                    //Read item "length": 1 byte
                    byteNumber = itemBytes[8];
                    if(fin.read(itemBytes, 0, byteNumber) == byteNumber){
                        //Read all info of an item
                        infoOffset[itemCount] = itemOffset;
                        infoSize[itemCount] = itemSize;
                        items[itemCount] = new String(itemBytes, 0, byteNumber, "UTF-8");
                        lcItems[itemCount] = items[itemCount].toLowerCase();

                        if(itemCount++ >= totalItems){
                            readMore = false;
                            if(fin.read() != -1) {
                                Log.d(TAG, "\nloadDictionaryItemInfoWithItemLength: End of File not reached!!!");
                            }
                        }
                    }
                    else {
                        readMore = false;
                    }
                }
                else {
                    readMore = false;
                }
            }

            Log.d(TAG, "\nRead items:" + itemCount);
            if(itemCount != totalItems) {
                totalItems = itemCount;
                Log.d(TAG, "\nWarning: expected items number:" + totalItems);
            }
            if(itemCount > 0) {
                dictReady = true;
            }

            fin.close();
        }
        catch (FileNotFoundException e){
            // The error handling code goes here
            Log.d(TAG, "FileNotFound"+indexFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isReady() {
        return dictReady;
    }

    public String[] getAllItemName() {
        if(!dictReady) {
            return new String[0];
        }
        else {
            return items;
        }
    }

    public String[] getAllItemNameInLowerCase() {
        if(!dictReady) {
            return new String[0];
        }
        else {
            return lcItems;
        }
    }

    public String getItemInfoFromDictionary(String itemID){
        if(!dictReady) {
            return "Dictionary is not ready!";
        }

        int foundAt = searchItemInItemArray(itemID);
        String infoStr;
        if(foundAt != -1) {
            infoStr = items[foundAt] + "\n";
            infoStr += readItemInfoFromDictFile(infoOffset[foundAt], infoSize[foundAt]);
        }
        else {
            infoStr = "Nothing found!";
        }

        return infoStr;
    }

    public int searchItemInItemArray(String item2Find) {
        if(!dictReady) {
            return -1;
        }

        int aPos = 0;
        int zPos = totalItems - 1;
        int mPos = -1;
        int compareResult = 0;
        int sNumber = 0;

        while(aPos <= zPos) {
            mPos = (aPos + zPos)/2;
            compareResult = item2Find.compareToIgnoreCase(items[mPos]);
            //Log.d(TAG, String.format("diff=%d",compareResult));
            if(compareResult == 0){
                break;
            }
            else if(compareResult > 0) {
                if(zPos > mPos) {
                    aPos = mPos + 1;
                }
                else {
                    break;
                }
            }
            else {
                if(aPos < mPos) {
                    zPos = mPos -1;
                }
                else {
                    break;
                }
            }
            sNumber++;
        }
        if(compareResult != 0 ) {
            Log.d(TAG, String.format("\nSearch %-2d times, Found a similar:%s", sNumber, items[mPos]));
        }
        return mPos;
    }

    private String readItemInfoFromDictFile(int infoPosition, int infoLength){

        int readBytes = 0;
        byte[] itemInfo = new byte[infoLength];

        try{
            fdict = new FileInputStream(dictFile);

            fdict.skip(infoPosition);
            readBytes = fdict.read(itemInfo);

            fdict.close();

            if( readBytes == infoLength) {
                String strInfo = new String(itemInfo, 0, readBytes, "UTF-8");
                return strInfo;
            }
            else{
                return "Read item info: not the same length!";
            }
        }
        catch (FileNotFoundException e){
            return "FileNotFound1: "+dictFile;
        }
        catch (IOException e) {
            return "Read item info: IOException";
        }
        catch (IndexOutOfBoundsException e) {
            return "Read item info: IndexOutOfBoundsException";
        }
    }

    private boolean readDictionaryGeneralInfo() {
        boolean okName = false;
        boolean okItemNumber = false;
        boolean okItemLength = false;
        boolean okDict = true;

        Pattern pName = Pattern.compile("^bookname=.*");
        Pattern pItemNum = Pattern.compile("^wordcount=\\d*");
        Pattern pItemLength = Pattern.compile("^itemmaxlength=\\d*");

        try {
            fin = new FileInputStream(infoFile);
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(fin, "UTF-8"));

            String str = null;
            while((str = bufReader.readLine()) != null) {
                if(pName.matcher(str).matches()) {
                    dictName = str.substring(9);
                    Log.d(TAG, String.format("\nDictionary name: %s", dictName));
                    okName = true;
                }

                if(pItemNum.matcher(str).matches()) {
                    totalItems = Integer.valueOf(str.substring(10)).intValue();
                    Log.d(TAG, String.format("\nwordcount: %d", totalItems));
                    okItemNumber = true;
                }

                if(pItemLength.matcher(str).matches()) {
                    itemMaxByteNumber = Integer.valueOf(str.substring(14)).intValue();
                    Log.d(TAG, String.format("\nitemmaxlength: %d", itemMaxByteNumber));
                    okItemLength = true;
                }
            }
            bufReader.close();
            fin.close();
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
            okDict = false;
        }
        catch(IOException e) {
            e.printStackTrace();
            okDict = false;
        }

        if(okDict && okName && okItemNumber && okItemLength) {
            return true;
        }
        else {
            Log.d(TAG, "\nCan not load the general info of this dictionary!");
            return false;
        }
    }
}
