/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.latmorphwebapp.beans;

import it.cnr.ilc.jauceps.app.JAucepsApp;
import it.cnr.ilc.jauceps.app.api.PrintAnalyses;
import it.cnr.ilc.jauceps.app.api.ReturnAnalyses;

import it.cnr.ilc.jauceps.lib.impl.InputFunctions;
import it.cnr.ilc.jauceps.lib.impl.Interact;
import it.cnr.ilc.jauceps.lib.impl.Lib;
import it.cnr.ilc.jauceps.lib.impl.Vars;
import it.cnr.ilc.jauceps.lib.outputobjects.AucepsResponse;
import it.cnr.ilc.jauceps.lib.structs.SilType;
import it.cnr.ilc.jauceps.lib.travellingobjects.TravellingQueries;
import it.cnr.ilc.jauceps.lib.travellingobjects.TravellingTables;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import utils.OutFormat;

/**
 *
 * @author Riccardo Del Gratta &lt;riccardo.delgratta@ilc.cnr.it&gt;
 */
@ManagedBean(name = "contextMenuView")

public class ContextMenuView implements Serializable {

    @ManagedProperty("#{searchPanelBean}")
    private SearchPanelBean service;
    private String json;

    public void lemmatize() {

        String str = service.getSelectedTextInArea();
        String flagword = "+w";
        String flagfile = "+f";
        String format = "+j";
        String[] args = new String[3];
        String[] words = str.split("\\s+");
        for (String a : words) {
            args = new String[3];
            args[0] = flagword;
            args[1] = a;
            args[2] = format;
            System.err.println("LEMMATIZE " + a + " " + args);
            lemmatize(args);
            manageJsonResponse(getJson());
        }

    }

    public void manageJsonResponse(String jsonstr) {
        String labelInForm = "input_wordform";
        String labelAltForm = "analyzed_wordform";
        String labelnumA = "number_of_analyses";
        String labelFound = "found";
        String labelAnalysis = "analyses";
        String labelSegments = "segments";
        String labelId = "id";
        String labelEnc = "enclitica";
        String labelPart = "part";
        String labelNumL = "num_lemma";
        String labelLemmas = "lemmas";
        String labelLemma = "lemma";
        String labelValues = "values";
        String labelMorfCodes = "morpho_codes";

        JsonReader jsonReader = Json.createReader(new StringReader(jsonstr));

        JsonObject obj = jsonReader.readObject();
        JsonArray analyses = obj.getJsonArray(labelAnalysis);
        for (JsonValue value : analyses) {
            System.err.println("JSON VALUE " + value);
        }

        jsonReader.close();

    }

    public void lemmatize(String[] args) {
        String routine = JAucepsApp.class.getName() + "/main";

        Vars vars = new Vars();
        SilType sil;
        Lib lib;
        Connection conn;
        TravellingTables travellingtables;
        TravellingQueries travellingqueries;
        InputFunctions inputfunctions;
        AucepsResponse response = null;

        //fields
        PrintStream po = null;
        PrintStream pu = null;
        BufferedWriter pubw = null;// = new BufferedWriter(new OutputStreamWriter(pu));
        BufferedWriter pobw = null; //= new BufferedWriter(new OutputStreamWriter(po));
        String wordform = "";
        String logmess = "";

        boolean retIniVal = false;
        int retval = 0;

        // vars
        Interact interact = new Interact();

        interact.startroutine(args);

        if (po == null || pu == null) {
            po = interact.getPo();
            pu = interact.getPu();
        }

//        po.println((interact.getPrintFormatted()) + " " + interact.getSw_file());
//        po.flush();
        sil = new SilType();

        lib = new Lib(sil);

        retIniVal = lib.initialize(lib.APP_DEFAULT_NAME, null, System.out);
        if (retIniVal) {
            conn = lib.getConn();
            sil = lib.getSil();
        } else {

            return;
        }
        // main code //

        if (retIniVal) {

            if (interact.getSw_file() == 0) {
                do {

                    travellingtables = new TravellingTables(conn);
                    travellingtables.setTtId(sil.getSilId());
                    travellingqueries = new TravellingQueries(conn);
                    travellingqueries.setTqId(sil.getSilId());
                    po = interact.getPo();
                    pu = interact.getPu();
                    pobw = interact.getPobw();
                    pubw = interact.getPubw();
                    sil = lib.resetSil(sil);
                    //wordform = interact.prompt("type the WORD-FORM you wish to analyze. end to exit");
                    if (!interact.isCallPrompt()) {
                        wordform = interact.prompt("type the WORD-FORM you wish to analyze. end to exit");
                    } else {
                        wordform = interact.getWordform();
                    }
                    if (wordform != null) {
                        response = new AucepsResponse(sil);
                        response.setResId(sil.getSilId());

                        inputfunctions = new InputFunctions(response, travellingtables, travellingqueries);
                        response = inputfunctions.silcall(conn, sil, wordform);

                        try {
                            ReturnAnalyses returnanalyses = new ReturnAnalyses(response, travellingqueries, travellingtables);
                            //System.err.println("PRINT FORMAT " + interact.getPrintFormatted());

                            switch (interact.getPrintFormatted()) {
                                case 0:
                                    returnanalyses.printAnalyses(OutFormat.OLD_LL, pobw, pubw);
                                    break;
                                case 1:
                                    returnanalyses.printAnalyses(OutFormat.COMPACT, pobw, pubw);
                                    break;
                                case 2:
                                    returnanalyses.printAnalyses(OutFormat.JSON, pobw, pubw);
                                    break;
                                default:
                                    returnanalyses.printAnalyses(OutFormat.OLD_LL, pobw, pubw);
                                    break;

                            }
                            sil = new SilType();
//                            pobw.flush();
//                            pubw.flush();
                            setJson(returnanalyses.getTheresponse());
                            service.setResponse(returnanalyses.getTheresponse());

                        } catch (Exception e) {
                            e.printStackTrace();

                            System.err.println("EXIT WITH RESPONSE " + e.getMessage());
                        }
                    }
                    if (interact.isCallPrompt()) {
                        wordform = null;
                    }
                } while (wordform != null);

            }
            if (interact.getSw_file() == 1) {

                File ini = interact.getPiFile();
                po = interact.getPo();
                pu = interact.getPu();
                pobw = interact.getPobw();
                pubw = interact.getPubw();
                BufferedReader br = null;
                List<String> words = new ArrayList<>();

                try {

                    String sCurrentLine;

                    br = new BufferedReader(new FileReader(ini));
                    int c = 0;
                    java.util.Date date_start = new java.util.Date();
                    while ((sCurrentLine = br.readLine()) != null) {
                        //po.println(sCurrentLine);
                        //words.add(sCurrentLine);
                        try {

                            java.util.Date date_init = new java.util.Date();
                            travellingtables = new TravellingTables(conn);
                            travellingtables.setTtId(sil.getSilId());
                            travellingqueries = new TravellingQueries(conn);
                            sil = lib.resetSil(sil);
                            travellingqueries.setTqId(sil.getSilId());

                            response = new AucepsResponse(sil);
                            response.setResId(sil.getSilId());

                            if ((c % 10000) == 0) {
                                java.util.Date date = new java.util.Date();
                                //System.err.println("WordForm: " + word+ " "+c+ " at "+new Timestamp(date.getTime()));
                                System.err.println("WordForm: " + sCurrentLine + " " + c + " at " + new Timestamp(date.getTime())
                                        + " lasting " + (date_start.getTime() - date_init.getTime()) + " (" + (date.getTime() - date_init.getTime()) + ")");
                                //date_init = date;
                                pobw.flush();
                                pubw.flush();

                            }

                            //System.err.println("WordForm: " + word);
                            inputfunctions = new InputFunctions(response, travellingtables, travellingqueries);
                            response = inputfunctions.silcall(conn, sil, sCurrentLine);

                            try {
                                PrintAnalyses printanalyses = new PrintAnalyses(response, travellingqueries, travellingtables);
                                printanalyses.printAnalyses(OutFormat.COMPACT, pobw, pubw);
                                sil = new SilType();

                            } catch (Exception e) {
                                //e.printStackTrace();

                                System.err.println("EXIT WITH RESPONSE " + e.getMessage());
                            }
                            c++;
                            //Runtime.getRuntime().gc();

                        } catch (IOException e) {
                        }

                    }
                    pubw.close();
                    pobw.close();

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (br != null) {
                            br.close();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                wordform = null;
            }

        }

        // print the results//
        //end//
    }

    /**
     * @return the service
     */
    public SearchPanelBean getService() {
        return service;
    }

    /**
     * @param service the service to set
     */
    public void setService(SearchPanelBean service) {
        this.service = service;
    }

    /**
     * @return the json
     */
    public String getJson() {
        return json;
    }

    /**
     * @param json the json to set
     */
    public void setJson(String json) {
        this.json = json;
    }

}
