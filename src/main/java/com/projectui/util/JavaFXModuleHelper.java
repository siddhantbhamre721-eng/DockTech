package com.projectui.util;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import sun.misc.Unsafe;

public class JavaFXModuleHelper {

    private static boolean initialized = false;

    public static synchronized void allowJavaFXWebAccess() {
        if (initialized) return;
        try {
            Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            Unsafe unsafe = (Unsafe) theUnsafeField.get(null);

            Field implLookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            Object base = unsafe.staticFieldBase(implLookupField);
            long offset = unsafe.staticFieldOffset(implLookupField);
            MethodHandles.Lookup implLookup = (MethodHandles.Lookup) unsafe.getObject(base, offset);

            Method implAddExports = Module.class.getDeclaredMethod("implAddExports", String.class, Module.class);
            Method implAddOpens = Module.class.getDeclaredMethod("implAddOpens", String.class, Module.class);

            Module unnamedModule = JavaFXModuleHelper.class.getClassLoader().getUnnamedModule();

            ModuleLayer bootLayer = ModuleLayer.boot();
            if (bootLayer != null) {
                for (Module m : bootLayer.modules()) {
                    if (m.getName() != null && (m.getName().startsWith("javafx.") || m.getName().startsWith("java.base"))) {
                        for (String pkg : m.getPackages()) {
                            try {
                                implLookup.unreflect(implAddExports).invoke(m, pkg, unnamedModule);
                                implLookup.unreflect(implAddOpens).invoke(m, pkg, unnamedModule);
                            } catch (Throwable ignored) {}
                        }
                    }
                }
            }

            Class<?>[] jfxClasses = new Class<?>[]{
                    javafx.scene.Node.class,
                    javafx.scene.control.Control.class,
                    javafx.stage.Stage.class,
                    javafx.beans.Observable.class
            };
            for (Class<?> cls : jfxClasses) {
                try {
                    Module m = cls.getModule();
                    if (m != null && m.isNamed()) {
                        for (String pkg : m.getPackages()) {
                            try {
                                implLookup.unreflect(implAddExports).invoke(m, pkg, unnamedModule);
                                implLookup.unreflect(implAddOpens).invoke(m, pkg, unnamedModule);
                            } catch (Throwable ignored) {}
                        }
                    }
                } catch (Throwable ignored) {}
            }

            initialized = true;
        } catch (Throwable t) {
            System.err.println("[MODULE HELPER] JavaFX internal module access notice: " + t.getMessage());
        }
    }
}
