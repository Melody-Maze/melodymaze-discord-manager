package app.melodymaze.discord.Manager.tickets;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "TICKETS")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private long id;

    @Column(name = "OWNER_ID")
    private long ownerId;

    @Column(name = "STATE")
    private TicketState ticketState;

    @Column(name = "CATEGORY")
    private TicketCategory ticketCategory;

    @Column(name = "SUPPORTER_ID")
    private long supporterId;

}
