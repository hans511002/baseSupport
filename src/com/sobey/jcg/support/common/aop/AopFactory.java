package com.sobey.jcg.support.common.aop;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.sobey.jcg.support.log4j.LogUtils;

public class AopFactory implements Opcodes {

	/**
	 * 根据原对象Class和对应的hander处理器生成一个被拦截了的对象。 注：原对象一定要有默认构造器
	 * 
	 * @param sourceClass
	 * @param handler
	 * @param methdIncludes
	 *            欲进行拦截的方法正则表达式集，如为null拦截所有的方法。
	 * @param filter
	 *            方法过滤接口，如果不为null，在正则表达式过滤之后执行此filter。
	 * @return
	 */
	public static Object getInstance(Class<?> sourceClass, InvokeHandler handler, String[] methdIncludes, IAopMethodFilter filter) {
		String subClassName = sourceClass.getCanonicalName() + "$ASM";
		Class<?> subClass = null;
		try {
			subClass = Class.forName(subClassName);
		} catch (ClassNotFoundException e) {
			// 出错未生成此类。用ASM操作字节码生成一个新的class，此Class继承于 sourceClass
			try {
				subClass = new ClassLoader(sourceClass.getClassLoader()) {
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
				}.defineClass(subClassName, getSubClassByte(sourceClass, methdIncludes, filter));
			} catch (ClassNotFoundException e1) {
				LogUtils.error("出现错误！", e1);
			}
		}
		if (subClass != null) {
			try {
				Object obj = subClass.newInstance();
				// 初始化Invoker对象。
				java.lang.reflect.Method method = subClass.getMethod("setInvokeHander", InvokeHandler.class);
				method.invoke(obj, handler);
				return obj;
			} catch (Exception e) {
				LogUtils.error("出现错误！", e);
			}
		}
		return null;
	}

	/**
	 * 根据原对象Class和对应的hander处理器生成一个被拦截了的对象。
	 * 
	 * @param sourceClass
	 * @param handler
	 * @param methdIncludes
	 * @return
	 */
	public static Object getInstance(Class<?> sourceClass, InvokeHandler handler, String[] methdIncludes) {
		return getInstance(sourceClass, handler, methdIncludes, null);
	}

	/**
	 * 根据原对象Class和对应的hander处理器生成一个被拦截了的对象。 注：远对象一定要有默认构造器
	 * 
	 * @param sourceClass
	 * @param handler
	 * @return
	 */
	public static Object getInstance(Class<?> sourceClass, InvokeHandler handler) {
		return getInstance(sourceClass, handler, null, null);
	}

	/**
	 * 获取生成的class二进制串
	 * 
	 * @param sourceClass
	 *            欲进行拦截的方法正则表达式，如为null拦截所有的方法。
	 * @param methodIncludes
	 * @return
	 */
	private static byte[] getSubClassByte(Class<?> sourceClass, String[] methodIncludes, IAopMethodFilter filter) {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		// 开始访问类
		String sourceInterName = Type.getType(sourceClass).getInternalName();
		String subInterName = sourceInterName + "$ASM";

		cw.visit(V1_1, ACC_PUBLIC, subInterName, null, sourceInterName, null);

		Type invokeType = Type.getType(InvokeHandler.class);
		// 添加一个字段invkoeHander
		FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE, "invokeHander", invokeType.getDescriptor(), null, null);
		fv.visitEnd();

		// 添加一个默认的构造函数。
		MethodVisitor mw = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mw.visitCode();
		mw.visitVarInsn(ALOAD, 0);
		mw.visitMethodInsn(INVOKESPECIAL, sourceInterName, "<init>", "()V");
		mw.visitInsn(RETURN);
		mw.visitMaxs(0, 0); // 自动计算退栈信息
		mw.visitEnd();

		// 为新生成的类新增一个方法setInvokeHander,用于设置拦截器
		mw = cw.visitMethod(ACC_PUBLIC, "setInvokeHander", "(" + invokeType.getDescriptor() + ")V", null, null);
		mw.visitCode();
		mw.visitVarInsn(ALOAD, 0);
		mw.visitVarInsn(ALOAD, 1);
		mw.visitFieldInsn(PUTFIELD, subInterName, "invokeHander", invokeType.getDescriptor());
		mw.visitInsn(RETURN);
		mw.visitMaxs(0, 0);
		mw.visitEnd();

		/**
		 * 开始进行方法修改与拷贝，加入InvokerHander逻辑。
		 */
		List<MethodNode> methods = getMethodNodes(sourceClass);
		for (MethodNode methodNode : methods) {
			if (methodNode.name.equals("<init>") || methodNode.access == ACC_PRIVATE) {// 构造函数不用继承，私有函数不虚继承
				continue;
			}
			// 进行方法过滤
			if (methodIncludes != null && methodIncludes.length > 0) {
				boolean isContinue = false;
				for (String partternStr : methodIncludes) {
					Pattern pattern = Pattern.compile(partternStr);
					Matcher matcher = pattern.matcher(methodNode.name);
					if (matcher.matches()) {
						isContinue = true;
						break;
					}
				}
				if (!isContinue) {
					continue;
				}
			}
			Type[] argTypes = Type.getArgumentTypes(methodNode.desc);
			Type returnType = Type.getReturnType(methodNode.desc);
			// System.out.print(returnType + " " + methodNode.name + "(");
			// for (Type type : argTypes) {
			// System.out.print(type.toString() + " ,");
			// }
			// System.out.println(") Opcode=" + returnType.getOpcode(IRETURN));
			// if ("queryForLongByNvl".equals(methodNode.name)) {
			// System.out.println(" returnType=" + returnType);
			// }
			// if ("queryForLong".equals(methodNode.name)) {
			// System.out.println(methodNode.name);
			// }
			if (filter != null) {
				Class<?>[] argClass = new Class[argTypes.length];
				int i = 0;
				for (Type type : argTypes) {
					argClass[i++] = getWarpClass(type);
				}
				if (!filter.filter(sourceClass, methodNode.name, argClass)) {
					continue;
				}
			}

			// 新增方法
			MethodVisitor visitor = cw.visitMethod(ACC_PUBLIC, methodNode.name, methodNode.desc, methodNode.signature,
			        (String[]) methodNode.exceptions.toArray(new String[methodNode.exceptions.size()]));
			// push this指针
			visitor.visitVarInsn(ALOAD, 0);
			visitor.visitFieldInsn(GETFIELD, subInterName, "invokeHander", invokeType.getDescriptor());
			visitor.visitVarInsn(ALOAD, 0);
			int stackIndex = genArgsObject(argTypes, visitor, 1);

			// 访问beforeHander方法。
			visitor.visitMethodInsn(INVOKEVIRTUAL, invokeType.getInternalName(), "beforeHanderInvoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Z");
			// 构造如果beforeHanderInvoke返回false，程序停止运行逻辑。
			Label label = new Label();
			visitor.visitJumpInsn(IFNE, label);

			visitDefaultValue(returnType, visitor);
			visitor.visitInsn(returnType.getOpcode(IRETURN));
			visitor.visitLabel(label);
			int returnVarStackIndex = 0;
			if (returnType.getOpcode(IRETURN) != RETURN) {
				returnVarStackIndex = stackIndex;
				visitDefaultValue(returnType, visitor);
				// System.out.println("returnType opcode=" + returnType.getOpcode(ISTORE) + " returnVarStackIndex="
				// + returnVarStackIndex);
				// System.out.println(" stackIndex=" + stackIndex);
				visitor.visitVarInsn(returnType.getOpcode(ISTORE), returnVarStackIndex);
			}
			// try catch 处理
			Label start = new Label();
			Label end = new Label();
			Label hander = new Label();
			visitor.visitTryCatchBlock(start, end, hander, Type.getType(Throwable.class).getInternalName());

			// try 代码段
			visitor.visitLabel(start);
			visitor.visitVarInsn(ALOAD, 0);
			int count = 1;
			for (Type arg : argTypes) {
				visitor.visitVarInsn(arg.getOpcode(ILOAD), count++);
				if (arg.getSort() == Type.DOUBLE || arg.getSort() == Type.LONG) {
					count++;
				}
			}
			// 调用父类同名方法
			visitor.visitMethodInsn(INVOKESPECIAL, sourceInterName, methodNode.name, methodNode.desc);
			if (returnType.getOpcode(IRETURN) != RETURN) {
				// System.out.println("returnType opcode=" + returnType.getOpcode(ISTORE) + " stackIndex=" +
				// stackIndex);
				visitor.visitVarInsn(returnType.getOpcode(ISTORE), returnVarStackIndex);
				if (returnType.getSort() == Type.DOUBLE || returnType.getSort() == Type.LONG) {
					stackIndex++;
				}
			}
			visitor.visitVarInsn(ALOAD, 0);
			visitor.visitFieldInsn(GETFIELD, subInterName, "invokeHander", invokeType.getDescriptor());
			visitor.visitVarInsn(ALOAD, 0);
			if (returnType.getOpcode(IRETURN) != RETURN) {
				visitor.visitVarInsn(returnType.getOpcode(ILOAD), returnVarStackIndex);// returnVarStackIndex
			} else {
				visitDefaultValue(Type.getType(Object.class), visitor);
			}
			// 装箱
			boxUp(returnType, visitor);
			genArgsObject(argTypes, visitor, 1);
			// 调用invoker beforeHanderInvoke方法。
			visitor.visitMethodInsn(INVOKEVIRTUAL, invokeType.getInternalName(), "afterHanderInvoke",
			        "(Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
			if (returnType.getOpcode(IRETURN) != RETURN) {
				Class<?> warpClass = getWarpClass(returnType, true);
				if (warpClass != Object.class) {// 检查返回类型。
					// 参数类型
					// System.err.println(Type.getType(warpClass).getDescriptor());
					if (warpClass == boolean.class || warpClass == int.class || warpClass == short.class || warpClass == char.class || warpClass == long.class
					        || warpClass == byte.class || warpClass == float.class || warpClass == double.class) {
						visitor.visitTypeInsn(CHECKCAST, Type.getType(warpClass).getDescriptor());
					} else if (warpClass != boolean[].class && warpClass != int[].class && warpClass != short[].class && warpClass != char[].class
					        && warpClass != long[].class && warpClass != byte[].class && warpClass != float[].class && warpClass != double[].class) {
						visitor.visitTypeInsn(CHECKCAST,
						        warpClass.isArray() ? Type.getType(warpClass).getDescriptor() : Type.getType(warpClass).getInternalName());
					} else {
						visitor.visitTypeInsn(CHECKCAST, Type.getType(warpClass).getDescriptor());
					}
				}
				if (warpClass.isArray()) {
					visitor.visitTypeInsn(CHECKCAST, Type.getType(warpClass).getDescriptor());
				}
				// 拆箱
				stripping(returnType, visitor);
			} else {
				visitor.visitInsn(POP);
			}
			// // 传给本地变量再返回
			// if (returnType.getOpcode(IRETURN) != RETURN) {
			// visitor.visitVarInsn(returnType.getOpcode(ISTORE), returnVarStackIndex);
			// visitor.visitVarInsn(returnType.getOpcode(ILOAD), returnVarStackIndex);
			// }
			visitor.visitInsn(returnType.getOpcode(IRETURN));

			// catch 代码段
			visitor.visitLabel(end);
			// Label gotoLabel = new Label();
			// visitor.visitJumpInsn(GOTO, gotoLabel);
			visitor.visitLabel(hander);
			// 存储Exception
			visitor.visitVarInsn(ASTORE, returnType.getOpcode(IRETURN) != RETURN ? ++stackIndex : stackIndex);
			visitor.visitVarInsn(ALOAD, 0);
			visitor.visitFieldInsn(GETFIELD, subInterName, "invokeHander", invokeType.getDescriptor());
			visitor.visitVarInsn(ALOAD, 0);
			visitor.visitVarInsn(ALOAD, stackIndex);

			stackIndex = genArgsObject(argTypes, visitor, 1);
			// 访问ExceptionInvoke
			visitor.visitMethodInsn(INVOKEVIRTUAL, invokeType.getInternalName(), "exceptionHanderInvoke",
			        "(Ljava/lang/Object;Ljava/lang/Throwable;[Ljava/lang/Object;)V");
			if (returnType.getOpcode(IRETURN) != RETURN) {
				visitDefaultValue(returnType, visitor);
				visitor.visitInsn(returnType.getOpcode(IRETURN));
			} else {
				// visitor.visitInsn(POP);
			}
			// visitor.visitLabel(gotoLabel);
			// 跳出trycath代码段

			visitor.visitInsn(returnType.getOpcode(IRETURN));
			visitor.visitMaxs(20, 20);
			// visitor.visitMaxs(0, 0);
			visitor.visitEnd();
		}
		cw.visitEnd();
		// try {
		// FileOutputStream out = new FileOutputStream("D:\\work\\Workspaces\\test3.class");
		// out.write(cw.toByteArray());
		// } catch (IOException e) {
		// e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
		// }

		return cw.toByteArray();
	}

	/**
	 * 获取原class对象的方法节点。
	 * 
	 * @param sourceClass
	 * @return
	 */
	private static List<MethodNode> getMethodNodes(Class<?> sourceClass) {
		try {
			// 用对应的classLoader加载class文件
			InputStream is = sourceClass.getClassLoader().getResourceAsStream(sourceClass.getCanonicalName().replace(".", "/") + ".class");
			ClassReader classReader = new ClassReader(is);
			ClassNode node = new ClassNode();
			ClassVisitor classAdapter = new ClassVisitor(ASM5, node) {
			};
			classReader.accept(classAdapter, ClassReader.EXPAND_FRAMES);
			List<MethodNode> methods = (List<MethodNode>) node.methods;
			return methods;
		} catch (IOException e) {
			LogUtils.error("获取方法节点失败", e);
		}
		return null;
	}

	/**
	 * 构造Invokerhander参数数组二进制汇编码
	 * 
	 * @param argTypes
	 * @param visitor
	 * @param stackIndex
	 *            :堆栈起始索引。
	 * @return 暂用退栈索引。
	 */
	private static int genArgsObject(Type[] argTypes, MethodVisitor visitor, int stackIndex) {
		// 根据函数的参数个数构造堆栈。
		visitor.visitInsn(ICONST_0 + argTypes.length);
		// 构造参数数组
		visitor.visitMultiANewArrayInsn("[Ljava/lang/Object;", 1);
		for (int i = 0; i < argTypes.length; i++) {
			visitor.visitInsn(DUP);
			visitor.visitInsn(ICONST_0 + i);
			// 是否基本类型
			visitor.visitVarInsn(argTypes[i].getOpcode(ILOAD), stackIndex);
			// 如果是Java基本类型，这里需要装箱。
			boxUp(argTypes[i], visitor);
			stackIndex++;
			// 双精度暂两个索引
			if (argTypes[i].getSort() == Type.DOUBLE || argTypes[i].getSort() == Type.LONG) {
				stackIndex++;
			}
			visitor.visitInsn(AASTORE);
		}
		return stackIndex;
	}

	/**
	 * 基础类型装箱操作。
	 * 
	 * @param type
	 * @param visitor
	 * @return 返回true代表已经装箱，false代表不需装箱
	 */
	private static boolean boxUp(Type type, MethodVisitor visitor) {
		switch (type.getSort()) {
		case Type.BOOLEAN: {
			visitor.visitMethodInsn(INVOKESTATIC, Type.getType(Boolean.class).getInternalName(), "valueOf", "(Z)Ljava/lang/Boolean;");
			break;
		}
		case Type.INT: {
			visitor.visitMethodInsn(INVOKESTATIC, Type.getType(Integer.class).getInternalName(), "valueOf", "(I)Ljava/lang/Integer;");
			break;
		}
		case Type.CHAR: {
			visitor.visitMethodInsn(INVOKESTATIC, Type.getType(Character.class).getInternalName(), "valueOf", "(C)Ljava/lang/Character;");
			break;
		}
		case Type.SHORT: {
			visitor.visitMethodInsn(INVOKESTATIC, Type.getType(Short.class).getInternalName(), "valueOf", "(S)Ljava/lang/Short;");
			break;
		}
		case Type.BYTE: {
			visitor.visitMethodInsn(INVOKESTATIC, Type.getType(Byte.class).getInternalName(), "valueOf", "(B)Ljava/lang/Byte;");
			break;
		}
		case Type.LONG: {
			visitor.visitMethodInsn(INVOKESTATIC, Type.getType(Long.class).getInternalName(), "valueOf", "(J)Ljava/lang/Long;");
			break;
		}
		case Type.FLOAT: {
			visitor.visitMethodInsn(INVOKESTATIC, Type.getType(Float.class).getInternalName(), "valueOf", "(F)Ljava/lang/Float;");
			break;
		}
		case Type.DOUBLE: {
			visitor.visitMethodInsn(INVOKESTATIC, Type.getType(Double.class).getInternalName(), "valueOf", "(D)Ljava/lang/Double;");
			break;
		}
		default:
			return false;
		}
		return true;
	}

	/**
	 * 基础类型拆箱操作
	 * 
	 * @param type
	 * @param
	 */
	private static boolean stripping(Type type, MethodVisitor visitor) {
		switch (type.getSort()) {
		case Type.BOOLEAN: {
			visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getType(Boolean.class).getInternalName(), "booleanValue", "()Z");
			break;
		}
		case Type.INT: {
			visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getType(Integer.class).getInternalName(), "intValue", "()I");
			break;
		}
		case Type.CHAR: {
			visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getType(Character.class).getInternalName(), "charValue", "()C");
			break;
		}
		case Type.SHORT: {
			visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getType(Short.class).getInternalName(), "shortValue", "()S");
			break;
		}
		case Type.BYTE: {
			visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getType(Byte.class).getInternalName(), "byteValue", "()B");
			break;
		}
		case Type.LONG: {
			visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getType(Long.class).getInternalName(), "longValue", "()J");
			break;
		}
		case Type.FLOAT: {
			visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getType(Float.class).getInternalName(), "floatValue", "()F");
			break;
		}
		case Type.DOUBLE: {
			visitor.visitMethodInsn(INVOKEVIRTUAL, Type.getType(Double.class).getInternalName(), "doubleValue", "()D");
			break;
		}
		default:
			return false;
		}
		return true;
	}

	private static Class<?> getWarpClass(Type type) {
		return getWarpClass(type, false);
	}

	private static Class<?> getWarpClass(Type type, boolean isTrans) {
		// claszz = Class.forName("[I");
		String typeName = type.toString();
		// System.out.println(typeName + "=" + type.getClassName());
		String className = null;
		int typev = type.getSort();
		if (typev > Type.VOID && typev <= Type.DOUBLE) {
			className = type.getClassName();
			if (!isTrans) {
				if (className.equals("int")) {
					return int.class;
				} else if (className.equals("byte")) {
					return byte.class;
				} else if (className.equals("long")) {
					return long.class;
				} else if (className.equals("short")) {
					return short.class;
				} else if (className.equals("double")) {
					return double.class;
				} else if (className.equals("float")) {
					return float.class;
				} else if (className.equals("char")) {
					return char.class;
				} else if (className.equals("boolean")) {
					return boolean.class;
				}
			} else {
				className = "java.lang.";
				if (type.getClassName().toLowerCase().equals("char")) {
					className += "Character";
				} else if (type.getClassName().toLowerCase().equals("int")) {
					className += "Integer";
				} else {
					className += type.getClassName().toLowerCase().substring(0, 1).toUpperCase() + type.getClassName().toLowerCase().substring(1);
				}
			}

		} else if (typev == Type.ARRAY) {
			className = type.getClassName();
			if (className.equals("int[]")) {
				className = "java.lang.Integer[]";
				return new int[0].getClass();
			} else if (className.equals("long[]")) {
				className = "java.lang.Long[]";
				return new long[0].getClass();
			} else if (className.equals("short[]")) {
				className = "java.lang.Short[]";
				return new short[0].getClass();
			} else if (className.equals("double[]")) {
				className = "java.lang.Double[]";
				return new double[0].getClass();
			} else if (className.equals("float[]")) {
				className = "java.lang.Float[]";
				return new float[0].getClass();
			} else if (className.equals("char[]")) {
				className = "java.lang.Character[]";
				return new char[0].getClass();
			} else if (className.equals("boolean[]")) {
				className = "java.lang.Boolean[]";
				return new boolean[0].getClass();
			} else {
				className = type.getClassName();
			}
		} else {
			className = type.getClassName();
		}
		try {
			if (typev == Type.OBJECT || (typev > Type.VOID && typev <= Type.DOUBLE)) {
				return Class.forName(className);
			} else {
				// System.out.println(type.getInternalName());
				if (className.endsWith("[]")) {// 数组类型
					int[] dims = new int[className.split("\\[\\]", 10).length - 1];
					for (int i = 0; i < dims.length; i++) {
						dims[i] = 0;
					}
					return Array.newInstance(Class.forName(className.replaceAll("\\[\\]", "")), dims).getClass();
				}
				return Class.forName(className);
			}

		} catch (ClassNotFoundException e) {
			LogUtils.error(null, e);
			return null;
		}
	}

	/**
	 * 根据类型访问其类型默认值。
	 * 
	 * @param type
	 * @param visitor
	 */
	private static void visitDefaultValue(Type type, MethodVisitor visitor) {
		// 构造返回默认值。
		if (type.getOpcode(IRETURN) != RETURN) {
			// 基本类型返回默认值0
			if (type.getSort() >= Type.BOOLEAN && type.getSort() <= Type.DOUBLE) {
				visitor.visitInsn(ICONST_0);
			} else {// 对象类型返回null
				visitor.visitInsn(ACONST_NULL);
			}
		}

	}

}
