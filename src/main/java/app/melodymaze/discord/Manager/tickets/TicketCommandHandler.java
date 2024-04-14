package app.melodymaze.discord.Manager.tickets;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

public class TicketCommandHandler extends ListenerAdapter {

    @Autowired
    private TicketService ticketService;

    @Value("${discord.role.discord-admin.id}")
    private long adminRoleId;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getName().equals("ticket")){
            String subcommand = event.getSubcommandName();
            switch (subcommand) {
                case "setup":
                    if (checkRole(event, adminRoleId)) {
                        ticketService.createSelectionMenu(event);
                    }
                    break;
            }
        }
    }

    public boolean checkRole(SlashCommandInteractionEvent event, long requiredRoleId) {
        List<Role> usersRoles = event.getMember().getRoles();
        Role requiredRole = event.getGuild().getRoleById(requiredRoleId);
        boolean hasRole = false;
        for (Role role : usersRoles) {
            System.out.println(role.toString());
            if (role.equals(requiredRole)) {
                hasRole = true;
            }
        }
        if (!hasRole) {
            event.reply("You are missing the required role to execute this command")
                    .setEphemeral(true).queue();
        }
        return hasRole;
    }

}
