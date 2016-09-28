/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cnr.ilc.latmorphwebapp.beans;

import it.cnr.ilc.jauceps.app.api.PrintAnalyses;
import it.cnr.ilc.jauceps.app.api.ReturnAnalyses;

import it.cnr.ilc.jauceps.lib.impl.InputFunctions;
import it.cnr.ilc.jauceps.lib.impl.Interact;
import it.cnr.ilc.jauceps.lib.impl.Lib;
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
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import it.cnr.ilc.jauceps.app.utils.OutFormat;
import java.util.HashMap;

/**
 *
 * @author Riccardo Del Gratta &lt;riccardo.delgratta@ilc.cnr.it&gt;
 */
@ManagedBean(name = "contextMenuView")

public class ContextMenuView implements Serializable {

    @ManagedProperty("#{searchPanelBean}")
    private SearchPanelBean service;
    private String json;
    private TreeNode root;
    private TreeNode mainRoot;
    private List<TreeNode> roots = new ArrayList<>();
    private HashMap<String, Integer> occhm = new HashMap<>();

    /**
     * The typeOfAnalysis manages 3 check boxes to show the results in a bitmap
     * fashion Results might be shown as complete, only lemmas (with pos), only
     * analysis of the form, lemmas and analysis of the form The values that
     * typeOfAnalysis are ONLY the following:
     * <ul>
     * <li>4 which means complete</li>
     * <li>3 which means lemmas and analysis of the form</li>
     * <li>2 which means only lemmas</li>
     * <li>1 which means only analysis of the form</li>
     */
    private int typeOfAnalysis = 4;

    @PostConstruct
    public void init() {
//        TreeNode node=new DefaultTreeNode("init");
//        List<TreeNode> nodes = getRoots();
//        nodes.add(node);
//        setRoots(nodes);
        service.setSelectedOutputFormats(new ArrayList<String>());
        service.setTextInArea("");
        //setTypeOfAnalysis(4);

    }

    /**
     * takes the form and
     */
    public void lemmatizeFormAndCreateResponse() {
        TreeNode node = null;
        List<TreeNode> nodes = getRoots();
        typeOfAnalysis = getTypeOfAnalysis();
        String str = service.getSelectedTextInArea();
        String flagword = "+w";
        String flagfile = "+f";
        String format = "+j";
        String[] args = new String[3];
        String[] words = str.split("\\s+");
        List<String> found = new ArrayList<>();
        for (String a : words) {
            args = new String[3];
            args[0] = flagword;
            args[1] = a;
            args[2] = format;
            if (!found.contains(a)) {
                System.err.println("Lemmatize wordform " + a);
                lemmatize(args);
                node = readJsonResponse(getJson());
                nodes.add(node);

                found.add(a);
            } else {
                System.err.println("Wordform " + a + " already lemmatized");
            }
            //manageJsonResponse(getJson());

        }
        setRoots(nodes);

        setMainRoot(createListOfNodes(nodes));

    }

    /**
     * Collapses all nodes into one
     *
     * @param nodes the list of nodes
     * @return the list of forms all collapsed in a single node
     */
    private TreeNode createListOfNodes(List<TreeNode> nodes) {

        if (getMainRoot() == null) {
            setMainRoot(new DefaultTreeNode("  Your Results  "));
        }
        getMainRoot().getChildren().addAll(nodes);
        return getMainRoot();

    }

    public TreeNode readJsonResponse(String jsonstr) {

        String labelInForm = "input_wordform";
        String labelAltForm = "analyzed_wordform";


        /* stuff for nodes */
        String __WORD__ = "";
        String __ALTWORD__ = "";

        /* complete json object */
        JsonReader jsonReader = Json.createReader(new StringReader(jsonstr));
        JsonObject obj = jsonReader.readObject();
        //System.err.println(typeOfAnalysis);

        /*create the root node (form/altform)*/
        __WORD__ = obj.getJsonString(labelInForm).getString();
        __ALTWORD__ = obj.getJsonString(labelAltForm).getString();
        __WORD__ = __WORD__ + " (" + __ALTWORD__ + ") ";
        root = createRootNode(__WORD__);

        if (typeOfAnalysis == 1) {
            /*add a node to show the morphoanalysis*/
            root.getChildren().add(createMorphoAnalysisForForm(obj, false));
        }

        if (typeOfAnalysis == 2) /*add a node for lemmas*/ {
            root.getChildren().add(createListOfLemmas(obj));
        }

        if (typeOfAnalysis == 3) /*add a node to show the morphoanalysis*/ {
            root.getChildren().add(createMorphoAnalysisForForm(obj, false));
            /*add a node for lemmas*/
            root.getChildren().add(createListOfLemmas(obj));
        }
        if (typeOfAnalysis == 4) {
            /*complete analysis*/
            root.getChildren().add(createCompleteTree(obj));

        }

        return root;

    }

    /**
     * return the root node
     *
     * @param root the label for the node
     * @return the root node
     */
    private TreeNode createRootNode(String root) {
        TreeNode node = null;
        if (node == null) {
            node = new DefaultTreeNode(root, null);
        }
        return node;

    }

    /**
     * manages the response to adjust it as a treenode
     *
     * @param obj the json object corresponding to the response
     * @return the complete node
     */
    private TreeNode createCompleteTree(JsonObject obj) {
        TreeNode node = null;
        if (node == null) {
            node = createMorphoAnalysisForForm(obj, true);
        }
        return node;
    }

    /**
     * manages the response to adjust it as a treenode
     *
     * @param obj the json object corresponding to the response
     * @param complete true to add lemmas to analysis
     * @return the analysis of the form (with lemmas if complete is true)
     */
    private TreeNode createMorphoAnalysisForForm(JsonObject obj, boolean complete) {
        String labelAnalysis = "analyses";
        String labelMorfCodes = "morpho_codes";
        String labelLemmas = "lemmas";
        String labelLemma = "lemma";
        String labelValues = "values";
        String labelLemType = "lem_type";
        String labelPos = "PoS";
        String __ANALISYS__ = "Analysis: ";
        String __FORMANALISYS__ = "Form Analysis";
        String __FORMMORPHANALISYS__ = "Morphological Analysis: ";
        String __CASO__ = "Caso";
        String __GENERE__ = "Genere";
        String __NUMERO__ = "Numero";
        String __MODOVERBALE__ = "Modo verbale";// : "Indicativo attivo",
        String __TEMPOVERBALE__ = "Tempo verbale";// : "Presente",
        String __PERSONA__ = "Persona";// : "Seconda"
        String __TYPE__ = "Type";// : "Comune",
        String __CF__ = "Categoria flessiva";// : "IV decl"

        String __CASOVALUE__ = "";
        String __GENEREVALUE__ = "";
        String __NUMEROVALUE__ = "";
        String __MODOVERBALEVALUE__ = "";
        String __TEMPOVERBALEVALUE__ = "";
        String __PERSONAVALUE__ = "";
        String __TYPEVALUE__ = "";
        String __CFVALUE__ = "";
        String __VALUES__ = "";
        String formmorphoanalysis = "Results:";
        String lemmamorphoanalysis = "Results:";

        String PoS = "";
        String lemmapos = "";
        boolean thereIsCaseOrGenreOrNumber = false;
        boolean thereIsStuffInLemma = false;
        TreeNode node = null;
        TreeNode morphoresultnode = null;
        int i = 0; // analyses
        int mc = 0; // morpho codes
        int j = 0; // lemmas
        /*create an array of analysis*/
        if (complete) {
            __FORMANALISYS__ = "Complete Results";
        } else {
            __FORMANALISYS__ = "Form Analysis";
        }
        JsonArray analyses = obj.getJsonArray(labelAnalysis);
        if (node == null) {

            node = new DefaultTreeNode(__FORMANALISYS__);

            for (JsonValue analysis : analyses) {
                mc = 0;
                j = 0;
                TreeNode nodeanalysis = new DefaultTreeNode(__ANALISYS__ + (i + 1), node);
                JsonObject objAnalysis = analyses.getJsonObject(i);
                JsonArray formcodes = objAnalysis.getJsonArray(labelMorfCodes);
                for (JsonValue formcode : formcodes) {
                    TreeNode temp = new DefaultTreeNode();
                    thereIsCaseOrGenreOrNumber = false;
                    __CASOVALUE__ = "";
                    __GENEREVALUE__ = "";
                    __NUMEROVALUE__ = "";
                    __MODOVERBALEVALUE__ = "";
                    __TEMPOVERBALEVALUE__ = "";
                    __PERSONAVALUE__ = "";

                    formmorphoanalysis = "";
                    JsonObject objFormCode = formcodes.getJsonObject(mc);
                    try {
                        __CASOVALUE__ = objFormCode.getString(__CASO__);
                        __CASOVALUE__ = __CASO__ + ": " + __CASOVALUE__;
                        thereIsCaseOrGenreOrNumber = true;
                    } catch (Exception e) {
                        __CASOVALUE__ = "";
                    }
                    try {
                        __GENEREVALUE__ = objFormCode.getString(__GENERE__);
                        __GENEREVALUE__ = __GENERE__ + ": " + __GENEREVALUE__;
                        thereIsCaseOrGenreOrNumber = true;
                    } catch (Exception e) {
                        __GENEREVALUE__ = "";
                    }
                    try {
                        __NUMEROVALUE__ = objFormCode.getString(__NUMERO__);
                        __NUMEROVALUE__ = __NUMERO__ + ": " + __NUMEROVALUE__;
                        thereIsCaseOrGenreOrNumber = true;
                    } catch (Exception e) {
                        __NUMEROVALUE__ = "";
                    }

                    try {
                        __MODOVERBALEVALUE__ = objFormCode.getString(__MODOVERBALE__);
                        __MODOVERBALEVALUE__ = __MODOVERBALE__ + ": " + __MODOVERBALEVALUE__;
                        thereIsCaseOrGenreOrNumber = true;
                    } catch (Exception e) {
                        __MODOVERBALEVALUE__ = "";
                    }
                    try {
                        __TEMPOVERBALEVALUE__ = objFormCode.getString(__TEMPOVERBALE__);
                        __TEMPOVERBALEVALUE__ = __TEMPOVERBALE__ + ": " + __TEMPOVERBALEVALUE__;
                        thereIsCaseOrGenreOrNumber = true;
                    } catch (Exception e) {
                        __TEMPOVERBALEVALUE__ = "";
                    }
                    try {
                        __PERSONAVALUE__ = objFormCode.getString(__PERSONA__);
                        __PERSONAVALUE__ = __PERSONA__ + ": " + __PERSONAVALUE__;
                        thereIsCaseOrGenreOrNumber = true;
                    } catch (Exception e) {
                        __PERSONAVALUE__ = "";
                    }
                    if (!__CASOVALUE__.equals("")) {
                        formmorphoanalysis = formmorphoanalysis + "\n" + __CASOVALUE__;
                    }
                    if (!__GENEREVALUE__.equals("")) {
                        formmorphoanalysis = formmorphoanalysis + "\n" + __GENEREVALUE__;
                    }
                    if (!__NUMEROVALUE__.equals("")) {
                        formmorphoanalysis = formmorphoanalysis + "\n" + __NUMEROVALUE__;
                    }

                    if (!__MODOVERBALEVALUE__.equals("")) {
                        formmorphoanalysis = formmorphoanalysis + "\n" + __MODOVERBALEVALUE__;
                    }
                    if (!__TEMPOVERBALEVALUE__.equals("")) {
                        formmorphoanalysis = formmorphoanalysis + "\n" + __TEMPOVERBALEVALUE__;
                    }
                    if (!__PERSONAVALUE__.equals("")) {
                        formmorphoanalysis = formmorphoanalysis + "\n" + __PERSONAVALUE__;
                    }
                    if (thereIsCaseOrGenreOrNumber) {
                        //TreeNode fmaNode = new DefaultTreeNode(__FORMMORPHANALISYS__ + mc, nodeanalysis);
                        temp = new DefaultTreeNode((i + 1) + "." + (mc + 1) + ") " + formmorphoanalysis, nodeanalysis);

                        //nodeanalysis.getChildren().add(fmaNode);
                    } else {
                        temp = new DefaultTreeNode((i + 1) + "." + (mc + 1) + ") " + "No analysis available", nodeanalysis);
                    }
                    mc++;
                }

                /*manage lemmas if complete*/
                if (complete) {
                    String __LEMMAS__ = "Lemma(s)";

                    //TreeNode node = null;
                    TreeNode nodelemma = new DefaultTreeNode(__LEMMAS__, nodeanalysis);

                    String __LEMMA__ = "";
                    String __LEMTYPE__ = "";
                    JsonArray lemmas = objAnalysis.getJsonArray(labelLemmas);
                    TreeNode temp = new DefaultTreeNode();
                    for (JsonValue lemma : lemmas) {
                        thereIsStuffInLemma = false;
                        lemmamorphoanalysis = "";
                        JsonObject objLemmas = lemmas.getJsonObject(j);
                        __LEMMA__ = objLemmas.getString(labelLemma);
                        __LEMTYPE__ = objLemmas.getString(labelLemType);

                        JsonArray codes = objLemmas.getJsonArray(labelMorfCodes);
                        temp = new DefaultTreeNode(__LEMMA__ + " (" + __LEMTYPE__ + ") ", nodelemma);
                        try {
                            __CFVALUE__ = codes.getJsonObject(0).getString(__CFVALUE__);
                            thereIsStuffInLemma = true;
                        } catch (Exception e) {
                            __CFVALUE__ = "";
                            thereIsStuffInLemma = false;
                        }

                        try {
                            __TYPEVALUE__ = codes.getJsonObject(0).getString(__TYPE__);
                            thereIsStuffInLemma = true;
                        } catch (Exception e) {
                            __TYPEVALUE__ = "";
                            thereIsStuffInLemma = false;
                        }

                        try {
                            __VALUES__ = codes.getJsonObject(0).getString(labelValues);
                            thereIsStuffInLemma = true;
                        } catch (Exception e) {
                            __VALUES__ = "";
                            thereIsStuffInLemma = false;
                        }

                        try {
                            PoS = codes.getJsonObject(0).getString(labelPos);
                            thereIsStuffInLemma = true;
                        } catch (Exception e) {
                            PoS = "";
                            thereIsStuffInLemma = false;
                        }

                        if (!__VALUES__.equals("")) {
                            lemmamorphoanalysis = lemmamorphoanalysis + "\n" + __VALUES__;
                        }

                        if (!PoS.equals("")) {
                            lemmamorphoanalysis = lemmamorphoanalysis + "\n" + PoS;
                        }

                        if (!__TYPEVALUE__.equals("")) {
                            lemmamorphoanalysis = lemmamorphoanalysis + "\n" + __TYPEVALUE__;
                        }

                        if (!__CFVALUE__.equals("")) {
                            lemmamorphoanalysis = lemmamorphoanalysis + "\n" + __CFVALUE__;
                        }

                        if (thereIsStuffInLemma) {
                            TreeNode lemmacodes = new DefaultTreeNode(lemmamorphoanalysis, temp);

                        }

                        j++;
                    }
                    nodeanalysis.getChildren().add(nodelemma);
                    //nodelemma = new DefaultTreeNode(__LEMMAS__, nodeanalysis);
                }
                node.getChildren().add(nodeanalysis);
                i++;
            }
        }

        return node;
    }

    private TreeNode createListOfLemmas(JsonObject obj) {

        String labelAnalysis = "analyses";

        String labelLemmas = "lemmas";
        String labelLemma = "lemma";

        String labelMorfCodes = "morpho_codes";
        String labelLemType = "lem_type";
        String labelPos = "PoS";
        List<String> lemmafound = new ArrayList<String>();
        String PoS = "";
        String lemmapos = "";

        String __LEMMAS__ = "Lemma(s)";

        TreeNode node = null;
        TreeNode nodelemma = new DefaultTreeNode();

        String __LEMMA__ = "";
        String __LEMTYPE__ = "";
        if (node == null) {
            node = new DefaultTreeNode(__LEMMAS__);
            //add analysis as subnodes
            JsonArray analyses = obj.getJsonArray(labelAnalysis);
            int i = 0; // analysises
            int j = 0; // lemmas
            for (JsonValue value : analyses) {
                j = 0;
                PoS = "";
                lemmapos = "";
                JsonObject objAnalyses = analyses.getJsonObject(i);
                JsonArray lemmas = objAnalyses.getJsonArray(labelLemmas);

                for (JsonValue lemma : lemmas) {
                    JsonObject objLemmas = lemmas.getJsonObject(j);
                    __LEMMA__ = objLemmas.getString(labelLemma);
                    __LEMTYPE__ = objLemmas.getString(labelLemType);

                    JsonArray codes = objLemmas.getJsonArray(labelMorfCodes);
                    PoS = codes.getJsonObject(0).getString(labelPos);
                    //System.err.println("\t\tJSON LEMMAS " + i + " " + lemma + " -" + PoS + "- ");
                    if (lemmas.size() > 1) {
                        if (__LEMTYPE__.equalsIgnoreCase("IPERLEMMA")) {
                            lemmapos = __LEMMA__ + " (" + PoS + ") ";
                            if (!lemmafound.contains(lemmapos)) {
                                lemmafound.add(lemmapos);
                            }

                        }
                    } else {
                        lemmapos = __LEMMA__ + " (" + PoS + ") ";
                        if (!lemmafound.contains(lemmapos)) {
                            lemmafound.add(lemmapos);
                        }
                    }

                    j++;
                }
                i++;
            }
            for (String temp : lemmafound) {
                nodelemma = new DefaultTreeNode(temp, node);
            }
        }

        return node;
    }

    public TreeNode getRoot() {
        return root;
    }

    /**
     * lemmatize the form
     *
     * @param args arguments +w form +j
     */
    public void lemmatize(String[] args) {
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
        boolean retIniVal = false;

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

    /**
     * @return the typeOfAnalysis
     */
    public int getTypeOfAnalysis() {
        typeOfAnalysis = service.getOutputmode();
        return typeOfAnalysis;
    }

    /**
     * @param typeOfAnalysis the typeOfAnalysis to set
     */
    public void setTypeOfAnalysis(int typeOfAnalysis) {
        this.typeOfAnalysis = typeOfAnalysis;
    }

    /**
     * @return the roots
     */
    public List<TreeNode> getRoots() {
        return roots;
    }

    /**
     * @param roots the roots to set
     */
    public void setRoots(List<TreeNode> roots) {
        this.roots = roots;
    }

    /**
     * @return the mainRoot
     */
    public TreeNode getMainRoot() {
        return mainRoot;
    }

    /**
     * @param mainRoot the mainRoot to set
     */
    public void setMainRoot(TreeNode mainRoot) {
        this.mainRoot = mainRoot;
    }

    /**
     * @return the occhm
     */
    public HashMap<String, Integer> getOcchm() {
        return occhm;
    }

    /**
     * @param occhm the occhm to set
     */
    public void setOcchm(HashMap<String, Integer> occhm) {
        this.occhm = occhm;
    }

}
