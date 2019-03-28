package cc.brainbook.android.multithreaddownload.handler;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import cc.brainbook.android.multithreaddownload.DownloadTask;
import cc.brainbook.android.multithreaddownload.bean.FileInfo;
import cc.brainbook.android.multithreaddownload.db.ThreadDAO;
import cc.brainbook.android.multithreaddownload.exception.DownloadException;
import cc.brainbook.android.multithreaddownload.interfaces.DownloadEvent;
import cc.brainbook.android.multithreaddownload.interfaces.OnProgressListener;

import static cc.brainbook.android.multithreaddownload.BuildConfig.DEBUG;

public class DownloadHandler extends Handler {
    private static final String TAG = "TAG";

    public static final int MSG_ERROR = -1;
    public static final int MSG_INIT = 0;
    public static final int MSG_START = 1;
    public static final int MSG_PAUSE = 2;
    public static final int MSG_STOP = 3;
    public static final int MSG_COMPLETE = 4;
    public static final int MSG_PROGRESS = 5;

    private DownloadTask mDownloadTask;
    private FileInfo mFileInfo;
    private DownloadEvent mDownloadEvent;
    private OnProgressListener mOnProgressListener;
    private ThreadDAO mThreadDAO;

    public DownloadHandler(DownloadTask downloadTask,
                           FileInfo fileInfo,
                           DownloadEvent downloadEvent,
                           OnProgressListener onProgressListener,
                           ThreadDAO threadDAO
    ) {
        this.mDownloadTask = downloadTask;
        this.mFileInfo = fileInfo;
        this.mDownloadEvent = downloadEvent;
        this.mOnProgressListener = onProgressListener;
        this.mThreadDAO = threadDAO;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_INIT:
                if (DEBUG) Log.d(TAG, "DownloadHandler# handleMessage(): msg.what = MSG_INIT");

                ///下载事件接口DownloadEvent
                if (mDownloadEvent != null) {
                    mDownloadEvent.onInit(mFileInfo, mDownloadTask.mThreadInfos);
                }

                break;
            case MSG_START:
                if (DEBUG) Log.d(TAG, "DownloadHandler# handleMessage(): msg.what = MSG_START");

                ///下载事件接口DownloadEvent
                if (mDownloadEvent != null) {
                    mDownloadEvent.onStart(mFileInfo, mDownloadTask.mThreadInfos);
                }

                break;
            case MSG_PAUSE:
                if (DEBUG) Log.d(TAG, "DownloadHandler# handleMessage(): msg.what = MSG_PAUSE");

                ///下载事件接口DownloadEvent
                if (mDownloadEvent != null) {
                    mDownloadEvent.onPause(mFileInfo, mDownloadTask.mThreadInfos);
                }

                break;
            case MSG_STOP:
                if (DEBUG) Log.d(TAG, "DownloadHandler# handleMessage(): msg.what = MSG_STOP");

                ///删除数据库中文件的所有线程信息
                if (DEBUG) Log.d(TAG, "DownloadHandler# handleMessage(): msg.what = MSG_STOP: 删除线程信息");
                mThreadDAO.deleteAllThread(mFileInfo.getFileUrl(), mFileInfo.getFileName(), mFileInfo.getFileSize(), mFileInfo.getSavePath());

                ///下载停止回调接口DownloadCallback
                if (mDownloadEvent != null) {
                    mDownloadEvent.onStop(mFileInfo, mDownloadTask.mThreadInfos);
                }

                break;
            case MSG_COMPLETE:
                if (DEBUG) Log.d(TAG, "DownloadHandler# handleMessage(): msg.what = MSG_COMPLETE");

                ///下载完成回调接口DownloadEvent
                if (mDownloadEvent != null) {
                    mDownloadEvent.onComplete(mFileInfo, mDownloadTask.mThreadInfos);
                }

                break;
            case MSG_PROGRESS:
                if (DEBUG) Log.d(TAG, "DownloadHandler# handleMessage(): msg.what = MSG_PROGRESS");

                ///下载进度回调接口DownloadEvent
                if (mOnProgressListener != null) {
                    if (mFileInfo.getStatus() != FileInfo.FILE_STATUS_START) {
                        ///下载文件状态为运行时需要修正进度更新显示的下载速度为0
                        mOnProgressListener.onProgress(mFileInfo, mDownloadTask.mThreadInfos,0, 0);
                    } else {
                        long diffTimeMillis = ((long[]) msg.obj)[0];
                        long diffFinishedBytes = ((long[]) msg.obj)[1];
                        mOnProgressListener.onProgress(mFileInfo, mDownloadTask.mThreadInfos, diffTimeMillis, diffFinishedBytes);
                    }
                }

                break;
            case MSG_ERROR:
                ///更新下载文件状态：下载错误
                if (DEBUG) Log.d(TAG, "更新下载文件状态：mFileInfo.setStatus(FileInfo.FILE_STATUS_ERROR)");
                mFileInfo.setStatus(FileInfo.FILE_STATUS_ERROR);

                ///下载错误回调接口DownloadEvent
                if (mDownloadEvent != null) {
                    mDownloadEvent.onError(mFileInfo, mDownloadTask.mThreadInfos, (Exception) msg.obj);
                }

                break;
        }
        super.handleMessage(msg);
    }

}
