package cn.muzin.resolver;

import cn.muzin.entity.Ticket;
import cn.muzin.entity.TicketStore;

public abstract class TicketStorageResolver {

    private TicketStore ticketStore;

    public TicketStorageResolver(){
        this.ticketStore = new TicketStore();
    }

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
