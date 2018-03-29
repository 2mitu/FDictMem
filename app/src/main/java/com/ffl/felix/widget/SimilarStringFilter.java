package com.ffl.felix.widget;

/**
 * Created by PengfeiLin on 2018/3/16.
 */
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SimilarStringFilter {
    private int threadCount;
    private ExecutorService exe;
    private String[] items;
    private String[] lcItems;
    private int itemCount;
    private String lcToFind;
    private int relation;
    private ArrayList<ArrayList<String>> filteredValues;
    private List<StringFilter> tasks;
    private ArrayList<String> searched;


    public SimilarStringFilter() {

        threadCount = Runtime.getRuntime().availableProcessors()-1;
        if(threadCount < 1) { threadCount = 1; }

        exe = Executors.newFixedThreadPool(threadCount);
        relation = 1;

        tasks = new ArrayList<StringFilter>();
        filteredValues = new ArrayList<ArrayList<String>>();
        for(int i = 0; i < threadCount; i++ ) {
            filteredValues.add(new ArrayList<String>());
        }
    }

    public void updateItems(String[] items, String[] lcItems) {
        this.items = items;
        itemCount = items.length;

        //convert Items To Lowercase
        this.lcItems = lcItems;
        System.out.printf("\nupdateItems: Items To Lowercase...");
/*        lcItems = new String[itemCount];
        for(int i = 0; i < itemCount; i++ ) {
            lcItems[i] = items[i].toLowerCase();
        }
*/
        //initialize stringFilters
        System.out.printf("\nupdateItems: initialize stringFilters...");
        tasks.clear();
        int aIndex, zIndex;
        for(int i = 0; i < threadCount; i++ ) {
            aIndex = i*itemCount/threadCount;
            zIndex = (i+1)*itemCount/threadCount - 1;
            if(i == threadCount -1){
                zIndex = itemCount - 1;
            }
            tasks.add(new StringFilter(aIndex, zIndex, i));
        }
    }

    public void setFiltertype(int relation){
        //0: Contains
        //1: Starts with
        //2: Ends with
        //3: Contains but not start/end with
        this.relation = relation;
    }

    public ArrayList<String> searchSimilarString(String objStr){
        lcToFind = objStr.toLowerCase();
        try {
            exe.invokeAll(tasks, 10, TimeUnit.SECONDS);
            //启动所有线程，等待它们完成或者超时5秒钟结束
            System.out.printf("\nSearching the string: %s", objStr);
            for(int i = 1; i < threadCount; i++ ) {
                filteredValues.get(0).addAll(filteredValues.get(i));
            }
        }
        catch (Exception e) {
            System.out.printf("\n%s", e.getCause());
        }
        searched = filteredValues.get(0);
        filteredValues.set(0, new ArrayList<String>());
        return searched;
    }

    public void shutdown() {
        exe.shutdown();
    }


    private class StringFilter implements Callable<ArrayList<String>> {
        private int begin;
        private int end;
        private int filterID;
        private ArrayList<String> filtered;
        private int candidateCount;
        private boolean candidate;
        private boolean contain ;
        private boolean start_With;
        private boolean end_With;
        private String[] words;
        private int word_i;
        private int word_split_size;
        String myName;

        public StringFilter(int begin, int end, int filterID) {
            this.begin = begin;
            this.end = end;
            this.filterID = filterID;
        }

        public ArrayList<String> call() {
            filtered = filteredValues.get(filterID);
            filtered.clear();
            candidateCount = 0;
            for(int i = begin; i <= end; i++) {
                if(isItemACandidate(lcItems[i])) {
                    filtered.add(items[i]);
                    candidateCount++;
                }
            }
            myName = Thread.currentThread().getName();
            System.out.printf("\n%s -- done: %6d", myName, candidateCount);
            return filtered;
        }

        private boolean isItemACandidate(String item){
            contain = item.contains(lcToFind);

            if(relation == 0) {
                return contain;
            }

            if(!contain){
                return false;
            }
            else {
                start_With = false;
                end_With = false;
                words = item.split(" ");
                word_split_size = words.length;
                for (word_i = 0; word_i < word_split_size; word_i++) {
                    start_With = start_With | words[word_i].startsWith(lcToFind);
                    end_With = end_With | words[word_i].endsWith(lcToFind);
                }

                switch(relation) {
                    case 1:
                        candidate = start_With;
                        break;
                    case 2:
                        candidate = end_With;
                        break;
                    case 3:
                        candidate = !start_With && !end_With;
                        break;
                }
                return candidate;
            }
        }
    }

}