package com.appchemy;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.os.Bundle;
import android.util.Log;
import android.widget.Spinner;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;

import com.zebra.adc.decoder.Barcode2DWithSoft;
import java.io.UnsupportedEncodingException;

/**
 * This class echoes a string called from JavaScript.
 */
public class Scanner extends CordovaPlugin {
    Barcode2DWithSoft barcode2DWithSoft;
    ProgressDialog mypDialog;
    HomeKeyEventBroadCastReceiver receiver;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        } else if (action.equals("scan")) {
            Toast.makeText(cordova.getActivity(),"Scanning",Toast.LENGTH_SHORT).show();
            this.scan(callbackContext);
            return true;
        } else if (action.equals("init")) {
            initCallback = callbackContext;
            this.init(callbackContext);
            return true;
        } else if (action.equals("trigger")) {
            triggerCallback = callbackContext;
            return true;
        } else if (action.equals("close")) {
            return true;
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    CallbackContext initCallback;
    CallbackContext triggerCallback;
    private void init(CallbackContext callbackContext) {
        receiver = new HomeKeyEventBroadCastReceiver();
        cordova.getActivity().registerReceiver(receiver, new IntentFilter("com.rscja.android.KEY_DOWN"));

        cordova.getActivity().runOnUiThread(new Runnable() {
            public void run() {
                new InitTask().execute("");
            }
        });
        
    }

    private void scan(CallbackContext callbackContext) {
        barcode2DWithSoft.scan();

        barcode2DWithSoft.setScanCallback(new Barcode2DWithSoft.ScanCallback(){
            @Override
            public void onScanComplete(int i, int length, byte[] bytes) {
                if (length < 1) {
                    callbackContext.error("Scan Fail");
                }else{
                    // SoundManage.PlaySound(MainActivity.this, SoundManage.SoundType.SUCCESS);
                    String barCode="";
    
    
                  //  String res = new String(dd,"gb2312");
                    try {
                        barCode = new String(bytes, 0, length, "ASCII");
                        callbackContext.success(barCode);
                    }
                    catch (UnsupportedEncodingException ex)   {
                        callbackContext.error("Scan Fail, exception, " + ex.getMessage());
                    }
                }
    
            }
        });
        
    }

    public class InitTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mypDialog;

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub

            Log.i("ScannerTAG","Running in background");
            boolean reuslt=false;
            if(barcode2DWithSoft!=null) {
                reuslt=  barcode2DWithSoft.open(cordova.getActivity());

            }
            return reuslt;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            Log.i("ScannerTAG","Executing in background");

            if(result){
//                barcode2DWithSoft.setParameter(324, 1);
//                barcode2DWithSoft.setParameter(300, 0); // Snapshot Aiming
//                barcode2DWithSoft.setParameter(361, 0); // Image Capture Illumination

Toast.makeText(cordova.getActivity(),"Success",Toast.LENGTH_SHORT).show();
                // interleaved 2 of 5
                barcode2DWithSoft.setParameter(6, 1);
                barcode2DWithSoft.setParameter(22, 0);
                barcode2DWithSoft.setParameter(23, 55);
                barcode2DWithSoft.setParameter(402, 1);
                
                Scanner.this.initCallback.success("success");
            }else{
                Toast.makeText(cordova.getActivity(),"fail",Toast.LENGTH_SHORT).show();
                Scanner.this.initCallback.error("fail");
            }
            mypDialog.cancel();
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();

            Scanner.this.barcode2DWithSoft = Barcode2DWithSoft.getInstance();

            mypDialog = new ProgressDialog(cordova.getActivity());
            mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mypDialog.setMessage("init...");
            mypDialog.setCanceledOnTouchOutside(false);
            mypDialog.show();

            Log.i("ScannerTAG","Done running in background");
        }

    }

    public class HomeKeyEventBroadCastReceiver extends BroadcastReceiver {

        static final String SYSTEM_REASON = "reason";
        static final String SYSTEM_HOME_KEY = "homekey";//home key
        static final String SYSTEM_RECENT_APPS = "recentapps";//long home key

        HomeKeyEventBroadCastReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("com.rscja.android.KEY_DOWN")) {
                int reason = intent.getIntExtra("Keycode",0);
                //getStringExtra
                boolean long1 = intent.getBooleanExtra("Pressed",false);
                // home key处理点
                PluginResult pluginResult = new  PluginResult(PluginResult.Status.OK, reason);
                pluginResult.setKeepCallback(true);
                Scanner.this.triggerCallback.sendPluginResult(pluginResult);
//                Scanner.this.triggerCallback.success(reason);
               // Toast.makeText(getApplicationContext(), "home key="+reason+",long1="+long1, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
