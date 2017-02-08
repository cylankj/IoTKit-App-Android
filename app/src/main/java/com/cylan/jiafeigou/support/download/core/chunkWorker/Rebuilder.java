package com.cylan.jiafeigou.support.download.core.chunkWorker;


import android.text.TextUtils;

import com.cylan.jiafeigou.support.download.database.elements.Chunk;
import com.cylan.jiafeigou.support.download.database.elements.Task;
import com.cylan.jiafeigou.support.download.report.listener.FailReason;
import com.cylan.jiafeigou.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Majid Golshadi on 4/15/2014.
 */
public class Rebuilder extends Thread {

    List<Chunk> taskChunks;
    Task task;
    Moderator observer;

    public Rebuilder(Task task, List<Chunk> taskChunks, Moderator moderator) {
        this.taskChunks = taskChunks;
        this.task = task;
        this.observer = moderator;
    }

    @Override
    public void run() {
        // notify to developer------------------------------------------------------------
        if (observer.downloadManagerListener != null)
            observer.downloadManagerListener.OnDownloadRebuildStart(task.id);
        if (TextUtils.isEmpty(task.save_address) || TextUtils.isEmpty(task.name)) {
            if (observer.downloadManagerListener != null)
                observer.downloadManagerListener.onFailed(-1, new FailReason("the downloaded file is missing,task is reInitialized?"));
            return;
        }
        File file = FileUtils.create(task.save_address, task.name + "." + task.extension);

        FileOutputStream finalFile = null;
        try {
            finalFile = new FileOutputStream(file, true);
        } catch (FileNotFoundException e) {
            if (observer.downloadManagerListener != null)
                observer.downloadManagerListener.onFailed(task.id, new FailReason(e.toString()));
            return;
        }

        byte[] readBuffer = new byte[1024];
        int read;
        for (Chunk chunk : taskChunks) {
            FileInputStream chFileIn =
                    FileUtils.getInputStream(task.save_address, String.valueOf(chunk.id));
            try {
                while ((read = chFileIn.read(readBuffer)) > 0) {
                    finalFile.write(readBuffer, 0, read);
                }
            } catch (IOException e) {
                if (observer.downloadManagerListener != null)
                    observer.downloadManagerListener.onFailed(task.id, new FailReason(e.toString()));
            }
            try {
                finalFile.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        observer.reBuildIsDone(task, taskChunks);
    }
}
