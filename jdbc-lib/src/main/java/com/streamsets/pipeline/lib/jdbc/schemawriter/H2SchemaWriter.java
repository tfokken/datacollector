/*
 * Copyright 2017 StreamSets Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.streamsets.pipeline.lib.jdbc.schemawriter;

import com.google.common.collect.ImmutableMap;
import com.streamsets.pipeline.api.Field;
import com.streamsets.pipeline.lib.jdbc.JdbcStageCheckedException;
import com.streamsets.pipeline.lib.jdbc.typesupport.JdbcType;
import com.streamsets.pipeline.lib.jdbc.typesupport.JdbcTypeInfo;
import com.zaxxer.hikari.HikariDataSource;

import java.util.LinkedHashMap;
import java.util.Map;

public class H2SchemaWriter extends JdbcAbstractSchemaWriter {
  private static final Map<JdbcType, String> jdbcTypeToName = ImmutableMap.<JdbcType, String>builder()
      .put(JdbcType.BIGINT, "bigint")
      .put(JdbcType.FLOAT, "real")
      .put(JdbcType.DOUBLE, "double precision")
      .put(JdbcType.DECIMAL, "numeric")
      .put(JdbcType.INTEGER, "integer")
      .put(JdbcType.CHAR, "character")
      .put(JdbcType.VARCHAR, "character varying")
      .put(JdbcType.DATE, "date")
      .put(JdbcType.TIME, "time")
      .put(JdbcType.TIMESTAMP, "timestamp")
      .put(JdbcType.BINARY, "bytea")
      .put(JdbcType.BOOLEAN, "boolean")
      .build();
  // Actual maxima are higher, but we just use H2 for testing
  private static final int MAX_SCALE = 1000000;
  private static final int MAX_PRECISION = 1000000;

  public H2SchemaWriter(HikariDataSource dataSource) {
    super(dataSource);
  }

  // H2 wants an ALTER TABLE command per column
  @Override
  protected String makeAlterTableSqlString(
      String schema, String tableName, LinkedHashMap<String, JdbcTypeInfo> columnDiff
  ) {
    String tableSchema = (schema == null) ? getDefaultSchema() : schema;
    StringBuilder sqlString = new StringBuilder();
    boolean first = true;
    for (Map.Entry<String, JdbcTypeInfo> entry : columnDiff.entrySet()) {
      if (first) {
        first = false;
      } else {
        sqlString.append("\n");
      }
      sqlString
          .append(ALTER_TABLE)
          .append(" ");
      if (tableSchema != null) {
        sqlString.append(tableSchema);
        sqlString.append(".");
      }
      sqlString.append(tableName)
          .append(" ")
          .append("ADD COLUMN")
          .append(" ")
          .append(entry.getKey())
          .append(" ")
          .append(entry.getValue().toString())
          .append(";");
    }

    return sqlString.toString();
  }

  @Override
  protected int getMaxScale() {
    return MAX_SCALE;
  }

  @Override
  protected int getMaxPrecision() {
    return MAX_PRECISION;
  }

  @Override
  public String getColumnTypeName(JdbcType jdbcType) {
    return jdbcTypeToName.get(jdbcType);
  }

  public static String getConnectionPrefix() {
    return "jdbc:h2:";
  }
}
