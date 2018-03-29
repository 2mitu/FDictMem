package com.ffl.felix.fdictmem;

/**
 * Created by PengfeiLin on 2018/1/12.
 */

interface FDictMemInterface {
    String getItemInfo(String item, boolean noExp); //Called by DictionaryPage
    void rememberJustSearchedItem(String item); //Called by DictionaryPage
    void changeDictionary(int dict); //Called by DictionaryPage
    boolean getDictionaryStatus(int dict); //Called by DictionaryPage
    String getDictionaryInfo(); //Called by DictionaryPage
    void showItemInfoInDictionaryPage(String item); //Called by HistoryPage
    int getMemoryPageColorChoice(); //Called by HistoryPage
    String getMemoryPageWordList(); //Called by HistoryPage
    void restoreMemoryPageSetting(int color, String wordList); //Called by HistoryPage
    int retrieveMemoryPageColorChoice(); //Called by MemoryPage
    String retrieveMemoryPageWordList(); //Called by MemoryPage
}
