package org.crp.flowable.mcp.service;

import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeveloperService {

    private final SqlSessionFactory sqlSessionFactory;

    public DeveloperService(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    /**
     * Maximum variable count per process definition.
     * @param defId deployed definition id
     * @param key process definition key matches with the process model id in the bpmn file
     * @param varCount maximum count of variables per the process instance
     */
    public record MaxVariableCount(String defId, String key, Long varCount) {}

    /**
     * Variable information.
     * @param id process instance
     * @param key process definition key matches with the process model id in the bpmn file
     * @param name variable name
     * @param type variable type
     */
    public record VariableInfo(String id, String key, String name, String type) {}

    /**
     * Dead letter job information.
     * @param id job ID
     * @param type job type
     * @param handlerType job handler type
     * @param handlerConfig job handler configuration
     * @param exceptionMessage exception message from the last failure
     * @param createTime when the job was created
     * @param elementId BPMN element ID where the job failed
     * @param processInstanceId ID of the process instance
     * @param procDefId process definition ID
     * @param key_ process definition key associated with the process model
     */
    public record DeadLetterJob(String id, String type, String handlerType, String handlerConfig,
                                String exceptionMessage, Instant createTime, String elementId,
                                String processInstanceId, String procDefId, String key_) {}

    /**
     * Detailed dead letter job information including full exception stacktrace.
     * @param jobId job ID
     * @param type job type
     * @param handlerType job handler type
     * @param handlerConfig job handler configuration
     * @param exceptionMessage exception message from the last failure
     * @param exceptionStacktrace the full exception stacktrace as a string
     * @param createTime when the job was created
     * @param elementId BPMN element ID where the job failed
     * @param processInstanceId ID of the process instance
     * @param procDefId process definition ID
     * @param key_ process definition key associated with the process model
     */
    public record DeadLetterJobDetail(String jobId, String type, String handlerType, String handlerConfig,
                                      String exceptionMessage, String exceptionStacktrace, Instant createTime,
                                      String elementId, String processInstanceId, String procDefId, String key_) {}

    /**
     * Failing runtime job information.
     * @param id job ID
     * @param type job type
     * @param handlerType job handler type
     * @param handlerConfig job handler configuration
     * @param retries number of retries remaining
     * @param exceptionMessage exception message from the last failure
     * @param createTime when the job was created
     * @param elementId BPMN element ID where the job failed
     * @param processInstanceId ID of the process instance
     * @param procDefId process definition ID
     * @param key_ process definition key associated with the process model
     */
    public record FailingRuntimeJob(String id, String type, String handlerType, String handlerConfig,
                                    Integer retries, String exceptionMessage, Instant createTime,
                                    String elementId, String processInstanceId, String procDefId, String key_) {}

    /**
     * Long-running transaction information.
     * @param actId activity instance ID
     * @param actName activity instance name
     * @param transactionOrder the transaction order value (highest per process definition)
     * @param key process definition key associated with the process model
     * @param procDefId process definition ID
     */
    public record LongRunningTransaction(String actId, String actName, Long transactionOrder,
                                          String key, String procDefId) {}

    /**
     * Case variable information.
     * @param id variable ID
     * @param scopeId case instance ID (scope ID)
     * @param scopeType scope type (e.g., "cmmn")
     * @param scopeDefinitionId case definition ID (scope definition ID)
     * @param scopeDefinitionKey case definition key
     * @param name variable name
     * @param type variable type
     */
    public record CaseVariableInfo(String id, String scopeId, String scopeType, String scopeDefinitionId,
                                   String scopeDefinitionKey, String name, String type) {}

    /**
     * Case activity information.
     * @param actId activity ID
     * @param actName activity name
     * @param scopeId case instance ID (scope ID)
     * @param scopeType scope type (e.g., "cmmn")
     * @param scopeDefinitionId case definition ID (scope definition ID)
     * @param scopeDefinitionKey case definition key
     */
    public record CaseActivityInfo(String actId, String actName, String scopeId, String scopeType,
                                   String scopeDefinitionId, String scopeDefinitionKey) {}

    /**
     * Case dead letter job information.
     * @param id job ID
     * @param type job type
     * @param handlerType job handler type
     * @param handlerConfig job handler configuration
     * @param exceptionMessage exception message from the last failure
     * @param createTime when the job was created
     * @param scopeId case instance ID (scope ID)
     * @param scopeType scope type (e.g., "cmmn")
     * @param scopeDefinitionId case definition ID (scope definition ID)
     * @param scopeDefinitionKey case definition key
     */
    public record CaseDeadLetterJob(String id, String type, String handlerType, String handlerConfig,
                                     String exceptionMessage, Instant createTime, String scopeId,
                                     String scopeType, String scopeDefinitionId, String scopeDefinitionKey) {}

    /**
     * Detailed case dead letter job information including full exception stacktrace.
     * @param jobId job ID
     * @param type job type
     * @param handlerType job handler type
     * @param handlerConfig job handler configuration
     * @param exceptionMessage exception message from the last failure
     * @param exceptionStacktrace the full exception stacktrace as a string
     * @param createTime when the job was created
     * @param scopeId case instance ID (scope ID)
     * @param scopeType scope type (e.g., "cmmn")
     * @param scopeDefinitionId case definition ID (scope definition ID)
     * @param scopeDefinitionKey case definition key
     */
    public record CaseDeadLetterJobDetail(String jobId, String type, String handlerType, String handlerConfig,
                                          String exceptionMessage, String exceptionStacktrace, Instant createTime,
                                          String scopeId, String scopeType, String scopeDefinitionId,
                                          String scopeDefinitionKey) {}

    /**
     * Case failing runtime job information.
     * @param id job ID
     * @param type job type
     * @param handlerType job handler type
     * @param handlerConfig job handler configuration
     * @param retries number of retries remaining
     * @param exceptionMessage exception message from the last failure
     * @param createTime when the job was created
     * @param scopeId case instance ID (scope ID)
     * @param scopeType scope type (e.g., "cmmn")
     * @param scopeDefinitionId case definition ID (scope definition ID)
     * @param scopeDefinitionKey case definition key
     */
    public record CaseFailingRuntimeJob(String id, String type, String handlerType, String handlerConfig,
                                        Integer retries, String exceptionMessage, Instant createTime,
                                        String scopeId, String scopeType, String scopeDefinitionId,
                                        String scopeDefinitionKey) {}

    @Tool(description = """
            Provides maximum variable count per process instance.
            Too many variables can indicate design issue.
            """)
    public List<MaxVariableCount> maxVariablesPerProcessDefinition(
            @ToolParam(description = "Process definition key to filter by", required = false) String definitionKey,
            @ToolParam(description = "Minimum start time for process instances", required = false) Instant startedAfter,
            @ToolParam(description = "Limit results to most recent deployments (null or <=0 means all, 1 the latest)", required = false) Integer latestDeployments) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            return getSelectList(sqlSession, "findMaxVariablesPerProcessDefinition", ParametersBuilder.create()
                    .add("definitionKey", definitionKey)
                    .add("startedAfter", startedAfter)
                    .add("latestDeployments", latestDeployments)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> getSelectList(SqlSession sqlSession, String statement, Object params) {
        return sqlSession.selectList(statement, params, new RowBounds(0, 50));
    }

    @Tool(description = """
            Provides list of variables per process definition limited by types.
            The problem could be that process definition is already outdated and currently deployed definition is fixed already.
            """)
    public List<VariableInfo> variableTypes(
            @ToolParam(description = "Process definition key to filter by", required = false) String definitionKey,
            @ToolParam(description = "Variable types to filter by", required = false) Collection<String> types,
            @ToolParam(description = "Minimum start time for process instances", required = false) Instant startedAfter,
            @ToolParam(description = "Limit results to most recent deployments (null or <=0 means all, 1 the latest)", required = false) Integer latestDeployments) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            return getSelectList(sqlSession, "findVariableByTypes", ParametersBuilder.create()
                    .add("definitionKey", definitionKey)
                    .add("types", types)
                    .add("startedAfter", startedAfter)
                    .add("latestDeployments", latestDeployments)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Tool(description = """
            Provides list of dead letter jobs.
            Jobs are ordered by createTime DESC.
            DeadLetter job indicates a serious problem in the execution.
            """)
    public List<DeadLetterJob> deadLetterJobs(
            @ToolParam(description = "Process definition key to filter by", required = false) String definitionKey,
            @ToolParam(description = "Minimum job creation time", required = false) Instant startedAfter,
            @ToolParam(description = "Limit results to most recent deployments (null or <=0 means all, 1 the latest)", required = false) Integer latestDeployments) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            return getSelectList(sqlSession, "findDeadLetterJobs", ParametersBuilder.create()
                    .add("definitionKey", definitionKey)
                    .add("startedAfter", startedAfter)
                    .add("latestDeployments", latestDeployments)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Tool(description = """
            Provides detailed information about dead letter jobs including the full exception stacktrace.
            Jobs are ordered by createTime DESC.
            """)
    public List<DeadLetterJobDetail> deadLetterJobDetails(
            @ToolParam(description = "Specific dead letter job ID to retrieve", required = false) String jobId,
            @ToolParam(description = "Process definition key to filter by", required = false) String definitionKey,
            @ToolParam(description = "Minimum job creation time", required = false) Instant startedAfter,
            @ToolParam(description = "Limit results to most recent deployments (null or <=0 means all, 1 the latest)", required = false) Integer latestDeployments) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            return getSelectList(sqlSession, "findDeadLetterJobDetails", ParametersBuilder.create()
                    .add("jobId", jobId)
                    .add("definitionKey", definitionKey)
                    .add("startedAfter", startedAfter)
                    .add("latestDeployments", latestDeployments)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Tool(description = """
            Provides list of failing runtime jobs.
            Jobs are ordered by createTime DESC.
            Only jobs with exception_msg_ are returned.
            """)
    public List<FailingRuntimeJob> failingRuntimeJobs(
            @ToolParam(description = "Process definition key to filter by", required = false) String definitionKey,
            @ToolParam(description = "Minimum job creation time", required = false) Instant startedAfter,
            @ToolParam(description = "Limit results to most recent deployments (null or <=0 means all, 1 the latest)", required = false) Integer latestDeployments) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            return getSelectList(sqlSession, "findFailingRuntimeJobs", ParametersBuilder.create()
                    .add("definitionKey", definitionKey)
                    .add("startedAfter", startedAfter)
                    .add("latestDeployments", latestDeployments)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Tool(description = """
            Provides list of activity instances with the highest transaction order for each process definition.
            High transaction order values indicate long-running process paths.
            """)
    public List<LongRunningTransaction> longRunningTransaction(
            @ToolParam(description = "Process definition key to filter by", required = false) String definitionKey,
            @ToolParam(description = "Limit results to most recent deployments (null or <=0 means all, 1 the latest)", required = false) Integer latestDeployments) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            return getSelectList(sqlSession, "longRunningTransaction", ParametersBuilder.create()
                    .add("definitionKey", definitionKey)
                    .add("latestDeployments", latestDeployments)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Tool(description = """
            Provides list of case variables.
            Variables are ordered by createTime DESC.
            scopeId is the case instance ID.
            """)
    public List<CaseVariableInfo> caseVariables(
            @ToolParam(description = "Case definition key to filter by", required = false) String caseDefinitionKey,
            @ToolParam(description = "Variable types to filter by", required = false) Collection<String> types,
            @ToolParam(description = "Minimum case start time", required = false) Instant startedAfter,
            @ToolParam(description = "Limit results to most recent deployments (null or <=0 means all, 1 the latest)", required = false) Integer latestDeployments) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            return getSelectList(sqlSession, "findCaseVariables", ParametersBuilder.create()
                    .add("caseDefinitionKey", caseDefinitionKey)
                    .add("types", types)
                    .add("startedAfter", startedAfter)
                    .add("latestDeployments", latestDeployments)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Tool(description = """
            Provides list of case dead letter jobs.
            Jobs are ordered by createTime DESC.
            scopeId is the case instance ID.
            """)
    public List<CaseDeadLetterJob> caseDeadLetterJobs(
            @ToolParam(description = "Case definition key to filter by", required = false) String caseDefinitionKey,
            @ToolParam(description = "Minimum job creation time", required = false) Instant startedAfter,
            @ToolParam(description = "Limit results to most recent deployments (null or <=0 means all, 1 the latest)", required = false) Integer latestDeployments) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            return getSelectList(sqlSession, "findCaseDeadLetterJobs", ParametersBuilder.create()
                    .add("caseDefinitionKey", caseDefinitionKey)
                    .add("startedAfter", startedAfter)
                    .add("latestDeployments", latestDeployments)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Tool(description = """
            Provides detailed information about case dead letter jobs including the full exception stacktrace.
            Jobs are ordered by createTime DESC.
            scopeId is the case instance ID.
            """)
    public List<CaseDeadLetterJobDetail> caseDeadLetterJobDetails(
            @ToolParam(description = "Specific dead letter job ID to retrieve", required = false) String jobId,
            @ToolParam(description = "Case definition key to filter by", required = false) String caseDefinitionKey,
            @ToolParam(description = "Minimum job creation time", required = false) Instant startedAfter,
            @ToolParam(description = "Limit results to most recent deployments (null or <=0 means all, 1 the latest)", required = false) Integer latestDeployments) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            return getSelectList(sqlSession, "findCaseDeadLetterJobDetails", ParametersBuilder.create()
                    .add("jobId", jobId)
                    .add("caseDefinitionKey", caseDefinitionKey)
                    .add("startedAfter", startedAfter)
                    .add("latestDeployments", latestDeployments)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Tool(description = """
            Provides list of case failing runtime jobs.
            Jobs are ordered by createTime DESC.
            Only jobs with exception_msg_ are returned.
            scopeId is the case instance ID.
            """)
    public List<CaseFailingRuntimeJob> caseFailingRuntimeJobs(
            @ToolParam(description = "Case definition key to filter by", required = false) String caseDefinitionKey,
            @ToolParam(description = "Minimum job creation time", required = false) Instant startedAfter,
            @ToolParam(description = "Limit results to most recent deployments (null or <=0 means all, 1 the latest)", required = false) Integer latestDeployments) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            return getSelectList(sqlSession, "findCaseFailingRuntimeJobs", ParametersBuilder.create()
                    .add("caseDefinitionKey", caseDefinitionKey)
                    .add("startedAfter", startedAfter)
                    .add("latestDeployments", latestDeployments)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Tool(description = """
            Provides list of case activities.
            Activities are ordered by createTime DESC.
            scopeId is the case instance ID.
            """)
    public List<CaseActivityInfo> caseActivities(
            @ToolParam(description = "Case definition key to filter by", required = false) String caseDefinitionKey,
            @ToolParam(description = "Minimum case start time", required = false) Instant startedAfter,
            @ToolParam(description = "Limit results to most recent deployments (null or <=0 means all, 1 the latest)", required = false) Integer latestDeployments) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            return getSelectList(sqlSession, "findCaseActivities", ParametersBuilder.create()
                    .add("caseDefinitionKey", caseDefinitionKey)
                    .add("startedAfter", startedAfter)
                    .add("latestDeployments", latestDeployments)
                    .build());
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
