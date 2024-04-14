package app.melodymaze.discord.Manager.discord;

import app.melodymaze.discord.Manager.listener.SelectionMenuListener;
import app.melodymaze.discord.Manager.tickets.TicketCommandHandler;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

@Slf4j
@Configuration
public class DiscordConfig {

    @Autowired
    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    @Value("${discord.token}")
    private String botToken;

    @Value("${discord.guild.id}")
    private Long guildId;

    @Bean
    public JDA getJda() throws InterruptedException {
        JDABuilder jdaBuilder = JDABuilder.createDefault(botToken);
        jdaBuilder.setLargeThreshold(50);
        jdaBuilder.setActivity(Activity.listening("MelodyMaze"));

        JDA jda = jdaBuilder.build();
        jda.awaitReady();

        SelectionMenuListener selectionMenuListener = new SelectionMenuListener();
        jda.addEventListener(selectionMenuListener);
        autowireCapableBeanFactory.autowireBean(selectionMenuListener);

        TicketCommandHandler ticketCommandHandler = new TicketCommandHandler();
        jda.addEventListener(ticketCommandHandler);
        autowireCapableBeanFactory.autowireBean(ticketCommandHandler);


        Guild guild = jda.getGuildById(guildId);
        guild.upsertCommand("ticket", "Create a ticket")
                .addSubcommands(new SubcommandData("setup", "Create the ticket menu"),
                        new SubcommandData("close", "Close your ticket"),
                        new SubcommandData("claim", "Claim the ticket"),
                        new SubcommandData("unclaim", "Unclaim the ticket"),
                        new SubcommandData("add", "Add a user to the ticket")
                                .addOption(OptionType.STRING, "member", "Member to be added", true)
                )
                .queue();




        return jda;
    }

}
