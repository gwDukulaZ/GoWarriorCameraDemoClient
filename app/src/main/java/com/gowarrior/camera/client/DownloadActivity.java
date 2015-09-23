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
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

/*
 * Activity where user can see the items in the S3 bucket and download stuff
 * from there
 */
public class DownloadActivity extends Activity {
    private static final String TAG = "GoWarriorCameraClient";
    private ListView mList;

    //private ObjectAdapter mAdapter;
    // keeps track of the objects the user has selected

    private Button mRefreshButton;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.gowarrior.camera.client.R.layout.activity_download);



        mList = (ListView) findViewById(com.gowarrior.camera.client.R.id.list);

//        mAdapter = new ObjectAdapter(this);
//        mList.setOnItemClickListener(new ItemClickListener());
//        mList.setAdapter(mAdapter);

        mRefreshButton = (Button) findViewById(com.gowarrior.camera.client.R.id.refresh);

        findViewById(com.gowarrior.camera.client.R.id.download).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // download all the objects that were selected

                finish();
            }
        });

//        findViewById(com.gowarrior.camera.client.R.id.refresh).setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                new RefreshTask().execute();
//            }
//        });
//
//        new RefreshTask().execute();
    }

//    private class RefreshTask extends AsyncTask<Void, Void, List<S3ObjectSummary>> {
//        @Override
//        protected void onPreExecute() {
//            mRefreshButton.setEnabled(false);
//            mRefreshButton.setText(com.gowarrior.camera.client.R.string.refreshing);
//        }
//
//        @Override
//        protected List<S3ObjectSummary> doInBackground(Void... params) {
//            // get all the objects in bucket
//            return mClient.listObjects(Constants.BUCKET_NAME.toLowerCase(Locale.US),
//                    Util.getPrefix(DownloadActivity.this)).getObjectSummaries();
//        }
//
//        @Override
//        protected void onPostExecute(List<S3ObjectSummary> objects) {
//            // now that we have all the keys, add them all to the adapter
//            mAdapter.clear();
//            mAdapter.addAll(objects);
//            mSelectedObjects.clear();
//            mRefreshButton.setEnabled(true);
//            mRefreshButton.setText(com.gowarrior.camera.client.R.string.refresh);
//        }
//    }
//
//    /*
//     * This lets the user click on anywhere in the row instead of just the
//     * checkbox to select the files to download
//     */
//    private class ItemClickListener implements OnItemClickListener {
//        @Override
//        public void onItemClick(AdapterView<?> parent, View view, int pos,
//                long id) {
//            S3ObjectSummary item = mAdapter.getItem(pos);
//            boolean checked = false;
//            // try removing, if it wasn't there add
//            if (!mSelectedObjects.remove(item)) {
//                mSelectedObjects.add(item);
//                checked = true;
//            }
//            ((ObjectAdapter.ViewHolder) view.getTag()).checkbox.setChecked(
//                    checked);
//        }
//    }
//
//    /* Adapter for all the S3 objects */
//    private class ObjectAdapter extends ArrayAdapter<S3ObjectSummary> {
//        public ObjectAdapter(Context context) {
//            super(context, com.gowarrior.camera.client.R.layout.bucket_row);
//        }
//
//        @Override
//        public View getView(int pos, View convertView, ViewGroup parent) {
//            ViewHolder holder;
//            if (convertView == null) {
//                convertView = LayoutInflater.from(getContext()).inflate(
//                        com.gowarrior.camera.client.R.layout.bucket_row, null);
//                holder = new ViewHolder(convertView);
//                convertView.setTag(holder);
//            } else {
//                holder = (ViewHolder) convertView.getTag();
//            }
//            S3ObjectSummary summary = getItem(pos);
//            holder.checkbox.setChecked(mSelectedObjects.contains(summary));
//            holder.key.setText(Util.getFileName(summary.getKey()));
//            holder.size.setText(String.valueOf(summary.getSize()));
//            return convertView;
//        }
//
//        public void addAll(Collection<? extends S3ObjectSummary> collection) {
//            for (S3ObjectSummary obj : collection) {
//                // if statement removes the "folder" from showing up
//                if (!obj.getKey().equals(Util.getPrefix(DownloadActivity.this)))
//                {
//                    add(obj);
//                }
//            }
//        }
//
//        private class ViewHolder {
//            private CheckBox checkbox;
//            private TextView key;
//            private TextView size;
//
//            private ViewHolder(View view) {
//                checkbox = (CheckBox) view.findViewById(com.gowarrior.camera.client.R.id.checkbox);
//                key = (TextView) view.findViewById(com.gowarrior.camera.client.R.id.key);
//                size = (TextView) view.findViewById(com.gowarrior.camera.client.R.id.size);
//            }
//        }
//    }
}
