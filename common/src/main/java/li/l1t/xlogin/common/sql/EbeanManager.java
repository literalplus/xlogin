/*
 * xLogin - An advanced authentication application and awesome punishment management thing
 * Copyright (C) 2013 - 2017 Philipp Nowak (https://github.com/xxyy)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
