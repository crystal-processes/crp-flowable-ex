package org.crp.flowable.mcp.service;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class DeveloperService {

    private final SqlSessionFactory sqlSessionFactory;

    public DeveloperService(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Tool(description = """
    Provides maximum variable count per process instance. Returns 
    def_id_ - deployed definition id,
    key_ - process definition key matches with the process model id in the bpmn file,
    var_count_ - maximum count of variables per the process instance. Too many variables can indicate design issue.
    
    The problem could be that process definition is already outdated and currently deployed definition is fixed already.
    """)
    public String maxVariablesPerProcessDefinition() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            return sqlSession.selectList("findMaxVariablesPerProcessDefinition").toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Tool(description = """
    Provides list of variables per process definition limited by types. Usual complex variable types are:
    bytes, serializable, longString, jpa-entity-list.
    Input parametersused to limit query only to: 
    processDefinitionKey
    types - collection of types
    
    The method returns array of:
    id_ - process instance,
    key_ - process definition key matches with the process model id in the bpmn file,
    name_ - variable name,
    type_ - variable type.
    
    The problem could be that process definition is already outdated and currently deployed definition is fixed already.
    """)
    public String variableTypes(String processDefinitionKey, Collection<String> types) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            return sqlSession.selectList("findVariableByTypes",
                    ParametersBuilder.create()
                            .add("processDefinitionKey", processDefinitionKey)
                            .add("types", types)
                            .build()
                    )
                    .toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class ParametersBuilder {
        Map<String, Object> parameters;

        private ParametersBuilder() {
            parameters = new HashMap<>();
        }

        public static ParametersBuilder create() {
            return new ParametersBuilder();
        }

        ParametersBuilder add(String key, Object value) {
            if (StringUtils.hasText(key)) {
                parameters.put(key, value);
            }
            return this;
        }

        Map<String, Object> build() {
            return parameters;
        }
    }
}

