package com.aqnichol.cunetusage;

import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * The NubbClient facilitates interaction with the Cornell NUBB website.
 */
public class NubbClient {

    private final int CONNECT_TIMEOUT = 15000;
    private final int READ_TIMEOUT = 10000;

    private CookieManager cookieManager = new CookieManager();
    private String username = null;
    private String password = null;

    public NubbClient() {
    }

    public void setUsername(String user) {
        username = user;
    }

    public void setPassword(String pass) {
        password = pass;
    }

    public boolean authenticate() throws IOException {
        URL url = new URL("https://nubb.cornell.edu/");
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.getHeaderFields();
        URL newURL = connection.getURL();
        connection.disconnect();
        Log.d("NUBB_CLIENT", "newURL is " + newURL);

        // TODO: this.

        return false;
    }

    private String getURL(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        putRequestCookies(connection);

        connection.connect();
        takeResponseCookies(connection);

        try {
            InputStream stream = connection.getInputStream();
            StringWriter writer = new StringWriter();
            IOUtils.copy(stream, writer, StandardCharsets.UTF_8);
            return stream.toString();
        } finally {
            connection.disconnect();
        }
    }

    private void takeResponseCookies(HttpURLConnection connection) {
        List<String> cookiesHeader = connection.getHeaderFields().get("Set-Cookie");
        if (cookiesHeader != null) {
            for (String cookie : cookiesHeader) {
                for (HttpCookie parsedCookie : HttpCookie.parse(cookie))
                cookieManager.getCookieStore().add(null, parsedCookie);
            }
        }
    }

    private void putRequestCookies(HttpURLConnection connection) {
        List<HttpCookie> cookies = cookieManager.getCookieStore().getCookies();
        if (cookies.size() > 0) {
            connection.setRequestProperty("Cookie", TextUtils.join(";", cookies));
        }
    }

}
