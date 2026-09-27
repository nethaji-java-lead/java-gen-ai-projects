package com.tscnet.dailyprocess.sftp;
import java.io.*; import java.util.*;
public interface SftpFileClient {
    List<String> listXmlFiles() throws IOException;
    InputStream read(String remotePath) throws IOException;
    void move(String source,String destination) throws IOException;
}
