package com.gowarrior.camera.client;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class TransferView extends LinearLayout {
    private static final String TAG = "GoWarriorCameraClient";
    private Context mContext;
    private TextView mText;
    private ImageButton mPause;
    private ImageButton mAbort;
    private Button mOpen;
    private TextView mCancel;
    private ProgressBar mProgress;

    private String mState = null;
    private String mType;
    private String mFileName;
    private int mProgressValue = 0;
    private int mDone = -1;

    public TransferView(Context context, String mTextValue, String mTransferType) {
        super(context);
        LayoutInflater.from(context).inflate(
                R.layout.transfer_view,
                this,
                true);

        mContext = context;
        mType = mTransferType;
        mFileName = mTextValue;

        mText = ((TextView) findViewById(R.id.text));
        mText.setText(mTextValue);
        mPause = (ImageButton) findViewById(R.id.left_button);
        mPause.setImageResource(R.drawable.pause);
        mAbort = (ImageButton) findViewById(R.id.right_button);
        mAbort.setImageResource(R.drawable.x);
        mOpen = (Button) findViewById(R.id.open);
        mCancel = (TextView) findViewById(R.id.canceled);
        mProgress = ((ProgressBar) findViewById(R.id.progress));

        //set click action
        mPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onPause();
            }
        });
        mAbort.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onAbort();
            }
        });

        refresh();
    }

    public void setStateAndProgress(String mNewState, int mNewProgressValue) {
        mState = mNewState;
        mProgressValue = mNewProgressValue;
        if(mState.equals("COMPLETE"))
            mProgressValue = 100;
    }

    /** refresh method for public use */
    public void refresh() {
        if(null != mState)
            refresh(mState);
    }

    /**
     * We use this method within the class so that we can have the UI update
     * quickly when the user selects something
     */
    private void refresh(String status) {
        int leftRes = R.drawable.pause;

        if(status.equals("IN_PROGRESS")) {
            mPause.setVisibility(View.VISIBLE);
            mAbort.setVisibility(View.VISIBLE);
            mCancel.setVisibility(View.GONE);
            mOpen.setVisibility(View.GONE);
            leftRes = R.drawable.pause;
            if (-1 == mDone) {
                mDone = 0;
            }
        } else if(status.equals("PAUSED")) {
            leftRes = R.drawable.play;
        } else if(status.equals("CANCELED")) {
            leftRes = R.drawable.play;
            mPause.setVisibility(View.GONE);
            mAbort.setVisibility(View.GONE);
            mCancel.setVisibility(View.VISIBLE);
        } else if(status.equals("COMPLETE")) {
            leftRes = R.drawable.play;
            mPause.setVisibility(View.GONE);
            mAbort.setVisibility(View.GONE);
            if(mType.equals("download")) {
                mOpen.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewIt();
                    }
                });
                mOpen.setVisibility(View.VISIBLE);
                if (0 == mDone) {
                    mDone = 1;
//                    viewIt();
                }
            }
        } else {
            Log.i(TAG, status + " status can not support!!!\n");
        }

        mPause.setImageResource(leftRes);
        mProgress.setProgress(mProgressValue);
    }

    /** What to do when user presses pause button */
    private void onPause() {
        Log.i(TAG, "To be Fixed!!");
        if (mState.equals("IN_PROGRESS")) {
        } else {
        }
    }

    /**What to do when user presses abort button */
    private void onAbort() {
        Log.i(TAG, "To be Fixed!!");
    }

    private void viewIt() {
        Uri mUri = Uri.fromFile(new File(Constants.DOWNLOAD_TO, mFileName));
        MimeTypeMap m = MimeTypeMap.getSingleton();
        String mimeType = m.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(
                mUri.toString()));

        try {
            // try opening activity to open file
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(mUri, mimeType);
            Log.v(TAG, "mimetype="+mimeType+" file="+ mUri.toString());
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.d(TAG, "", e);
            // if file fails to be opened, show error message
            Toast.makeText(mContext, R.string.nothing_found_to_open_file, Toast.LENGTH_SHORT).show();
        }
    }
}
