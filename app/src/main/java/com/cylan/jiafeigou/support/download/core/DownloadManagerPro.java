package com.cylan.jiafeigou.support.download.core;


import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.support.download.core.chunkWorker.Moderator;
import com.cylan.jiafeigou.support.download.core.enums.QueueSort;
import com.cylan.jiafeigou.support.download.core.enums.TaskStates;
import com.cylan.jiafeigou.support.download.core.worker.AsyncStartDownload;
import com.cylan.jiafeigou.support.download.core.worker.QueueModerator;
import com.cylan.jiafeigou.support.download.database.ChunksDataSource;
import com.cylan.jiafeigou.support.download.database.DatabaseHelper;
import com.cylan.jiafeigou.support.download.database.TasksDataSource;
import com.cylan.jiafeigou.support.download.database.elements.Chunk;
import com.cylan.jiafeigou.support.download.database.elements.Task;
import com.cylan.jiafeigou.support.download.net.NetConfig;
import com.cylan.jiafeigou.support.download.report.ReportStructure;
import com.cylan.jiafeigou.support.download.report.exceptions.QueueDownloadInProgressException;
import com.cylan.jiafeigou.support.download.report.exceptions.QueueDownloadNotStartedException;
import com.cylan.jiafeigou.support.download.report.listener.DownloadManagerListener;
import com.cylan.jiafeigou.support.download.report.listener.DownloadManagerListenerModerator;
import com.cylan.jiafeigou.support.download.report.listener.FailReason;
import com.cylan.jiafeigou.support.download.utils.L;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Majid Golshadi on 4/10/2014.
 */
public class DownloadManagerPro {

    private static final int MAX_CHUNKS = 16;

    static String SAVE_FILE_FOLDER = null;

    private Moderator moderator;

    private DatabaseHelper dbHelper;

    private TasksDataSource tasksDataSource;

    private ChunksDataSource chunksDataSource;

    private QueueModerator qt;

    private TaskBuilder taskBuilder;

    private NetConfig.Builder netConfigBuilder;
    //    private Config config;
    private DownloadManagerListenerModerator downloadManagerListener;

    private static DownloadManagerPro downloadManagerPro;

    public static DownloadManagerPro getInstance() {
        if (downloadManagerPro == null) {
            synchronized (DownloadManagerPro.class) {
                if (downloadManagerPro == null)
                    downloadManagerPro = new DownloadManagerPro();
            }
        }
        return downloadManagerPro;
    }

//    public void init(Config config) {
//        this.config = config;
//
//    }

    public int initTask(TaskBuilder taskBuilder) throws JfgException {
        this.taskBuilder = taskBuilder;
//        if (config == null)
//            throw new JfgException("config is null,you may be forget to initialize");
        if (taskBuilder == null)
            throw new JfgException("taskBuilder==null or context==null");
        if (TextUtils.isEmpty(taskBuilder.url))
            throw new JfgException("url==null");
        if (TextUtils.isEmpty(taskBuilder.saveName))
            throw new JfgException("saveName==null");
        setMaxChunk(taskBuilder.maxChunks);
        initFolder();
        return initTask();
    }

    private void initDataBase() {
//        if (config == null)
//            throw new IllegalStateException("config is null,you may be forget to initialize");
        dbHelper = new DatabaseHelper(ContextUtils.getContext());
        // ready database data source to access tables
        tasksDataSource = new TasksDataSource();
        tasksDataSource.openDatabase(dbHelper);
        chunksDataSource = new ChunksDataSource();
        chunksDataSource.openDatabase(dbHelper);
        // moderate chunks to download one task
        moderator = new Moderator(tasksDataSource, chunksDataSource);
    }

    private void initFolder() {
        File saveFolder = new File(taskBuilder.sdCardFolderAddress);
        if (!saveFolder.exists())
            saveFolder.mkdirs();
        SAVE_FILE_FOLDER = saveFolder.getAbsolutePath();
        downloadManagerListener = new DownloadManagerListenerModerator(taskBuilder.downloadManagerListener);
    }

    private int initTask() {
        String saveName = taskBuilder.saveName;
        int chunk = taskBuilder.maxChunks;
        if (!taskBuilder.overwrite)
            saveName = getUniqueName(saveName);
        else
            deleteSameDownloadNameTask(saveName);
        L.d("overwrite");
        chunk = setMaxChunk(chunk);
        L.d("ma chunk");
        return insertNewTask(saveName, taskBuilder.url, chunk, taskBuilder.sdCardFolderAddress + File.separator + taskBuilder.saveName, taskBuilder.priority, taskBuilder.desc);
    }

//    public static class Config {
//        private Context context;
//
//        public Config setContext(Context context) {
//            this.context = context;
//            return this;
//        }
//    }

    public static class TaskBuilder {
        private int maxChunks;
        boolean overwrite = false;
        boolean priority = false;
        private int allowNetType;
        private String url;
        private String saveName;
        private String sdCardFolderAddress;
        private String desc;
        private DownloadManagerListener downloadManagerListener;

        public TaskBuilder setAllowNetType(int allowNetType) {
            this.allowNetType = allowNetType;
            return this;
        }

        public TaskBuilder setDesc(String desc) {
            this.desc = desc;
            return this;
        }


        public TaskBuilder setSaveName(String saveName) {
            this.saveName = saveName;
            return this;
        }

        public TaskBuilder setDownloadManagerListener(DownloadManagerListener downloadManagerListener) {
            this.downloadManagerListener = downloadManagerListener;
            return this;
        }

        public TaskBuilder setMaxChunks(int maxChunks) {
            this.maxChunks = maxChunks;
            return this;
        }

        public TaskBuilder setOverwrite(boolean overwrite) {
            this.overwrite = overwrite;
            return this;
        }

        public TaskBuilder setPriority(boolean priority) {
            this.priority = priority;
            return this;
        }

        public TaskBuilder setSdCardFolderAddress(String sdCardFolderAddress) {
            this.sdCardFolderAddress = sdCardFolderAddress;
            return this;
        }

        public TaskBuilder setUrl(String url) {
            this.url = url;
            return this;
        }

        @Override
        public String toString() {
            return "TaskBuilder{" +
                    "maxChunks=" + maxChunks +
                    ", overwrite=" + overwrite +
                    ", priority=" + priority +
                    ", allowNetType=" + allowNetType +
                    ", url='" + url + '\'' +
                    ", saveName='" + saveName + '\'' +
                    ", sdCardFolderAddress='" + sdCardFolderAddress + '\'' +
                    '}';
        }
    }

    /**
     * <p>
     * Download manager pro Object constructor
     * </p>
     */
    private DownloadManagerPro() {
        initDataBase();
    }

    /**
     * <p>
     * first of all check task state and depend on initSubscription download process from where ever need
     * </p>
     *
     * @param token now token is download task msgId
     * @throws IOException
     */
    public void startDownload(int token) {
        Task task = null;
        try {
            // switch on task state
            task = tasksDataSource.getTaskInfo(token);
            L.d("task state : " + task.toJsonObject().toString());
            if (task.state == TaskStates.END) {
                if (downloadManagerListener != null)
                    downloadManagerListener.OnDownloadCompleted(task.id);
                return;
            }
            Thread asyncStartDownload
                    = new AsyncStartDownload(tasksDataSource, chunksDataSource, moderator, downloadManagerListener, task);
            L.d("define async download: " + task.state);
            asyncStartDownload.start();
            L.d("define async download started");
        } catch (Exception e) {
            if (downloadManagerListener != null)
                downloadManagerListener.onFailed(task == null ? -1 : task.id, new FailReason(e.toString()));
        }
    }


    /**
     * @param downloadTaskPerTime
     */
    public void startQueueDownload(int downloadTaskPerTime, int priority)
            throws QueueDownloadInProgressException {

        Moderator localModerator = new Moderator(tasksDataSource, chunksDataSource);
        List<Task> unCompletedTasks = tasksDataSource.getUnCompletedTasks(priority);

        if (qt == null) {
            qt = new QueueModerator(tasksDataSource, chunksDataSource,
                    localModerator, downloadManagerListener, unCompletedTasks, downloadTaskPerTime);
            qt.startQueue();

        } else {
            throw new QueueDownloadInProgressException();
        }
    }


    /**
     * <p>
     * pause separate download task
     * </p>
     *
     * @param token
     */
    public void pauseDownload(int token) {
        moderator.pause(token);
    }

    /**
     * pause queue download
     *
     * @throws QueueDownloadNotStartedException
     */
    public void pauseQueueDownload()
            throws QueueDownloadNotStartedException {

        if (qt != null) {
            qt.pause();
            qt = null;
        } else {
            throw new QueueDownloadNotStartedException();
        }
    }


    //-----------Reports

    /**
     * report task download status in "ReportStructure" style
     *
     * @param token when you add a new download task it's return to you
     * @return
     */
    public ReportStructure singleDownloadStatus(int token) {
        ReportStructure report = new ReportStructure();
        Task task = tasksDataSource.getTaskInfo(token);
        if (task != null) {
            List<Chunk> taskChunks = chunksDataSource.chunksRelatedTask(task.id);
            report.setObjectValues(task, taskChunks);

            return report;
        }

        return null;
    }


    /**
     * <p>
     * it's an report method for
     * return list of download task in same state that developer want as ReportStructure List object
     * </p>
     *
     * @param state 0. get all downloads Status
     *              1. init
     *              2. ready
     *              3. downloading
     *              4. paused
     *              5. download finished
     *              6. end
     * @return
     */
    public List<ReportStructure> downloadTasksInSameState(int state) {
        List<ReportStructure> reportList;
        List<Task> inStateTasks = tasksDataSource.getTasksInState(state);

        reportList = readyTaskList(inStateTasks);

        return reportList;
    }


    /**
     * return list of last completed Download tasks in "ReportStructure" style
     * you can use it as notifier
     *
     * @return
     */
    public List<ReportStructure> lastCompletedDownloads() {
        List<Task> lastCompleted = tasksDataSource.getUnNotifiedCompleted();
        return readyTaskList(lastCompleted);
    }


    private List<ReportStructure> readyTaskList(List<Task> tasks) {
        List<ReportStructure> reportList = new ArrayList<ReportStructure>();

        for (Task task : tasks) {
            List<Chunk> taskChunks = chunksDataSource.chunksRelatedTask(task.id);
            ReportStructure singleReport = new ReportStructure();
            singleReport.setObjectValues(task, taskChunks);
            reportList.add(singleReport);
        }

        return reportList;
    }


    /**
     * <p>
     * check all notified tasks
     * so in another "lastCompletedDownloads" call ,completed task does not show again
     * <p/>
     * persian:
     * "lastCompletedDownloads" list akharin task haii ke takmil shodeand ra namayesh midahad
     * ba seda zadan in method tamami task haii ke dar gozaresh e ghabli elam shode boodand ra
     * az liste "lastCompeletedDownloads" hazf mikonad
     * <p/>
     * !!!SHIT!!!
     * </p>
     *
     * @return true or false
     */
    public boolean notifiedTaskChecked() {
        return tasksDataSource.checkUnNotifiedTasks();
    }


    /**
     * delete download task from db and if you set deleteTaskFile as true
     * it's go to saved folder and delete that file
     *
     * @param token          when you add a new download task it's return to you
     * @param deleteTaskFile delete completed download file from sd card if you set it true
     * @return "true" if anything goes right
     * "false" if something goes wrong
     */
    public boolean delete(int token, boolean deleteTaskFile) {
        Task task = tasksDataSource.getTaskInfo(token);
        if (task.url != null) {
            List<Chunk> taskChunks =
                    chunksDataSource.chunksRelatedTask(task.id);
            for (Chunk chunk : taskChunks) {
                FileUtils.delete(task.save_address, String.valueOf(chunk.id));
                chunksDataSource.delete(chunk.id);
            }

            if (deleteTaskFile) {
                long size = FileUtils.size(task.save_address, task.name + "." + task.extension);
                if (size > 0) {
                    FileUtils.delete(task.save_address, task.name + "." + task.extension);
                }
            }

            return tasksDataSource.delete(task.id);
        }

        return false;
    }

    /**
     * 清空下载
     *
     * @param fileName
     */
    public void deleteTask(String fileName) {
        Task task = tasksDataSource.getTaskInfoWithName(fileName);
        if (task != null && task.id >= 0) {
            pauseDownload(task.id);
            //
            delete(task.id, true);
        }
    }

    /**
     * close db connection
     * if your activity goes to paused or stop state
     * you have to call this method to disconnect from db
     */
    public void dispose() {
        dbHelper.close();
    }


    private List<Task> unCompleted() {
        return tasksDataSource.getUnCompletedTasks(QueueSort.oldestFirst);
    }

    private int insertNewTask(String taskName, String url, int chunk, String save_address, boolean priority, String desc) {
        Task task = new Task(0, taskName, url, TaskStates.INIT, chunk, save_address, priority, desc);
        task.id = (int) tasksDataSource.insertTask(task);
        L.d("task msgId " + String.valueOf(task.id));
        return task.id;
    }


    private int setMaxChunk(int chunk) {

        if (chunk < MAX_CHUNKS)
            return chunk;

        return MAX_CHUNKS;
    }

    private String getUniqueName(String name) {
        String uniqueName = name;
        int count = 0;

        while (isDuplicatedName(uniqueName)) {
            uniqueName = name + "_" + count;
            count++;
        }

        return uniqueName;
    }

    private boolean isDuplicatedName(String name) {
        return tasksDataSource.containsTask(name);
    }


    /*
        valid values are
            INIT          = 0;
            READY         = 1;
            DOWNLOADING   = 2;
            PAUSED        = 3;
            DOWNLOAD_FINISHED      = 4;
            END           = 5;
        so if his token was wrong return -1
     */
    private void deleteSameDownloadNameTask(String saveName) {
        if (tasksDataSource.containsTask(saveName)) {
            Task task = tasksDataSource.getTaskInfoWithName(saveName);
            if (task != null) {
                pauseDownload(task.id);
                tasksDataSource.delete(task.id);
                FileUtils.delete(task.save_address, task.name + "." + task.extension);
            }
        }
    }

    public boolean getDownloadState(String fileName) {
        Task task = tasksDataSource.getTaskInfoWithName(fileName);
        if (task != null) {
            //最好的方式是检查当前连接中的任务.
            boolean isTask = moderator.isDownloading(task.id);
            //10s之内更都算
            if (isTask && System.currentTimeMillis() - task.lastUpdateTime <= 10 * 1000L) {
                return true;
            }
        }
        return false;
    }
}
