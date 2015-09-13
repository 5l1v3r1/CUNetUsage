package com.aqnichol.cunetusage;

import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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

    /**
     * Authenticate as the previously-specified user.
     * @return true if the authentication succeeded, false if the user's credentials did not work.
     * @throws IOException if any network requests fail or if any parsing errors occur.
     */
    public boolean authenticate() throws IOException {
        URL postURL = getAuthenticationPostURL();

        HttpURLConnection conn = makeConnection(postURL);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String postData = "netid=" + URLEncoder.encode(username, "UTF-8") + "&password=" +
                URLEncoder.encode(password, "UTF-8") + "&Submit=Login";
        conn.setRequestProperty("Content-Length", "" + postData.getBytes().length);

        try {
            IOUtils.copy(new StringReader(postData), conn.getOutputStream(),
                    StandardCharsets.UTF_8);
            takeResponseCookies(conn);

            if (conn.getResponseCode() != 303) {
                return false;
            } else {
                String location = conn.getHeaderFields().get("Location").get(0);
                getURL(new URL(location));
                return true;
            }
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Get the URL to which authentication credentials should be POSTed.
     * @return the full URL to which the authentication form should be posted.
     * @throws IOException if any requests fail or if the login form cannot be parsed.
     */
    private URL getAuthenticationPostURL() throws IOException {
        String pageContent = getURL(new URL("https://nubb.cornell.edu/"));

        String actionPreamble = "method=\"post\" action=\"";
        int startIndex = pageContent.indexOf(actionPreamble);
        if (startIndex < 0) {
            throw new IOException("form action not found");
        }
        startIndex += actionPreamble.length();
        int endIndex = pageContent.indexOf("\"", startIndex);
        if (endIndex < 0) {
            throw new IOException("form action not found");
        }

        String actionPath = pageContent.substring(startIndex, endIndex);
        actionPath = actionPath.replaceAll(" ", "%20");
        return new URL("https://web2.login.cornell.edu/" + actionPath);
    }

    private String getURL(URL url) throws IOException {
        HttpURLConnection connection = makeConnection(url);
        connection.connect();
        takeResponseCookies(connection);

        try {
            StringWriter writer = new StringWriter();
            IOUtils.copy(connection.getInputStream(), writer, StandardCharsets.UTF_8);
            return writer.toString();
        } finally {
            connection.disconnect();
        }
    }

    private HttpURLConnection makeConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        putRequestCookies(connection);
        return connection;
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
