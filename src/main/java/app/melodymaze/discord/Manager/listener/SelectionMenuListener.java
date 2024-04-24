package app.melodymaze.discord.Manager.listener;

import app.melodymaze.discord.Manager.tickets.TicketService;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;

public class SelectionMenuListener extends ListenerAdapter {

    @Autowired
    private TicketService ticketService;

    @Override
    public void onGenericSelectMenuInteraction(@NotNull GenericSelectMenuInteractionEvent event) {
        List<String> values = event.getValues();
        String[] menuType = values.get(0).split("-");
        switch (menuType[0]) {
            case "ticket":
                TextChannel textChannel = ticketService.createTicket(event.getGuild(), Objects.requireNonNull(event.getMember()), menuType[1]);
                event.reply("Your ticket has been created " + textChannel.getAsMention())
                        .setEphemeral(true).queue();
                SelectMenu selectMenu = StringSelectMenu.create("menu:create")
                        .setPlaceholder("Select a support category")
                        .addOption("Website", "ticket-website",
                                "Recieve support regarding the website", Emoji.fromFormatted("🔗"))
                        .addOption("Discord", "ticket-discord",
                                "Recieve support regarding our Discord",
                                Emoji.fromCustom("discord", 1229040004295164006L, false))
                        .addOption("Report", "ticket-report",
                                "Report another member", Emoji.fromFormatted("🚩"))
                        .addOption("Other", "ticket-other",
                                "Need help regarding something different?", Emoji.fromFormatted("💬"))
                        .build();
                event.getJDA().getTextChannelById(event.getChannelIdLong())
                        .editMessageComponentsById(event.getMessageIdLong())
                        .setActionRow(selectMenu)
                        .queue();
                break;

            case "change":
                ticketService.changeTicketCategory(event, menuType[1], menuType[2]);
                break;
        }
    }

}
