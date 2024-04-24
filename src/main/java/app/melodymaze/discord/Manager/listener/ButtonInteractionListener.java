package app.melodymaze.discord.Manager.listener;

import app.melodymaze.discord.Manager.tickets.Ticket;
import app.melodymaze.discord.Manager.tickets.TicketRepository;
import app.melodymaze.discord.Manager.tickets.TicketService;
import app.melodymaze.discord.Manager.tickets.TicketState;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.awt.*;
import java.util.Optional;

public class ButtonInteractionListener extends ListenerAdapter {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketService ticketService;

    @Value("${discord.role.staff.id}")
    private Long staffId;

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String fullButtonId = event.getInteraction().getButton().getId();
        String buttonType = fullButtonId.split("-")[0];
        String buttonId = fullButtonId.split("-")[1];
        Long ticketId = Long.valueOf(fullButtonId.split("-")[2]);

        Member member = event.getMember();
        if (buttonType.equals("ticket")) {
            switch (buttonId) {
                case "claim":
                    if (member.getRoles().contains(event.getGuild().getRoleById(staffId))) {
                        ticketService.claimTicket(event, ticketId);
                    }
                    else {
                        event.reply("You don't have the necessary permissions to claim this ticket!")
                                .setEphemeral(true).queue();
                    }
                    break;

                case "close":
                    if (member.getRoles().contains(event.getGuild().getRoleById(staffId))) {
                        ticketService.closeTicket(event, ticketId);
                    }
                    else {
                        event.reply("You don't have the necessary permissions to close this ticket!")
                                .setEphemeral(true).queue();
                    }
                    break;

                case "change":
                    if (member.getRoles().contains(event.getGuild().getRoleById(staffId))) {
                        ticketService.changeTicketCategoryMessage(event, ticketId);
                    }
                    else {
                        event.reply("You don't have the necessary permissions to close this ticket!")
                                .setEphemeral(true).queue();
                    }
                    break;
            }
        }
    }
}
