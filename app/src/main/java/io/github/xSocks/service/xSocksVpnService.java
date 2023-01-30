package io.github.xSocks.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.VpnService;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import io.github.xSocks.BuildConfig;
import io.github.xSocks.R;
import io.github.xSocks.aidl.Config;
import io.github.xSocks.aidl.IxSocksService;
import io.github.xSocks.aidl.IxSocksServiceCallback;
import io.github.xSocks.model.ProxiedApp;
import io.github.xSocks.ui.AppManagerActivity;
import io.github.xSocks.ui.MainActivity;
import io.github.xSocks.ui.xSocksRunnerActivity;
import io.github.xSocks.utils.ConfigUtils;
import io.github.xSocks.utils.Console;
import io.github.xSocks.utils.Constants;
import io.github.xSocks.utils.Utils;
import rx.schedulers.Schedulers;
import xsocks.Xsocks.*;
import rx.util.async.Async;

public class xSocksVpnService extends VpnService {

    private String TAG = "xSocks";
    private static final String VPN_ADDRESS = "26.26.26.1";
    private Config config = null;
    public ParcelFileDescriptor vpnInterface;
    private BroadcastReceiver closeReceiver = null;
    private Constants.State state = Constants.State.INIT;
    private int callbackCount = 0;
    int VPN_MTU = 1500*20;
    private final RemoteCallbackList<IxSocksServiceCallback> callbacks = new RemoteCallbackList<>();

    private IxSocksService.Stub binder = new IxSocksService.Stub() {
        @Override
        public int getState() throws RemoteException {
            return state.ordinal();
        }

        @Override
        public void registerCallback(IxSocksServiceCallback cb) throws RemoteException {
            if (cb != null) {
                callbacks.register(cb);
                callbackCount += 1;
            }
        }

        @Override
        public void unregisterCallback(IxSocksServiceCallback cb) throws RemoteException {
            if (cb != null ) {
                callbacks.unregister(cb);
                callbackCount -= 1;
            }
            if (callbackCount == 0 && state != Constants.State.CONNECTING && state != Constants.State.CONNECTED) {
                stopSelf();
            }
        }

        @Override
        public void start(Config config) {
            if (state != Constants.State.CONNECTING && state != Constants.State.STOPPING) {
                startRunner(config);
            }
        }

        @Override
        public void stop() throws RemoteException {
            if (state != Constants.State.CONNECTING && state != Constants.State.STOPPING) {
                stopRunner();
            }
        }
    };

    private void notifyForegroundAlert(String title, String info, Boolean visible) {
        Intent openIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, openIntent, 0);
        Intent closeIntent = new Intent(Constants.Action.CLOSE);
        PendingIntent actionIntent = PendingIntent.getBroadcast(this, 0, closeIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setWhen(0)
                .setTicker(title)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(info)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_logo)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                        getString(R.string.stop), actionIntent);

        if (visible) {
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_MIN);
        }
        startForeground(1, builder.build());
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        String action = intent.getAction();
        if (VpnService.SERVICE_INTERFACE.equals(action)) {
            return super.onBind(intent);
        } else if (Constants.Action.SERVICE.equals(action)) {
            return binder;
        }
        return null;
    }

    @Override
    public void onRevoke() {
        stopRunner();
    }



    private void startxSocksDaemon(Integer tunFd) throws IOException {
        String serverAddr=config.protocol+"://"+config.proxy+":"+config.remotePort;
        String password=config.sitekey;
        String caFile="";
        Boolean skipVerify=false;
        Integer tunType=0;
        String unixSockTun="";
        Integer smartDns=0;
        Integer localDns=0;
        Integer muxNum=0;
        Integer udpProxy=1;
        Integer mtu=4500;
        Boolean tunSmartProxy=false;
        if(config.verifyCert.equals("skip")){
            skipVerify=true;
        }
        if(config.verifyCert.equals("self_signed")){
            caFile=config.caFile;
        }
     
        tunType= Integer.parseInt(config.tunType);
        
        mtu=VPN_MTU;
        String ipFile=Constants.Path.BASE + "iptable.txt";
        xsocks.Xsocks.start("",serverAddr,password,caFile,skipVerify,tunType,"",tunFd,muxNum,localDns,smartDns,udpProxy,mtu,tunSmartProxy,ipFile);
    }



    private int startVpn() throws IOException {
     
        Builder builder = new Builder();
        builder.setSession(config.profileName);
        builder.setMtu(VPN_MTU);
        builder.addAddress(VPN_ADDRESS, 24);
        builder.addDnsServer("8.8.4.4");
        if (Utils.isLollipopOrAbove()) {
            try {
                if (!config.isGlobalProxy) {
                    ProxiedApp[] apps = AppManagerActivity.getProxiedApps(this, config.proxiedAppString);
                    for (ProxiedApp app : apps) {
                        if (config.isBypassApps) {
                            builder.addDisallowedApplication(app.getPackageName());

                        } else {
                            builder.addAllowedApplication(app.getPackageName());
                        }
                    }
                    if (config.isBypassApps) {
                        Log.d("isBypassApps",this.getApplicationContext().getPackageName());
                        builder.addDisallowedApplication(this.getApplicationContext().getPackageName());
                    }
                }else{
                   //全局的话加自己白名单就OK
                    Log.d("isBypassApps",this.getApplicationContext().getPackageName());
                    builder.addDisallowedApplication(this.getApplicationContext().getPackageName());
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Package name not found");
            }
        }

        builder.addRoute("0.0.0.0", 0);
        builder.setBlocking(false);
     //  builder.setBlocking(true);
        vpnInterface = builder.establish();
        if (vpnInterface == null) {
            Log.e(TAG, "vpn interface is null");
            return -1;
        }
        int fd = vpnInterface.getFd();
        return fd;
    }

    private boolean startDaemons() {
        try {
            int fd = startVpn();
            if (fd != -1) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            startxSocksDaemon(fd);
                        }catch (IOException e){

                        }
                    }
                }).start();
                return true;
            }
        } catch (IOException e) {
            Log.e(TAG, "Got " + e.getMessage());
            return false;
        }
        return false;
    }

    private void startRunner(Config c) {
        config = c;
        // register close closeReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SHUTDOWN);
        filter.addAction(Constants.Action.CLOSE);
        closeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(context, R.string.stopping, Toast.LENGTH_SHORT).show();
                stopRunner();
            }
        };
        registerReceiver(closeReceiver, filter);
        // ensure the VPNService is prepared
        if (VpnService.prepare(this) != null) {
            Intent i = new Intent(this, xSocksRunnerActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            return;
        }
        changeState(Constants.State.CONNECTING);
        Async.runAsync(Schedulers.newThread(), (observer, subscription) -> {
            if (config != null) {
                //关闭之前的
                xsocks.Xsocks.shutdown();
                boolean resolved = true;
                //检测连通
                if(config.protocol.equals("wss")){
                    boolean PortState=Utils.checkConnect(config.proxy,config.remotePort);
                    if (!PortState) {
                        resolved = false;
                    }
                    Log.e("proxy:",config.proxy);
                }

                if (resolved && startDaemons()) {
                    notifyForegroundAlert(getString(R.string.forward_success),
                            getString(R.string.service_running, config.profileName),
                            false);
                    changeState(Constants.State.CONNECTED);
                } else {
                    changeState(Constants.State.STOPPED, getString(R.string.service_failed));
                    stopRunner();
                }
            }
        });
    }

    private void stopRunner() {
        stopForeground(true);
        changeState(Constants.State.STOPPING);
        xsocks.Xsocks.shutdown();
        if (vpnInterface != null) {
            try {
                vpnInterface.close();
            } catch (IOException e) {
                // close failed
            }
        }
        // stop the service if no callback registered
        if (callbackCount == 0) {
            stopSelf();
        }
        // clean up receiver
        if (closeReceiver != null) {
            unregisterReceiver(closeReceiver);
            closeReceiver = null;
        }
        changeState(Constants.State.STOPPED);
    }

    private void changeState(Constants.State s) {
        changeState(s, null);
    }

    public void changeState(Constants.State s, String msg) {
        Handler handler = new Handler(getBaseContext().getMainLooper());
        handler.post(() -> {
            if (state != s) {
                if (callbackCount > 0) {
                    int n = callbacks.beginBroadcast();
                    for (int i = 0; i <= n - 1; i++) {
                        try {
                            callbacks.getBroadcastItem(i).stateChanged(s.ordinal(), msg);
                        } catch (RemoteException e) {
                            // Ignore
                        }
                    }
                    callbacks.finishBroadcast();
                }
                state = s;
            }
        });
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_REDELIVER_INTENT;
    }
}
