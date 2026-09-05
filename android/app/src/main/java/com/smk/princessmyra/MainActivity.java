package com.smk.princessmyra;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.JavascriptInterface;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

public class MainActivity extends Activity {
    private WebView web;
    private TextToSpeech tts;
    private boolean ttsReady = false;

    public class TtsBridge {
        @JavascriptInterface
        public void speak(String text, String lang, float pitch, float rate) {
            if (tts == null || !ttsReady) return;
            try {
                tts.stop();
                Locale loc = Locale.forLanguageTag(lang == null ? "en-IN" : lang);
                int r = tts.setLanguage(loc);
                if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) tts.setLanguage(Locale.forLanguageTag("en-IN"));
                tts.setPitch(pitch);
                tts.setSpeechRate(rate);
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "myrush");
            } catch (Exception e) { }
        }
        @JavascriptInterface
        public void stop() { try { if (tts != null) tts.stop(); } catch (Exception e) { } }
        @JavascriptInterface
        public boolean available(String lang) {
            if (tts == null || !ttsReady) return false;
            try {
                int r = tts.isLanguageAvailable(Locale.forLanguageTag(lang));
                return r >= TextToSpeech.LANG_AVAILABLE;
            } catch (Exception e) { return false; }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override public void onInit(int status) { ttsReady = status == TextToSpeech.SUCCESS; }
        });

        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        web.setWebChromeClient(new WebChromeClient());
        web.addJavascriptInterface(new TtsBridge(), "AndroidTTS");
        web.setBackgroundColor(0xFF0B0A2A);
        web.setOverScrollMode(View.OVER_SCROLL_NEVER);
        web.setVerticalScrollBarEnabled(false);
        web.setHorizontalScrollBarEnabled(false);
        web.loadUrl("file:///android_asset/index.html");
        setContentView(web);
        hideBars();
    }

    private void hideBars() {
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideBars();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (tts != null) tts.stop();
        if (web != null) web.onPause();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (web != null) web.onResume();
        hideBars();
    }
}
