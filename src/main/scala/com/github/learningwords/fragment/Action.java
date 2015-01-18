package com.github.learningwords.fragment;

/**
 * Created by dmgcodevil on 1/18/2015.
 */
public interface Action<R> {

    R doAction();

    void onComplete();

}
