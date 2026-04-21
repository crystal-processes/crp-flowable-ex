package org.crp.flowable.mcp.config;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.crp.flowable.mcp.service.DeveloperService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

@ConditionalOnProperty(name = "crp.flowable.mcp.enabled", havingValue = "true")
@AutoConfiguration
@EnableConfigurationProperties({
        FlowableMcpProperties.class
})
public class FlowableMcpServerConfiguration {

    @Bean
    public ToolCallbackProvider flowableTools(DeveloperService developerService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(developerService)
                .build();
    }

    @Bean
    public DeveloperService developerService(SqlSessionFactory sqlSessionFactory) {
        return new DeveloperService(sqlSessionFactory);
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(FlowableMcpProperties properties, DataSource dataSource) throws IOException {
        try (InputStream inputStream = Resources.getResourceAsStream(properties.getMappingConfig())) {
            Reader reader = new InputStreamReader(inputStream);
            Properties props = new Properties();
            props.put("prefix", properties.getDatatablePrefix());
            JdbcTransactionFactory transactionFactory = new JdbcTransactionFactory();
            Environment environment = new Environment("default", transactionFactory, dataSource);
            XMLConfigBuilder parser = new XMLConfigBuilder(reader, "", props);
            Configuration configuration = parser.parse();
            configuration.setEnvironment(environment);

            //configuration.addMapper(VariableMapper.class);
                SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
                return builder.build(configuration);
        }
    }
}