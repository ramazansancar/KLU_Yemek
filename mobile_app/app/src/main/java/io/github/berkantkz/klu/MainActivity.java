package io.github.berkantkz.klu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import io.github.berkantkz.klu.Utils.TinyDB;

public class MainActivity extends Activity {

    TinyDB tinydb;
    static ArrayList<KLU_List> list;
    static KLU_Adapter adapter;
    private InterstitialAd mInterstitialAd;
    @SuppressLint("StaticFieldLeak")
    static ProgressBar pb;
    int counter = 0;
    static String today, date, day, content = "";
    @SuppressLint("StaticFieldLeak")
    static TextView tv_today, today_top;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tinydb  = new TinyDB(getApplicationContext());

        MobileAds.initialize(getApplicationContext(),"ca-app-pub-2951689275458403~2892686723");
        startInterstitialAd();
        
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        pb = findViewById(R.id.pb);

        list = new ArrayList<>();

        final GridView gridView = findViewById(R.id.gv);
        tv_today = findViewById(R.id.tv_today);
        today_top = findViewById(R.id.today_top);
        gridView.setPadding(0,0,0, AdSize.BANNER.getHeightInPixels(this));

        adapter = new KLU_Adapter(getApplicationContext(), R.layout.row, list);
        gridView.setAdapter(adapter);

        startInterstitialAd();

        if (isNetworkAvailable()) {
            new JSONAsyncTask().execute("https://berkantkz.me/KLU_Yemek/list.json");
        } else {
            new AlertDialog.Builder(MainActivity.this, R.style.DialogTheme)
                    .setTitle("Bağlantı yok")
                    .setMessage("Kullanılabilir bir bağlantı yok. Bir ağa bağlandıktan sonra uygulamayı yeniden çalıştırın.")
                    .setNegativeButton("ÇIKIŞ", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new AlertDialog.Builder(MainActivity.this, R.style.DialogTheme)
                        .setTitle(list.get(position).getDate().replace("-01-"," Ocak ").replace("-02-", " Şubat ").replace("-03-"," Mart ").replace("-04-", " Nisan ").replace("-05-", " Mayıs ").replace("-06-"," Haziran ").replace("-07-", " Temmuz ").replace("-08-", " Ağustos ").replace("-09-", " Eylül ").replace("-10-" ," Ekim ").replace("-11-", " Kasım ").replace("-12-", " Aralık  ").toUpperCase() + list.get(position).getDay())
                        .setMessage(list.get(position).getContent().replace(",",""))
                        .setPositiveButton("KAPAT", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startInterstitialAd();
                            }
                        })
                        .setCancelable(false)
                        .show();
                startInterstitialAd();
            }
        });
    }

    private static class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... urls) {
            try {
                HttpGet httppost = new HttpGet(urls[0]);
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse response = httpclient.execute(httppost);

                if (response.getStatusLine().getStatusCode() == 200) {
                    HttpEntity entity = response.getEntity();
                    String data = EntityUtils.toString(entity);
                    JSONArray jarray = new JSONArray(data);

                    for (int i = 0; i < jarray.length(); i++) {
                        JSONObject object = jarray.getJSONObject(i);

                        KLU_List KLUList = new KLU_List();

                        KLUList.setDay(object.getString("day"));
                        KLUList.setContent(object.getString("content"));
                        KLUList.setDate(object.getString("date"));

                        list.add(KLUList);

                        Time rn = new Time(Time.getCurrentTimezone());
                        rn.setToNow();
                        int month = rn.month + 1;
                        int monthday = rn.monthDay;
                        int year = rn.year;
                        today = year + "-" + month + "-" + monthday;
                        date = object.getString("date").replace("-0","-").replace(" ", "");

                        if (date.equals(today)) {
                            content = object.getString("content");
                            day = object.getString("day");
                            date = date.replace("-01-"," Ocak ").replace("-02-", " Şubat ").replace("-03-"," Mart ").replace("-04-", " Nisan ").replace("-05-", " Mayıs ").replace("-06-"," Haziran ").replace("-07-", " Temmuz ").replace("-08-", " Ağustos ").replace("-09-", " Eylül ").replace("-10-" ," Ekim ").replace("-11-", " Kasım ").replace("-12-", " Aralık ").toUpperCase() + " " + day;
                        }

                    }
                    return true;
                }

            } catch ( IOException | JSONException e) {
                e.printStackTrace();
            }
            return false;

        }

        @SuppressLint("SetTextI18n")
        protected void onPostExecute(Boolean result) {
            adapter.notifyDataSetChanged();
            pb.setVisibility(View.GONE);
            if (!content.equals("")) {
                today_top.setText("BUGÜN: " + today);
                tv_today.setText(content);
            } else {
                tv_today.setText("Bugün için herhangi bir veri kullanılabilir değil.");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_about) {
            new AlertDialog.Builder(MainActivity.this, R.style.DialogTheme)
                    .setTitle("Hakkında")
                    .setMessage("Kullanılan liste Kırklareli Üniversitesi resmi web sayfasından alınmıştır. \n\nUygulama kaynağı GitHub'da bulunabilir. \n\n - Berkant Korkmaz, berkantkz")
                    .setPositiveButton("KAPAT", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            counter++;
                            if (counter == 4) {
                                tinydb.putBoolean("isAdsDisabled",true);
                                Toast.makeText(MainActivity.this, "Reklâmlar devre dışı bırakıldı.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNeutralButton("Kaynağa git", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Uri uri = Uri.parse("https://github.com/berkantkz/KLU_Yemek");
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        }
                    })
                    .setCancelable(false)
                    .show();
            startInterstitialAd();
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isNetworkAvailable() { // TODO: Use non-deprecated classes asap.
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void startInterstitialAd() {
        if (!tinydb.getBoolean("isAdsDisabled")) {
            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId("ca-app-pub-2951689275458403/2628252404");
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    mInterstitialAd.show();
                }
            });
        } else {
            Log.d("KLU_YEMEK"," berkantkz presents!!");
        }
    }
}
