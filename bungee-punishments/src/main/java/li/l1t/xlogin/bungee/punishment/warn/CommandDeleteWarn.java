/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package li.l1t.xlogin.bungee.punishment.warn;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import li.l1t.common.XycConstants;
import li.l1t.common.bungee.ChatHelper;
import li.l1t.xlogin.bungee.JSONChatHelper;
import li.l1t.xlogin.common.api.punishments.XLoginWarning;
import li.l1t.xlogin.common.authedplayer.AuthedPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * A command which allows to delete warnings.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 28.8.14
 */
class CommandDeleteWarn extends Command implements TabExecutor {
    public static final String PERMISSION = "xlogin.delwarn";
    private final WarnModule module;


    public CommandDeleteWarn(WarnModule module) {
        super("delwarn", PERMISSION, "dw");
        this.module = module;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0 && !args[0].equalsIgnoreCase("help")) {
            try {
                ExecutionBuilder exec = new ExecutionBuilder(sender, args);
                List<WarningInfo> matched = exec.getMatching();

                if (matched.isEmpty()) {
                    throw new IllegalArgumentException("Keine Verwarnungen passen zu deinen Argumenten!");
                }

                for (WarningInfo warning : matched) {
                    boolean hasPermission = exec.checkPermission(warning);
                    if (hasPermission) { //Sends a message by itself if permission is denied
                        if (exec.getFlags().contains(DelWarnFlag.FORCE)) {
                            warning.delete();
                        } else if (exec.getFlags().contains(DelWarnFlag.UNKNOWN_REASON)) {
                            warning.setState(XLoginWarning.WarningState.UNKNOWN_REASON).save();
                        } else if (exec.getFlags().contains(DelWarnFlag.REMOVE_FLAGS)) {
                            warning.setState(XLoginWarning.WarningState.VALID).save();
                        } else {
                            warning.setState(XLoginWarning.WarningState.INVALID).save();
                        }
                    }

                    sender.sendMessage(new ComponentBuilder("Verwarnung: ").color(ChatColor.YELLOW)
                            .append("target=" + warning.getTargetName(module.getPlugin().getRepository()) + ", ")
                            .append("source=" + warning.getSourceName(module.getPlugin().getRepository()) + ", ")
                            .append("reason=" + ChatColor.translateAlternateColorCodes('&', warning.getReason()))
                            .append("state=" + warning.getState().name())
                            .append((hasPermission ? " " : " nicht ") + "modifiziert.")
                            .create());
                }

                return;
            } catch (IllegalArgumentException e) {
                sender.sendMessage(new ComponentBuilder("Fehler: ").color(ChatColor.RED)
                        .append(e.getMessage()).color(ChatColor.YELLOW).create());
            }
        }

        sender.sendMessage(new ComponentBuilder("Verwendung: ").color(ChatColor.RED)
                .append("/dw [-f|-u|-r] <Spieler> [Anzahl]").color(ChatColor.YELLOW).create());
        sender.sendMessage(new ComponentBuilder("Verwendung: ").color(ChatColor.RED)
                .append("/dw [-f|-u|-r] -i [ID]]").color(ChatColor.YELLOW).create());
        sender.sendMessage(new ComponentBuilder("Verwendung: ").color(ChatColor.RED)
                .append("/dw [-f|-u|-r] -l").color(ChatColor.YELLOW).create());
        for (DelWarnFlag flag : DelWarnFlag.values()) {
            sender.sendMessage(new ComponentBuilder(flag.getFlag() + ": " + flag.getDescription()).color(ChatColor.YELLOW).create());
        }
        sender.sendMessage(new ComponentBuilder("Arr-gumente sind order-sensitive!").color(ChatColor.YELLOW).create());
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length > 1) {
            return ImmutableSet.of();
        }

        Set<String> matches = new HashSet<>();

        if (args.length == 1) {
            String search = args[0].toLowerCase();
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                if (player.getName().toLowerCase().startsWith(search)) {
                    matches.add(player.getName());
                }
            }
        } else {
            for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                matches.add(player.getName());
            }
        }

        return matches;
    }

    enum DelWarnFlag {
        FORCE('f', "Löscht den Warn endgültig (nicht empfohlen))", "xlogin.delwarn.force"),
        UNKNOWN_REASON('u', "Markiert den Warn als 'unbekannter Grund", null),
        REMOVE_FLAGS('r', "Entfernt alle Markierungen (f|u)", "xlogin.delwarn.clear"),
        LAST('l', "Wählt den letzten von dir vergebenen Warn", null),
        ID('i', "Wählt einen Warn nach seiner ID", null);

        private final String flag;
        private final String description;
        private final String permission;

        DelWarnFlag(char flag, String description, String permission) {
            this.description = description;
            this.permission = permission;
            this.flag = "-" + flag;
        }

        public String getFlag() {
            return flag;
        }

        public boolean is(String arg) {
            return getFlag().equalsIgnoreCase(arg);
        }

        public String getDescription() {
            return description;
        }

        public String getPermission() {
            return permission;
        }
    }

    private class ExecutionBuilder {
        private final CommandSender sender;
        private final String[] args;
        private EnumSet<DelWarnFlag> flags = EnumSet.noneOf(DelWarnFlag.class);
        private int argStartIndex;

        private ExecutionBuilder(CommandSender sender, String[] args) throws IllegalArgumentException {
            this.sender = sender;
            this.args = args;
            parseFlags();
        }

        private void parseFlags() throws IllegalArgumentException {
            int i;
            for (i = 0; i < args.length; i++) {
                boolean isFlag = false;
                for (DelWarnFlag flag : DelWarnFlag.values()) {
                    if (flag.is(args[i])) {
                        isFlag = true;
                        flags.add(flag);
                        break;
                    }
                }
                if (!isFlag) {
                    argStartIndex = i;
                    return;
                }
            } //All arguments must be flags when we reach this
            if (!flags.contains(DelWarnFlag.LAST)) {
                throw new IllegalArgumentException("Du musst zumindest einen Parameter angeben (der keine Flag ist)!");
            }
            argStartIndex = i;
        }

        public List<WarningInfo> getMatching() throws IllegalArgumentException {
            if (flags.contains(DelWarnFlag.ID)) {
                if (!StringUtils.isNumeric(args[argStartIndex])) {
                    throw new IllegalArgumentException("Parameter für -i muss eine Zahl sein! (Gefunden: " + args[argStartIndex] + ")");
                }
                return ImmutableList.of(validateWINotNull(module.getWarning(Integer.parseInt(args[argStartIndex]))));
            } else if (flags.contains(DelWarnFlag.LAST)) {
                return ImmutableList.of(validateWINotNull(module.getLastIssuedBy(ChatHelper.getSenderId(sender))));
            }

            int amount = 1;
            if (args.length >= argStartIndex + 2) {
                if (!StringUtils.isNumeric(args[argStartIndex + 1])) {
                    throw new IllegalArgumentException("Parameter 2 muss eine Zahl sein! (Gefunden: " + args[argStartIndex + 1] + ")");
                }
                amount = Integer.parseInt(args[argStartIndex + 1]);
            }

            List<AuthedPlayer> matchedPlayers = module.getPlugin().getRepository().getProfiles(args[argStartIndex]);
            if (matchedPlayers.isEmpty()) {
                throw new IllegalArgumentException("Keine Spieler mit diesem Namen/dieser UUID gefunden!");
            } else if (matchedPlayers.size() > 1) {
                JSONChatHelper.listPossiblePlayers(sender, matchedPlayers, module.getPlugin(),
                        "Hier klicken, um " + amount + " Verwarnungen\ndieses Spielers zu bearbeiten.",
                        String.format("dw %s%%s %d", getFlagString(), amount)); //getFlagString() may return an empty string
            }

            List<WarningInfo> warnings = module.getWarningsByTarget(matchedPlayers.get(0).getUniqueId());
            List<WarningInfo> toDelete = new ArrayList<>(amount);
            for (int i = warnings.size() - 1; i >= 0; i--) {
                if (toDelete.size() < amount) {
                    toDelete.add(warnings.get(i)); //There needs to be a less brute-force way to do this
                }
            }

            return toDelete;
        }

        public WarningInfo validateWINotNull(WarningInfo warningInfo) {
            if (warningInfo == null) {
                throw new IllegalArgumentException("Keine passende Verwarnung gefunden!");
            }
            return warningInfo;
        }

        public boolean checkPermission(WarningInfo warningInfo) {
            for (DelWarnFlag flag : flags) {
                if (flag.getPermission() != null && !sender.hasPermission(flag.getPermission())) {
                    sender.sendMessage(new ComponentBuilder("Du darfst diese Flag nicht verwenden: " + flag.name() + " (" + flag.getFlag() + ")").color(ChatColor.RED).create());
                    return false;
                }
            }

            if (!sender.hasPermission("xlogin.delwarn.others") && !warningInfo.getSourceId().equals(ChatHelper.getSenderId(sender))
                    && !warningInfo.getSourceId().equals(XycConstants.NIL_UUID)) { //allow everyone to delete automatic warns since they tend to be inaccurate
                sender.sendMessage(new ComponentBuilder("Du darfst keine Verwarnungen Anderer löschen!").color(ChatColor.RED).create());
                return false;
            }

            return true;
        }

        public EnumSet<DelWarnFlag> getFlags() {
            return flags;
        }

        public String getFlagString() {
            if (flags.isEmpty()) {
                return "";
            }

            StringBuilder stringBuilder = new StringBuilder();

            for (DelWarnFlag flag : flags) {
                stringBuilder.append(flag.getFlag()).append(" ");
            }

            return stringBuilder.toString();
        }
    }
}
