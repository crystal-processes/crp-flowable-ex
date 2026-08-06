/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.content.engine.impl.db;

import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.impl.db.EngineSchemaManagerLockConfiguration;
import org.flowable.common.engine.impl.db.EngineSqlScriptBasedDbSchemaManager;
import org.flowable.content.engine.ContentEngine;
import org.flowable.content.engine.impl.util.CommandContextUtil;

import java.util.Map;

public class ContentDbSchemaManager extends EngineSqlScriptBasedDbSchemaManager {

    protected static final String CONTENT_DB_SCHEMA_LOCK_NAME = "contentDbSchemaLock";
    protected static final Map<String, String> changeLogVersionMap = Map.ofEntries(
            Map.entry("1", "6.0.0.5"),
            Map.entry("2", "6.1.1.0"),
            Map.entry("3", "6.3.0.0"),
            Map.entry("4", "6.3.1.0"),
            Map.entry("5", "6.4.0.0"),
            Map.entry("6", "6.4.1.3"),
            Map.entry("7", "6.6.0.0"),
            Map.entry("8", "6.6.0.0"),
            Map.entry("9", "6.8.0.0"),
            Map.entry("10", "7.1.0.0")
    );
    public ContentDbSchemaManager() {
        super("content", new EngineSchemaManagerLockConfiguration(CommandContextUtil::getContentEngineConfiguration));
    }

    @Override
    protected String getResourcesRootDirectory() {
        return "org/flowable/content/db";
    }

    @Override
    protected String getEngineVersion() {
        return ContentEngine.VERSION;
    }

    @Override
    protected String getSchemaVersionPropertyName() {
        return "content.schema.version";
    }

    @Override
    protected String getDbSchemaLockName() {
        return CONTENT_DB_SCHEMA_LOCK_NAME;
    }

    @Override
    protected String getEngineTableName() {
        return "ACT_CO_CONTENT_ITEM";
    }

    @Override
    protected String getChangeLogTableName() {
        return "ACT_CO_DATABASECHANGELOG";
    }

    @Override
    protected String getDbVersionForChangelogVersion(String changeLogVersion) {
        if (StringUtils.isNotEmpty(changeLogVersion) && changeLogVersionMap.containsKey(changeLogVersion)) {
            return changeLogVersionMap.get(changeLogVersion);
        }
        return "5.99.0.0";
    }

}
