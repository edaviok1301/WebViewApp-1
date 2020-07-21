package webView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class echoes a string called from JavaScript.
 */
public class WebView extends CordovaPlugin {


    protected static final String LOG_TAG = "InAppBrowser";
    private static final String EXIT_EVENT = "exit";
    private static final String MESSAGE_EVENT = "message";

    private Dialog dialog;
    private android.webkit.WebView inAppWebView;
    private CallbackContext callbackContext;
    private boolean showLocationBar = true;
    private boolean showZoomControls = true;
    private boolean openWindowHidden = false;
    private boolean clearAllCache = false;
    private boolean clearSessionCache = false;
    private boolean mediaPlaybackRequiresUserGesture = false;
    private boolean useWideViewPort = true;
    private ValueCallback<Uri> mUploadCallback;
    private ValueCallback<Uri[]> mUploadCallbackLollipop;
    private final static int FILECHOOSER_REQUESTCODE = 1;
    private final static int FILECHOOSER_REQUESTCODE_LOLLIPOP = 2;
    private String closeButtonCaption = "";
    private String closeButtonColor = "";
    private boolean leftToRight = false;
    private int toolbarColor = Color.parseColor("#00B4E6");//android.graphics.Color.BLUE;
    private boolean hideNavigationButtons = false;
    private boolean showFooter = false;
    private String footerColor = "";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            //String message = args.getString(0);
            this.coolMethod(args, callbackContext);
            return true;
        }
        return false;
    }

    private void coolMethod(JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (args != null && args.length() > 0) {
            String url = args.getJSONArray(0).getString(0);
            String headers= args.getJSONArray(0).getString(1);
            showWebPage(url,headers);
            callbackContext.success("");

        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private Map<String, String> parseHeaders(String optString) throws JSONException {
        String headersString = optString;
        Map<String,String> mapHeaders = new HashMap<>();
        if(optString == null){
            return null;}
        else {
            //optString = "{'TokenApp':'105DAA873E24530197FECEA029F9F950E57B6C126658FBBB10E4C45F219FD1BC93DC1BA82F87E8D33E903F0694B2B1F98E248B78E480264F8F9561E03C5A1FCD1011252ED307B90727644A2B41528DCFBC9447E5FED078E000E788DBE88F170B16F4EBE1F02FC4EE3E0058687C1043D39D5A7E6D8863C258'}";
            JSONObject headersJSON = new JSONObject(headersString);
            Iterator x = headersJSON.keys();
            while(x.hasNext()){
                String key = (String) x.next();
                mapHeaders.put(key,headersJSON.getString(key));
            }
            return mapHeaders;
        }
    }

    public String showWebPage(final String url, final String headersString) {
        // Determine if we should hide the location bar.
        showLocationBar = true;
        showZoomControls = true;
        openWindowHidden = false;
        mediaPlaybackRequiresUserGesture = false;

        final CordovaWebView thatWebView = this.webView;

        // Create dialog in new thread
        Runnable runnable = new Runnable() {
            /**
             * Convert our DIP units to Pixels
             *
             * @return int
             */
            private int dpToPixels(int dipValue) {
                int value = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP,
                        (float) dipValue,
                        cordova.getActivity().getResources().getDisplayMetrics()
                );

                return value;
            }

            private View createCloseButton(int id) {
                View _close;
                Resources activityRes = cordova.getActivity().getResources();

                if (closeButtonCaption != "") {
                    // Use TextView for text
                    TextView close = new TextView(cordova.getActivity());
                    close.setText(closeButtonCaption);
                    close.setTextSize(20);
                    if (closeButtonColor != "") close.setTextColor(android.graphics.Color.parseColor(closeButtonColor));
                    close.setGravity(android.view.Gravity.CENTER_VERTICAL);
                    close.setPadding(this.dpToPixels(10), 0, this.dpToPixels(10), 0);
                    _close = close;
                }
                else {
                    ImageButton close = new ImageButton(cordova.getActivity());
                    int closeResId = activityRes.getIdentifier("ic_action_remove", "drawable", cordova.getActivity().getPackageName());
                    Drawable closeIcon = activityRes.getDrawable(closeResId);
                    if (closeButtonColor != "") close.setColorFilter(android.graphics.Color.parseColor(closeButtonColor));
                    close.setImageDrawable(closeIcon);
                    close.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    if (Build.VERSION.SDK_INT >= 16)
                        close.getAdjustViewBounds();

                    _close = close;
                }

                RelativeLayout.LayoutParams closeLayoutParams = new RelativeLayout.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.MATCH_PARENT);
                if (leftToRight) closeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                else closeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                _close.setLayoutParams(closeLayoutParams);

                if (Build.VERSION.SDK_INT >= 16)
                    _close.setBackground(null);
                else
                    _close.setBackgroundDrawable(null);

                _close.setContentDescription("Close Button");
                _close.setId(Integer.valueOf(id));
                _close.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        closeDialog();
                    }
                });

                return _close;
            }

            @SuppressLint("NewApi")
            public void run() {

                // CB-6702 InAppBrowser hangs when opening more than one instance
                if (dialog != null) {
                    dialog.dismiss();
                };

                // Let's create the main dialog
                dialog= new Dialog(cordova.getContext(),android.R.style.Theme_NoTitleBar);
                dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                dialog.setCancelable(true);
                //dialog.setInAppBroswer(getInAppBrowser());

                // Main container layout
                LinearLayout main = new LinearLayout(cordova.getActivity());
                main.setOrientation(LinearLayout.VERTICAL);

                // Toolbar layout
                RelativeLayout toolbar = new RelativeLayout(cordova.getActivity());
                //Please, no more black!
                toolbar.setBackgroundColor(toolbarColor);
                toolbar.setLayoutParams(new RelativeLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, this.dpToPixels(44)));
                toolbar.setPadding(this.dpToPixels(2), this.dpToPixels(2), this.dpToPixels(2), this.dpToPixels(2));
                if (leftToRight) {
                    toolbar.setHorizontalGravity(Gravity.LEFT);
                } else {
                    toolbar.setHorizontalGravity(Gravity.RIGHT);
                }
                toolbar.setVerticalGravity(Gravity.TOP);

                // Action Button Container layout
                RelativeLayout actionButtonContainer = new RelativeLayout(cordova.getActivity());
                RelativeLayout.LayoutParams actionButtonLayoutParams = new RelativeLayout.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
                if (leftToRight) actionButtonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                else actionButtonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                actionButtonContainer.setLayoutParams(actionButtonLayoutParams);
                actionButtonContainer.setHorizontalGravity(Gravity.LEFT);
                actionButtonContainer.setVerticalGravity(Gravity.CENTER_VERTICAL);
                actionButtonContainer.setId(leftToRight ? Integer.valueOf(5) : Integer.valueOf(1));

                // Back button
                RelativeLayout.LayoutParams backLayoutParams = new RelativeLayout.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.MATCH_PARENT);
                backLayoutParams.addRule(RelativeLayout.ALIGN_LEFT);

                Resources activityRes = cordova.getActivity().getResources();
                //int backResId = activityRes.getIdentifier("ic_action_previous_item", "drawable", cordova.getActivity().getPackageName());
                //Drawable backIcon = activityRes.getDrawable(backResId);

                // Forward button
                RelativeLayout.LayoutParams forwardLayoutParams = new RelativeLayout.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.MATCH_PARENT);
                forwardLayoutParams.addRule(RelativeLayout.RIGHT_OF, 2);
                //int fwdResId = activityRes.getIdentifier("ic_action_next_item", "drawable", cordova.getActivity().getPackageName());
                //Drawable fwdIcon = activityRes.getDrawable(fwdResId);


                // Edit Text Box
                RelativeLayout.LayoutParams textLayoutParams = new RelativeLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                textLayoutParams.addRule(RelativeLayout.RIGHT_OF, 1);
                textLayoutParams.addRule(RelativeLayout.LEFT_OF, 5);


                // Header Close/Done button
                int closeButtonId = leftToRight ? 1 : 5;
                View close = createCloseButton(closeButtonId);
                toolbar.addView(close);

                // Footer
                RelativeLayout footer = new RelativeLayout(cordova.getActivity());
                int _footerColor;
                if(footerColor != "") {
                    _footerColor = Color.parseColor(footerColor);
                } else {
                    _footerColor = android.graphics.Color.LTGRAY;
                }
                footer.setBackgroundColor(_footerColor);
                RelativeLayout.LayoutParams footerLayout = new RelativeLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, this.dpToPixels(44));
                footerLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                footer.setLayoutParams(footerLayout);
                if (closeButtonCaption != "") footer.setPadding(this.dpToPixels(8), this.dpToPixels(8), this.dpToPixels(8), this.dpToPixels(8));
                footer.setHorizontalGravity(Gravity.LEFT);
                footer.setVerticalGravity(Gravity.BOTTOM);

                View footerClose = createCloseButton(7);
                footer.addView(footerClose);


                // WebView
                inAppWebView = new android.webkit.WebView(cordova.getActivity());
                inAppWebView.setLayoutParams(new LinearLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT));
                inAppWebView.setId(Integer.valueOf(6));
                // File Chooser Implemented ChromeClient
                //currentClient = new InAppBrowser.InAppBrowserClient(thatWebView, edittext, beforeload);
                //inAppWebView.setWebViewClient(currentClient);
                WebSettings settings = inAppWebView.getSettings();
                settings.setJavaScriptEnabled(true);
                settings.setJavaScriptCanOpenWindowsAutomatically(true);
                settings.setBuiltInZoomControls(showZoomControls);
                settings.setPluginState(android.webkit.WebSettings.PluginState.ON);

                // Add postMessage interface
                class JsObject {
                    @JavascriptInterface
                    public void postMessage(String data) {
                        try {
                            JSONObject obj = new JSONObject();
                            obj.put("type", MESSAGE_EVENT);
                            obj.put("data", new JSONObject(data));
                            //sendUpdate(obj, true);
                        } catch (JSONException ex) {
                            LOG.e(LOG_TAG, "data object passed to postMessage has caused a JSON error.");
                        }
                    }
                }

                if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    settings.setMediaPlaybackRequiresUserGesture(mediaPlaybackRequiresUserGesture);
                    inAppWebView.addJavascriptInterface(new JsObject(), "cordova_iab");
                }

                String overrideUserAgent = preferences.getString("OverrideUserAgent", null);
                String appendUserAgent = preferences.getString("AppendUserAgent", null);

                if (overrideUserAgent != null) {
                    settings.setUserAgentString(overrideUserAgent);
                }
                if (appendUserAgent != null) {
                    settings.setUserAgentString(settings.getUserAgentString() + appendUserAgent);
                }

                //Toggle whether this is enabled or not!
                Bundle appSettings = cordova.getActivity().getIntent().getExtras();
                boolean enableDatabase = appSettings == null ? true : appSettings.getBoolean("InAppBrowserStorageEnabled", true);
                if (enableDatabase) {
                    String databasePath = cordova.getActivity().getApplicationContext().getDir("inAppBrowserDB", Context.MODE_PRIVATE).getPath();
                    settings.setDatabasePath(databasePath);
                    settings.setDatabaseEnabled(true);
                }
                settings.setDomStorageEnabled(true);

                if (clearAllCache) {
                    CookieManager.getInstance().removeAllCookie();
                } else if (clearSessionCache) {
                    CookieManager.getInstance().removeSessionCookie();
                }

                // Enable Thirdparty Cookies on >=Android 5.0 device
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    CookieManager.getInstance().setAcceptThirdPartyCookies(inAppWebView,true);
                }


                Map<String, String> headerURL = new HashMap<>();
                try {
                    headerURL = parseHeaders(headersString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                inAppWebView.loadUrl(url,headerURL);
                inAppWebView.setId(Integer.valueOf(6));
                inAppWebView.getSettings().setLoadWithOverviewMode(true);
                inAppWebView.getSettings().setUseWideViewPort(useWideViewPort);
                inAppWebView.requestFocus();
                inAppWebView.requestFocusFromTouch();

                // Add the back and forward buttons to our action button container layout

                // Add the views to our toolbar if they haven't been disabled
                if (!hideNavigationButtons) toolbar.addView(actionButtonContainer);

                // Don't add the toolbar if its been disabled
                //if (getShowLocationBar()) {
                    // Add our toolbar to our main view/layout
                    main.addView(toolbar);
                //}

                // Add our webview to our main view/layout
                RelativeLayout webViewLayout = new RelativeLayout(cordova.getActivity());
                webViewLayout.addView(inAppWebView);
                main.addView(webViewLayout);

                // Don't add the footer unless it's been enabled
                if (showFooter) {
                    webViewLayout.addView(footer);
                }

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(dialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;

                if (dialog != null) {
                    dialog.setContentView(main);
                    dialog.show();
                    dialog.getWindow().setAttributes(lp);
                }
                // the goal of openhidden is to load the url and not display it
                // Show() needs to be called to cause the URL to be loaded
                if (openWindowHidden && dialog != null) {
                    dialog.hide();
                }
            }
        };
        this.cordova.getActivity().runOnUiThread(runnable);
        return "";
    }

    public void closeDialog() {
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final android.webkit.WebView childView = inAppWebView;
                // The JS protects against multiple calls, so this should happen only when
                // closeDialog() is called by other native code.
                if (childView == null) {
                    return;
                }

                childView.setWebViewClient(new WebViewClient() {
                    // NB: wait for about:blank before dismissing
                    public void onPageFinished(android.webkit.WebView view, String url) {
                        if (dialog != null) {
                            dialog.dismiss();
                            dialog = null;
                        }
                    }
                });
                // NB: From SDK 19: "If you call methods on WebView from any thread
                // other than your app's UI thread, it can cause unexpected results."
                // http://developer.android.com/guide/webapps/migrating.html#Threads
                childView.loadUrl("about:blank");

                try {
                    JSONObject obj = new JSONObject();
                    obj.put("type", EXIT_EVENT);
                    //sendUpdate(obj, false);
                } catch (JSONException ex) {
                    LOG.d(LOG_TAG, "Should never happen");
                }
            }
        });
    }
}