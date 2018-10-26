package qmpark.tntjoy.com.tucaowebview;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class MainActivity extends AppCompatActivity {

    private WebView h5WebView;
    ProgressBar webProgressBar;
    public static String webUrl = "https://support.qq.com/product/1221"; //意见反馈 ,1221是产品id，

    public ValueCallback<Uri[]> mUploadMessageForAndroid5;
    public ValueCallback<Uri> mUploadMessage;
    public final static int FILE_CHOOSER_RESULT_CODE_FOR_ANDROID_5 = 2;
    private final static int FILE_CHOOSER_RESULT_CODE = 1;// 表单的结果回调
    private Uri imageUri;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        h5WebView = findViewById(R.id.wv_main);
        webProgressBar = findViewById(R.id.webProgressBar);

        initWeb();

    }

    private void initWeb(){
        WebSettings mWebSettings = h5WebView.getSettings();
        mWebSettings.setJavaScriptEnabled(true); //允许加载javaScript
        mWebSettings.setSupportZoom(true);       //是否允许缩放
        mWebSettings.setUseWideViewPort(true);   //设置加载进来的页面自适应手机屏幕
        mWebSettings.setLoadWithOverviewMode(true);
        mWebSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);

       // h5WebView.loadUrl(webUrl); //不传参数
        String headimgurl  = "https://oss.tnt-joy.com/bunny/wx-static/login_avatar_icon.png";
        String openid = "userMobile"; // 用户的openid
        String nickname = "userName"; // 用户的nickname
        String clientInfo = "clientInfo"; // 自定义的一些信息

        /* 准备post参数 */
        String postData = "nickname=" + nickname + "&avatar="+headimgurl+ "&openid=" + openid+ "&clientInfo=" + clientInfo;
        h5WebView.postUrl(webUrl, postData.getBytes()); //携带用户信息的


        //H5加载链接监听
        h5WebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent intent = null;
                //根据拦截的url来判断是否拦截跳转
                if (url.contains("test")) {
                  //  intent = new Intent(getContext(), Test.class);
                } else if (url.contains("returnBackController")) {
                    //...
                }else if (url.contains("....")) {
                    h5WebView.loadUrl("javascript:reload()");
                }else {
                    h5WebView.loadUrl(webUrl);
                }
                if (intent != null){
                    startActivity(intent);
                }
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            //    webProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
//                if (webProgressBar != null){
//                    webProgressBar.setVisibility(View.GONE);
//                }
            }
        });


        //H5界面加载进度监听
        h5WebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {

                if (webProgressBar != null){
                    if (newProgress > 95){
                        webProgressBar.setVisibility(View.GONE);
                    }else if (newProgress < 95 && webProgressBar.getVisibility() == View.GONE){
                        webProgressBar.setVisibility(View.VISIBLE);
                    }
                    webProgressBar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }

            // For Android < 5.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                openFileChooserImpl(uploadMsg);
            }

            // For Android => 5.0
            public boolean onShowFileChooser (WebView webView, ValueCallback<Uri[]> uploadMsg,
                                              WebChromeClient.FileChooserParams fileChooserParams) {
                onenFileChooseImpleForAndroid(uploadMsg);
                return true;
            }


        });

        /**
         * 监听手机返回按键，点击返回H5就返回上一级
         */
        h5WebView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((keyCode == KeyEvent.KEYCODE_BACK) && h5WebView.canGoBack()) {
                    h5WebView.goBack();
                    return true;
                }
                return false;
            }
        });




    }

    /**
     * android 5.0 以下开启图片选择（原生）
     *
     * 可以自己改图片选择框架。
     */
    private void openFileChooserImpl(ValueCallback<Uri> uploadMsg) {
        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "File Chooser"), FILE_CHOOSER_RESULT_CODE);
    }

    /**
     * android 5.0(含) 以上开启图片选择（原生）
     *
     * 可以自己改图片选择框架。
     */
    private void onenFileChooseImpleForAndroid(ValueCallback<Uri[]> filePathCallback) {
        mUploadMessageForAndroid5 = filePathCallback;
        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("image/*");

        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");

        startActivityForResult(chooserIntent, FILE_CHOOSER_RESULT_CODE_FOR_ANDROID_5);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode,Intent intent) {
        Uri result = (intent == null || resultCode != RESULT_OK) ? null: intent.getData();
        switch (requestCode){
            case FILE_CHOOSER_RESULT_CODE:  //android 5.0以下 选择图片回调

                if (null == mUploadMessage)
                    return;
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;

                break;

            case FILE_CHOOSER_RESULT_CODE_FOR_ANDROID_5:  //android 5.0(含) 以上 选择图片回调

                if (null == mUploadMessageForAndroid5)
                    return;
                if (result != null) {
                    mUploadMessageForAndroid5.onReceiveValue(new Uri[]{result});
                } else {
                    mUploadMessageForAndroid5.onReceiveValue(new Uri[]{});
                }
                mUploadMessageForAndroid5 = null;

                break;
        }
    }





}
