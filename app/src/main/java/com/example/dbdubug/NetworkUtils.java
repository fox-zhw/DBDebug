package com.example.dbdubug;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.IntDef;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * @version 1.1
 * @date 2018/10/11
 */

public class NetworkUtils {
	public static final String TAG = "Weather0-NetworkUtils";
	private Context mContext = null;
	private NetworkConnectChangedReceiver networkConnectChangedReceiver;
	private final List<INetworkStateListener> networkConnectListeners = new ArrayList<>();
	@NetState
	private volatile int netState = STATE_UNKNOWN;
	public static final int STATE_CONNECTING = 1;
	public static final int STATE_CONNECTED = 2;
	public static final int STATE_SUSPENDED = 3;
	public static final int STATE_DISCONNECTING = 4;
	public static final int STATE_DISCONNECTED = 5;
	public static final int STATE_UNKNOWN = 6;
	
	@NetType
	private volatile int netType = TYPE_UNKNOWN;
	public static final int TYPE_WIFI = 11;
	public static final int TYPE_MOBILE = 12;
	public static final int TYPE_UNKNOWN = 13;
	
	/**
	 * 网络状态
	 */
	@IntDef({STATE_CONNECTING,
			STATE_CONNECTED,
			STATE_SUSPENDED,
			STATE_DISCONNECTING,
			STATE_DISCONNECTED,
			STATE_UNKNOWN})
	@Retention(RetentionPolicy.SOURCE)
	public @interface NetState {
	
	}
	
	/**
	 * 网络类型
	 */
	@IntDef({TYPE_WIFI,
			TYPE_MOBILE,
			TYPE_UNKNOWN})
	@Retention(RetentionPolicy.SOURCE)
	public @interface NetType {
	
	}
	
	private NetworkUtils() {
	}
	
	public static NetworkUtils getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder {
		private static final NetworkUtils INSTANCE = new NetworkUtils();
	}
	
	public void init(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("Error context == null");
		}
		mContext = context;
		networkConnectChangedReceiver = new NetworkConnectChangedReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		mContext.registerReceiver(networkConnectChangedReceiver, filter);
	}
	
	public void destroy() {
		if (mContext != null && networkConnectChangedReceiver != null) {
			mContext.unregisterReceiver(networkConnectChangedReceiver);
		}
	}
	
	/**
	 * 当前是否有网络连接
	 *
	 * @return
	 */
	public boolean hasNetwork() {
		if (mContext == null) {
			throw new IllegalStateException("Error mContext == null");
		}
		ConnectivityManager manager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (manager == null) {
			return false;
		}
		
		NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
		return (activeNetwork != null && activeNetwork.isAvailable() && activeNetwork.isConnected());
	}
	
	/**
	 * 添加网络状态监听
	 *
	 * @param listener
	 */
	public void addNetworkStateListener(INetworkStateListener listener) {
		if (listener != null) {
			networkConnectListeners.add(listener);
		}
	}
	
	/**
	 * 移除网络状态监听
	 *
	 * @param listener
	 */
	public void removeNetworkStateListener(INetworkStateListener listener) {
		networkConnectListeners.remove(listener);
	}
	
	private class NetworkConnectChangedReceiver extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// 监听网络连接，包括wifi和移动数据的打开和关闭,以及连接上可用的连接都会接到监听
			if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
				ConnectivityManager manager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
				if (manager == null) {
					Log.e(TAG, "onReceive: manager == null");
					return;
				}
				
				NetworkInfo networkInfo = manager.getActiveNetworkInfo();
				
				if (networkInfo != null) {
					NetworkInfo.State state = networkInfo.getState();
					Log.e(TAG, "onReceive: state" + state);
					// 网络类型 wifi | mobile
					if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
						netType = TYPE_WIFI;
					} else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
						netType = TYPE_MOBILE;
					}
					
					// 网络状态 connected | disconnected
					if (NetworkInfo.State.CONNECTED == state && networkInfo.isAvailable()) {
						Log.e(TAG, netType + "已连接");
						netState = STATE_CONNECTED;
					} else {
						Log.e(TAG, netType + "已断开");
						netState = STATE_DISCONNECTED;
					}
				} else {
					Log.e(TAG, "已断开" + netType);
					netState = STATE_DISCONNECTED;
				}
				
				for (INetworkStateListener listener : networkConnectListeners) {
					listener.onNetworkState(netState, netType);
				}
			}
		}
	}
	
	/**
	 * 网络状态监听
	 */
	public interface INetworkStateListener {
		/**
		 * @param netState {@link NetState}
		 * @param type     {@link NetType}
		 */
		void onNetworkState(@NetState int netState, @NetType int type);
	}
}
