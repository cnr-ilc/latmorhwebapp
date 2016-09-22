/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.latmorphwebapp;

import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

/**
 *
 * @author Riccardo Del Gratta &lt;riccardo.delgratta@ilc.cnr.it&gt;
 */
@ManagedBean
public class MainBean {

    private String selectedTextInArea;

public void setSelectedText() {
    FacesContext context = FacesContext.getCurrentInstance();
    Map map = context.getExternalContext().getRequestParameterMap();
    selectedTextInArea = (String) map.get("selectedText");
}

public String getSelectedTextInArea() {
    return selectedTextInArea;
}

public void setSelectedTextInArea(String selectedTextInArea) {
    this.selectedTextInArea = selectedTextInArea;
}

}
