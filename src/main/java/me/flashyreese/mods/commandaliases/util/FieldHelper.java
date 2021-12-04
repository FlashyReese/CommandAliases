package me.flashyreese.mods.commandaliases.util;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

// For Java 12+ final field injection
// https://stackoverflow.com/questions/56039341/get-declared-fields-of-java-lang-reflect-fields-in-jdk12/
public final class FieldHelper {

    private static final VarHandle MODIFIERS;

    static {
        try {
            Module java_base = Field.class.getModule(), unnamed = FieldHelper.class.getModule();
            java_base.addOpens("java.lang.reflect", unnamed);
            java_base.addOpens("java.util", unnamed);
            final MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
            MODIFIERS = lookup.findVarHandle(Field.class, "modifiers", int.class);
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void makeNonFinal(final Field field) {
        final int mods = field.getModifiers();
        if (Modifier.isFinal(mods)) {
            MODIFIERS.set(field, mods & ~Modifier.FINAL);
        }
    }

    public static void setFinal(final Class<? extends Object> cls, final Object obj, final String fieldName, final Object value) throws NoSuchFieldException, IllegalAccessException {
        final Field field = cls.getDeclaredField(fieldName);

        final String[] javaVersion = System.getProperty("java.version").split("\\.");
        if (Integer.parseInt(javaVersion[0]) <= 11) {
            // Only Java 11 and lower
            final Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        } else {
            // Java 12 and above can't access final fields that easily
            FieldHelper.makeNonFinal(field);
        }

        field.setAccessible(true);
        field.set(obj, value);
    }

    public static void setFinal(final Object obj, final String fieldName, final Object value) throws NoSuchFieldException, IllegalAccessException {
        setFinal(obj.getClass(), obj, fieldName, value);
    }
}
