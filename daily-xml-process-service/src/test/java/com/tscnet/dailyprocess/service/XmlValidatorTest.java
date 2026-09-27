package com.tscnet.dailyprocess.service;
import com.tscnet.dailyprocess.validator.XmlValidator; import org.junit.jupiter.api.Test; import java.io.*; import static org.junit.jupiter.api.Assertions.*;
class XmlValidatorTest { @Test void rejectsDoctype(){String x="<?xml version=\"1.0\"?><!DOCTYPE a [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]><a>&xxe;</a>"; var r=new XmlValidator().validate(new ByteArrayInputStream(x.getBytes())); assertFalse(r.valid());} }
