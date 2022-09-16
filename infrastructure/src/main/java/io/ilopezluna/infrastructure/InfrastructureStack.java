package io.ilopezluna.infrastructure;

import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.CpuUtilizationScalingProps;
import software.amazon.awscdk.services.ecs.MemoryUtilizationScalingProps;
import software.amazon.awscdk.services.ecs.ScalableTaskCount;
import software.amazon.awscdk.services.ecs.Secret;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.rds.AuroraPostgresClusterEngineProps;
import software.amazon.awscdk.services.rds.AuroraPostgresEngineVersion;
import software.amazon.awscdk.services.rds.Credentials;
import software.amazon.awscdk.services.rds.DatabaseCluster;
import software.amazon.awscdk.services.rds.DatabaseClusterEngine;
import software.amazon.awscdk.services.rds.DatabaseSecret;
import software.amazon.awscdk.services.rds.DatabaseSecretProps;
import software.amazon.awscdk.services.rds.IClusterEngine;
import software.amazon.awscdk.services.rds.InstanceProps;
import software.constructs.Construct;

import java.util.Map;

public class InfrastructureStack extends Stack {

    private final static String PREFIX = "ilopezluna";
    private final static int DB_PORT = 5432;

    public InfrastructureStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public InfrastructureStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        final DatabaseSecret databaseSecret = new DatabaseSecret(this, "dbSecret", DatabaseSecretProps.builder()
            .username("dbUser")
            .secretName("dbPassword")
            .build());

        final Vpc vpc = Vpc.Builder.create(this, prefixName("VPC"))
            .maxAzs(2)  // Default is all AZs in region
            .build();

        final DatabaseCluster dbCluster = getDatabaseCluster(databaseSecret, vpc);

        final Cluster cluster = Cluster.Builder.create(this, prefixName("Cluster"))
            .vpc(vpc).build();

        // Create a load-balanced Fargate service and make it public
        final ApplicationLoadBalancedFargateService fargateService = ApplicationLoadBalancedFargateService.Builder.create(this, prefixName("FargateService"))
            .cluster(cluster)           // Required
            .cpu(256)                   // Default is 256
            .desiredCount(1)            // Default is 1
            .taskImageOptions(
                ApplicationLoadBalancedTaskImageOptions.builder()
                    .image(ContainerImage.fromAsset("../application"))
                    .containerPort(8080)    // The default is port 80, The Spring boot default port is 8080
                    .secrets(Map.of("DB_CREDENTIALS", Secret.fromSecretsManager(databaseSecret)))
                    .build())
            .memoryLimitMiB(512)       // Default is 512
            .publicLoadBalancer(true)   // Default is false
            .build();

        // Configure health check
        fargateService.getTargetGroup().configureHealthCheck(HealthCheck.builder()
            .healthyHttpCodes("200") // Specify which http codes are considered healthy
            // The load balancer REQUIRES a healthcheck endpoint to determine the state of the app.
            // In this example, we're using the Spring Actuator. Configure this in your app if missing.
            .path("/actuator/info")
            .port("80") // The default is port 80
            .build());

        // Allow ingress from Fargate to RDS (add a rule in the RDS security group)
        dbCluster.getConnections().allowFrom(fargateService.getService(), Port.tcp(DB_PORT));

        final ScalableTaskCount scalableTarget = fargateService.getService().autoScaleTaskCount(EnableScalingProps.builder()
            .minCapacity(1)
            .maxCapacity(3)
            .build());
        scalableTarget.scaleOnCpuUtilization("CpuScaling", CpuUtilizationScalingProps.builder()
            .targetUtilizationPercent(50)
            .build());
        scalableTarget.scaleOnMemoryUtilization("MemoryScaling", MemoryUtilizationScalingProps.builder()
            .targetUtilizationPercent(50)
            .build());
    }

    @NotNull
    private DatabaseCluster getDatabaseCluster(DatabaseSecret databaseSecret, Vpc vpc) {

        final Credentials credentials = Credentials.fromSecret(databaseSecret);

        // Create Aurora cluster DB
        final IClusterEngine dbEngine = DatabaseClusterEngine
            .auroraPostgres(AuroraPostgresClusterEngineProps.builder()
                .version(AuroraPostgresEngineVersion.VER_14_3)
                .build());

        return DatabaseCluster.Builder.create(this, "DB")
            .engine(dbEngine)
            .credentials(credentials)
            .defaultDatabaseName("dbName")
            .port(DB_PORT)
            .instanceProps(InstanceProps.builder()
                .instanceType(InstanceType.of(InstanceClass.T3, InstanceSize.MEDIUM))
                .vpc(vpc)
                .vpcSubnets(SubnetSelection.builder().build())
                .build())
            .build();
    }

    private static String prefixName(String name) {
        return String.format("%s%s", PREFIX, name);
    }
}
