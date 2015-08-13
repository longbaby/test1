package org.jasig.cas.ticket.registry;

import java.util.Collection;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.springframework.beans.factory.DisposableBean;
import com.icbc.emall.cache.CacheManager;

/**
 * Key-value ticket registry implementation that stores tickets in memcached keyed on the ticket ID.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.3
 */
public final class TairTicketRegistry extends AbstractDistributedTicketRegistry implements DisposableBean {

    @NotNull
    private final CacheManager client;

    /**
     * TGT cache entry timeout in seconds.
     */
    @Min(0)
    private final int tgtTimeout;

    /**
     * ST cache entry timeout in seconds.
     */
    @Min(0)
    private final int stTimeout;


    /**
     * Creates a new instance that stores tickets in the given memcached hosts.
     *
     * @param hostnames                   Array of memcached hosts where each element is of the form host:port.
     * @param ticketGrantingTicketTimeOut TGT timeout in seconds.
     * @param serviceTicketTimeOut        ST timeout in seconds.
     */
    public TairTicketRegistry(final String[] hostnames, final int ticketGrantingTicketTimeOut, final int serviceTicketTimeOut) {
        try {
            this.client = CacheManager.getInstance();
        } catch (final Exception e) {
            throw new IllegalArgumentException("Invalid memcached host specification.", e);
        }
        this.tgtTimeout = ticketGrantingTicketTimeOut;
        this.stTimeout = serviceTicketTimeOut;
    }

    /**
     * This alternative constructor takes time in milliseconds.
     * It has the timeout parameters in order to create a unique method signature.
     *
     * @param ticketGrantingTicketTimeOut TGT timeout in milliseconds.
     * @param serviceTicketTimeOut ST timeout in milliseconds.
     * @param hostnames  Array of memcached hosts where each element is of the form host:port.
     * @see TairCacheTicketRegistry#MemCacheTicketRegistry(String[], int, int)
     */
    @Deprecated
    public TairTicketRegistry(final long ticketGrantingTicketTimeOut, final long serviceTicketTimeOut, final String[] hostnames) {
        //this(hostnames, (int) (ticketGrantingTicketTimeOut / 1000), (int) (serviceTicketTimeOut / 1000));
    	this(hostnames, (int) (ticketGrantingTicketTimeOut / 1000), (int) (serviceTicketTimeOut / 1000));
    }

    /**
     * Creates a new instance using the given memcached client instance, which is presumably configured via
     * <code>net.spy.memcached.spring.MemcachedClientFactoryBean</code>.
     *
     * @param client                      TairCacheManager client.
     * @param ticketGrantingTicketTimeOut TGT timeout in seconds.
     * @param serviceTicketTimeOut        ST timeout in seconds.
     */
    public TairTicketRegistry(final CacheManager client, final int ticketGrantingTicketTimeOut, final int serviceTicketTimeOut) {
        this.tgtTimeout = ticketGrantingTicketTimeOut;
        this.stTimeout = serviceTicketTimeOut;
        this.client = client;
    }

    protected void updateTicket(final Ticket ticket) {
        log.debug("Updating ticket {}", ticket);
        try {
        	log.info("TairTicketRegistry=======updateTicket{}  key :"+ticket.getId()+"= value :"+ticket.toString());
        	//boolean ret = this.client.putCache(ticket.getId(), ticket);
        	boolean ret = this.client.putCache(ticket.getId(), ticket,getTimeout(ticket));
        	if(ret == false){
    			log.error("TairTicketRegistry=======updateTicket{}  key :"+ticket.getId()+"= value :"+ticket.toString()+"  putCache return false ");
    		}else{
    			log.info("TairTicketRegistry=======updateTicket{}  key :"+ticket.getId()+"= value :"+ticket.toString()+"  putCache return true ");
    		}
        } catch (final Exception e) {
            log.warn("Interrupted while waiting for response to async replace operation for ticket {}. " +
                    "Cannot determine whether update was successful.", ticket);
        }
    }

    public void addTicket(final Ticket ticket) {
        log.debug("Adding ticket {}", ticket);
        try {
        		log.info("TairTicketRegistry=======addTicket{}  key :"+ticket.getId()+"= value :"+ticket.toString());
        		boolean ret = this.client.putCache(ticket.getId(), ticket,getTimeout(ticket));
        		if(ret == false){
        			log.error("TairTicketRegistry=======addTicket{}  key :"+ticket.getId()+"= value :"+ticket.toString()+"=  expiration:"+getTimeout(ticket)+"  putCache return false ");
        		}else{
        			log.info("TairTicketRegistry=======addTicket{}  key :"+ticket.getId()+"= value :"+ticket.toString()+"=  expiration:"+getTimeout(ticket)+"  putCache return true ");
        		}
        } catch (final Exception e) {
            log.warn("Interrupted while waiting for response to async add operation for ticket {}. " +
                    "Cannot determine whether add was successful.", ticket);
        }
    }

    public boolean deleteTicket(final String ticketId) {
        log.debug("Deleting ticket {}", ticketId);
        try {
        	this.client.removeCache(ticketId);
        	return true;
        } catch (final Exception e) {
            log.error("Failed deleting {}", ticketId, e);
        }
        return false;
    }

    public Ticket getTicket(final String ticketId) {
        try {/*
            final com.taobao.tair.Result<com.taobao.tair.DataEntry> r = ((com.taobao.tair.Result<com.taobao.tair.DataEntry>) this.client.getCache(ticketId));
            com.taobao.tair.DataEntry de = r.getValue();
            if(de != null){
            	final Ticket t =(Ticket) de.getValue();
                if (t != null) {
                    return getProxiedTicketInstance(t);
                }
            }
        	*/
        	final Ticket t =(Ticket) this.client.getCache(ticketId);
        	 if (t != null) {
                 return getProxiedTicketInstance(t);
             }else{
            	 log.error("TairTicketRegistry=======getTicket{}  key :"+ticketId +" is null");
             }
        } catch (final Exception e) {
            log.error("Failed fetching {} ", ticketId, e);
        }
        return null;
    }

    /**
     * This operation is not supported.
     *
     * @throws UnsupportedOperationException if you try and call this operation.
     */
    public Collection<Ticket> getTickets() {
        throw new UnsupportedOperationException("GetTickets not supported.");
    }

    public void destroy() throws Exception {
    	this.client.destroy();
    }

    /**
     * As of version 3.5, this operation has no effect since async writes can cause registry consistency issues.
     */
    @Deprecated
    public void setSynchronizeUpdatesToRegistry(final boolean b) { /* NOOP */ }

    @Override
    protected boolean needsCallback() {
        return true;
    }

    private int getTimeout(final Ticket t) {
        if (t instanceof TicketGrantingTicket) {
            return this.tgtTimeout;
        } else if (t instanceof ServiceTicket) {
            return this.stTimeout;
        }
        throw new IllegalArgumentException("Invalid ticket type");
    }
}
