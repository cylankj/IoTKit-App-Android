package com.cylan.jiafeigou.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.cylan.jiafeigou.base.MyApp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class BaseAdapter<T> extends android.widget.BaseAdapter {
    /**
     * 绑定的Activity
     */
    protected Activity mActivity;
    /**
     * 上下文
     */
    protected Context mContext;
    /**
     *
     */
    protected LayoutInflater mInflater;
    /**
     * Application
     */
    protected MyApp mApplication;
    /**
     * 绑定的数据源
     */
    private List<T> mObjects = new ArrayList<T>();
    /**
     * Lock used to modify the content of {@link #mObjects}. Any write operation
     * performed on the array should be synchronized on this lock. This lock is
     * also used by the filter (see  to make a synchronized
     * copy of the original array of data.
     */
    private final Object mLock = new Object();
    // A copy of the original mObjects array, initialized from and then used
    // instead as soon as
    // the mFilter ArrayFilter is used. mObjects will then only contain the
    // filtered values.
    private ArrayList<T> mOriginalValues;

    /**
     * Indicates whether or not {@link #notifyDataSetChanged()} must be called
     * whenever {@link #mObjects} is modified.
     */
    public boolean mNotifyOnChange = true;

    public BaseAdapter(Activity activity, List<T> list) {
        this.mActivity = activity;
        this.mContext = activity;
        mInflater = LayoutInflater.from(mContext);
        mApplication = (MyApp) activity.getApplication();
        this.mObjects = list;
    }

    @Override
    public int getCount() {
        return mObjects.size();
    }

    @Override
    public T getItem(int position) {
        return mObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Adds the specified object at the end of the array.
     *
     * @param object The object to add at the end of the array.
     */
    public void add(T object) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.add(object);
            } else {
                mObjects.add(object);
            }
        }
        if (mNotifyOnChange)
            notifyDataSetChanged();
    }

    /**
     * Adds the specified object at the first of the array.
     *
     * @param object The object to add at the end of the array.
     */
    public void addFirst(T object){
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.add(0, object);
            } else {
                mObjects.add(0, object);
            }
        }
        if (mNotifyOnChange)
            notifyDataSetChanged();
    }

    /**
     * Adds the specified Collection at the end of the array.
     *
     * @param collection The Collection to add at the end of the array.
     */
    public void addAll(Collection<? extends T> collection) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.addAll(collection);
            } else {
                mObjects.addAll(collection);
            }
        }
        if (mNotifyOnChange)
            notifyDataSetChanged();
    }

    /**
     * Adds the specified items at the end of the array.
     *
     * @param items The items to add at the end of the array.
     */
    public void addAll(T... items) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                Collections.addAll(mOriginalValues, items);
            } else {
                Collections.addAll(mObjects, items);
            }
        }
        if (mNotifyOnChange)
            notifyDataSetChanged();
    }

    /**
     * Inserts the specified object at the specified index in the array.
     *
     * @param mObjects The object to insert into the array.
     * @param index  The index at which the object must be inserted.
     */
    public void insert(int index,List<T> mObjects) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.addAll(index, mObjects);
            } else {
                mObjects.addAll(index, mObjects);
            }
        }
        if (mNotifyOnChange)
            notifyDataSetChanged();
    }

    /**
     * Removes the specified object from the array.
     *
     * @param object The object to remove.
     */
    public void remove(T object) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.remove(object);
            } else {
                mObjects.remove(object);
            }
        }
        if (mNotifyOnChange)
            notifyDataSetChanged();
    }

    /**
     * Remove all elements from the list.
     */
    public void clear() {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                mOriginalValues.clear();
            } else {
                mObjects.clear();
            }
        }
        if (mNotifyOnChange)
            notifyDataSetChanged();
    }

    /**
     * Sorts the content of this adapter using the specified comparator.
     *
     * @param comparator The comparator used to sort the objects contained in this
     *                   adapter.
     */
    public void sort(Comparator<? super T> comparator) {
        synchronized (mLock) {
            if (mOriginalValues != null) {
                Collections.sort(mOriginalValues, comparator);
            } else {
                Collections.sort(mObjects, comparator);
            }
        }
        if (mNotifyOnChange)
            notifyDataSetChanged();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        mNotifyOnChange = true;
    }

    /**
     * Control whether methods that change the list ({@link #add},
     * {@link #insert}, {@link #remove}, {@link #clear}) automatically call
     * {@link #notifyDataSetChanged}. If set to false, caller must manually call
     * notifyDataSetChanged() to have the changes reflected in the attached
     * view. The default is true, and calling notifyDataSetChanged() resets the
     * flag to true.
     *
     * @param notifyOnChange if true, modifications to the list will automatically call
     *                       {@link #notifyDataSetChanged}
     */
    public void setNotifyOnChange(boolean notifyOnChange) {
        mNotifyOnChange = notifyOnChange;
    }

    /**
     * Returns the position of the specified item in the array.
     *
     * @param item The item to retrieve the position of.
     * @return The position of the specified item.
     */
    public int getPosition(T item) {
        return mObjects.indexOf(item);
    }

    /**
     * @param text
     */
    protected void showText(String text) {
        Toast.makeText(mActivity, text, Toast.LENGTH_SHORT).show();
    }

    public List<T> getData() {
        return mObjects;
    }

}
