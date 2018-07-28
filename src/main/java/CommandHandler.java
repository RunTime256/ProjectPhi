import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class CommandHandler
{
    private Map<String, Command> commands;
    private Map<String, Command> hiddenCommands;
    public final int NEW_GUILD_MESSAGE_ID = 1;

    public CommandHandler()
    {
        commands = new HashMap<>();
        hiddenCommands = new HashMap<>();

        //Add all commands to the map
        new ManagerCommands(commands);
        new OwnerCommands(commands);
        new AdminCommands(commands);
        new ModeratorCommands(commands);
        new TestCommands(commands);
        new UtilityCommands(commands);
        new HelpCommands(commands);
        new EventCommands(commands);
        new GameCommands(commands);

        //Commands to be added at a later date that are testable
        new HelpCommands(hiddenCommands);
        new HiddenCommands(hiddenCommands);
        new TwitchCommands(hiddenCommands);
    }

    //Updates playing text when starting up
    @EventSubscriber
    public void handle(ReadyEvent event)
    {
        //event.getClient().changePresence(StatusType.ONLINE, ActivityType.PLAYING, "underwater");
        event.getClient().changePresence(StatusType.ONLINE, ActivityType.PLAYING, "+help");
        //event.getClient().changePresence(StatusType.ONLINE, ActivityType.WATCHING, "from afar");
        //event.getClient().changePresence(StatusType.INVISIBLE);


    }

    //Sends a welcome message upon joining a new server

    /*
    @EventSubscriber
    public void OnGuildCreate(GuildCreateEvent event)
    {
        try
        {
            String sql;
            List<Object> params = new ArrayList<>();
            sql = "SELECT * FROM DiscordDB.Guilds WHERE GuildID = ?";
            params.add(event.getGuild().getLongID());
            PreparedStatement statement = JDBCConnection.getStatement(sql, params);
            ResultSet set = statement.executeQuery();

            if (!set.next())
            {
                //Insert guild into table
                statement.close();
                sql = "INSERT INTO DiscordDB.Guilds VALUES(?)";
                params = new ArrayList<>();
                params.add(event.getGuild().getLongID());
                statement = JDBCConnection.getStatement(sql, params);
                statement.executeUpdate();

                //Send new guild message to guild owner
                sql = "SELECT DiscordDB.EntryDescription FROM MessageEntry WHERE EntryID = ?";
                params = new ArrayList<>();
                params.add(NEW_GUILD_MESSAGE_ID);
                statement = JDBCConnection.getStatement(sql, params);
                set = statement.executeQuery();
                if (set.next())
                {
                    String message = set.getString("EntryDescription");
                    IChannel channel = event.getGuild().getOwner().getOrCreatePMChannel();
                    BotUtils.sendMessage(channel, message);
                }
                statement.close();
            }
            statement.close();
        }
        catch (SQLException e)
        {
            System.out.println("Couldn't process Result Set");
            e.printStackTrace();
            return;
        }
    }
    */


    //Performs a command if the message received triggers one
    @EventSubscriber
    public void OnMessageReceived(MessageReceivedEvent event)
    {
        String message = event.getMessage().getContent();
        String[] args = message.split(" ");
        int numArgs = args.length - 1;

        if (numArgs == -1)
        {
            return;
        }

        //Checks to see if there is a passive command to run
        if (!(args[0].startsWith(BotUtils.BOT_PREFIX) || args[0].startsWith(BotUtils.HIDDEN_PREFIX)))
        {
            //Replicate DMs if mimicking is enabled
            if (event.getGuild() == null && ManagerCommands.isMimicActive() && ManagerCommands.getMimicReceive().getLongID() == event.getChannel().getLongID())
            {
                BotUtils.sendMessage(ManagerCommands.getMimicSend(), event.getMessage().getContent());
            }
            return;
        }

        String comStr = args[0].substring(1);

        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        argsList.remove(0);
        Map<String, Command> map;
        //Determines command map based on prefix
        if (args[0].startsWith(BotUtils.BOT_PREFIX))
        {
            map = commands;
        }
        else if (args[0].startsWith(BotUtils.HIDDEN_PREFIX))
        {
            map = hiddenCommands;
        }
        else
        {
            return;
        }

        //Checks to see if there is a command with the given key
        if (map.containsKey(comStr))
        {
            try
            {
                event.getChannel().setTypingStatus(true);
                map.get(comStr).execute(event, argsList);
            }
            catch (Exception e)
            {
                BotUtils.sendCommandError(event.getChannel());
                System.out.println("\nNew " + e.getClass().getSimpleName() + " at " + BotUtils.formatDate(BotUtils.now().toInstant()) + "\n");
                e.printStackTrace();
            }
            finally
            {
                event.getChannel().setTypingStatus(false);
            }
        }
    }
}