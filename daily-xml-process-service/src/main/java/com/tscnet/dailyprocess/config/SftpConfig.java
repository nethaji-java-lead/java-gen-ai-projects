package com.tscnet.dailyprocess.config;
import org.springframework.context.annotation.*; import org.springframework.integration.sftp.session.DefaultSftpSessionFactory; import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
@Configuration public class SftpConfig {
 @Bean public DefaultSftpSessionFactory sftpSessionFactory(){var f=new DefaultSftpSessionFactory(true); f.setHost(System.getenv().getOrDefault("SFTP_HOST","localhost")); f.setPort(Integer.parseInt(System.getenv().getOrDefault("SFTP_PORT","22"))); f.setUser(System.getenv().getOrDefault("SFTP_USER","tscnet")); f.setPassword(System.getenv().getOrDefault("SFTP_PASSWORD","change-me")); f.setAllowUnknownKeys(false); return f;}
 @Bean public SftpRemoteFileTemplate sftpRemoteFileTemplate(DefaultSftpSessionFactory f){return new SftpRemoteFileTemplate(f);}
}
