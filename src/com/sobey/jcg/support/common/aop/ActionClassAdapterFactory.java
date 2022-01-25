package com.sobey.jcg.support.common.aop;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.sobey.jcg.support.log4j.LogUtils;

public class ActionClassAdapterFactory implements Opcodes {

	private static ActionClassAdapterFactory instance = new ActionClassAdapterFactory();

	private GeneratorClassLoader loader = new GeneratorClassLoader(this.getClass().getClassLoader());

	private ActionClassAdapterFactory() {
	}

	public static ActionClassAdapterFactory getInstance() {
		return instance;
	}

	public Class<?> getInterface(Class<?> clazz) throws ClassNotFoundException {
		String suffix = "$ASM";// 接口 名的后缀
		String className = classNameConvert(clazz.getCanonicalName());// 转换类名,将.转换为/
		String interfaceClassName = className + suffix; // 接口名
		String interfaceName = convertClassName(interfaceClassName);
		try {
			if (loader.contains(interfaceName)) {
				return loader.findClass(interfaceName);
			}
			Class<?> c = loader.loadClass(interfaceName);
			if (c != null) {
				return c;
			}
		} catch (Exception e1) {
		}
		// new一个类写入器
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);

		// 定义一个接口,此接口继承于java.lang.Object
		writer.visit(V1_1, ACC_PUBLIC + ACC_ABSTRACT + ACC_INTERFACE, interfaceClassName, null, "java/lang/Object",
				null);

		// 定义在接口中定义方法
		Method[] methods = clazz.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (method.getModifiers() == Modifier.PUBLIC) {
				MethodVisitor visitor = writer.visitMethod(ACC_PUBLIC + ACC_ABSTRACT, method.getName(),
						Type.getMethodDescriptor(method), null, null);
				visitor.visitMaxs(2, 2);
				visitor.visitEnd();
			}
		}

		// 将已定义好的接口class输出到一个byte数组中
		byte[] data = writer.toByteArray();

		// 加载并返回接口的class对象
		return loader.defineClass(clazz.getCanonicalName() + suffix, data);
	}

	public Class<?> getAdapter(Class<?> clazz, String interfaceName) throws ClassNotFoundException {
		String suffix = "$ASMImpl";// 子类的后缀
		String className = classNameConvert(clazz.getCanonicalName());// 转换类名,将.转换为/
		String childClassName = className + suffix;// 子类名称
		String childImplClassName = convertClassName(childClassName);// 子类名称
		try {
			if (loader.contains(childImplClassName)) {
				return loader.findClass(childImplClassName);
			}
			Class<?> c = loader.loadClass(childImplClassName);
			if (c != null) {
				return c;
			}
		} catch (Exception e1) {
		}
		// new一个类写入器
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);

		// 定义一个类,此类继承于clazz,并实现interfaceName接口
		writer.visit(V1_1, ACC_PUBLIC, childClassName, null, className,
				new String[] { classNameConvert(interfaceName) });

		// 添加构造方法
		MethodVisitor visitor = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		visitor.visitVarInsn(ALOAD, 0);
		visitor.visitMethodInsn(INVOKESPECIAL, className, "<init>", "()V");
		visitor.visitInsn(RETURN);
		visitor.visitMaxs(1, 1);
		visitor.visitEnd();

		// 将已定义好的接口class输出到一个byte数组中
		byte[] data = writer.toByteArray();

		// 加载并返回接口的class对象
		return loader.defineClass(clazz.getCanonicalName() + suffix, data);
	}

	// ----------------------------------------------private methods-------------------------------------

	private String classNameConvert(String className) {
		return className.replaceAll("\\.", "/");
	}

	private String convertClassName(String name) {
		return name.replaceAll("/", "\\.");
	}

	// ----------------------------------------------inner class-------------------------------------------------------
	class GeneratorClassLoader extends ClassLoader {
		public GeneratorClassLoader(ClassLoader parent) {
			super(parent);
		}

		public Class<?> defineClass(String className, byte[] data) throws ClassNotFoundException {
			try {
				Class<?> clazz = super.defineClass(className, data, 0, data.length);
				super.resolveClass(clazz);
				return clazz;
			} catch (Exception e) {
				LogUtils.error("加载动态生成的类失败：" + className, e);
			}
			return null;
		}

		public boolean contains(String className) {
			boolean rtn = false;
			try {
				super.findClass(className);
				rtn = true;
			} catch (Exception e) {
			}
			return rtn;
		}

		public Class<?> findClass(String name) throws ClassNotFoundException {
			return super.findClass(name);
			// try {
			// } catch (ClassNotFoundException e1) {
			// try {
			// return super.loadClass(name);
			// } catch (ClassNotFoundException e) {
			// throw e;
			// }
			// }
		}
	}
}
