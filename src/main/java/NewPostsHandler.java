import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import mkremins.fanciful.FancyMessage;

public class NewPostsHandler  {

    private static final String cachedFile = "data.txt";
    private static final ConfigHandler config = new ConfigHandler();
    private static final String hash = CraftXen.config().getConfigData("xenapi_secret");
    private static final String apiUrl = "http://craftblock.me/api.php?hash=" + hash;
    private static final String requestPosts = "&action=getPosts&order_by=post_date";
    private static final String requestPost = "&action=getPost&value=";
    private static final CraftXen instance = CraftXen.getInstance();

    public NewPostsHandler() {
        int s = 120;
        Bukkit.getScheduler().runTaskTimerAsynchronously(instance, () -> checkData(), s, s);
    }

    public void checkData() {
        String freshData = callUrl(apiUrl + requestPosts);
        String cachedData = getCached();
        int freshDataLastId = getLastId(freshData);
        int cachedDataLastId = getLastId(cachedData);
        if (!cachedData.isEmpty()) {
            if (freshDataLastId != cachedDataLastId) {
                cacheData(freshData);
                String template = "&a${name} &7posted on thread: &o&e${thread}";
                //todo: we should only have to make one call for json, not multiple
                Map<String, String> data = new HashMap<>();
                data.put("name", getPostData(freshDataLastId, "username"));
                data.put("thread", getPostData(freshDataLastId, "title"));
                String formattedString = StrSubstitutor.replace(template, data);
                String title = ChatColor.translateAlternateColorCodes('&', formattedString);
                String message = stripContent(getPostData(freshDataLastId, "message_html"));
                broadcastFancyPost(title, message, getPostData(freshDataLastId, "absolute_url"));
            }
        } else {
            cacheData(freshData);
        }
    }

    public void broadcastPost(String data) {
        String prefix = "[CB Forums] ";
        instance.getServer().broadcastMessage(prefix + data);
    }

    public void broadcastFancyPost(String title, String message, String link) {
        String prefix = "&7[&4Forums&7]";
        String formattedPrefix = ChatColor.translateAlternateColorCodes('&', prefix);
        new FancyMessage(ChatColor.GRAY + "============ " + formattedPrefix + ChatColor.GRAY +" ============")
                .then("\n\n" + title + "\n").link(link)
                .then(ChatColor.DARK_GRAY + message + "\n\n").style(ChatColor.ITALIC)
                .then(ChatColor.GRAY + "============")
                .then(ChatColor.AQUA + " [View Post] ").link(link)
                .formattedTooltip(new FancyMessage("Click to Open"))
                .then(ChatColor.GRAY + "==========")
                .send(instance  .getServer().getOnlinePlayers());
    }

    public  String stripContent(String content) {
        String a = content.replaceAll("<blockquote>(.*)<\\/blockquote>", "").replaceAll("<[^>]+>", "");
        String b = a.length() > 52 ? a.substring(0, 52) +"..." : a;
        String c = StringEscapeUtils.unescapeHtml(b);
        return c + "...";
    }


    private String callUrl(String requestUrl) {
        System.setProperty("http.agent", "Chrome");
        StringBuilder sb = new StringBuilder();
        try {
            URLConnection urlConn = new URL(requestUrl).openConnection();
            urlConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            urlConn.connect();
            InputStreamReader in = null;

            if (urlConn != null)
                urlConn.setReadTimeout(60*1000);
            if (urlConn != null && urlConn.getInputStream() != null)  {
                in = new InputStreamReader(urlConn.getInputStream(),
                        Charset.defaultCharset());
                BufferedReader bufferedReader = new BufferedReader(in);
                if (bufferedReader != null) {
                    int cp;
                    while ((cp = bufferedReader.read()) != -1) {
                        sb.append((char) cp);
                    }

                    bufferedReader.close();
                }
            }

            in.close();
        } catch (Exception e) {
            throw new RuntimeException("Exception calling URL:" + requestUrl, e);
        }

        return sb.toString();
    }

    private String getCached() {

        File file = new File(cachedFile);
        if (!file.exists()) {
            try {
                file.createNewFile();
                cacheData("{}");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        StringBuilder fileContents = new StringBuilder((int) file.length());
        try (Scanner scanner = new Scanner(file)) {

            String lineSeparator = System.getProperty("line.separator");

            try {
                while (scanner.hasNextLine()) {
                    fileContents.append(scanner.nextLine() + lineSeparator);
                }
                return fileContents.toString();
            } finally {
                scanner.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;

    }

    public void cacheData(String data) {

        File file = new File(cachedFile);

        try (FileOutputStream fop = new FileOutputStream(file)) {

            if (!file.exists()) {
                file.createNewFile();
            }

            byte[] contentInBytes = data.getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getLastId(String data)  {
        JSONObject jsonData;
        int id = 0;
        try {
            jsonData = new JSONObject(data);
            JSONArray posts = jsonData.getJSONArray("posts");
            JSONObject row = posts.getJSONObject(0);
            id = row.getInt("post_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return id;
    }

    private String getPostData(int postId, String key) {
        String data = getPost(postId);
        JSONObject jsonData;
        String content = null;
        try {
            jsonData = new JSONObject(data);
            if (key != null) {
                content = jsonData.getString(key);
            } else {
                content = jsonData.toString();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    public String getPost(int postId) {
        String data = callUrl(apiUrl + requestPost + postId);
        return data;
    }

    public void createTestPost() {
        String requestTestPost = "&action=createPost&grab_as=misskoa&thread_id=15224&message=Test";
        callUrl(apiUrl + requestTestPost);
        broadcastPost("Created Post");
    }

    public String[] getExcludedForums() {
        return config.getConfigData("exclude_forums").split(",");
    }



}
