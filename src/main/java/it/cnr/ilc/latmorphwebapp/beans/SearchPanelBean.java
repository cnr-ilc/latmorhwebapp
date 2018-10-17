/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.latmorphwebapp.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

/**
 *
 * @author Riccardo Del Gratta &lt;riccardo.delgratta@ilc.cnr.it&gt;
 */
@ManagedBean(name = "searchPanelBean")
@ViewScoped
public class SearchPanelBean implements Serializable {

    private String selectedTextInArea = "";
    private String textInArea = "";

    private String response = "";

    private List<String> selectedOutputFormats = new ArrayList<String>();
    private int outputmode = 4;

    public void setSelectedText() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map map = context.getExternalContext().getRequestParameterMap();
        selectedTextInArea = (String) map.get("selectedText");
    }

    /**
     * @return the selectedTextInArea
     */
    public String getSelectedTextInArea() {
        return selectedTextInArea;
    }

    /**
     * @param selectedTextInArea the selectedTextInArea to set
     */
    public void setSelectedTextInArea(String selectedTextInArea) {

        this.selectedTextInArea = selectedTextInArea;
    }

    /**
     * @return the textInArea
     */
    public String getTextInArea() {
        return textInArea;
    }

    /**
     * @param textInArea the textInArea to set
     */
    public void setTextInArea(String textInArea) {
        this.textInArea = textInArea;
    }

    /**
     * @return the response
     */
    public String getResponse() {
        return response;
    }

    /**
     * @param response the response to set
     */
    public void setResponse(String response) {
        this.response = response;
    }

    /**
     * @return the selectedOutputFormats
     */
    public List<String> getSelectedOutputFormats() {
        return selectedOutputFormats;
    }

    /**
     * @param selectedOutputFormats the selectedOutputFormats to set
     */
    public void setSelectedOutputFormats(List<String> selectedOutputFormats) {
        this.selectedOutputFormats = selectedOutputFormats;
    }

    /**
     * @return the outputmode
     */
    public int getOutputmode() {
        int myval = 0;
        int ret=0;
        for (String val : getSelectedOutputFormats()) {
            myval = Integer.parseInt(val);
            ret=ret+myval;
        }
        if(ret==0 || ret>=4)
            ret=4;
        outputmode=ret;
        setSelectedOutputFormats(new ArrayList<String>());
        return outputmode;
    }

    /**
     * @param outputmode the outputmode to set
     */
    public void setOutputmode(int outputmode) {
        this.outputmode = outputmode;
    }

}
