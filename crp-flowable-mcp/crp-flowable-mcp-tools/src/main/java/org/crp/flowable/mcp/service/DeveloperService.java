package org.crp.flowable.mcp.service;

import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
    
    Input parameters used to limit query only to:
    definitionKey - String representing the process definition key to filter by (optional). definitionKey maps to process
                    model id.
    startedAfter - Instant representing the minimum start time for process instances (inclusive)
    latestDeployments - Integer limiting results to only the most recent deployments (null or <=0 means all deployments, 1 latest)
    
    The problem could be that process definition is already outdated and currently deployed definition is fixed already.
    """)
    public String maxVariablesPerProcessDefinition(String definitionKey, Instant startedAfter, Integer latestDeployments) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            return getSelectList(sqlSession, "findMaxVariablesPerProcessDefinition", ParametersBuilder.create()
                    .add("definitionKey", definitionKey)
                    .add("startedAfter", startedAfter)
                    .add("latestDeployments", latestDeployments)
                    .build()).toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Object> getSelectList(SqlSession sqlSession, String statement, Object params) {
        return sqlSession.selectList(statement, params, new RowBounds(0, 50));
    }

    @Tool(description = """
    Provides list of variables per process definition limited by types. Usual complex variable types are:
    bytes, serializable, longString, jpa-entity-list.
    Input parameters used to limit query only to:
    definitionKey - String representing the process definition key to filter by (optional). definitionKey maps to process
                    model id.
    types - collection of types
    startedAfter - Instant representing the minimum start time for process instances (inclusive)
    latestDeployments - Integer limiting results to only the most recent deployments (null or <=0 means all deployments)
    
    The method returns array of:
    id_ - process instance,
    key_ - process definition key matches with the process model id in the bpmn file,
    name_ - variable name,
    type_ - variable type.
    
    The problem could be that process definition is already outdated and currently deployed definition is fixed already.
    """)
    public String variableTypes(String definitionKey, Collection<String> types, Instant startedAfter, Integer latestDeployments) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            return getSelectList(sqlSession, "findVariableByTypes", ParametersBuilder.create()
                    .add("definitionKey", definitionKey)
                    .add("types", types)
                    .add("startedAfter", startedAfter)
                    .add("latestDeployments", latestDeployments)
                    .build())
                    .toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Tool(description = """
    Provides list of dead letter jobs. Dead letter jobs are failed jobs that have exhausted all retries.
    Returns information about failed jobs including:
    id_ - job ID,
    type_ - job type,
    handler_type_ - job handler type,
    handler_config_ - job handler configuration,
    exception_message_ - exception message from the last failure,
    create_time_ - when the job was created,
    element_id_ - BPMN element ID where the job failed,
    process_instance_id_ - ID of the process instance,
    proc_def_id_ - process definition ID,
    key_ - process definition key associated with the process model.
    
    Jobs are ordered by create_time_ DESC (newest first).
    
    Input parameters used to limit query only to:
    definitionKey - String representing the process definition key to filter by (optional)  definitionKey maps to process
                    model id.
    startedAfter - Instant representing the minimum job creation time (inclusive)
    latestDeployments - Integer limiting results to only the most recent deployments (null or <=0 means all deployments)
    
    DeadLetter job indicates a serious problem in the execution, which needs immediate attention.
    The problem could be that the process definition is already outdated and currently deployed definition is fixed already.
    """)
    public String deadLetterJobs(String definitionKey, Instant startedAfter, Integer latestDeployments) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            return getSelectList(sqlSession, "findDeadLetterJobs", ParametersBuilder.create()
                    .add("definitionKey", definitionKey)
                    .add("startedAfter", startedAfter)
                    .add("latestDeployments", latestDeployments)
                    .build()).toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("javadoc")
    @Tool(description = """
    Provides list of failing runtime jobs. These are jobs that have failed but still have retries remaining.
    Returns information about failing jobs including:
    id_ - job ID,
    type_ - job type,
    handler_type_ - job handler type,
    handler_config_ - job handler configuration,
    retries_ - number of retries remaining,
    exception_message_ - exception message from the last failure,
    create_time_ - when the job was created,
    element_id_ - BPMN element ID where the job failed,
    process_instance_id_ - ID of the process instance,
    proc_def_id_ - process definition ID,
    key_ - process definition key associated with the process model.
    
    Jobs are ordered by create_time_ DESC (newest first).
    Only jobs with exception_msg_ (failed jobs) are returned.
    
    Input parameters used to limit query only to:
    definitionKey - String representing the process definition key to filter by (optional). definitionKey maps to process
                    model id.
    startedAfter - Instant representing the minimum job creation time (inclusive)
    latestDeployments - Integer limiting results to only the most recent deployments (null or <=0 means all deployments)
    
    Failing runtime jobs indicate ongoing execution problems that may resolve automatically through retries.
    However, persistent failures suggest underlying issues that need investigation.
    The problem could be that the process definition is already outdated and currently deployed definition is fixed already.
    """)
    public String failingRuntimeJobs(String definitionKey, Instant startedAfter, Integer latestDeployments) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            return getSelectList(sqlSession, "findFailingRuntimeJobs", ParametersBuilder.create()
                    .add("definitionKey", definitionKey)
                    .add("startedAfter", startedAfter)
                    .add("latestDeployments", latestDeployments)
                    .build()).toString();
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

