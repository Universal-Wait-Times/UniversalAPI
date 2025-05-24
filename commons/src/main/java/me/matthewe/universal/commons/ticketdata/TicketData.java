package me.matthewe.universal.commons.ticketdata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@AllArgsConstructor
@Data
@ToString
public class TicketData {
    private String date;
    private int available;
    private int capacity;
    private int typesRemaining;


}
