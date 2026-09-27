package com.tscnet.dailyprocess.sftp;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.io.*; import java.nio.file.*; import java.util.*;

@Component @Profile("demo")
public class LocalDemoSftpFileClient implements SftpFileClient {
    private final Path root=Paths.get("./demo-sftp");
    private Path resolve(String p){return root.resolve(p).normalize();}
    public List<String> listXmlFiles() throws IOException { Path d=resolve("filesToProcess"); Files.createDirectories(d); try(var s=Files.list(d)){return s.filter(Files::isRegularFile).map(p->p.getFileName().toString()).filter(n->n.endsWith(".xml")).toList();} }
    public InputStream read(String p) throws IOException { return Files.newInputStream(resolve(p)); }
    public void move(String source,String destination) throws IOException { Path dst=resolve(destination); Files.createDirectories(dst.getParent()); Files.move(resolve(source),dst,StandardCopyOption.REPLACE_EXISTING); }
}
