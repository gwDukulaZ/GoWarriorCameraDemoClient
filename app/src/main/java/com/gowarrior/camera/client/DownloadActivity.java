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
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Activity where user can see the items in the S3 bucket and download stuff
 * from there
 */
public class DownloadActivity extends Activity {
    private static final String TAG = "GoWarriorCameraClient";
    private ListView mList;
    private ObjectAdapter mAdapter;

    // keeps track of the objects the user has selected
    private HashSet<String> mSelectedObjects =
            new HashSet<String>();

    private Button mRefreshButton;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.gowarrior.camera.client.R.layout.activity_download);

        mList = (ListView) findViewById(com.gowarrior.camera.client.R.id.list);

        mAdapter = new ObjectAdapter(this);
        mList.setOnItemClickListener(new ItemClickListener());
        mList.setAdapter(mAdapter);

        mRefreshButton = (Button) findViewById(com.gowarrior.camera.client.R.id.refresh);

        findViewById(com.gowarrior.camera.client.R.id.download).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // download all the objects that were selected
                for (String obj : mSelectedObjects) {
                    MainActivity.cloudTool.downloadFile(obj, Util.getDownloadPath());
                    Log.d(TAG, "the need download file [" + obj + "] size is " + MainActivity.cloudTool.getFileSize(obj));
                }
                finish();
            }
        });

        findViewById(com.gowarrior.camera.client.R.id.refresh).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new RefreshTask().execute();
            }
        });

        new RefreshTask().execute();
    }

    private class RefreshTask extends AsyncTask<Void, Void, List<String>> {
        @Override
        protected void onPreExecute() {
            mRefreshButton.setEnabled(false);
            mRefreshButton.setText(com.gowarrior.camera.client.R.string.refreshing);
            MainActivity.cloudTool.syncWithCloud();
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            // get all the objects in bucket
            return MainActivity.cloudTool.getCloudFileList();
        }

        @Override
        protected void onPostExecute(List<String> objects) {
            // now that we have all the keys, add them all to the adapter
            mAdapter.clear();
            mAdapter.addAll(objects);
            mSelectedObjects.clear();
            mRefreshButton.setEnabled(true);
            mRefreshButton.setText(com.gowarrior.camera.client.R.string.refresh);
        }
    }

    /**
     * This lets the user click on anywhere in the row instead of just the
     * checkbox to select the files to download
     */
    private class ItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int pos,
                long id) {
            String item = mAdapter.getItem(pos);
            boolean checked = false;
            // try removing, if it wasn't there add
            if (!mSelectedObjects.remove(item)) {
                mSelectedObjects.add(item);
                checked = true;
            }
            ((ObjectAdapter.ViewHolder) view.getTag()).checkbox.setChecked(
                    checked);
        }
    }

    /** Adapter for all file objects */
    private class ObjectAdapter extends ArrayAdapter<String> {
        public ObjectAdapter(Context context) {
            super(context, com.gowarrior.camera.client.R.layout.bucket_row);
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(
                        com.gowarrior.camera.client.R.layout.bucket_row, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String summary = getItem(pos);
            holder.checkbox.setChecked(mSelectedObjects.contains(summary));
            holder.key.setText(summary);
            holder.size.setText(String.valueOf(MainActivity.cloudTool.getFileSize(summary)));
            return convertView;
        }

        public void addAll(Collection<? extends String> collection) {
            for (String obj : collection) {
                add(obj);
            }
        }

        private class ViewHolder {
            private CheckBox checkbox;
            private TextView key;
            private TextView size;

            private ViewHolder(View view) {
                checkbox = (CheckBox) view.findViewById(com.gowarrior.camera.client.R.id.checkbox);
                key = (TextView) view.findViewById(com.gowarrior.camera.client.R.id.key);
                size = (TextView) view.findViewById(com.gowarrior.camera.client.R.id.size);
            }
        }
    }
}
