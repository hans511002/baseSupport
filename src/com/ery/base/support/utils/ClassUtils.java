package com.ery.base.support.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import com.ery.base.support.log4j.LogUtils;


public class ClassUtils {

	private static DClassLoader classLoader = new DClassLoader();
	static {
		classLoader.setIgnoreConflict(true);
	}

	public static class DClassLoader extends URLClassLoader {

		boolean ignoreConflict = false;// 是否忽略冲突
		private Set<String> path = new HashSet<String>();

		public DClassLoader() {
			this(getSystemClassLoader());
		}

		public DClassLoader(ClassLoader parent) {
			super(new URL[] {}, parent);
		}

		
		public void setIgnoreConflict(boolean ignoreConflict) {
			this.ignoreConflict = ignoreConflict;
		}

		
		public void addURL(URL... urls) {
			if (urls != null) {
				for (URL url : urls) {
					if (!path.contains(url.getPath())) {
						super.addURL(url);
						path.add(url.getPath());
					}
				}
			}
		}

		
		public void addFile(File... files) throws IOException {
			if (files != null) {
				for (File file : files) {
					if (file != null) {
						URL url = file.toURI().toURL();
						if (!path.contains(url.getPath())) {
							super.addURL(url);
							path.add(url.getPath());
						}
					}
				}
			}
		}

		
		public Class<?> getClassByLoad(String name) throws Exception {
			Class<?> c = null;
			try {
				c = super.findSystemClass(name);
			} catch (ClassNotFoundException e) {
				return super.loadClass(name);
			}
			if (ignoreConflict) {
				LogUtils.warn("需载入的类[" + name + "]与系统类冲突,忽略冲突,返回系统类！");
				return c;
			} else {
				throw new Exception("需载入的类[" + name + "]与系统类冲突!");
			}
		}
	}

	public static class JavaStringObject extends SimpleJavaFileObject {
		private String code;

		public JavaStringObject(String name, String code) {
			// super(URI.create("string:///" + name.replace('.', '/') +
			// Kind.SOURCE.extension), Kind.SOURCE);
			super(URI.create(name + ".java"), Kind.SOURCE);
			this.code = code;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
			return code;
		}
	}

	
	public static Class getClassByJar(String jarName, String className) throws Exception {
		classLoader.addFile(new File(jarName));
		return classLoader.getClassByLoad(className);
	}

	
	public static Class getClassByCode(String javaCode, String className) throws Exception {
		String[] arr = className.split(".");
		JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
		JavaFileObject fileObject = new ClassUtils.JavaStringObject(arr[arr.length - 1], javaCode);
		JavaCompiler.CompilationTask task = javaCompiler.getTask(null, null, null,
				Arrays.asList("-d", ClassLoader.getSystemClassLoader().getResource("").getPath()), null,
				Arrays.asList(fileObject));
		boolean success = task.call();
		if (success) {
			return ClassLoader.getSystemClassLoader().loadClass(className);
		} else {
			throw new RuntimeException("编译失败");
		}
	}

	
	public static LinkedHashMap<String, Field> getAllFields(Class clazz, Class supClass, boolean hasPrivate) {
		LinkedHashMap<String, Field> fieldMap = new LinkedHashMap<String, Field>();
		Field[] fs = hasPrivate ? clazz.getDeclaredFields() : clazz.getFields();
		if (fs != null) {
			for (Field field : fs) {
				fieldMap.put(field.getName(), field);
			}
		}
		if (supClass.isAssignableFrom(clazz)) {
			for (clazz = clazz.getSuperclass();; clazz = clazz.getSuperclass()) {
				fs = hasPrivate ? clazz.getDeclaredFields() : clazz.getFields();
				if (fs != null) {
					for (Field field : fs) {
						if (!fieldMap.containsKey(field.getName())) {
							fieldMap.put(field.getName(), field);
						}
					}
				}
				if (clazz == supClass || clazz.equals(supClass)) {
					break;
				}
			}
		}
		return fieldMap;
	}

	public static List<Class<?>> getAllClassByInterface(Class<?> c) {
		String packageName = c.getPackage().getName();
		return getAllClassByInterface(c, packageName);
	}

	public static List<Class<?>> getAllClassByInterface(Class<?> c, String packageName) {
		// 返回结果
		List<Class<?>> allClass = new ArrayList<Class<?>>();
		// 如果不是一个接口，则不做处理
		try {
			// 获得当前包下以及子包下的所有类
			allClass = getClasses(packageName, c);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return allClass;
	}

	// 从一个包中查找出所有的类，在jar包中不能查找
	private static List<Class<?>> getClasses(String packageName, Class c) throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		System.err.println(classLoader.getResource("").toString());
		// 用'/'代替'.'路径
		String path = packageName.replace('.', '/');
		Enumeration<URL> dirs = classLoader.getResources(path);
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		while (dirs.hasMoreElements()) {
			URL url = dirs.nextElement();
			String protocol = url.getProtocol();
			if ("file".equals(protocol)) {
				LogUtils.info("扫描file类型的class文件....");
				String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
				findClassesByFile(classes, packageName, filePath, c);
			} else if ("jar".equals(protocol)) {
				LogUtils.info("扫描jar文件中的类....");
				findClassesByJar(packageName, url, classes, c);
			}
		}
		return classes;
	}

	private static void findClassesByFile(List<Class<?>> classes, String packageName, String packagePath, Class c) {
		File dir = new File(packagePath);
		if (!dir.exists() || !dir.isDirectory()) {
			return;
		}
		File[] dirfiles = dir.listFiles();
		for (File file : dirfiles) {
			if (file.isDirectory()) {
				findClassesByFile(classes, packageName + "." + file.getName(), file.getAbsolutePath(), c);
			} else {
				String className = file.getName().substring(0, file.getName().length() - 6);
				try {
					Class clazz = Thread.currentThread().getContextClassLoader()
							.loadClass(packageName + '.' + className);
					// clazz = Class.forName(packageName + '.' +
					// file.getName().substring(0, file.getName().length() -
					// 6));
					if (!c.equals(clazz) && (c.isAssignableFrom(clazz) || c.isAssignableFrom(clazz))) {
						classes.add(clazz);
					}
				} catch (ClassNotFoundException e) {
				}
			}
		}
	}

	
	private static void findClassesByJar(String basePackage, URL url, List<Class<?>> classes, Class c) {
		String packageName = basePackage;
		String package2Path = packageName.replace('.', '/');
		JarFile jar;
		try {
			jar = ((JarURLConnection) url.openConnection()).getJarFile();
			Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String name = entry.getName();
				if (!name.startsWith(package2Path) || entry.isDirectory()) {
					continue;
				}
				String classSimpleName = name.substring(name.lastIndexOf('/') + 1);
				if (!classSimpleName.endsWith(".class")) {
					continue;
				}
				String className = name.replace('/', '.');
				className = className.substring(0, className.length() - 6);
				try {
					Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
					if (!c.equals(clazz) && (c.isAssignableFrom(clazz) || c.isAssignableFrom(clazz))) {
						classes.add(clazz);
					}
				} catch (ClassNotFoundException e) {
					LogUtils.error("Class.forName error:", e);
				}
			}
		} catch (IOException e) {
			LogUtils.error("IOException error:", e);
		}
	}

}
