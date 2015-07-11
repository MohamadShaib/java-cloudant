package com.cloudant.client.api.model;


/**
 * Contains the response returned from CouchDB.
 * <p/>
 * <p>The response typically contains an <tt>id</tt> and <tt>rev</tt> values,
 * additional data might be returned such as <tt>error</tt> from Bulk request.
 *
 * @author Ganesh K Choudhary
 * @see DatabaseBase#save(Object)
 * @since 0.0.1
 */
public class Response {
    private org.lightcouch.Response response;

    public Response() {
        this.response = new org.lightcouch.Response();
    }

    public Response(org.lightcouch.Response response) {
        this.response = response;
    }

    /**
     * @return the <tt>id</tt> of the response
     */
    public String getId() {
        return response.getId();
    }

    /**
     * @return the <tt>rev</tt> of the response
     */
    public String getRev() {
        return response.getRev();
    }

    /**
     * @return
     */
    public String getError() {
        return response.getError();
    }

    /**
     * @return
     */
    public String getReason() {
        return response.getReason();
    }


    /**
     * @return <tt>id</tt> and <tt>rev</tt> concatenated.
     */
    public String toString() {
        return response.toString();
    }


}
