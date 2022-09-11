package io.ilopezluna.infrastructure;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.constructs.Construct;

public class InfrastructureStack extends Stack {

    private final static String PREFIX = "ilopezluna";

    public InfrastructureStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public InfrastructureStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        Vpc vpc = Vpc.Builder.create(this, prefixName("VPC"))
            .maxAzs(1)  // Default is all AZs in region
            .build();

        Cluster cluster = Cluster.Builder.create(this, prefixName("Cluster"))
            .vpc(vpc).build();

        // Create a load-balanced Fargate service and make it public
        ApplicationLoadBalancedFargateService.Builder.create(this, prefixName("FargateService"))
            .cluster(cluster)           // Required
            .cpu(256)                   // Default is 256
            .desiredCount(1)            // Default is 1
            .taskImageOptions(
                ApplicationLoadBalancedTaskImageOptions.builder()
                    .image(ContainerImage.fromAsset("../application"))
                    .build())
            .memoryLimitMiB(512)       // Default is 512
            .publicLoadBalancer(true)   // Default is false
            .build();
    }

    private static String prefixName(String name) {
        return String.format("%s%s", PREFIX, name);
    }
}
