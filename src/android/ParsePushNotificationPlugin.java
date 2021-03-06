package com.cranberrygame.cordova.plugin.pushnotification.parsepushnotification;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;
//
import com.parse.*;
//
import android.content.SharedPreferences;
import android.content.Context;

public class ParsePushNotificationPlugin extends CordovaPlugin {
	private static final String LOG_TAG = "ParsePush";
	private CallbackContext callbackContextKeepCallback;
	private static boolean destroyed;
		
    @Override
	public void pluginInitialize() {
		Log.d(LOG_TAG, "pluginInitialize()");
		super.pluginInitialize();
    }	

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyed = true;
    }
    
    public static boolean destroyed() {
    	return destroyed;
    }
    
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		Log.d(LOG_TAG, "execute()");
		if (action.equals("setUp")) {
			setUp(action, args, callbackContext);

			return true;
		}			
		else if (action.equals("subscribeToChannel")) {
			subscribeToChannel(action, args, callbackContext);
			
			return true;
		}
		else if (action.equals("unsubscribe")) {
			unsubscribe(action, args, callbackContext);
						
			return true;
		}
		
		return false; // Returning false results in a "MethodNotFound" error.
	}
	
	private void setUp(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		Log.d(LOG_TAG, "setUp()");
		final String applicationId = args.getString(0);
		final String clientKey = args.getString(1);		
		Log.d(LOG_TAG, String.format("%s", applicationId));			
		Log.d(LOG_TAG, String.format("%s", clientKey));
		
		callbackContextKeepCallback = callbackContext;
			
		cordova.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				_setUp(applicationId, clientKey);
			}
		});
	}

	private void subscribeToChannel(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		Log.d(LOG_TAG, "subscribeToChannel()");
		final String channel = args.getString(0);
		Log.d(LOG_TAG, String.format("%s", channel));
		
		cordova.getActivity().runOnUiThread(new Runnable(){
			@Override
			public void run() {
				_subscribeToChannel(channel);
			}
		});
	}

	private void unsubscribe(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		Log.d(LOG_TAG, "unsubscribe()");
		final String channel = args.getString(0);
		Log.d(LOG_TAG, String.format("%s",channel));
		
		cordova.getActivity().runOnUiThread(new Runnable(){
			@Override
			public void run() {
				_unsubscribe(channel);
			}
		});
	}
	
    private void _setUp(String applicationId, String clientKey) {
		Log.d(LOG_TAG, "_setUp()");
        try {
           	Parse.initialize(cordova.getActivity(), applicationId, clientKey);
    	   	ParseInstallation.getCurrentInstallation().save();

			SharedPreferences sharedPref = cordova.getActivity().getSharedPreferences("cordova-plugin-pushnotification-parse", Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putString("applicationId", applicationId);
			editor.putString("clientKey", clientKey);
			editor.apply();
		
			PluginResult pr = new PluginResult(PluginResult.Status.OK, "onRegisterAsPushNotificationClientSucceeded");
			pr.setKeepCallback(true);
			callbackContextKeepCallback.sendPluginResult(pr);
        } 
        catch (ParseException e) {
			PluginResult pr = new PluginResult(PluginResult.Status.ERROR, "onRegisterAsPushNotificationClientFailed");
			pr.setKeepCallback(true);
			callbackContextKeepCallback.sendPluginResult(pr);		
        }		
    }

    private void _subscribeToChannel(String channel) {
		Log.d(LOG_TAG, "_subscribeToChannel()");
        ParsePush.subscribeInBackground(channel, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
            		PluginResult pr = new PluginResult(PluginResult.Status.OK, "onSubscribeToChannelSucceeded");
            		pr.setKeepCallback(true);
            		callbackContextKeepCallback.sendPluginResult(pr);
                } 
                else {
            		PluginResult pr = new PluginResult(PluginResult.Status.ERROR, "onSubscribeToChannelFailed");
            		pr.setKeepCallback(true);
            		callbackContextKeepCallback.sendPluginResult(pr);                    
                }
            }
        });
    }

    private void _unsubscribe(String channel) {
		Log.d(LOG_TAG, "_unsubscribe()");
        ParsePush.unsubscribeInBackground(channel, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
            		PluginResult pr = new PluginResult(PluginResult.Status.OK, "onUnsubscribeSucceeded");
            		pr.setKeepCallback(true);
            		callbackContextKeepCallback.sendPluginResult(pr);
                }
                else {
            		PluginResult pr = new PluginResult(PluginResult.Status.ERROR, "onUnsubscribeFailed");
            		pr.setKeepCallback(true);
            		callbackContextKeepCallback.sendPluginResult(pr);	
                }
            }
        });
    }	
}

