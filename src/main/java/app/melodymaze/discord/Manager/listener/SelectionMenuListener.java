package app.melodymaze.discord.Manager.listener;

import app.melodymaze.discord.Manager.tickets.TicketService;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenuInteraction;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class SelectionMenuListener extends ListenerAdapter {

    @Autowired
    private TicketService ticketService;

    @Override
    public void onGenericSelectMenuInteraction(@NotNull GenericSelectMenuInteractionEvent event) {
        List<String> values = event.getValues();
        String[] menuType = values.get(0).split("-");
        switch (menuType[0]) {
            case "ticket":
                TextChannel textChannel = ticketService.createTicket(event.getGuild(), event.getMember(), menuType[1]);
                event.reply("Your ticket has been created " + textChannel.getAsMention())
                        .setEphemeral(true).queue();
                break;
        }
    }

}
