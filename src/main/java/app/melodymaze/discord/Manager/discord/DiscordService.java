package app.melodymaze.discord.Manager.discord;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.springframework.stereotype.Service;
import org.w3c.dom.Text;

@Service
public class DiscordService {

    public TextChannel createTextChannel(Guild guild, String channelName, Category category) {
        category.createTextChannel(channelName).complete();

        return (TextChannel)category.getChannels().get(category.getChannels().size()-1);
    }

    public void moveChannel(Guild guild, ChannelType channelType, long channelId, Category toCategory) {
        switch (channelType) {
            case TEXT:
                guild.getTextChannelById(channelId).getManager().setParent(toCategory);
                break;

            case VOICE:
                guild.getVoiceChannelById(channelId).getManager().setParent(toCategory);
                break;
        }
    }

}
