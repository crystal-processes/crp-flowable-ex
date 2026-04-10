package org.crp.flowable.mpc.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class DeveloperService {

    private final JdbcTemplate jdbcTemplate;

    public DeveloperService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Tool(description = """
    Provides maximum variable count per process instance. Returns 
    def_id_ - deployed definition id,
    key_ - process definition key matches with the process model id in the bpmn file,
    var_count_ - maximum count of variables per the process instance. Too many variables can indicate design issue.
    
    The problem could be that process definition is already outdated and currently deployed definition is fixed already.
    """)
    public String maxVariablesPerProcessDefinition() {
        String sql = """
                select proc_def_count.def_id_, proc_def_count.key_, max(proc_def_count.var_count_) from (
                    select prc.id_, prc.proc_def_id_ as def_id_, count(*) as var_count_ from act_hi_varinst as var
                    left join act_hi_procinst as prc on var.proc_inst_id_ = prc.id_
                    left join act_re_procdef as def on prc.proc_def_id_ = def.id_
                    group by prc.id_, prc.proc_def_id_, def.key_
                ) as proc_def_count
                group by def_id_
        """;
        return jdbcTemplate.queryForList(sql).toString();
    }

    @Tool(description = """
    Provides list of variables per process definition limited by types (input parameter). Usual complex variable types are:
    bytes, serializable, longString, jpa-entity-list.
    Returns 
    id_ - process instance,
    key_ - process definition key matches with the process model id in the bpmn file,
    var_count_ - maximum count of variables per the process instance. Too many variables can indicate design issue.
    
    The problem could be that process definition is already outdated and currently deployed definition is fixed already.
    """)
    public String variableTypes(Collection<String> types) {
        String sql = """
                select prc.id_ as id_, prc.proc_def_id_, def.key_, var.name_, var.var_type_ from act_hi_varinst as var
                                        where var.type_ in (:types)
                                        left join act_hi_procinst as prc on var.proc_inst_id_ = prc.id_
                                        left join act_re_procdef as def on prc.proc_def_id_ = def.id_
                                        group by prc.id_, prc.proc_def_id_, def.key_, var.name_, var.var_type_
        """;
        return jdbcTemplate.queryForList(sql, types).toString();
    }
}
