package app.melodymaze.discord.Manager.discord;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class DiscordConfig {

    @Value("${discord.token}")
    private String botToken;

    @Bean
    public JDA getJda() {
        JDABuilder jdaBuilder = JDABuilder.createDefault(botToken);
        jdaBuilder.setActivity(Activity.listening("MelodyMaze"));

        JDA jda = jdaBuilder.build();

        return jda;
    }

}
