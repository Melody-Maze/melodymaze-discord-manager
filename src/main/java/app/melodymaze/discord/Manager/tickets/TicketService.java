package app.melodymaze.discord.Manager.tickets;

import app.melodymaze.discord.Manager.discord.DiscordService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private DiscordService discordService;

    @Value("${discord.guild.id}")
    private long guildId;

    @Value("${discord.guild.channel.ticket.id}")
    private long ticketChannelId;

    @Value("${discord.guild.category.ticket.id}")
    private long ticketCategoryId;

    public void createSelectionMenu(SlashCommandInteractionEvent event) {
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

        EmbedBuilder emBuilder = new EmbedBuilder();
        emBuilder.setColor(Color.RED);
        emBuilder.setTitle("Create a ticket");
        emBuilder.setDescription("Select a ticket category from the menu below!\n\n" +
                "The ticket will automatically be created and you will be notified after your selection");

        event.reply("Ticket selection menu is being setup").queue();
        event.getJDA().getGuildById(guildId)
                .getTextChannelById(ticketChannelId)
                .sendMessageEmbeds(emBuilder.build())
                .addActionRow(selectMenu)
                .queue();
    }

    public TextChannel createTicket(Guild guild, Member ticketOwner, String category) {
        TicketCategory ticketCategory = TicketCategory.valueOf(category.toUpperCase());
        Ticket ticket = saveTicket(ticketOwner.getIdLong(), ticketCategory);

        Collection<Permission> permissions = new ArrayList<>();
        permissions.add(Permission.VIEW_CHANNEL);
        permissions.add(Permission.MESSAGE_SEND);

        TextChannel textChannel = discordService.createTextChannel(guild, "ticket-"+ticket.getId(), guild.getCategoryById(ticketCategoryId));
        textChannel.getManager().putMemberPermissionOverride(
                ticketOwner.getIdLong(), permissions, null)
                .complete();

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Ticket of " + ticketOwner.getEffectiveName())
                .setDescription("Please write the reason why you have opened the ticket while you wait.\n\n" +
                        "**Ticket Category:** " + category.toUpperCase() + "\n" +
                        "**Ticket Status:** " + ticket.getTicketState())
                .setColor(Color.RED);

        textChannel.sendMessageEmbeds(embedBuilder.build())
                .queue();
        textChannel.sendMessage(ticketOwner.getAsMention()).queue();

        return textChannel;
    }

    public Ticket saveTicket(Long ownerID, TicketCategory ticketCategory) {
        Ticket ticket = new Ticket();
        ticket.setOwnerId(ownerID);
        ticket.setTicketState(TicketState.UNCLAIMED);
        ticket.setTicketCategory(ticketCategory);

        ticketRepository.save(ticket);

        return ticket;
    }

}
