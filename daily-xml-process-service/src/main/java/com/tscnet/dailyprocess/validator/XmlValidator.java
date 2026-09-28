package com.tscnet.dailyprocess.validator;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.xml.sax.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;
import java.io.*;
import java.util.*;

@Service
public class XmlValidator {
    private static final String XSD_FILE = "xsd/balancing_market_document.xsd";

    public XmlValidationResult validate(InputStream input) {
        List<String> errors = new ArrayList<>();
        String mrid = null;
        try {
            byte[] data = input.readAllBytes();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            var doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(data));
            var nodes = doc.getElementsByTagNameNS("*", "mRID");
            if (nodes.getLength() > 0) mrid = nodes.item(0).getTextContent();
            var resource = new ClassPathResource(XSD_FILE);
            if (resource.exists()) {
                SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                sf.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
                sf.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
                Schema schema = sf.newSchema(new StreamSource(resource.getInputStream()));
                schema.newValidator().validate(new StreamSource(new ByteArrayInputStream(data)));
            }
            return new XmlValidationResult(true, errors, mrid);
        } catch (Exception e) {
            errors.add(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
            return new XmlValidationResult(false, errors, mrid);
        }
    }
}
