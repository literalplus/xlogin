package io.github.xxyy.xlogin.common.sql;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.AutofetchConfig;
import com.avaje.ebean.config.AutofetchMode;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import io.github.xxyy.common.sql.SqlConnectable;
import io.github.xxyy.common.sql.SqlConnectables;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.Validate;

/**
 * Helps managing Avaje Ebean.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 14.5.14
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EbeanManager {
    public static final String EBEAN_SERVER_NAME = "xlocommon_ebean";

    @Getter
    private static EbeanServer ebean;

    /**
     * Sets the singleton Ebean server.
     * If a server is already set, this throws an exception.
     *
     * @param ebeanServer Ebean server to save or {@code null} to remove the currently stored Ebean server.
     * @return The current {@link #getEbean()}.
     * @see #getEbean()
     */
    public static EbeanServer setEbean(final EbeanServer ebeanServer) {
        Validate.isTrue(ebeanServer == null || ebean == null, "Cannot re-set singleton EbeanManager#ebean!");

        ebean = ebeanServer;

        return ebean;
    }


    public static EbeanServer initialise(SqlConnectable sqlConnectable) {
        ServerConfig config = new ServerConfig();
        config.setName(EBEAN_SERVER_NAME);

        DataSourceConfig sqlDbConfig = new DataSourceConfig();
        sqlDbConfig.setUsername(sqlConnectable.getSqlUser());
        sqlDbConfig.setPassword(sqlConnectable.getSqlPwd());
        sqlDbConfig.setUrl(SqlConnectables.getHostString(sqlConnectable));
        config.setDataSourceConfig(sqlDbConfig);

        config.setDdlGenerate(true);
        config.setDdlRun(true);

        config.setDefaultServer(false);

        AutofetchConfig autofetchConfig = new AutofetchConfig();

        autofetchConfig.setQueryTuning(true);
        autofetchConfig.setProfiling(true);
        autofetchConfig.setProfilingRate(0.05F);
        autofetchConfig.setProfilingMin(4);
        autofetchConfig.setProfilingBase(10);
        autofetchConfig.setMode(AutofetchMode.DEFAULT_ON);
        config.setAutofetchConfig(autofetchConfig);

        EbeanServer ebeanServer = EbeanServerFactory.create(config);

        if (getEbean() == null) {
            setEbean(ebeanServer);
        }

        return ebeanServer;
    }
}
