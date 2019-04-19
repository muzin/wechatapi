package com.fangle.resolver;

import com.fangle.entity.Ticket;
import com.fangle.entity.TicketStore;

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
