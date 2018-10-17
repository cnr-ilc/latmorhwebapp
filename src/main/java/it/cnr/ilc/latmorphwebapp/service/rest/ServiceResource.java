/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.latmorphwebapp.service.rest;

import it.cnr.ilc.latmorphwebapp.ServiceAnalyzer;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * REST Web Service
 *
 * @author Riccardo Del Gratta &lt;riccardo.delgratta@ilc.cnr.it&gt;
 */
@Path("/")
public class ServiceResource {

    @Context
    private UriInfo context;

    private ServiceAnalyzer analyzer = new ServiceAnalyzer();

//    @ManagedProperty("#{ContextMenuView}")
//    private ContextMenuView service;
    /**
     * Creates a new instance of ServiceResource
     */
    public ServiceResource() {
    }

    /**
     * Retrieves representation of an instance of
     * it.cnr.ilc.latmorphwebapp.service.rest.ServiceResource
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getHtml() {
        return context.getQueryParameters().toString();
    }

    /**
     * PUT method for updating or creating an instance of ServiceResource
     *
     * @param content representation for the resource
     */
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    public void putHtml(String content) {
    }

    @GET
    @Path("/complete")
    @Produces("application/json")
    public String getJsonAsText(@Context UriInfo info) {

        String word = info.getQueryParameters().getFirst("word");
        analyzer.lemmatizeFormAndCreateResponseForServices(word);

        return analyzer.getTheResponse(); //Response

    }
    
    @GET
    @Path("/complete/{word}")
    @Produces("application/json")
    public String getTest(@PathParam("word") String word) {

        //String word = info.getQueryParameters().getFirst("word");
        analyzer.lemmatizeFormAndCreateResponseForServices(word);

        return analyzer.getTheResponse(); //Response

    }

    @POST
    @Path("/post")
    public String create(@QueryParam("word") String word){
        
        analyzer.lemmatizeFormAndCreateResponseForServices(word);

        return analyzer.getTheResponse(); //Response
        
    }

//    @Path("/")
//    public void redirect() {
//    }
}
