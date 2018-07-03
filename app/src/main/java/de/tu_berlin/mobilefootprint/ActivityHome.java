package de.tu_berlin.mobilefootprint;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.tu_berlin.mobilefootprint.util.CellServiceManager;
import de.tu_berlin.mobilefootprint.util.DataLoaderTask;

/**
 * This is the default launcher activity of CellServiceApp. It contains basic permission checking,
 * dialog handling and an async task for loading data.
 *
 * @author johannes
 */

public class ActivityHome extends AbstractActivity {

    public static final int REQUEST_PERMISSIONS = 1;
    public static final String SIMPLE_TIME = " dd.MM.yy HH:mm";
    public static final String NONE = "Last refresh: none";
    public static boolean INITIAL_LOAD = true;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        checkPermissions();
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion < Build.VERSION_CODES.M) {
            CellServiceManager mgr = CellServiceManager.getInstance(this);
            if (!mgr.isServiceRunning()) {
                mgr.startService();
            }
            startInitialLoading();
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_home;
    }

    @Override
    protected int getOptionsMenuResourceId() {
        return R.menu.activity_home;
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            showPermissionInfoDialog();

        } else {
            startInitialLoading();
        }
    }

    protected void showPermissionInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityHome.this);
        builder.setMessage("To visualize your personal Mobile Footprint in order to give " +
                "an impression what the data retention law in Germany will collect, it's necessary " +
                "to request permissions. " +
                "Mobile Footprint will access the information of cell towers you are connected to and " +
                "estimate the coarse position of your device. " +
                "It's required to collect meta information of your inbound and outbound calls, " +
                "short messages and mobile data usage, which will be stored in a database. " +
                "No content of these data records is captured. The denial of one or more permissions " +
                "will cause incomplete visualizations.")
                .setTitle("Mobile Footprint requires permissions!");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                ActivityCompat.requestPermissions(ActivityHome.this,
                        new String[] {
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.READ_SMS,
                                Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_CONTACTS
                        },
                        REQUEST_PERMISSIONS);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    CellServiceManager mgr = CellServiceManager.getInstance(this);

                    if (!mgr.isServiceRunning()) {
                        mgr.startService();
                    }
                    startInitialLoading();
                }
                break;
            }
        }
    }

    private void startInitialLoading() {
        if (INITIAL_LOAD) {
            INITIAL_LOAD = false;
            DataLoaderTask initialLoaderTask = new HomeLoaderTask(context, 0);
            initialLoaderTask.execute();
        } else {
            setRefreshTimes();
            setButtonListener();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    private void setButtonListener() {
        Button mapButton = (Button) findViewById(R.id.button_activity_map);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityHome.this, ActivityMap.class);
                startActivity(intent);
            }
        });

        Button hotspotsButton = (Button) findViewById(R.id.button_hotspots);
        hotspotsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long heatmapTs = sharedPref.getLong(DataLoaderTask.KEY_TS_HEATMAP, 0l);
                if (heatmapTs == 0l) {
                    DialogInterface.OnClickListener ocl = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            new HomeLoaderTask(context, DataLoaderTask.HEATMAP_LOAD).execute();
                        }
                    };
                    showQuestionDialogForHeatMap(ocl);
                } else {
                    Intent intent = new Intent(ActivityHome.this, ActivityHeatMap.class);
                    startActivity(intent);
                }
            }
        });
        Button statsButton = (Button) findViewById(R.id.button_activity_statistics);
        statsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityHome.this, ActivityStatistics.class);
                startActivity(intent);
            }
        });

        Button contactsButton = (Button) findViewById(R.id.button_activity_contacts);
        contactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityHome.this, ActivityStatistics.class);
                intent.putExtra("switch_to_contacts", true);
                startActivity(intent);
            }
        });


        Button reloadButton = (Button) findViewById(R.id.button_reload);
        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new HomeLoaderTask(context, DataLoaderTask.RECORD_LOAD).execute();
            }
        });

        Button heatMapButton = (Button) findViewById(R.id.button_heatmap);
        heatMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener ocl = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new HomeLoaderTask(context, DataLoaderTask.HEATMAP_LOAD).execute();
                    }
                };
                showQuestionDialogForHeatMap(ocl);
            }
        });

        Button aboutButton = (Button) findViewById(R.id.button_more);
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityHome.this, ActivityAbout.class);
                startActivity(intent);
            }
        });
    }

    private void setRefreshTimes() {
        SimpleDateFormat sdf = new SimpleDateFormat(SIMPLE_TIME);
        long tsLoading = sharedPref.getLong(DataLoaderTask.KEY_TS_DATA, 0l);
        long tsHeatMap = sharedPref.getLong(DataLoaderTask.KEY_TS_HEATMAP, 0l);
        TextView refreshDataTv = (TextView) findViewById(R.id.refresh_loading);
        TextView refreshHeatMapTv = (TextView) findViewById(R.id.refresh_heatmap);

        Date loadingDate = new Date(tsLoading);
        Date heatMapDate = new Date(tsHeatMap);

        if (tsHeatMap == 0l) {
            refreshHeatMapTv.setText(NONE);
        } else {
            refreshHeatMapTv.setText("Last refresh: " + sdf.format(heatMapDate));
        }

        if (tsLoading == 0l) {
            refreshDataTv.setText(NONE);
        } else {
            refreshDataTv.setText("Last refresh: " + sdf.format(loadingDate));
        }
    }

    private class HomeLoaderTask extends DataLoaderTask {

        public HomeLoaderTask(Context ctx, int mode) {
            super(ctx, mode);
            this.progress = new Progress(this);
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(ActivityHome.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            switch (mode) {
                case 0:
                    progressDialog.setTitle("Loading Data Traces");
                    progressDialog.setMessage("Collecting initial data ...");
                    progressDialog.setIndeterminate(true);
                    progressDialog.setCancelable(false);
                    break;
                case RECORD_LOAD:
                    progressDialog.setTitle("Reloading data ...");
                    progressDialog.setIndeterminate(true);
                    break;
                case HEATMAP_LOAD:
                    progressDialog.setTitle("Processing most visited places ...");
                    break;
                default:
                    break;
            }
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            setRefreshTimes();
            if (mode == 0) {
                setButtonListener();
            } else if (mode == HEATMAP_LOAD) {
                Intent intent = new Intent(ActivityHome.this, ActivityHeatMap.class);
                startActivity(intent);
            }
            mode = -1;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            progressDialog.setProgress(progress[0]);
        }

    }
}
