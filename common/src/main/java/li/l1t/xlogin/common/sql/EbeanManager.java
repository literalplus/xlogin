/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package li.l1t.xlogin.common.sql;

/**
 * Helps managing Avaje Ebean.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 14.5.14
 */
public final class EbeanManager {
//    public static final String EBEAN_SERVER_NAME = "xlocommon_ebean";
//
//    private static EbeanServer ebean;
//
//    private EbeanManager() {
//    }
//
//    /**
//     * Sets the singleton Ebean server.
//     * If a server is already set, this throws an exception.
//     *
//     * @param ebeanServer Ebean server to save or {@code null} to remove the currently stored Ebean server.
//     * @return The current {@link #getEbean()}.
//     * @see #getEbean()
//     */
//    public static EbeanServer setEbean(final EbeanServer ebeanServer) {
//        Validate.isTrue(ebeanServer == null || ebean == null, "Cannot re-set singleton EbeanManager#ebean!");
//
//        ebean = ebeanServer;
//
//        return ebean;
//    }


//    public static EbeanServer initialise(SqlConnectable sqlConnectable) {
//        ServerConfig config = new ServerConfig();
//
//        DataSourceConfig sqlDbConfig = new DataSourceConfig();
//        sqlDbConfig.setUsername(sqlConnectable.getSqlUser());
//        sqlDbConfig.setPassword(sqlConnectable.getSqlPwd());
//        sqlDbConfig.setUrl(SqlConnectables.getHostString(sqlConnectable));
//        sqlDbConfig.setDriver(com.mysql.jdbc.Driver.class.getName());
//        config.setDataSourceConfig(sqlDbConfig);
//        config.setClasses(Arrays.asList(IpAddress.class, AuthedPlayer.class, Session.class, FailedLoginAttempt.class));
//
//        MysqlDataSource dataSource = new MysqlDataSource();
//        dataSource.setPassword(sqlConnectable.getSqlPwd());
//        dataSource.setUser(sqlConnectable.getSqlUser());
//        dataSource.setUrl(SqlConnectables.getHostString(sqlConnectable));
//        dataSource.setURL(dataSource.getUrl()); //TODO
//        config.setDataSource(dataSource);
//        System.out.println(sqlConnectable.getSqlHost());
//        System.out.println(dataSource.getUrl());
//
//        config.setDdlGenerate(true);
//        config.setDdlRun(true);
//
//        config.setName(EBEAN_SERVER_NAME);
//        config.setDefaultServer(false);
//        config.setRegister(false);
//
//        AutofetchConfig autofetchConfig = new AutofetchConfig();
//
//        autofetchConfig.setQueryTuning(true);
//        autofetchConfig.setProfiling(true);
//        autofetchConfig.setProfilingRate(0.05F);
//        autofetchConfig.setProfilingMin(4);
//        autofetchConfig.setProfilingBase(10);
//        autofetchConfig.setMode(AutofetchMode.DEFAULT_ON);
//        config.setAutofetchConfig(autofetchConfig);
//
//        EbeanServer ebeanServer = EbeanServerFactory.create(config);
//
//        if (getEbean() == null) {
//            setEbean(ebeanServer);
//        }
//
//        return ebeanServer;
//    }
//
//    public static EbeanServer getEbean() {
//        return EbeanManager.ebean;
//    }
}
