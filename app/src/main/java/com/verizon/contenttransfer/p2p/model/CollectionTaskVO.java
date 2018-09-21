package com.verizon.contenttransfer.p2p.model;

import android.os.AsyncTask;

import com.verizon.contenttransfer.utils.LogUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by rahiahm on 4/18/2017.
 */
public class CollectionTaskVO {

    private static final String TAG = CollectionTaskVO.class.getName();

    private static CollectionTaskVO instance;
    private Map<String, AsyncTask> collectionTasks = new HashMap<String, AsyncTask>();

    public static CollectionTaskVO getInstance() {
        if (instance == null) {
            instance = new CollectionTaskVO();
        }
        return instance;
    }
    public void reset(){
        cancelTasks();
        instance = null;
    }
    public void registerTask(String label, AsyncTask task){
        if(label != null && task!=null) {
            collectionTasks.put(label, task);
            LogUtil.d(TAG, "New task added :"+label);
        }
        else {LogUtil.e(TAG, "Attempted to add a null value");}
    }
    public boolean isCollectionFinished(List<String> key){

        boolean done = true;
        for (int i = 0; i < key.size(); i++) {
            AsyncTask task = collectionTasks.get(key.get(i));
            LogUtil.d(TAG,"Checking finish status for task ["+i+"] of "+collectionTasks.size());
            if(task.getStatus()  == AsyncTask.Status.RUNNING){
                done = false;
            }
        }
        return done ;
    }
    public boolean isCollectionFinished(String[] keys){

        boolean done = true;
        for(String key:keys)
        {
            AsyncTask task = collectionTasks.get(key);
            if(task.getStatus()  == AsyncTask.Status.RUNNING){
                done = false;
            }
        }
        return done ;
    }
    public boolean isCollectionFinished(String key){

        boolean done = true;
        AsyncTask task = collectionTasks.get(key);
        if(task != null && task.getStatus() == AsyncTask.Status.RUNNING){
            done = false;
        }

        return done ;
    }
    public void cancelNonSelectedTask(List<String> keys){
        if(collectionTasks != null){
            Iterator<Map.Entry<String, AsyncTask>> iterator = collectionTasks.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, AsyncTask> entry = (Map.Entry<String, AsyncTask>) iterator.next();
                if(!keys.contains(entry.getKey())){
                    cancelTasks(entry.getKey());
                }
            }
        }
    }

    public boolean isAllCollectionFinished(){

        if(collectionTasks != null){
            Iterator<Map.Entry<String, AsyncTask>> iterator = collectionTasks.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, AsyncTask> entry = (Map.Entry<String, AsyncTask>) iterator.next();
                if(!isCollectionFinished(entry.getKey())){
                    return false;
                }
            }
        }
        return true ;
    }
    public void cancelTasks(String [] keys){
        for(String key:keys)
        {
            AsyncTask task = collectionTasks.get(key);
            if(task.getStatus()  == AsyncTask.Status.RUNNING){
                LogUtil.e(TAG, "Cancelling " + key + " collection");
                task.cancel(true);
            }
        }
    }

    public void cancelTasks(String key){

        AsyncTask task = collectionTasks.get(key);
        if(task.getStatus()  == AsyncTask.Status.RUNNING){
            LogUtil.e(TAG, "Cancelling " + key + " collection");
            task.cancel(true);
        }

    }
    public boolean cancelTasks(){
        if(collectionTasks != null){
            Iterator<Map.Entry<String, AsyncTask>> iterator = collectionTasks.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, AsyncTask> entry = (Map.Entry<String, AsyncTask>) iterator.next();
                cancelTasks(entry.getKey());
            }
            collectionTasks.clear();
        }
        return true ;
    }
}
