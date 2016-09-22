/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.latmorphwebapp.beans;

import java.io.Serializable;
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

public class SearchPanelBean implements Serializable{

    private String selectedTextInArea = "";
    private String textInArea = "";
    private int discontinuosSelection = 0;
    private String response="";
    

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
     * @return the discontinuosSelection
     */
    public int getDiscontinuosSelection() {
        return discontinuosSelection;
    }

    /**
     * @param discontinuosSelection the discontinuosSelection to set
     */
    public void setDiscontinuosSelection(int discontinuosSelection) {
        this.discontinuosSelection = discontinuosSelection;
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

   

}
