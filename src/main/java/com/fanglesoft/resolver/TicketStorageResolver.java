package com.fanglesoft.resolver;

import com.fanglesoft.entity.AccessToken;
import com.fanglesoft.entity.Ticket;
import com.fanglesoft.entity.TicketStore;

public abstract class TicketStorageResolver {

    private TicketStore ticketStore;

    public TicketStorageResolver(){}

    public TicketStorageResolver(TicketStore store){
        this.ticketStore = store;
    }

    public abstract Ticket getTicket(String type);

    public abstract void saveTicket(String type, Ticket ticket);

    public TicketStorageResolver setTicketStore(TicketStore store){
        this.ticketStore = store;
        return this;
    }

    public TicketStore getTicketStore(){
        return this.ticketStore;
    }

}
