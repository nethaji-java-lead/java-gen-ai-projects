package com.tscnet.dailyprocess.validator;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class XmlValidator {

    private static final String XSD_FILE =
            "xsd/balancing_market_document.xsd";

    /**
     * Validates XML against the configured XSD.
     *
     * @param xml XML content
     * @return validation result containing valid/invalid status and errors
     */
    public XmlValidationResult validateXml(String xml) {

        List<String> errors = new ArrayList<>();

        try {
            // Load XSD from classpath
            ClassPathResource xsdResource =
                    new ClassPathResource(XSD_FILE);

            if (!xsdResource.exists()) {
                errors.add("XSD file not found: " + XSD_FILE);
                return new XmlValidationResult(false, errors);
            }

            // Create SchemaFactory
            SchemaFactory schemaFactory =
                    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // Secure XML processing
            schemaFactory.setProperty(
                    XMLConstants.ACCESS_EXTERNAL_DTD,
                    ""
            );

            schemaFactory.setProperty(
                    XMLConstants.ACCESS_EXTERNAL_SCHEMA,
                    ""
            );

            // Load XSD
            Schema schema;

            try (InputStream xsdInputStream = xsdResource.getInputStream()) {

                schema = schemaFactory.newSchema(
                        new StreamSource(xsdInputStream)
                );
            }

            // Create validator
            var validator = schema.newValidator();

            // Validate XML
            validator.validate(
                    new StreamSource(
                            new java.io.StringReader(xml)
                    )
            );

        } catch (SAXException e) {
            errors.add("XSD validation error: " + e.getMessage());
        } catch (IOException e) {
            errors.add("Unable to read XSD/XML: " + e.getMessage());
        }

        return new XmlValidationResult(
                errors.isEmpty(),
                errors
        );
    }

    private String formatError(
            String type,
            SAXParseException exception) {

        return String.format(
                "%s at line %d, column %d: %s",
                type,
                exception.getLineNumber(),
                exception.getColumnNumber(),
                exception.getMessage()
        );
    }
}