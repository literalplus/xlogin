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

package li.l1t.xlogin.bungee.misc;

import com.google.common.base.Preconditions;
import com.timgroup.statsd.StatsDClient;
import org.apache.commons.net.util.SubnetUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Manages a list of proxy IP ranges from a file in the xLogin plugin folder. Ranges are defined in CIDR format, and the
 * manager offers add, contains, clear and load operations.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-01-10
 */
public class ProxyListManager {
    private static final Pattern IPV4_PATTERN = Pattern.compile("(\\d{1,3}\\.){3}\\d{1,3}");
    private static final Pattern CIDRV4_PATTERN = Pattern.compile(
            "(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})/(\\d{1,3})"
    ); //used by Apache commons-net, so copying here, you never know
    private final SubnetUtils.SubnetInfo dummySubnet = new SubnetUtils("127.0.0.1/32").getInfo(); //because that one method is not static
    private final StatsDClient statsd;
    private List<SubnetUtils.SubnetInfo> proxyRanges = new ArrayList<>();

    public ProxyListManager(StatsDClient statsd) {
        this.statsd = statsd;
    }

    /**
     * Adds an IP address range in CIDR notation to the list of proxy ranges.
     *
     * @param cidr      the CIDR notation of the range to add
     * @param proxyFile the file to persist the address to
     * @throws IllegalStateException    if the range could not be added to the file
     * @throws IllegalArgumentException if the range notation doesn't follow CIDR format (e.g. 192.168.1.1/24)
     */
    public void addProxy(String cidr, File proxyFile) {
        SubnetUtils subnetUtils = new SubnetUtils(cidr);
        subnetUtils.setInclusiveHostCount(true);
        proxyRanges.add(subnetUtils.getInfo());
        if (!proxyFile.exists()) {
            try {
                Files.createDirectories(proxyFile.getParentFile().toPath());
                Files.createFile(proxyFile.toPath());
            } catch (IOException e) {
                throw new IllegalStateException("Could not create proxy list: " + e.getMessage(), e);
            }
        }

        try (FileWriter writer = new FileWriter(proxyFile, true)) {
            writer.append("\n").append(cidr);
        } catch (IOException e) {
            throw new IllegalStateException("Could not update proxy list: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if given address is a blocked proxy known to this manager.
     *
     * @param address the address to check
     * @return whether given address is a known proxy
     */
    public boolean isBlockedProxy(InetSocketAddress address) {
        long startMs = System.currentTimeMillis();
        int ipInt = dummySubnet.asInteger(address.getAddress().getHostAddress()); //wtf Apache why is this not static
        for (SubnetUtils.SubnetInfo subnetInfo : proxyRanges) {
            if (subnetInfo.isInRange(ipInt)) {
                return true;
            }
        }
        statsd.recordExecutionTimeToNow("proxy-check", startMs);
        return false;
    }

    /**
     * Loads proxy definitions from all files in a directory and adds them to this manager.
     *
     * @param directory the directory to load from
     * @param logger    the logger to log errors to
     */
    public void loadFromDirectory(File directory, Logger logger) {
        Preconditions.checkNotNull(directory, "directory");
        if (!directory.isDirectory()) {
            if (!directory.mkdirs()) {
                logger.warning("Could not create proxy list directory: " + directory.getName());
                return;
            }
        }

        //isDirectory check above prevents null
        //noinspection ConstantConditions
        for (File file : directory.listFiles()) {
            try {
                loadFromFile(file);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Could not load a proxy list file: " + file.getName(), e);
            }
        }
    }

    /**
     * Loads proxy definitions from a file and adds them to this manager.
     *
     * @param file the file to load from
     * @throws IOException if an error occurs while loading
     */
    public void loadFromFile(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!CIDRV4_PATTERN.matcher(line).matches()) {
                    if (IPV4_PATTERN.matcher(line).matches()) {
                        line += "/32"; //If we don't have a subnet specification, just use that address
                    } else {
                        continue;
                    }
                } //We check ourselves to save the exception overhead - not sure about what the actual impact is though
                SubnetUtils subnetUtils = new SubnetUtils(line);
                subnetUtils.setInclusiveHostCount(true);
                proxyRanges.add(subnetUtils.getInfo());
            }
        }
    }

    /**
     * Clears this manager's proxy list.
     */
    public void clearProxyList() {
        proxyRanges.clear();
    }
}
