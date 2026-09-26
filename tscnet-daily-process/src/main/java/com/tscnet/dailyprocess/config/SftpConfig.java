package com.tscnet.dailyprocess.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;

@Configuration
public class SftpConfig {

    @Value("${sftp.host}")
    private String sftpHost;

    @Value("${sftp.port}")
    private int sftpPort;

    @Value("${sftp.user}")
    private String sftpUser;

    @Value("${sftp.password}")
    private String sftpPassword;

    @Bean
    public DefaultSftpSessionFactory sftpSessionFactory() {

        DefaultSftpSessionFactory factory =
                new DefaultSftpSessionFactory(true);

        factory.setHost(sftpHost);
        factory.setPort(sftpPort);
        factory.setUser(sftpUser);
        factory.setPassword(sftpPassword);

        // For local development only
        factory.setAllowUnknownKeys(true);

        return factory;
    }

    @Bean
    public SftpRemoteFileTemplate sftpRemoteFileTemplate(
            DefaultSftpSessionFactory sftpSessionFactory) {

        return new SftpRemoteFileTemplate(sftpSessionFactory);
    }
}