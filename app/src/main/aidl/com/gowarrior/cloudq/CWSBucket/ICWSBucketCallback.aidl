// ICWSBucketCallback.aidl
package com.gowarrior.cloudq.CWSBucket;

// Declare any non-default types here with import statements

interface ICWSBucketCallback {
    /**
     * this callback is used to register to CWSBucket service as param of AIDL API
     * int CWSBucketInit(boolean enableAutoSizeLimit, in ICWSBucketCallback callback);
     * it will be called by CWSBucket service when upload/download/delete state/progress
     * change. state=COMPLETE means action is done.
     *
     * @param handle
     *            the handle of CWSBucket service user, it's the return value of AIDL
     *            API int CWSBucketInit(boolean enableAutoSizeLimit, in ICWSBucketCallback callback);.
     * @param id
     *            the upload/download/delete operation id, it's the return value of AIDL
     *            API int CWSBucketUpload(int handle, in Uri uri, String object);
     *            int CWSBucketDownload(int handle, String object, in Uri uri);
     *            int CWSBucketDelete(int handle, String filesrc);.
     * @param type
     *            the type of upload/download/delete operation.
     * @param object
     *            the file name of upload/download/delete operation.
     * @param state
     *            the state of upload/download/delete operation.
     * @param bytesCurrent
     *            the transferred size of upload/download operation.
     * @param bytesTotal
     *            the total file size of upload/download operation.
     */
    void onNotify(int handle, int id, String type, String object, String state, long bytesCurrent, long bytesTotal);
}
