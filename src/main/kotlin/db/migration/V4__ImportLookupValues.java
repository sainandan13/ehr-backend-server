package db.migration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

/**
 * Flyway Java migration for V3: enable pgcrypto, alter id default,
 * and import lookup_values from CSV on the classpath.
 */
public class V4__ImportLookupValues extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        // Unwrap to the Postgres-specific BaseConnection:
        BaseConnection pgConnection = context.getConnection()
                .unwrap(BaseConnection.class);

        // 1) Enable pgcrypto extension
        try (var stmt = context.getConnection().createStatement()) {
            stmt.execute("CREATE EXTENSION IF NOT EXISTS pgcrypto");
        }

        // 2) Alter id column default to gen_random_uuid()
        try (var stmt = context.getConnection().createStatement()) {
            stmt.execute(
                    "ALTER TABLE lookup_values " +
                            "ALTER COLUMN id SET DEFAULT gen_random_uuid()"
            );
        }

        // 3) Load the CSV via CopyManager
        CopyManager copyManager = new CopyManager(pgConnection);

        // CSV must live on the classpath under /data/lookup_values.csv
        try (InputStream csv =
                     getClass().getResourceAsStream("/data/lookup_values.csv")) {
            if (csv == null) {
                throw new IllegalStateException(
                        "Could not find /data/lookup_values.csv on classpath"
                );
            }

            String copySql =
                    "COPY lookup_values(" +
                            "  category, code, display_name, sort_order, active, created_at, updated_at" +
                            ") FROM STDIN WITH (FORMAT csv, HEADER true)";

            copyManager.copyIn(
                    copySql,
                    new InputStreamReader(csv, StandardCharsets.UTF_8)
            );
        }
    }
}
