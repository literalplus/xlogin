/*
 * Copyright (C) 2014-2016 Philipp Nowak (Literallie; xxyy98+xlo@gmail.com; The Author)
 *
 * This application and all related code, assets and concepts are protected by international Copyright laws.
 * Any usage, including, but not limited to, decompilation, execution, compilation and distribution,
 *  is explicitly and strictly prohibited without explicit written permission from The Author.
 * Any such permission can be revoked at any time.
 * Legal steps may be taken in case of a violation of these terms.
 */

package li.l1t.xlogin.common.module;


import li.l1t.xlogin.bungee.XLoginBungee;
import li.l1t.xlogin.common.module.annotation.CanHasPotato;
import li.l1t.xlogin.common.module.annotation.Module;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Apparently we also have a module manager now. It takes care of modules
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 28.8.14
 */ //Some time, we need to move this into XYC. Me and my potato pet.
public class ModuleManager {
    private final XLoginBungee plugin;
    private final Map<String, XLoginModule> modules = new HashMap<>();

    public ModuleManager(XLoginBungee plugin) {
        this.plugin = plugin;
    }

    @SafeVarargs
    public final void enable(@Nonnull Class<? extends XLoginModule>... moduleClasses) {
        for (Class<? extends XLoginModule> clazz : moduleClasses) {
            enableModule(clazz, null);
        }
    }

    public XLoginModule getModule(@Nonnull Class<? extends XLoginModule> clazz) {
        return getModule(clazz.getSimpleName());
    }

    public XLoginModule getModule(String moduleName) {
        return modules.get(moduleName);
    }

    private boolean enableModule(@Nonnull Class<?> inputClazz, @Nullable List<Class<? extends XLoginModule>> causedBy) {
        if (causedBy == null) {
            causedBy = new ArrayList<>();
        } else {
            causedBy = new ArrayList<>(causedBy);
        }
        if (inputClazz.isAssignableFrom(plugin.getClass()) || modules.containsKey(inputClazz.getSimpleName())) {
            return true;
        }
        if (!XLoginModule.class.isAssignableFrom(inputClazz)) {
            if (!causedBy.isEmpty()) {
                plugin.getLogger().warning("[" + causedBy.get(0).getSimpleName() + "] Can only inject plugin or modules! (Requested: " + inputClazz.getName() + ")");
            }
            return false;
        }
        @SuppressWarnings("unchecked") Class<? extends XLoginModule> clazz = (Class<? extends XLoginModule>) inputClazz;
        causedBy.add(clazz);

        Module modAnnotation = clazz.getAnnotation(Module.class);
        boolean enableByDefault = false;
        if (modAnnotation != null) {
            enableByDefault = modAnnotation.enableByDefault();
            for (Class<? extends XLoginModule> depClass : modAnnotation.dependencies()) {
                if (causedBy.contains(depClass)) {
                    plugin.getLogger().warning("[" + clazz.getSimpleName() + " <-> " + causedBy.get(0).getSimpleName() + "] Skipping: Cyclic dependency detected!");
                }

                if (!enableModule(depClass, causedBy)) { //causedBy already has `clazz`
                    plugin.getLogger().warning("[" + clazz.getSimpleName() + "] Skipping: Missing dependency: " + depClass.getSimpleName() + "!");
                    return false;
                }
            }
        }

        XLoginModule instance = obtainInstance(clazz);

        if (instance == null) {
            return false;
        }

        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            injectPotato(field, instance, causedBy);
        }

        modules.put(clazz.getSimpleName(), instance);
        Boolean enabled = plugin.getConfig().getEnabledModules().get(clazz.getSimpleName());
        if (enabled == null) {
            plugin.getConfig().getEnabledModules().put(clazz.getSimpleName(), enableByDefault);
            enabled = enableByDefault;
        }

        if (enabled) {
            instance.enabled = true;
            try {
                instance.enable();
            } catch (Exception e) {
                instance.enabled = false;
                plugin.getLogger().warning("[" + clazz.getSimpleName() + "] Skipping: Encountered exception while enabling!");
                e.printStackTrace();
                return false;
            }
            plugin.getLogger().info("[" + clazz.getSimpleName() + "] Successfully enabled!");
        } else {
            plugin.getLogger().info("[" + clazz.getSimpleName() + "] Disabled in config, skipping!");
        }

        return true; //Was about time
    }

    @SuppressWarnings("unchecked")
    private void injectPotato(@Nonnull Field field, XLoginModule instance, List<Class<? extends XLoginModule>> causedBy) {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        CanHasPotato potato = field.getAnnotation(CanHasPotato.class);
        if (potato != null) {
            Object toInject = null;
            if (potato.value().isAssignableFrom(plugin.getClass())) {
                toInject = plugin;
            } else { //enableModule() checks if it is actually a module class
                if (enableModule(potato.value(), causedBy)) { //causedBy already has `clazz`
                    toInject = getModule((Class<? extends XLoginModule>) potato.value());
                }
            }

            try {
                field.set(instance, toInject);
            } catch (IllegalAccessException ignore) {
                //Can't happen
            }
        }
    }

    private XLoginModule obtainInstance(@Nonnull Class<? extends XLoginModule> clazz) {
        try {
            Constructor<? extends XLoginModule> constructor = clazz.getConstructor();
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            plugin.getLogger().warning("[" + clazz.getSimpleName() + "] Skipping: Could not create instance: Missing default constructor!");
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            plugin.getLogger().warning("[" + clazz.getSimpleName() + "] Skipping: Sorry, an error occurred while obtaining an instance:");
            e.printStackTrace();
        }

        return null;
    }
}
