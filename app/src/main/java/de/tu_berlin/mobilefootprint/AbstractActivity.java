package de.tu_berlin.mobilefootprint;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import de.tu_berlin.mobilefootprint.util.DataLoaderTask;

public abstract class AbstractActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Context context;
    NavigationView navigationView;
    SharedPreferences sharedPref;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        this.context = getApplicationContext();
        sharedPref = context.getSharedPreferences(DataLoaderTask.KEY_SHARED_PREFS, Context.MODE_PRIVATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    protected abstract int getLayoutResourceId();

    protected abstract int getOptionsMenuResourceId();

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

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(getOptionsMenuResourceId(), menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        Intent intent;

        switch (id) {
            case R.id.nav_home:
                intent = new Intent(this, ActivityHome.class);
                startActivity(intent);
                break;
            case R.id.nav_mapview:
                intent = new Intent(this, ActivityMap.class);
                startActivity(intent);
                break;
            case R.id.nav_hotspots:
                Long heatmapTs = sharedPref.getLong(DataLoaderTask.KEY_TS_HEATMAP, 0l);
                if (heatmapTs == 0l) {
                    DialogInterface.OnClickListener ocl = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            new AbsLoaderTask(context, DataLoaderTask.HEATMAP_LOAD).execute();
                        }
                    };
                    showQuestionDialogForHeatMap(ocl);
                } else {
                    intent = new Intent(this, ActivityHeatMap.class);
                    startActivity(intent);
                }
                break;
            case R.id.nav_statistics:
                intent = new Intent(this, ActivityStatistics.class);
                startActivity(intent);
                break;
            case R.id.nav_list:
                intent = new Intent(this, ActivityList.class);
                startActivity(intent);
                break;
            case R.id.nav_settings:
                intent = new Intent(this, ActivitySettings.class);
                startActivity(intent);
                break;
            case R.id.nav_about:
                intent = new Intent(this, ActivityAbout.class);
                startActivity(intent);
                break;
            default:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    protected void showQuestionDialogForHeatMap(DialogInterface.OnClickListener ocl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(AbstractActivity.this);
        builder.setMessage("To show most visited places preprocessing will take " +
                "some time. Continue?")
                .setTitle("Preprocessing necessary!");
        builder.setPositiveButton(R.string.ok, ocl);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private class AbsLoaderTask extends DataLoaderTask {

        public AbsLoaderTask(Context ctx, int mode) {
            super(ctx, mode);
            this.progress = new Progress(this);
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(AbstractActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            switch (mode) {
                case 0:
                    progressDialog.setTitle("Loading CellServiceApp");
                    progressDialog.setMessage("Collecting initial data...");
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
            if (mode == HEATMAP_LOAD) {
                Intent intent = new Intent(AbstractActivity.this, ActivityHeatMap.class);
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
