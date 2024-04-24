package app.melodymaze.discord.Manager.discord;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DiscordService {

    public TextChannel createTextChannel(String channelName, Category category) {
        category.createTextChannel(channelName).complete();

        return (TextChannel)category.getChannels().getLast();
    }

    public void moveChannel(Guild guild, ChannelType channelType, long channelId, Category toCategory) {
        switch (channelType) {
            case TEXT:
                Objects.requireNonNull(guild.getTextChannelById(channelId)).getManager().setParent(toCategory);
                break;

            case VOICE:
                Objects.requireNonNull(guild.getVoiceChannelById(channelId)).getManager().setParent(toCategory);
                break;
        }
    }

}
