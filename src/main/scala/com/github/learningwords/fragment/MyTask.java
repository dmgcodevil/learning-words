package com.github.learningwords.fragment;

import android.os.SystemClock;

import com.github.learningwords.Word;
import com.github.learningwords.android.common.task.AsyncTask;
import com.github.learningwords.android.common.task.TaskParams;

/**
 * Created by dmgcodevil on 1/18/2015.
 */
public class MyTask extends AsyncTask<Word, Void, String>
{
    private TaskFragment mFragment;
    private int mProgress = 0;
    private Action<String> action;

    public MyTask(Action<String> action) {
        this.action = action;
    }

    void setFragment(TaskFragment fragment)
    {
        mFragment = fragment;
    }

    @Override
    protected void onProgressUpdate(Void... unused)
    {
        if (mFragment == null)
            return;
        mFragment.updateProgress(mProgress);
    }

    @Override
    protected void onPostExecute(String unused)
    {
        if (mFragment == null)
            return;
        mFragment.taskFinished();
        action.onComplete();
    }

    @Override
    protected String doInBackground(TaskParams<Word> params) {
        for (int i = 0; i < 10; i++)
        {
            // Check if this has been cancelled, e.g. when the dialog is dismissed.
            if (isCancelled())
                return null;

            SystemClock.sleep(500);
            mProgress = i * 10;
            publishProgress();
        }
        return action.doAction();
    }

}