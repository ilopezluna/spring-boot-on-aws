package io.ilopezluna.infrastructure;

import org.junit.jupiter.api.Test;
import software.amazon.awscdk.App;
import software.amazon.awscdk.assertions.Template;

import java.util.Collections;
import java.util.Map;

public class InfrastructureTest {

    @Test
    public void testStack() {
        final App app = new App();
        final InfrastructureStack stack = new InfrastructureStack(app, "InfrastructureStack");
        final Template template = Template.fromStack(stack);
        template.resourceCountIs("AWS::EC2::VPC", 1);
        template.resourceCountIs("AWS::ECS::Cluster", 1);
        template.resourceCountIs("AWS::ECS::Service", 1);
        template.resourceCountIs("AWS::ECS::TaskDefinition", 1);
        template.resourceCountIs("AWS::ElasticLoadBalancingV2::LoadBalancer", 1);
        template.resourceCountIs("AWS::ElasticLoadBalancingV2::LoadBalancer", 1);
        template.resourceCountIs("AWS::RDS::DBCluster", 1);

        template.hasResourceProperties("AWS::EC2::VPC", Map.of(
            "Tags", Collections.singletonList(Map.of( "Value", "InfrastructureStack/ilopezlunaVPC"))
        ));
    }
}
