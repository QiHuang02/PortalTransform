package cn.qihuang02.project_dimension.register;

import cn.qihuang02.project_dimension.symbol.IDimensionSymbol;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class DimensionSymbolRegistry {
    private static final String SYMBOL_PACKAGE = "cn.qihuang02.project_dimension.symbol";
    private static final String SYMBOL_PACKAGE_PATH = SYMBOL_PACKAGE.replace('.', '/');
    private static final Type DIMENSION_SYMBOL_ANNOTATION = Type.getType(DimensionSymbol.class);
    private static final List<IDimensionSymbol> BUILTIN_SYMBOLS = discoverSymbols();
    private static final Map<String, IDimensionSymbol> BY_KEY = indexByKey();

    public static List<IDimensionSymbol> builtins() {
        return BUILTIN_SYMBOLS;
    }

    public static Map<String, IDimensionSymbol> byKey() {
        return BY_KEY;
    }

    public static List<IDimensionSymbol> compounds() {
        return BUILTIN_SYMBOLS.stream()
                .filter(IDimensionSymbol::compound)
                .toList();
    }

    private static Map<String, IDimensionSymbol> indexByKey() {
        Map<String, IDimensionSymbol> symbols = new LinkedHashMap<>();
        for (IDimensionSymbol symbol : BUILTIN_SYMBOLS) {
            if (symbols.put(symbol.key(), symbol) != null) {
                throw new IllegalStateException("重复注册象征：" + symbol.key());
            }
        }
        return Map.copyOf(symbols);
    }

    private static List<IDimensionSymbol> discoverSymbols() {
        // 生产环境使用 NeoForge 扫描结果，单元测试环境回退到 classpath 扫描。
        Set<String> classNames = new HashSet<>(discoverFromModScan());
        if (classNames.isEmpty()) {
            classNames.addAll(discoverFromClasspath());
        }

        return classNames.stream()
                .sorted()
                .map(DimensionSymbolRegistry::instantiate)
                .filter(Objects::nonNull)
                .toList();
    }

    private static Set<String> discoverFromModScan() {
        try {
            ModList modList = ModList.get();
            if (modList == null) {
                return Set.of();
            }

            Set<String> classNames = new HashSet<>();
            modList.getAllScanData().stream()
                    .map(ModFileScanData::getAnnotations)
                    .flatMap(Set::stream)
                    .filter(annotation -> DIMENSION_SYMBOL_ANNOTATION.equals(annotation.annotationType()))
                    .forEach(annotation -> classNames.add(annotation.clazz().getClassName()));
            return classNames;
        } catch (RuntimeException | LinkageError ignored) {
            return Set.of();
        }
    }

    private static Set<String> discoverFromClasspath() {
        Set<String> classNames = new HashSet<>();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> resources = loader.getResources(SYMBOL_PACKAGE_PATH);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if ("file".equals(resource.getProtocol())) {
                    scanDirectory(new File(resource.toURI()), SYMBOL_PACKAGE, classNames);
                } else if ("jar".equals(resource.getProtocol())) {
                    scanJar(resource, classNames);
                }
            }
        } catch (IOException | URISyntaxException ex) {
            throw new IllegalStateException("扫描维度象征类失败", ex);
        }
        return classNames;
    }

    private static void scanDirectory(File directory, String packageName, Set<String> classNames) {
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName(), classNames);
            } else if (file.getName().endsWith(".class")) {
                String simpleName = file.getName().substring(0, file.getName().length() - ".class".length());
                classNames.add(packageName + "." + simpleName);
            }
        }
    }

    private static void scanJar(URL resource, Set<String> classNames) throws IOException {
        JarURLConnection connection = (JarURLConnection) resource.openConnection();
        try (JarFile jar = connection.getJarFile()) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!entry.isDirectory() && name.startsWith(SYMBOL_PACKAGE_PATH) && name.endsWith(".class")) {
                    classNames.add(name.substring(0, name.length() - ".class".length()).replace('/', '.'));
                }
            }
        }
    }

    private static IDimensionSymbol instantiate(String className) {
        try {
            Class<?> type = Class.forName(className);
            if (!IDimensionSymbol.class.isAssignableFrom(type)
                    || !type.isAnnotationPresent(DimensionSymbol.class)
                    || type.isInterface()
                    || Modifier.isAbstract(type.getModifiers())) {
                return null;
            }
            return (IDimensionSymbol) type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("创建维度象征实例失败：" + className, ex);
        }
    }
}
