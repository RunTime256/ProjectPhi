import sx.blah.discord.handle.obj.IChannel;

import java.util.Map;

public class OwnerCommands
{
    public static final String PERMS = "owner_commands.json";
    private String comName = Command.OWNER;

    public OwnerCommands(Map<String, Command> map)
    {
        map.put("shutdown", new Command("shutdown", "Shuts down the bot", BotUtils.BOT_PREFIX + "shutdown", AccessLevel.OWNER, (event, args) ->
        {
            IChannel channel = event.getChannel();
            if (Command.hasChannelPerms(comName, event.getGuild(), channel.getLongID()))
            {
                BotUtils.sendMessage(channel, "Shutting down...");
                event.getClient().logout();
            }
        }));
    }
}