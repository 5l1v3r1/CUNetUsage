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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The NubbClient facilitates interaction with the Cornell NUBB website.
 */
public class NubbClient {

    public static class GeneralMonthInfo {
        public long totalUsageMB;
        public long freeUsageMB;
        public long billableUsageMB;
        public String billingRate;
        public String totalCharge;
    }

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
                // Make the next request to get the cookie from it.
                String location = conn.getHeaderFields().get("Location").get(0);
                getURL(new URL(location), false);
                return true;
            }
        } finally {
            conn.disconnect();
        }
    }

    /**
     * fetchGeneralMonthInfo gets the GeneralMonthInfo for the current month.
     * @returns an instance of GeneralMonthInfo which is completely owned by the caller. It will not
     * be modified or referenced by this NubbClient.
     * @throws IOException if the info could not be fetched or parsed.
     */
    public GeneralMonthInfo fetchGeneralMonthInfo() throws IOException {
        String page = getURL(new URL("https://nubb.cornell.edu/"));
        try {
            return parseMonthInfo(page);
        } catch (Exception e) {
            Log.v("NubbClient", "got exception " + e);
            throw new IOException("could not parse month info");
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

    private GeneralMonthInfo parseMonthInfo(String infoPage) throws IllegalStateException,
            NumberFormatException {
        GeneralMonthInfo info = new GeneralMonthInfo();

        String[] patternStrings = new String[]{
                "<td><b>Total Usage \\(MB\\):<\\/b> <\\/td><td>(.*?)<\\/td>",
                "FREE Monthly Usage \\(MB\\):<\\/b> <\\/td><td>(.*?)<\\/td>",
                "<td><b>Billable Usage \\(MB\\):<\\/b> <\\/td><td>(.*?)<\\/td>",
                "<td><b>Billing Rate:<\\/b> <\\/td><td>(.*?)<\\/td>",
                "Total Charge:<\\/b> <\\/td><td><b>(.*?)<\\/b><\\/td>"
        };

        String[] extracted = new String[5];
        for (int i = 0; i < 5; ++i) {
            Pattern p = Pattern.compile(patternStrings[i]);
            Matcher m = p.matcher(infoPage);
            m.find();
            extracted[i] = m.group(1);
        }

        info.totalUsageMB = Integer.parseInt(extracted[0].replaceAll(",", ""));
        info.freeUsageMB = Integer.parseInt(extracted[1].replaceAll(",", ""));
        info.billableUsageMB = Integer.parseInt(extracted[2].replaceAll(",", ""));
        info.billingRate = extracted[3];
        info.totalCharge = extracted[4];

        return info;
    }

    private String getURL(URL url) throws IOException {
        return getURL(url, true);
    }

    private String getURL(URL url, boolean followRedirects) throws IOException {
        HttpURLConnection connection = makeConnection(url);
        if (!followRedirects) {
            connection.setInstanceFollowRedirects(false);
        }
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
