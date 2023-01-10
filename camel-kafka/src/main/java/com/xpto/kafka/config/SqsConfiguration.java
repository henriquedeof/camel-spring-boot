package com.xpto.kafka.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.AssumeRoleRequest;

import java.util.UUID;

@Slf4j
@Configuration
public class SqsConfiguration {

//    @Autowired
//    private EventProperties eventProperties;

    @Value("${aws.profile}")
    private String profile;

    @Value("${aws.role-arn}")
    private String roleArn;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;


    @Bean(name = "sqsClient")
    public SqsClient amazonSQS() {
        final Region region = Region.of(this.region);
        if (StringUtils.isNotBlank(this.roleArn)) {
            log.info("Setting AWS SQS client using Role");
            final StsClient stsClient = StsClient.builder()
                    .region(region)
                    .build();
            final AssumeRoleRequest assumeRoleRequest = AssumeRoleRequest.builder()
                    .roleSessionName(UUID.randomUUID().toString())
                    .roleArn(this.roleArn)
                    .build();
            final StsAssumeRoleCredentialsProvider stsAssumeRoleCredentialsProvider = StsAssumeRoleCredentialsProvider.builder()
                    .stsClient(stsClient)
                    .refreshRequest(assumeRoleRequest)
                    .build();
            return SqsClient.builder()
                    .credentialsProvider(stsAssumeRoleCredentialsProvider)
                    .region(region)
                    .build();
        }
        else if (StringUtils.isNotBlank(this.profile)) {
            log.info("Setting AWS SQS client using Profile");
            return SqsClient.builder()
                    .credentialsProvider(ProfileCredentialsProvider.create(this.profile))
                    .region(region)
                    .build();
        }
        else if (StringUtils.isNotBlank(this.accessKey) && StringUtils.isNotBlank(this.secretKey)) {
            log.info("Setting AWS SQS client using Access and Secret keys");
            AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(this.accessKey, this.secretKey);
            return SqsClient.builder()
                    .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
                    .region(region)
                    .build();
        }
        else {
            log.info("Default AWS SQS client");
            return SqsClient.builder().region(region).build();
        }
    }
}
