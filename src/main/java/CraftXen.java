import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;



public class CraftXen extends JavaPlugin {

    /**
     * TODO:
     * Bookmark by id
     */

    private static CraftXen instance;
    private static ConfigHandler config;
    private NewPostsHandler newPostsTask;

    @Override
    public void onEnable() {
        instance = this;
        config = new ConfigHandler();
        newPostsTask = new NewPostsHandler();
    }

    @Override
    public void onDisable() {

    }
    public static ConfigHandler config() {
        return config;
    }
    public static CraftXen getInstance() {
        return instance;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("post")) {
            newPostsTask.createTestPost();
            return true;
        }
        return false;
    }

}
