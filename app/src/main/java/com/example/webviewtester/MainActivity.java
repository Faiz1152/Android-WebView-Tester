package com.example.webviewtester;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final String INITIAL_URL = "https://example.com";

    private WebView webView;
    private EditText urlBox;
    private ImageButton btnBack;
    private ImageButton btnForward;
    private ImageButton btnReload;
    private ImageButton btnGo;

    // Matches a bare domain like "example.com", "youtube.com", "sub.example.co.uk"
    // (letters/digits/hyphens, one or more dot-separated labels, a 2+ letter TLD),
    // optionally followed by a path/query/port. No scheme, no spaces.
    private static final Pattern DOMAIN_PATTERN = Pattern.compile(
            "^([a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}(:\\d+)?([/?#].*)?$"
    );

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        urlBox = findViewById(R.id.urlBox);
        btnBack = findViewById(R.id.btnBack);
        btnForward = findViewById(R.id.btnForward);
        btnReload = findViewById(R.id.btnReload);
        btnGo = findViewById(R.id.btnGo);

        setupWebView();
        setupToolbar();

        urlBox.setText(INITIAL_URL);
        webView.loadUrl(INITIAL_URL);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setBlockNetworkImage(false);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setDatabaseEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Keep the address box in sync with the actual loaded URL,
                // but avoid clobbering the user while they're actively editing it.
                if (!urlBox.isFocused() && url != null) {
                    urlBox.setText(url);
                }
                updateNavButtonState();
            }

            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                super.doUpdateVisitedHistory(view, url, isReload);
                updateNavButtonState();
            }
        });

        webView.setWebChromeClient(new WebChromeClient());
    }

    private void setupToolbar() {
        btnBack.setOnClickListener(v -> {
            if (webView.canGoBack()) {
                webView.goBack();
            }
        });

        btnForward.setOnClickListener(v -> {
            if (webView.canGoForward()) {
                webView.goForward();
            }
        });

        btnReload.setOnClickListener(v -> webView.reload());

        btnGo.setOnClickListener(v -> submitUrlBox());

        urlBox.setOnEditorActionListener((v, actionId, event) -> {
            boolean isGoAction = actionId == EditorInfo.IME_ACTION_GO
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN);
            if (isGoAction) {
                submitUrlBox();
                return true;
            }
            return false;
        });

        updateNavButtonState();
    }

    private void updateNavButtonState() {
        btnBack.setEnabled(webView.canGoBack());
        btnBack.setAlpha(webView.canGoBack() ? 1.0f : 0.4f);
        btnForward.setEnabled(webView.canGoForward());
        btnForward.setAlpha(webView.canGoForward() ? 1.0f : 0.4f);
    }

    private void submitUrlBox() {
        String input = urlBox.getText().toString().trim();
        if (input.isEmpty()) {
            return;
        }
        String resolved = resolveInput(input);
        webView.loadUrl(resolved);
        urlBox.clearFocus();
    }

    /**
     * Resolves what the user typed into a URL to load, following these rules:
     *  - "https://..." or "http://..." -> load exactly as entered (scheme preserved as-is)
     *  - "www.something.tld" -> "https://www.something.tld" (www preserved, not added)
     *  - "something.tld" (bare domain, no www) -> "https://something.tld" (no www added)
     *  - anything else not recognizable as a URL/domain -> Google search query
     */
    private String resolveInput(String input) {
        // Already has an explicit scheme: load exactly as entered, never rewrite http->https.
        if (input.matches("(?i)^https?://.*")) {
            return input;
        }

        // No scheme, no spaces, and it matches a plausible domain shape -> treat as a URL.
        if (!input.contains(" ") && looksLikeDomain(input)) {
            return "https://" + input;
        }

        // Otherwise, treat it as a search query.
        return "https://www.google.com/search?q=" + urlEncode(input);
    }

    private boolean looksLikeDomain(String input) {
        // Reject anything containing whitespace outright (handled by caller too, kept for safety).
        if (input.contains(" ")) {
            return false;
        }
        // Strip a trailing slash-only case is fine; rely on regex + Android's own host check.
        if (DOMAIN_PATTERN.matcher(input).matches()) {
            return true;
        }
        // Fallback: let Android's own URL heuristics catch edge cases (e.g. IP addresses).
        return Patterns.WEB_URL.matcher(input).matches() && !input.contains(" ");
    }

    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return Uri.encode(value);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
