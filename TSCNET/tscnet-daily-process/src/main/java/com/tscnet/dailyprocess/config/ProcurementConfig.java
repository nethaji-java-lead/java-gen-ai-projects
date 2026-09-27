package com.tscnet.dailyprocess.config;

import com.tscnet.dailyprocess.model.ProcurementAssessmentProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ProcurementAssessmentProperties.class)
public class ProcurementConfig {
}
