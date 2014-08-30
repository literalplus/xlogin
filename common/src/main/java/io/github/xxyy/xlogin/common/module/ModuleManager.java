package io.github.xxyy.xlogin.common.module;

import org.jetbrains.annotations.NotNull;

import io.github.xxyy.lib.intellij_annotations.Nullable;
import io.github.xxyy.xlogin.bungee.XLoginBungee;
import io.github.xxyy.xlogin.common.module.annotation.CanHasPotato;
import io.github.xxyy.xlogin.common.module.annotation.Module;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
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
    public final void enable(Class<? extends XLoginModule>... moduleClasses) {
        for (Class<? extends XLoginModule> clazz : moduleClasses) {
            enableModule(clazz, null);
        }
    }

    public XLoginModule getModule(Class<? extends XLoginModule> clazz) {
        return getModule(clazz.getSimpleName());
    }

    public XLoginModule getModule(String moduleName) {
        return modules.get(moduleName);
    }

    //clazz == null to request plugin instance
    private boolean enableModule(@NotNull Class<?> inputClazz, @Nullable Class<? extends XLoginModule> causedBy) {
        if (plugin.getClass().isAssignableFrom(inputClazz) || modules.containsKey(inputClazz.getSimpleName())) {
            return true;
        }
        if (!XLoginModule.class.isAssignableFrom(inputClazz)) {
            plugin.getLogger().warning("[" + causedBy.getSimpleName() + "] Can only inject plugin or modules! (Requested: " + inputClazz.getName() + ")");
            return false;
        }
        @SuppressWarnings("unchecked") Class<? extends XLoginModule> clazz = (Class<? extends XLoginModule>) inputClazz;

        Module modAnnotation = clazz.getDeclaredAnnotation(Module.class);
        boolean enableByDefault = false;
        if (modAnnotation != null) {
            enableByDefault = modAnnotation.enableByDefault();
            for (Class<? extends XLoginModule> depClass : modAnnotation.dependencies()) {
                if (depClass.equals(causedBy)) {
                    plugin.getLogger().warning("[" + clazz.getSimpleName() + " <-> " + causedBy.getSimpleName() + "] Skipping: Cyclic dependency detected!");
                }

                if (!enableModule(depClass, clazz)) {
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
            injectPotato(field, clazz, instance);
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
            plugin.getLogger().warning("[" + clazz.getSimpleName() + "] Successfully enabled!");
        }

        return true; //Was about time
    }

    @SuppressWarnings("unchecked")
    private void injectPotato(Field field, Class<? extends XLoginModule> clazz, XLoginModule instance) {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        CanHasPotato potato = field.getAnnotation(CanHasPotato.class);
        if (potato != null) {
            Object toInject = null;
            if (plugin.getClass().isAssignableFrom(potato.value())) {
                toInject = plugin;
            } else { //enableModule() checks if it is actually a module class
                if (enableModule(potato.value(), clazz)) {
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

    private XLoginModule obtainInstance(Class<? extends XLoginModule> clazz) {
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
