package com.example.islamic.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.res.Configuration;

import com.example.islamic.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

public class SurahDetail extends AppCompatActivity {
    TextView surah_name;
    WebView quranWebView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surah_detail);

        surah_name = findViewById(R.id.surah_name);
        quranWebView = findViewById(R.id.quran_webview);

        quranWebView.getSettings().setJavaScriptEnabled(false);
        quranWebView.getSettings().setDefaultTextEncodingName("utf-8");
        quranWebView.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkTheme = currentNightMode == Configuration.UI_MODE_NIGHT_YES;

        String bodyBgColor;
        String bodyTextColor;
        String ayaNumberBgColor;
        String ayaNumberTextColor;
        String bismillahTextColor;

        if (isDarkTheme) {
            bodyBgColor = "#121212";
            bodyTextColor = "#F0F0F0";
            ayaNumberBgColor = "#4CAF50";
            ayaNumberTextColor = "#FFFFFF";
            bismillahTextColor = "#FFFFFF";
        } else {
            bodyBgColor = "#f5f5f5";
            bodyTextColor = "#212121";
            ayaNumberBgColor = "#6a0000";
            ayaNumberTextColor = "#FFFFFF";
            bismillahTextColor = "#212121";
        }


        Bundle bundle = getIntent().getExtras();
        String name = bundle.getString("name");
        int targetIndex = bundle.getInt("index");
        surah_name.setText(name);

        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<html>")
                .append("<head>")
                .append("<style type=\"text/css\">")

                .append("@font-face { font-family: 'UthmanicHafs'; src: url('file:///android_asset/fonts/UthmanicHafs_v20.ttf'); }")

                .append("body { ")
                .append("text-align: justify; ")
                .append("direction: rtl; ")
                .append("font-family: 'UthmanicHafs'; ")
                .append("font-size: 25px; ")
                .append("line-height: 2.3; ")
                .append("padding: 10px; ")
                .append("font-weight: normal; ")
                .append("background-color: ").append(bodyBgColor).append("; ")
                .append("color: ").append(bodyTextColor).append("; }")


                .append(".aya_number { ")
                .append("font-family: sans-serif; ")
                .append("font-size: 16px; ")
                .append("background-color: ").append(ayaNumberBgColor).append("; ")
                .append("color: ").append(ayaNumberTextColor).append("; ")
                .append("border-radius: 50%; ")
                .append("padding: 4px; ")
                .append("margin: 0 3px; ")
                .append("vertical-align: middle; ")
                .append("display: inline-flex; ")
                .append("align-items: center; ")
                .append("justify-content: center; ")
                .append("min-width: 30px; ")
                .append("height: 30px; ")
                .append("line-height: 1; }")


                .append(".bismillah { ")
                .append("font-family: 'UthmanicHafs'; ")
                .append("font-size: 38px; ")
                .append("text-align: center; ")
                .append("margin-top: 25px; ")
                .append("margin-bottom: 30px; ")
                .append("display: block; ")
                .append("font-weight: bold; ")
                .append("color: ").append(bismillahTextColor).append("; }")
                .append("</style>")
                .append("</head>")
                .append("<body>");

        try {
            JSONObject jsonObject = new JSONObject(JsonDataFromAsset()); //
            JSONArray jsonArray = jsonObject.getJSONArray("surah"); //

            for (int i = 0; i < jsonArray.length(); i++) { //
                JSONObject surahData = jsonArray.getJSONObject(i); //
                if (targetIndex == surahData.getInt("index")) { //
                    JSONArray ayaArray = surahData.getJSONArray("aya"); //


                    if (targetIndex == 1) {
                        htmlBuilder.append("<p class=\"bismillah\">بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ</p>"); //
                    } else if (targetIndex != 9 && ayaArray.length() > 0) {
                        JSONObject firstAya = ayaArray.getJSONObject(0);
                        if (firstAya.has("bismillah")) {
                            String bismillahText = firstAya.getString("bismillah");
                            if (!bismillahText.isEmpty()) {
                                htmlBuilder.append("<p class=\"bismillah\">")
                                        .append(bismillahText)
                                        .append("</p>");
                            }
                        }
                    }


                    for (int j = 0; j < ayaArray.length(); j++) {
                        JSONObject ayaData = ayaArray.getJSONObject(j);
                        String ayaText = ayaData.getString("text");
                        int ayaIndex = ayaData.getInt("index");
                        if (targetIndex == 1 && ayaIndex == 1 && ayaText.trim().equals("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ")) {
                            continue;
                        }

                        htmlBuilder.append(ayaText)
                                .append("<span class=\"aya_number\">")
                                .append(ayaIndex)
                                .append("</span> ");
                    }
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            htmlBuilder.append("<p>حدث خطأ في تحميل المحتوى: " + e.getMessage() + "</p>");
        }

        htmlBuilder.append("</body></html>");
        quranWebView.loadDataWithBaseURL("file:///android_asset/", htmlBuilder.toString(), "text/html", "utf-8", null);
    }

    private String JsonDataFromAsset() {
        String json = null;
        try {
            InputStream inputStream = getAssets().open("QuranDetails.json");
            int sizeOfFile = inputStream.available();
            byte[] bufferData = new byte[sizeOfFile];
            inputStream.read(bufferData);
            inputStream.close();
            json = new String(bufferData, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }
}