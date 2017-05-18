package com.popameeting.gtfs.neo4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by ted on 3/20/2017.
 */
@Service
public class NjTransitGtfsService {

    public static class NjTransitCookie {

        String JSESSIONID;
        String __utmz;
        String __utma;
        String __utmc;
        String __utmb;

    }

    private static final int BUFFER_SIZE = 4096;

    @Value("${njgtfs.login}")
    private String userName;
    @Value("${njgtfs.password}")
    private String password;
    @Value("${neo4j.import.path}")
    private String foldername;

    boolean debug = false;

    private final static Logger log = LoggerFactory.getLogger(NjTransitGtfsService.class);


    public Boolean grabGtfs() {
        if (!downloadtGtfs()) return false;
        if (!unzipFile(foldername)) return false;
        return true;
    }

    public Boolean downloadtGtfs() {
        String urlStr = "https://www.njtransit.com/mt/mt_servlet.srv?hdnPageAction=MTDevResourceDownloadTo&Category=rail";
        NjTransitCookie cookies = login();

        if (cookies == null) {
            return false;
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            log.info("We received an interrupt while getting ready to download the gtfs file", e);
            return false;
        }

        int READ_TIMEOUT = 10000;
        String  POST_METHOD = "GET";
        String data = "cacheType=1";

        String cookiesStr;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            cookiesStr = getCookieStr(cookies);

            connection.setRequestProperty("Cookie", cookiesStr);

            connection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestMethod(POST_METHOD);
            connection.setDoOutput(true);

            int responseCode = connection.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK) {
                log.error("The download link seems to be broken with a response code " + responseCode);
                return null;
            }

            downloadFile(urlStr, "./", connection );


        } catch (Exception e ) {
            e.printStackTrace();
        }

        return true;

    }

    private static boolean unzipFile(String foldername) {

        File newFile = null;

        try {
            String fileZip = foldername + ".zip";
            new File("./rail_data").mkdirs();
            byte[] buffer = new byte[1024];
            ZipInputStream zis = null;

            zis = new ZipInputStream(new FileInputStream(fileZip));

            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                String fileName = zipEntry.getName();
                newFile = new File(foldername + "/" + fileName);
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        } catch (FileNotFoundException e) {
            log.debug("Could not create zip file to unzip", e);
            return false;
        } catch (IOException e) {
            log.debug("There was a problem unzipping the " + newFile + " file to disk", e);
            return false;
        }

        return true;


    }

    private String getCookieStr(NjTransitCookie cookies) throws UnsupportedEncodingException {
        String cookiesStr = new String();

        if (!StringUtils.isEmpty(cookies.JSESSIONID)) {
            cookiesStr = cookiesStr + "JSESSIONID=" + URLEncoder.encode(cookies.JSESSIONID, "UTF-8");
        }

        if (!StringUtils.isEmpty(cookies.__utmz)) {
            cookiesStr = cookiesStr + "; __utmz=" + cookies.__utmz;
        }

        if (!StringUtils.isEmpty(cookies.__utma)) {
            cookiesStr = cookiesStr + "; __utma=" + cookies.__utma;
        }

        if (!StringUtils.isEmpty(cookies.__utmc)) {
            cookiesStr = cookiesStr + "; __utmc=" + cookies.__utmc;
        }

        if (!StringUtils.isEmpty(cookies.__utmb)) {
            cookiesStr = cookiesStr + "; __utmb=" + cookies.__utmb;
        }
        return cookiesStr;
    }

    private NjTransitCookie getLoginPage() throws IOException {
        String urlStr = "https://www.njtransit.com/mt/mt_servlet.srv?hdnPageAction=MTDevLoginTo";
        NjTransitCookie result = new NjTransitCookie();
        int READ_TIMEOUT = 10000;
        String  POST_METHOD = "GET";

        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection)
                url.openConnection();

        connection.setReadTimeout(READ_TIMEOUT);
        connection.setRequestMethod(POST_METHOD);
        connection.setDoOutput(true);

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            log.error("Login page failed to load with response code " + responseCode);
            return null;
        }

        result.JSESSIONID = getCookie(connection, "JSESSIONID");
        result.__utmz = getCookie(connection, "__utmz");
        result.__utma = getCookie(connection, "__utma");
        result.__utmc = getCookie(connection, "__utmc");
        result.__utmb = getCookie(connection, "__utmb");

        return result;
    }

    private NjTransitCookie login() {
        String urlStr = "https://www.njtransit.com/mt/mt_servlet.srv?hdnPageAction=MTDevLoginSubmitTo";
        int READ_TIMEOUT = 10000;
        String  POST_METHOD = "POST";
        //String jSessionId = "";

        NjTransitCookie result = new NjTransitCookie();

        String data = "userName="+userName+"&password="+ password;

        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection)
                    url.openConnection();

            NjTransitCookie cookies = getLoginPage();

            if (cookies == null) {
                return null;
            }

            String cookiesStr = getCookieStr(cookies);

            connection.setRequestProperty("Cookie", cookiesStr);
            connection.setRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            //connection.setRequestProperty("Accept-Encoding","gzip, deflate, br");
            connection.setRequestProperty("Accept-Language","en-US,en;q=0.8");
            connection.setRequestProperty("Cache-Control","max-age=0");
            connection.setRequestProperty("Connection","keep-alive");
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            connection.setRequestProperty("Host","www.njtransit.com");
            connection.setRequestProperty("Origin","https://www.njtransit.com");
            connection.setRequestProperty("Upgrade-Insecure-Requests","1");
            connection.setRequestProperty("Referer", "https://www.njtransit.com/mt/mt_servlet.srv?hdnPageAction=MTDevLoginTo");
            connection.setRequestProperty("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");

            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestMethod(POST_METHOD);
            connection.setDoOutput(true);

            OutputStream os = new
                    BufferedOutputStream(connection.getOutputStream());
            os.write(data.getBytes());
            os.flush();

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                log.error("Login failed with response code " + responseCode);
                return null;
            }

            result.JSESSIONID = getCookie(connection, "JSESSIONID");
            result.__utmz = getCookie(connection, "__utmz");
            result.__utma = getCookie(connection, "__utma");
            result.__utmc = getCookie(connection, "__utmc");
            result.__utmb = getCookie(connection, "__utmb");

            if (debug) {
                downloadFile(urlStr, "./", connection);
            }

        } catch (Exception e ) {
            e.printStackTrace();
        }
        return result;
    }

    private String getCookie(HttpURLConnection conn, String cookieName) {
        Map<String, List<String>> headerFields = conn.getHeaderFields();

        Set<String> headerFieldsSet = headerFields.keySet();
        Iterator<String> hearerFieldsIter = headerFieldsSet.iterator();

        String result = "";

        while (hearerFieldsIter.hasNext()) {

            String headerFieldKey = hearerFieldsIter.next();

            if ("Set-Cookie".equalsIgnoreCase(headerFieldKey)) {

                List<String> headerFieldValue = headerFields.get(headerFieldKey);

                for (String headerValue : headerFieldValue) {

                    System.out.println("Cookie Found...");

                    String[] fields = headerValue.split(";\\s*");

                    String cookieValue = fields[0];

                    if (cookieValue.trim().startsWith(cookieName)) result = cookieValue.trim().split("=")[1].trim();

                    String expires = null;
                    String path = null;
                    String domain = null;
                    boolean secure = false;

                    // Parse each field
                    for (int j = 1; j < fields.length; j++) {
                        if ("secure".equalsIgnoreCase(fields[j])) {
                            secure = true;
                        }
                        else if (fields[j].indexOf('=') > 0) {
                            String[] f = fields[j].split("=");
                            if ("expires".equalsIgnoreCase(f[0])) {
                                expires = f[1];
                            }
                            else if ("domain".equalsIgnoreCase(f[0])) {
                                domain = f[1];
                            }
                            else if ("path".equalsIgnoreCase(f[0])) {
                                path = f[1];
                            }
                        }

                    }

                }

            }

        }
        return result;
    }


    public static void downloadFile(String fileURL, String saveDir, HttpURLConnection httpConn )
            throws IOException {
        //URL url = new URL(fileURL);
        //HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = "";
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            int contentLength = httpConn.getContentLength();

            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 9,
                            disposition.length() - 1);
                }
            } else {
                // extracts file name from URL
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                        fileURL.length());
            }

            System.out.println("Content-Type = " + contentType);
            System.out.println("Content-Disposition = " + disposition);
            System.out.println("Content-Length = " + contentLength);
            System.out.println("fileName = " + fileName);

            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            String saveFilePath = saveDir + File.separator + fileName;

            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);

            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            log.info("File downloaded");
        } else {
            log.error("No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
    }
}
