package edu.harvard.iq.dataverse.datavariable;


import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;


public class VariableMetadataDDIParser {


    public static final String LEVEL_VARIABLE = "variable";

    public VariableMetadataDDIParser () {

    }

    public void processDataDscr(XMLStreamReader xmlr, Map<Long,VariableMetadata> mapVarToVarMet, Map<Long,VarGroup> varGroupMap) throws XMLStreamException {

        for (int event = xmlr.next(); event != XMLStreamConstants.END_DOCUMENT; event = xmlr.next()) {
            if (event == XMLStreamConstants.START_ELEMENT) {
                if (xmlr.getLocalName().equals("varGrp")) {
                    processVarGrp(xmlr, varGroupMap);
                } else {
                    if (xmlr.getLocalName().equals("var")) {
                        processVar(xmlr, mapVarToVarMet);
                    }
                }
            }
        }

    }

    private  void processVarGrp(XMLStreamReader xmlr , Map<Long,VarGroup> varGroupMap) throws XMLStreamException {
        String _id_v = xmlr.getAttributeValue(null, "ID");
        String _id = _id_v.replace("VG", "");
        long id = Long.parseLong(_id);
        VarGroup vg = new VarGroup();
        vg.setId(id);
        Set<DataVariable> varsInGroups = null;

        String vars =  xmlr.getAttributeValue(null, "var");
        if (vars != null) {
            vars = vars.trim();
            String[] parts = vars.split(" ");
            varsInGroups = new HashSet<DataVariable>();
            for (int i=0; i< parts.length; i++)
            {
                long varId = Long.parseLong(parts[i].replace("v", ""));
                DataVariable dv = new DataVariable();
                dv.setId(varId);
                varsInGroups.add(dv);
            }
        }

        for (int event = xmlr.next(); event != XMLStreamConstants.END_DOCUMENT; event = xmlr.next()) {
            if (event == XMLStreamConstants.START_ELEMENT) {
                if (xmlr.getLocalName().equals("labl")) {
                    processLabel(xmlr, vg);
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if (xmlr.getLocalName().equals("varGrp")) {
                    vg.setVarsInGroup(varsInGroups);
                    varGroupMap.put(id, vg);
                    return;
                }
            }
        }


    }

    private void processVar(XMLStreamReader xmlr,  Map<Long, VariableMetadata> mapVarToVarMet ) throws XMLStreamException {

        String _id_v = xmlr.getAttributeValue(null, "ID");
        String _id = _id_v.replace("v", "");

        long id = Long.parseLong(_id);
        DataVariable dv = new DataVariable();
        dv.setId(id);

        VariableMetadata newVM = new VariableMetadata();

        String wgt =  xmlr.getAttributeValue(null, "wgt");
        if (wgt != null && wgt.equals("wgt")) {
            newVM.setIsweightvar(true);
        } else {
            newVM.setIsweightvar(false);
        }

        String wgt_var =  xmlr.getAttributeValue(null, "wgt-var");
        if (wgt_var != null && wgt_var.startsWith("v")) {
            long wgt_id = Long.parseLong(wgt_var.replace("v", ""));
            DataVariable weightVariable = new DataVariable();
            weightVariable.setId(wgt_id);
            newVM.setWeightvariable(weightVariable);

        } else {
            newVM.setWeightvariable(null);
        }

        for (int event = xmlr.next(); event != XMLStreamConstants.END_DOCUMENT; event = xmlr.next()) {
            if (event == XMLStreamConstants.START_ELEMENT) {
                if (xmlr.getLocalName().equals("labl")) {
                    processLabel(xmlr, newVM);
                } else if (xmlr.getLocalName().equals("universe")) {
                    processUniverse(xmlr, newVM);
                } else if (xmlr.getLocalName().equals("notes")) {
                    processNote(xmlr, newVM);
                } else if (xmlr.getLocalName().equals("qstn")) {
                    processQstn(xmlr, newVM);
                }

            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if (xmlr.getLocalName().equals("var")) {
                        newVM.setDataVariable(dv);
                        mapVarToVarMet.put(id,newVM);
                    return;
                }
            }
        }

    }

    private void processLabel (XMLStreamReader xmlr, VarGroup vg ) throws XMLStreamException {
        String labl = parseText(xmlr);
        vg.setLabel(labl);

        return;
    }

    private void processLabel (XMLStreamReader xmlr, VariableMetadata newVM) throws XMLStreamException {

        if (LEVEL_VARIABLE.equalsIgnoreCase( xmlr.getAttributeValue(null, "level") ) ) {
            String lable = parseText(xmlr, false);
            if (lable != null && !lable.isEmpty()) {
                newVM.setLabel(lable);
            }

        }

        return;
    }

    private void processQstn(XMLStreamReader xmlr, VariableMetadata newVM) throws XMLStreamException {

        for (int event = xmlr.next(); event != XMLStreamConstants.END_DOCUMENT; event = xmlr.next()) {
            if (event == XMLStreamConstants.START_ELEMENT) {
                if (xmlr.getLocalName().equals("qstnLit")) {
                    String text = parseText(xmlr, false);
                    newVM.setLiteralquestion(text);

                } else if (xmlr.getLocalName().equals("ivuInstr")) {
                    String text = parseText(xmlr, false);
                    newVM.setInterviewinstruction(text);
                }
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                if (xmlr.getLocalName().equals("qstn")) return;
            }
        }

    }

    private void processUniverse (XMLStreamReader xmlr, VariableMetadata newVM ) throws XMLStreamException {
        String universe = parseText(xmlr);
        newVM.setUniverse(universe);

        return;
    }

    private void processNote (XMLStreamReader xmlr,  VariableMetadata newVM) throws XMLStreamException {

        String unf_type =  xmlr.getAttributeValue(null, "type");
        String note = parseText(xmlr,false);

        if (unf_type == null )  {
            newVM.setNotes(note);
        }
        return;
    }

    private String parseText(XMLStreamReader xmlr) throws XMLStreamException {
        return parseText(xmlr,true);
    }

    private String parseText(XMLStreamReader xmlr, boolean scrubText) throws XMLStreamException {
        String tempString = getElementText(xmlr);
        if (scrubText) {
            tempString = tempString.trim().replace('\n',' ');
        }
        return tempString;
    }

    private String getElementText(XMLStreamReader xmlr) throws XMLStreamException {
        if(xmlr.getEventType() != XMLStreamConstants.START_ELEMENT) {
            throw new XMLStreamException("parser must be on START_ELEMENT to read next text", xmlr.getLocation());
        }
        int eventType = xmlr.next();
        StringBuilder content = new StringBuilder();
        while(eventType != XMLStreamConstants.END_ELEMENT ) {
            if(eventType == XMLStreamConstants.CHARACTERS
                    || eventType == XMLStreamConstants.CDATA
                    || eventType == XMLStreamConstants.SPACE) {
                content.append(xmlr.getText());
            } else if(eventType == XMLStreamConstants.PROCESSING_INSTRUCTION
                    || eventType == XMLStreamConstants.COMMENT
                    || eventType == XMLStreamConstants.ENTITY_REFERENCE) {
                // skipping
            } else if(eventType == XMLStreamConstants.END_DOCUMENT) {
                throw new XMLStreamException("unexpected end of document when reading element text content");
            } else if(eventType == XMLStreamConstants.START_ELEMENT) {
                throw new XMLStreamException("element text content may not contain START_ELEMENT", xmlr.getLocation());
            } else {
                throw new XMLStreamException("Unexpected event type "+eventType, xmlr.getLocation());
            }
            eventType = xmlr.next();
        }
        return content.toString();
    }

}
