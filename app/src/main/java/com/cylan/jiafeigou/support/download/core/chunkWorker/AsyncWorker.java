package com.cylan.jiafeigou.support.download.core.chunkWorker;


import com.cylan.jiafeigou.support.download.database.elements.Chunk;
import com.cylan.jiafeigou.support.download.database.elements.Task;
import com.cylan.jiafeigou.support.download.report.listener.FailReason;
import com.cylan.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;


/**
 * Created by Majid Golshadi on 4/14/2014.
 */
public class AsyncWorker extends Thread {

    private final static int BUFFER_SIZE = 1024;

    private final Task task;
    private final Chunk chunk;
    private final Moderator observer;
    private byte[] buffer;
    private ConnectionWatchDog watchDog;

    public boolean stop = false;

    volatile boolean interrupt = false;

    public AsyncWorker(Task task, Chunk chunk, Moderator moderator) {
        buffer = new byte[BUFFER_SIZE];
        this.task = task;
        this.chunk = chunk;
        this.observer = moderator;
    }

    @Override
    public void interrupt() {
        super.interrupt();
        interrupt = true;
    }

    @Override
    public void run() {
        try {
            URL url = new URL(task.url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(60 * 1000);
            connection.setReadTimeout(60 * 1000);
            System.setProperty("http.keepAlive", "false");
            if (chunk.end != 0) // support unresumable links
                connection.setRequestProperty("Range", "bytes=" + chunk.begin + "-" + chunk.end);
            connection.connect();
            File cf = new File(FileUtils.address(task.save_address, String.valueOf(chunk.id)));
            InputStream remoteFileIn = connection.getInputStream();
            FileOutputStream chunkFile = new FileOutputStream(cf, true);

            int len = 0;
            // set watchDoger to stop thread after 1sec if no connection lost
            watchDog = new ConnectionWatchDog(60 * 1000, this);
            watchDog.start();
            while (!interrupt &&
                    (len = remoteFileIn.read(buffer)) > 0) {
                watchDog.reset();
                chunkFile.write(buffer, 0, len);
                process(len);
            }

            chunkFile.flush();
            chunkFile.close();
            watchDog.interrupt();
            connection.disconnect();

            if (!interrupt) {
                observer.rebuild(chunk);
            }
        } catch (SocketTimeoutException e) {
            observer.onFailed(task.id, new FailReason(e.toString()));
            pauseRelatedTask();
        } catch (IOException e) {
            observer.onFailed(task.id, new FailReason(e.toString()));
        }
    }

    private void process(int read) {
        observer.process(chunk.task_id, read);
    }

    private void pauseRelatedTask() {
        observer.pause(task.id);
    }

    private boolean flag = true;

    public void connectionTimeOut() {
        if (flag) {
            watchDog.interrupt();
            flag = false;
            observer.onFailed(task.id, new FailReason("connectionTimeOut"));
            pauseRelatedTask();
        }

    }

}
