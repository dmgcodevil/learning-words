package com.github.learningwords.android.common.task;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by dmgcodevil on 1/17/2015.
 */
public abstract class AsyncTask<Params, Progress, Result> extends android.os.AsyncTask<Params, Progress, Result> implements Serializable{

    protected abstract Result doInBackground(TaskParams<Params> params);

    @Override
    @SafeVarargs
    protected final Result doInBackground(Params... params) {
        TaskParams<Params> taskParams;
        if (params != null && params.length > 0) {
            taskParams = new TaskParams<>(Arrays.asList(params));
        } else {
            taskParams = new TaskParams<>(Collections.<Params>emptyList());
        }
        return doInBackground(taskParams);
    }
}
