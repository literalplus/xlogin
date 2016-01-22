/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package io.github.xxyy.xlogin.common.module;

import com.google.common.base.Preconditions;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Manages a list of proxies from a file in the xLogin plugin folder.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 2016-01-10
 */
public class ProxyListManager {
    private Set<String> proxies = Collections.synchronizedSet(new HashSet<String>());
    private static final Pattern IP_PATTERN = Pattern.compile("(\\d{1,3}\\.){3}\\d{1,3}");

    /**
     * Adds an ip address in the form "000.000.000.000" to the proxy list.
     *
     * @param ip        the ip address string to add
     * @param proxyFile the file to persist the address to
     */
    public void addProxy(String ip, File proxyFile) {
        if (!IP_PATTERN.matcher(ip).matches()){
            throw new IllegalArgumentException("Invalid IP: " + ip);
        }
        proxies.add(ip);
        if (!proxyFile.exists()){
            try {
                Files.createDirectories(proxyFile.getParentFile().toPath());
                Files.createFile(proxyFile.toPath());
            } catch (IOException e) {
                throw new IllegalStateException("Could not create proxy list: " + e.getMessage(), e);
            }
        }

        try (FileWriter writer = new FileWriter(proxyFile, true)) {
            writer.append("\n").append(ip);
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
        return proxies.contains(address.getAddress().getHostAddress());
    }

    /**
     * Loads proxy definitions from all files in a directory and adds them to this manager.
     *
     * @param directory the directory to load from
     * @param logger    the logger to log errors to
     */
    public void loadFromDirectory(File directory, Logger logger) {
        Preconditions.checkNotNull(directory, "directory");
        if (!directory.isDirectory()){
            if (!directory.mkdirs()){
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
                if (IP_PATTERN.matcher(line).matches()){
                    proxies.add(line);
                }
            }
        }
    }

    /**
     * Clears this manager's proxy list.
     */
    public void clearProxyList() {
        proxies.clear();
    }
}
