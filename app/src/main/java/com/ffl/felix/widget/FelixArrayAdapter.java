package com.ffl.felix.widget;

import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by PengfeiLin on 2018/1/22.
 */

public class FelixArrayAdapter extends BaseAdapter implements Filterable {
    private final static String TAG = "FelixArrayAdapter";
    private final Object mLock = new Object();
    private ArrayList<String> mOriginalValues;//All the items
    private List<String> mObjects;//Filtered items
    protected ArrayFilter mFilter;
    private final LayoutInflater mInflater;
    private SimilarStringFilter felixFilter;

    /**
     * The resource indicating what views to inflate to display the content of this
     * array adapter.
     */
    private final int mResource;

    public FelixArrayAdapter(Context context, int resource) {
        mInflater = LayoutInflater.from(context);
        mResource = resource;
        mOriginalValues = new ArrayList<>();
        mObjects = new ArrayList<>();
        felixFilter = new SimilarStringFilter();
    }

    public void updateOriginalValues(String[] values, String[] lcValues) {
        synchronized (mLock) {
            mOriginalValues.clear();
            //mOriginalValues.addAll(Arrays.asList(values));
            felixFilter.updateItems(values, lcValues);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(mInflater, position, convertView, parent, mResource);
    }

    private View createViewFromResource(LayoutInflater inflater, int position,
                                        View convertView, ViewGroup parent, int resource) {
        final View view;
        final TextView text;
        if (convertView == null) {
            view = inflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }
        try {
            text = (TextView) view;
        } catch (ClassCastException e) {
            Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
            throw new IllegalStateException(
                    "ArrayAdapter requires the resource ID to be a TextView", e);
        }
        final String item = getItem(position);
        text.setText(item);
        return view;
    }

    @Override
    public String getItem(int position) {
        return mObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return mObjects.size();
    }

    public void setFilterType(int filterType) {
        felixFilter.setFiltertype(filterType);
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    protected void finalize() throws java.lang.Throwable {
        felixFilter.shutdown();
        Log.d(TAG, "SimilarStringFilter shutdown...");
        super.finalize();
    }

    private class ArrayFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            final FilterResults results = new FilterResults();
            if (prefix == null || prefix.length() == 0) {
                final ArrayList<String> list;
                synchronized (mLock) {
                    list = new ArrayList<>(mOriginalValues);
                }
                results.values = list;
                results.count = list.size();
            } else {
                ArrayList<String> newValues = felixFilter.searchSimilarString(prefix.toString());
                results.values = newValues;
                results.count = newValues.size();
/*                System.out.printf("\n**********************************");
                for(int i = 0; i < results.count; i++) {
                    System.out.printf("\n%s", newValues.get(i));
                }
*/            }
            return results;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //noinspection unchecked
            mObjects = (List<String>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
