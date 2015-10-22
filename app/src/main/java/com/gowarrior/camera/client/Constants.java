/*
 * Copyright 2010-2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.gowarrior.camera.client;

import android.os.Environment;

public class Constants {
    public static final String WAITUPLOAD = "upload";
    public static final String WAITDOWNLOAD = "download";
    public static final String UPLOADING = "uploading";
    public static final String DOWNLOADING = "downloading";
    public static final String WAITDELETE = "delete";
    public static final String COMPLETE = "COMPLETE";
    public static final String DOWNLOAD_TO = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/snapshot";

}