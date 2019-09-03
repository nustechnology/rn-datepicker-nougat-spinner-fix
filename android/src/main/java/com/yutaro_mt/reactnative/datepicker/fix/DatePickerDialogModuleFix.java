package com.yutaro_mt.reactnative.datepicker.fix;

import android.support.v4.app.FragmentActivity;
import android.app.DatePickerDialog;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.modules.datepicker.DatePickerDialogModule;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javax.annotation.Nullable;

public class DatePickerDialogModuleFix extends DatePickerDialogModule {

  public static final String FRAG_TAG = "DatePickerDialogModuleFix";

  public DatePickerDialogModuleFix(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  public String getName() {
    return "DatePickerAndroidNougatFix";
  }

  @ReactMethod
  public void open(@Nullable final ReadableMap options, Promise promise) {
    FragmentActivity activity = (FragmentActivity)getCurrentActivity();
    if (activity == null){
      super.open(options,promise);
      return;
    }
    //remove existing fragment
    FragmentManager fragmentManager = activity.getSupportFragmentManager();
    Fragment oldFragment = fragmentManager.findFragmentByTag(FRAG_TAG);
    if (oldFragment != null) {
      DialogFragment df = (DialogFragment) oldFragment;
      df.dismiss();
    }
    // create dialog
    DatePickerDialogFragmentFix fragment = new DatePickerDialogFragmentFix();
    try {
      if (options != null) {
        Method method = DatePickerDialogModule.class.getDeclaredMethod("createFragmentArguments", ReadableMap.class);
        method.setAccessible(true);
        final Bundle args =(Bundle)method.invoke(this, options);
        fragment.setArguments(args);
      }
      // create listener instance
      Class<?> listenerClass = Class.forName("com.facebook.react.modules.datepicker.DatePickerDialogModule$DatePickerDialogListener");
      Constructor<?> listenerConstructor = listenerClass.getDeclaredConstructor(DatePickerDialogModule.class, Promise.class);
      listenerConstructor.setAccessible(true);
      Object listenerInstance = listenerConstructor.newInstance((DatePickerDialogModule)this, promise);
      //set listeners
      Method setOnDateSetListener = fragment.getClass().getSuperclass().getDeclaredMethod("setOnDateSetListener", DatePickerDialog.OnDateSetListener.class);
      Method setOnDismissListener = fragment.getClass().getSuperclass().getDeclaredMethod("setOnDismissListener", DialogInterface.OnDismissListener.class);
      setOnDateSetListener.setAccessible(true);
      setOnDismissListener.setAccessible(true);
      setOnDateSetListener.invoke(fragment, listenerInstance);
      setOnDismissListener.invoke(fragment, listenerInstance);
    } catch (Exception e) {
      super.open(options, promise);
      return;
    }
    fragment.show(fragmentManager, FRAG_TAG);
  }
}
