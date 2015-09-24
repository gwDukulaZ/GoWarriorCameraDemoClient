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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gowarrior.cloudq.CWSBucket.ICWSBucketAidlInterface;
import com.gowarrior.cloudq.CWSPipe.CWSPipeActionListener;
import com.gowarrior.cloudq.CWSPipe.CWSPipeCallback;
import com.gowarrior.cloudq.CWSPipe.CWSPipeClient;
import com.gowarrior.cloudq.CWSPipe.CWSPipeMessage;

import org.eclipse.paho.client.mqttv3.ICWSDeliveryToken;
import org.eclipse.paho.client.mqttv3.ICWSPipeToken;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/* 
 * Activity where the user can see the history of transfers, go to the downloads
 * page, or upload images/videos.
 *
 * The reason we separate image and videos is for compatibility with Android versions
 * that don't support multiple MIME types. We only allow videos and images because
 * they are nice for demonstration
 */
public class MainActivity extends Activity {

    private static final String TAG = "GoWarriorCameraClient";
    private static final int REFRESH_DELAY = 500;

    private Timer mTimer;
    private LinearLayout mLayout;


    private CWSPipeClient mCWSPipeClient;

    String path;
    private boolean allReady = false;
    private Button showPicsBtn;
    private Button mAlarmButton;
    private Button mSnapshotButton;
    private boolean clientconnected = false;
    private Date lastConnectTime;

    Looper looper = Looper.myLooper();
    MyHandler myHandler = new MyHandler(looper);


    public static CloudTool cloudTool;
    private ICWSBucketAidlInterface myBucket;

    class MyHandler extends Handler {
        public MyHandler() {}
        public MyHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            String msgR = (String)msg.obj;
            Log.v(TAG,"handle msg:"+msgR);
            if (msgR.startsWith("alarm")) {
                if (msgR.contains("done")) {
                    mAlarmButton.setEnabled(true);
                    mAlarmButton.setText(R.string.sendalarm);
                } else {
                    mAlarmButton.setText(msgR);
                }
            } else if (msgR.startsWith("snapshot")) {
                if (msgR.contains("done")) {
                    mSnapshotButton.setEnabled(true);
                    mSnapshotButton.setText(R.string.sendphoto);
                    String[] keys = new String[1];
                    keys[0] = msgR.substring(msgR.indexOf('-')+1);

                    cloudTool.downloadFile(keys[0],Constants.DOWNLOAD_TO);
                } else {
                    mSnapshotButton.setText(msgR);
                }
            } else if (msgR.equals("connect")) {
                boolean ignore = false;
                if (clientconnected) {
                    Date cur = new Date();
                    long connectduration = cur.getTime() - lastConnectTime.getTime();
                    /*
                    if (connectduration <= 3000) {
                        // connect lost within 1s, just ignore it??
                        Log.v(TAG,"ERROR ------------------ now ignore connect lost within 3s, what will happen?");
                        ignore = true;
                    }*/
                    Log.v(TAG,"client mqtt connection lost, duration="+ connectduration);
                }
                if (!ignore) {
                    mAlarmButton.setEnabled(false);
                    mSnapshotButton.setEnabled(false);
                    new clientconnect().execute(clientconnected, myHandler);
                }
            } else if (msgR.equals("connected")) {
                mAlarmButton.setEnabled(true);
                mSnapshotButton.setEnabled(true);
                lastConnectTime = new Date();
                if (!clientconnected) {
                    clientconnected = true;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);



        setContentView(R.layout.activity_main);

        mLayout = (LinearLayout) findViewById(R.id.transfers_layout);

        cloudServiceBind();

        path = Constants.DOWNLOAD_TO;
        checkDir(path);

        showPicsBtn = (Button) findViewById(R.id.showpicture);
        mAlarmButton = (Button) findViewById(R.id.sendalarm);
        mSnapshotButton = (Button) findViewById(R.id.sendphoto);
        mAlarmButton.setEnabled(false);
        mSnapshotButton.setEnabled(false);

        showPicsBtn.setText("View Downloaded Snapshots");

        showPicsBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "The path is " + path);
                folderScan(path);

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivity(intent);
//                startActivityForResult(intent, 1);
            }
        });

        findViewById(R.id.autodownload).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!allReady) {
                    Toast.makeText(getApplicationContext(), "Cloud not ready for download", Toast.LENGTH_SHORT)
                            .show();
                    return;
                } else {
                    findViewById(R.id.autodownload).setEnabled(false);
                    new AutoDownload().execute();
                }
            }
        });

        findViewById(R.id.download).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!allReady) {
                    Toast.makeText(getApplicationContext(), "Cloud not ready for download", Toast.LENGTH_SHORT)
                            .show();
                    return;
                } else {
                    Intent intent = new Intent(MainActivity.this, com.gowarrior.camera.client.DownloadActivity.class);
                    startActivity(intent);
                }
            }
        });

        mAlarmButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlarmButton.setEnabled(false);
                mAlarmButton.setText("alarm sending cmd ...");
                sendAlarm();
                //new testalarm().execute(myHandler);
            }
        });

        mSnapshotButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSnapshotButton.setEnabled(false);
                mSnapshotButton.setText("snapshot sending cmd ...");
                sendPhoto();
                //new testsnapshot().execute(myHandler);
            }
        });

        mTimer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        syncModels();
                    //TODO UI refresh
                    }
                });
            }
        };
        mTimer.schedule(task, 0, REFRESH_DELAY);

        //TODO
        new Thread(new Runnable() {
            @Override
            public void run() {
                createCWSPipe();
            }
        }).start();
    }


    private void checkDir(String path) {
        File Dir = new File(path);
        if (!Dir.exists()) {
            boolean ret = Dir.mkdirs();
            Log.v(TAG, "mkdir " + path + " ret=" + ret);
        }
    }

    private void createCWSPipe() {
        ICWSPipeToken token;
        //TODO
        mCWSPipeClient = new CWSPipeClient(this);

        CWSPipeCallback cb = new CWSPipeCallback() {

            @Override
            public void pipeConnectionLost(Throwable throwable) {
                Log.d(TAG, "connectionLost");
                if (myHandler != null) {
                    Message msg = myHandler.obtainMessage(0, 0, 0, "connect");
                    myHandler.sendMessage(msg);
                    Log.v(TAG,"send msg client connect to mainactivity handle");
                }
            }

            @Override
            public void pipeMessageArrived(String s, byte[] payload) {
                String msgR = new String(payload);
                Log.d(TAG, "messageArrived:"+s+" payload:"+msgR);
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (myHandler != null) {
                    Message msg = myHandler.obtainMessage(0, 0, 0, msgR);
                    myHandler.sendMessage(msg);
                    Log.v(TAG,"send msg to mainactivity handle");
                }
            }

            @Override
            public void pipeDeliveryComplete(ICWSDeliveryToken token) {
                Log.d(TAG, "deliveryComplete, token="+token.toString());
            }


        };
        mCWSPipeClient.setCallback(cb);

        Log.v(TAG, "wait for myHandler ready!");
        while (myHandler == null) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (myHandler != null) {
            Message msg = myHandler.obtainMessage(0, 0, 0, "connect");
            myHandler.sendMessage(msg);
            Log.v(TAG,"send msg client connect to mainactivity handle");
        }
    }

    private void subPipeTopic() {
        mCWSPipeClient.subscribe();
    }

    private void pubPipeTopic(String payload) {
        CWSPipeMessage msg = new CWSPipeMessage(payload.getBytes());
        msg.setQos(2);
        mCWSPipeClient.publish(msg);
    }

    private void closePipe() {
        if (mCWSPipeClient != null) {
            mCWSPipeClient.close();
            mCWSPipeClient = null;
        }
    }

    private void sendAlarm() {
        pubPipeTopic("alarm");
    }

    private void sendPhoto() {
        pubPipeTopic("photo");
    }

    /*
     * When we get a Uri back from the gallery, upload the associated
     * image/video
     */
    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestory");
        cloudServiceUnbind();
        closePipe();
        mLayout.removeAllViews();
        super.onDestroy();
        mTimer.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncModels();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mTimer.purge();
    }

    /* makes sure that we are up to date on the transfers */
    private void syncModels() {
        //TODO
    }




    private class clientconnect extends AsyncTask<Object, Void, Integer> {

        @Override
        protected Integer doInBackground(Object... params) {
            boolean connected = (Boolean)params[0];
            Log.v(TAG, "do remote client connect");
            ICWSPipeToken token;
            Log.v(TAG, "connect start");
            token = mCWSPipeClient.connect();
            try {
                token.waitForCompletion();
                MyHandler myHandler = (MyHandler)params[1];
                Message msg = myHandler.obtainMessage(0, 0, 0, "connected");
                myHandler.sendMessage(msg);
            } catch (MqttException e) {
                e.printStackTrace();
            }
            Log.v(TAG, "connect end");
            //TODO
            subPipeTopic();
            Log.v(TAG, "subPipeTopic end");

            return 0;
        }
    }

    private class testalarm extends AsyncTask<Object, Void, Integer> {

        @Override
        protected Integer doInBackground(Object... params) {
            MyHandler myHandler = (MyHandler)params[0];
            if (myHandler != null) {
                Message msg = myHandler.obtainMessage(0, 0, 0, "alarm posted");
                myHandler.sendMessage(msg);
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                msg = myHandler.obtainMessage(0, 0, 0, "alarm in process");
                myHandler.sendMessage(msg);
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                msg = myHandler.obtainMessage(0, 0, 0, "alarm done");
                myHandler.sendMessage(msg);
            }


            return 0;
        }
    }



    private class AutoDownload extends AsyncTask<Object, Void, Integer> {

        @Override
        protected Integer doInBackground(Object... params) {

            return cloudTool.downloadFile(Constants.DOWNLOAD_TO);
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result == 0) {
                Log.v(TAG, "hint No new files need download");
                Toast.makeText(getApplicationContext(), "No new files need download", Toast.LENGTH_SHORT)
                        .show();
            } else {
                String hint = String.valueOf(result) + " files downloaded";
                Log.v(TAG, "hint " + hint);
                Toast.makeText(getApplicationContext(), hint, Toast.LENGTH_SHORT)
                        .show();
            }
            findViewById(R.id.autodownload).setEnabled(true);
        }
    }











    private void fileScan(String file){
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file)));
    }

    private void folderScan(String path){
        File file = new File(path);
        if(file.exists() && file.isDirectory()){
            File[] array = file.listFiles();
            for(int i=0;i<array.length;i++){
                File f = array[i];
                if(f.isFile()){
                    String name = f.getName();
                    if(name.endsWith(".png") || name.endsWith(".jpg")){
                        fileScan(f.getAbsolutePath());
                    }
                }
                else {
                    folderScan(f.getAbsolutePath());
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean keyHandled = true;
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                keyHandled = false;
                break;
            default:
                keyHandled = false;
                break;
        }
        if (!keyHandled)
            keyHandled = super.onKeyDown(keyCode, event);

        return keyHandled;
    }




    private ServiceConnection bucketSC = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            myBucket = ICWSBucketAidlInterface.Stub.asInterface(service);
            //TODO
            cloudTool = new CloudTool();
            cloudTool.setCloudService(myBucket);

            int i =cloudTool.cloudServiceInit();
            if (i > 0){
                allReady = true;
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
        }
    };

    private void cloudServiceBind(){
        Intent mIntent = new Intent();
        mIntent.setAction(ICWSBucketAidlInterface.class.getName());
        Intent serverIntent = getExplicitIntent(getApplicationContext(), mIntent);
        if(null != serverIntent) {
            Intent intent = new Intent(serverIntent);
            intent.setPackage(getPackageName());
            Log.d(TAG, "bindService");
            bindService(intent, bucketSC, Context.BIND_AUTO_CREATE);
        }else{
            Log.e(TAG, "[bind] Not find cloud service!!");
        }
    }

    private void cloudServiceUnbind(){
        Intent mIntent = new Intent();
        mIntent.setAction(ICWSBucketAidlInterface.class.getName());
        Intent serverIntent = getExplicitIntent(getApplicationContext(), mIntent);
        if(null != serverIntent) {
            Intent intent = new Intent(serverIntent);
            intent.setPackage(getPackageName());
            Log.d(TAG, "unbindService");
            unbindService(bucketSC);
        }else{
            Log.e(TAG, "[unbind] Not find cloud service!!");
        }
    }

    public static Intent getExplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }
        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);
        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);
        // Set the component to be explicit
        explicitIntent.setComponent(component);
        return explicitIntent;
    }
}
