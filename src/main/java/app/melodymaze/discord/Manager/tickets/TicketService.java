package app.melodymaze.discord.Manager.tickets;

import app.melodymaze.discord.Manager.discord.DiscordService;
import app.melodymaze.discord.Manager.listener.ButtonInteractionListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import org.jetbrains.annotations.NotNull;
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

    @Value("${discord.guild.category.archive.id}")
    private long ticketArchiveId;

    @Value("${discord.guild.channel.mod.id}")
    private long modChannelId;

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

        TextChannel textChannel = discordService.createTextChannel("ticket-"+ticket.getId(), guild.getCategoryById(ticketCategoryId));
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
                .addActionRow(
                        Button.success("ticket-claim-"+ticket.getId(), "Claim"),
                        Button.primary("ticket-change-"+ticket.getId(), "Change Category"),
                        Button.danger("ticket-close-"+ticket.getId(), "Close")
                )
                .complete().pin().queue();

        TextChannel modChannel = guild.getTextChannelById(modChannelId);
        EmbedBuilder modEmbed = new EmbedBuilder();
        modEmbed.setTitle("New Ticket Opened!")
                .setDescription("New ticket opened by " + ticketOwner.getAsMention() + " at " + textChannel.getAsMention()
                + "\n**Category:** " + category.toUpperCase())
                .setColor(Color.RED);
        modChannel.sendMessage("@here").queue();
        modChannel.sendMessageEmbeds(modEmbed.build()).queue();

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

    public void claimTicket(ButtonInteractionEvent event, Long ticketId) {
        Member member = event.getMember();
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        ticket.setSupporterId(member.getIdLong());
        ticket.setTicketState(TicketState.CLAIMED);

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Claimed")
                .setDescription("This ticket has been claimed by " + member.getAsMention())
                .setColor(Color.RED);

        event.replyEmbeds(embedBuilder.build()).queue();

        EmbedBuilder embedBuilderToEdit = new EmbedBuilder(event.getInteraction().getMessage().getEmbeds().getLast());
        embedBuilderToEdit.setDescription("Please write the reason why you have opened the ticket while you wait\n" +
                        "**Ticket Category:** " + String.valueOf(ticket.getTicketCategory()).toUpperCase()
                        + "\n **Ticket Status: **" + String.valueOf(ticket.getTicketState()).toUpperCase())
                .setFooter("Claimed by " + member.getEffectiveName(), member.getEffectiveAvatarUrl());

        event.getInteraction().getMessage().editMessageEmbeds(embedBuilderToEdit.build()).queue();
    }

    public void closeTicket(ButtonInteractionEvent event, Long ticketId) {
        Member member = event.getMember();
        Ticket ticket = ticketRepository.findById(Long.valueOf(ticketId)).orElse(null);
        ticket.setTicketState(TicketState.CLOSED);

        Collection<Permission> permissions = new ArrayList<>();
        permissions.add(Permission.VIEW_CHANNEL);
        permissions.add(Permission.MESSAGE_SEND);

        TextChannelManager textChannelManager = event.getChannel().asTextChannel().getManager();
        textChannelManager.setParent(event.getJDA().getCategoryById(ticketArchiveId));
        textChannelManager.putMemberPermissionOverride(
                        ticket.getOwnerId(), null, permissions)
                .complete();

        EmbedBuilder embedBuilderClosed = new EmbedBuilder();
        embedBuilderClosed.setTitle("Ticket closed!")
                .setDescription("This ticket was closed by " + member.getAsMention())
                .setColor(Color.RED);

        event.replyEmbeds(embedBuilderClosed.build()).queue();

        EmbedBuilder embedBuilderToEdit = new EmbedBuilder(event.getInteraction().getMessage().getEmbeds().getLast());
        embedBuilderToEdit.setDescription(
                "Please write the reason why you have opened the ticket while you wait\n" +
                        "**Ticket Category:** " + String.valueOf(ticket.getTicketCategory()).toUpperCase()
                        + "\n **Ticket Status: **" + String.valueOf(ticket.getTicketState()).toUpperCase());

        event.getInteraction().getMessage().editMessageEmbeds(embedBuilderToEdit.build()).queue();

    }

    public void changeTicketCategoryMessage(ButtonInteractionEvent event, long ticketId) {
        SelectMenu selectMenu = StringSelectMenu.create("menu:change")
                .setPlaceholder("Select a support category")
                .addOption("Website", "change-website-"+ticketId,
                        "Recieve support regarding the website-", Emoji.fromFormatted("🔗"))
                .addOption("Discord", "change-discord-"+ticketId,
                        "Recieve support regarding our Discord",
                        Emoji.fromCustom("discord", 1229040004295164006L, false))
                .addOption("Report", "change-report-"+ticketId,
                        "Report another member", Emoji.fromFormatted("🚩"))
                .addOption("Other", "change-other-"+ticketId,
                        "Need help regarding something different?", Emoji.fromFormatted("💬"))
                .build();

        event.reply("Select a new category")
                .addActionRow(selectMenu).setEphemeral(true).queue();
    }

    public void changeTicketCategory(GenericSelectMenuInteractionEvent event, String newCategory, String ticketId) {
        Ticket ticket = ticketRepository.findById(Long.valueOf(ticketId)).orElse(null);

        ticket.setTicketCategory(TicketCategory.valueOf(newCategory.toUpperCase()));
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Category changed!")
                .setDescription("The category has been changed to " + newCategory.toUpperCase());

        event.replyEmbeds(embedBuilder.build()).complete();
        event.getChannel().asTextChannel()
                .getHistoryAfter(event.getMessageIdLong(), 1).complete().getRetrievedHistory().getFirst().pin().queue();
        event.getInteraction().getMessage().delete().queue();

    }


}
