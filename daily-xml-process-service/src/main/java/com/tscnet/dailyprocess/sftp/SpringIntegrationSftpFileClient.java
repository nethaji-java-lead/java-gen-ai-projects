package com.tscnet.dailyprocess.sftp;

import lombok.RequiredArgsConstructor;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import java.io.*; import java.util.*;

@Component
@Profile("!demo")
@RequiredArgsConstructor
public class SpringIntegrationSftpFileClient implements SftpFileClient {
    private static final String IN="filesToProcess";
    private final SftpRemoteFileTemplate template;
    @Override public List<String> listXmlFiles() throws IOException {
        return template.execute(session -> Arrays.stream(session.list(IN)).map(SftpClient.DirEntry::getFilename)
                .filter(n -> n.toLowerCase(Locale.ROOT).endsWith(".xml")).toList());
    }
    @Override public InputStream read(String remotePath) throws IOException {
        return template.execute(session -> new ByteArrayInputStream(session.readRaw(remotePath).readAllBytes()));
    }
    @Override public void move(String source,String destination) throws IOException {
        template.execute(session -> { session.rename(source,destination); return null; });
    }
}
