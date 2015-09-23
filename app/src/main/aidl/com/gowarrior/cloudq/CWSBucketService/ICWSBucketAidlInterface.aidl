// ICWSBucketAidlInterface.aidl
package com.gowarrior.cloudq.CWSBucketService;

// Declare any non-default types here with import statements
import com.gowarrior.cloudq.CWSBucketService.ICWSBucketCallback;

interface ICWSBucketAidlInterface {
    /**
     * this API is used to init CWSBucket service. it should be called to get valid handle after
     * startService/bindService and before any other AIDL APIs. CWSBucket service will limit user's
     * files total size, APP can enable service's auto-size-limitation function by setting
     * enableAutoSizeLimit=true, or will get fail return value when do upload if total size exceed
     * and need to do delete. the auto-size-limitation function will delete oldest files to reserve
     * space for upload.
     *
     * @param enableAutoSizeLimit
     *            switch of service auto-size-limitation function.
     * @param callback
     *            the service callback registered by APP to receive notify of upload/download/delete.
     * @param timeout
     *            the timeout of service init. it may take long time since Bucket is in cloud. -1 means
     *            wait untill success. 1000 means 1000ms.
     * @return the handle of service operation of APP, it's the first param of other AIDL APIs.
     *          valid value is >0, -1 means fail, such as service is not ready.
     */
    int CWSBucketInit(boolean enableAutoSizeLimit, in ICWSBucketCallback callback, int timeout);

    /**
     * this API is used to uninit CWSBucket service.
     *
     * @param handle
     *            the handle of service operation of APP.
     * @return the result of service uninit, valid value is 0, -1 means fail, such as service is not ready
     *          or handle not valid, etc.
     */
    int CWSBucketFinish(int handle);

    /**
     * this API is used to upload a file.
     *
     * @param handle
     *            the handle of service operation of APP.
     * @param uri
     *            the uri of need-upload file. Now only support single file upload
     * @param object
     *            the destination file name including extension name in bucket, if null,
     *            service will use the name of uploaded file.
     * @return the id of upload operation, valid value is >0, -1 means fail, such as service is not ready
     *          or uri is not a valid file or other param invalid, etc.
     */
    int CWSBucketUpload(int handle, in Uri uri, String object);

    /**
     * this API is used to download a object(file) in bucket.
     *
     * @param handle
     *            the handle of service operation of APP.
     * @param object
     *            the object(file) that need-download. Now only support single object download
     * @param uri
     *            the destination file uri in local to save the download, it's ok whether exist of not.
     * @return the id of download operation, valid value is >0, -1 means fail, such as service is not ready
     *          or object is not a valid file or other param invalid, etc.
     */
    int CWSBucketDownload(int handle, String object, in Uri uri);

    /**
     * this API is used to delete a object(file) in bucket.
     *
     * @param handle
     *            the handle of service operation of APP.
     * @param object
     *            the object(file) that need-delete. Now only support single object delete
     * @return the id of delete operation, valid value is >0, -1 means fail, such as service is not ready
     *          or object is not a valid file or other param invalid, etc.
     */
    int CWSBucketDelete(int handle, String object);

    /**
     * this API is used to query upload/download/delete operation state. it can be
     * IN_PROGRESS/COMPLETE/FAIL/ERROR, etc.
     *
     * @param handle
     *            the handle of service operation of APP.
     * @param id
     *            the id of upload/download/delete
     * @return the state of upload/download/delete operation. null means fail, such as invalid id.
     */
    String CWSBucketUDloadState(int handle, int id);

    /**
     * this API is used to query the transferred size of upload/download operation.
     *
     * @param handle
     *            the handle of service operation of APP.
     * @param id
     *            the id of upload/download
     * @return the transferred size of upload/download operation. -1 means fail. such as invalid id.
     */
    long CWSBucketUDloadSize(int handle, int id);

    /**
     * this API is used to query the object(file) size in bucket.
     *
     * @param handle
     *            the handle of service operation of APP.
     * @param object
     *            the object(file) in bucket.
     * @return the size of object(file). -1 means fail. such as invalid object.
     */
    long CWSBucketGetFileSize(int handle, String object);

    /**
     * this API is used to query the object(file) list in bucket.
     *
     * @param handle
     *            the handle of service operation of APP.
     * @return the list of object(file). null means fail. such as invalid param.
     */
    List<String> CWSBucketList(int handle);

    /**
     * this API is used to refresh all objects(files) in bucket.
     *
     * @param handle
     *            the handle of service operation of APP.
     * @return the list of object(file). null means fail. such as invalid param.
     */
    boolean CWSBucketRefresh(int handle);

}
