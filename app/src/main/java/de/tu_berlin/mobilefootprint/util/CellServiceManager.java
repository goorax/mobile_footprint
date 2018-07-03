package de.tu_berlin.mobilefootprint.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import de.tu_berlin.snet.cellservice.CellService;

/**
 * Created by niels on 1/5/17.
 */

public class CellServiceManager {

    private static CellServiceManager instance;
    private static Context context;

    private CellServiceManager() {}

    public static synchronized CellServiceManager getInstance(Context context) {

        if (instance == null) {

            instance = new CellServiceManager();
        }

        instance.setContext(context);

        return instance;
    }

    private void setContext(Context context) {

        this.context = context;
    }

    public void startService() {

        context.startService(new Intent(context, CellService.class));
    }

    public void stopService() {

        context.stopService(new Intent(context, CellService.class));
    }

    public boolean isServiceRunning() {

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {

            if (CellService.class.getName().equals(service.service.getClassName())) {

                return true;
            }
        }

        return false;
    }
}
