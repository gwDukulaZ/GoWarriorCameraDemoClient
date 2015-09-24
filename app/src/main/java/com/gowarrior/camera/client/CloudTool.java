package com.gowarrior.camera.client;

import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.gowarrior.cloudq.CWSBucket.ICWSBucketAidlInterface;
import com.gowarrior.cloudq.CWSBucket.ICWSBucketCallback;

import java.io.File;
import java.util.List;


public class CloudTool extends ICWSBucketCallback.Stub {
    private static final String TAG = "CloudTool";
    private int mHandle = -1;
    private ICWSBucketAidlInterface mCloudService = null;
    private CloudToolListener progressListener;

    public void setListener(CloudToolListener listener){
        progressListener = listener;
    }

    @Override
    public void onNotify(int handle, int id, String type, String object, String state, long bytesCurrent, long bytesTotal) throws RemoteException {
        if(null != progressListener){
            int percent=0;
            if(0 != bytesTotal) {
                percent = (int) ((bytesCurrent * 100) / bytesTotal);
            }
            progressListener.onProgress(object,state,percent);
        }
    }

    public interface CloudToolListener{
        void onProgress(String filename, String state, int percent);
    }



    public void setCloudService(ICWSBucketAidlInterface service) {
        mCloudService = service;

    }

    public int cloudServiceInit() {
        if (null == mCloudService) {
            return -1;
        }
        if (-1 == mHandle) {
            try {
                Log.i(TAG, "cloudServiceInit [in] ");
                mHandle = mCloudService.CWSBucketInit(false, this, -1);
                Log.i(TAG, "cloudServiceInit [out]");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return mHandle;
    }

    int getHandle() {
        return mHandle;
    }

    public boolean isReady() {
        return ((null != mCloudService) && (-1 != mHandle));
    }

    public List<String> getCloudFileList() {
        List<String> list = null;
        if (isReady()) {
            try {
                list = mCloudService.CWSBucketList(mHandle);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Log.w(TAG, "cloudService not ready");
        }
        return list;
    }

    public boolean syncWithCloud() {
        boolean ret = false;
        if ((null != mCloudService) && (-1 != mHandle)) {
            try {
                ret = mCloudService.CWSBucketRefresh(mHandle);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public int uploadFile(String absolutePath) {
        int id = -1;
        if (null == mCloudService || -1 == mHandle) {
            Log.e(TAG, "cloud service unbind !");
            return -1;
        }
        Uri uri = Uri.fromFile(new File(absolutePath));
        id = uploadFile(uri);
        return id;
    }

    public int uploadFile(String localDir, String fileName) {
        int id = -1;
        if (null == mCloudService || -1 == mHandle) {
            Log.e(TAG, "cloud service unbind !");
            return -1;
        }
        Uri uri = Uri.fromFile(new File(localDir + File.separator + fileName));
        id = uploadFile(uri);
        return id;
    }

    public int uploadFile(Uri uri) {
        int id = -1;
        try {
            Log.d(TAG, "++Upload++ " + uri);
            String key = new String();
            id = mCloudService.CWSBucketUpload(mHandle, uri, key);
            Log.v(TAG, "CWSBucketUpload return id=" + id + " ,key=" + key);
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.e(TAG, "RemoteException !!!");
        }
        return id;
    }


    public void deleteFile(String object) {
        try {
            mCloudService.CWSBucketDelete(mHandle, object);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }









    //-----download file-----
    public int downloadFile(String path) {
        int downloadsize = 0;

        int id;
        String mpath;
        String object;
        //path
        if (null == path)
            //default download path
            mpath = Util.getDownloadPath();
        else
            mpath = path;


        //file
        List<String> list = getCloudFileList();

        for (int i = 0; i < list.size(); i++) {
            object = list.get(i);
            File tmpfile = new File(mpath, object);
            if (tmpfile.exists()) {
                if (tmpfile.length() == getFileSize(object)) {
                    continue;
                } else {
                    id = downloadFile(object, mpath);
                    if (id > -1) {
                        downloadsize++;
                    }
                }

            } else {
                id = downloadFile(object, mpath);
                if (id > -1) {
                    downloadsize++;
                }

            }


        }

        return downloadsize;
    }

    public int downloadFile(String object, String localDir) {
        int id = -1;
        if (null == mCloudService || -1 == mHandle) {
            Log.e(TAG, "cloud service unbind !");
            return -1;
        }
        try {
            Log.d(TAG, "++Download++");
            long size = mCloudService.CWSBucketGetFileSize(mHandle, object);
            Log.d(TAG, "++call CWSBucketDownload++ object=" + object + " size=" + size);
            Uri uri = Uri.fromFile(new File(localDir, object));
            id = mCloudService.CWSBucketDownload(mHandle, object, uri);
            if (id < 0) {
                Log.e(TAG, "Download failed !  " + object);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return id;
    }

    private long getFileSize(String object) {
        long filesize = 0;
        try {
            filesize = mCloudService.CWSBucketGetFileSize(mHandle, object);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return filesize;
    }


}