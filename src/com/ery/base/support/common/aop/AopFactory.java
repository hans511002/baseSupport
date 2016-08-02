package com.ery.base.support.common.aop;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.ery.base.support.log4j.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AopFactory implements Opcodes{

    
    public static Object getInstance(Class<?> sourceClass,InvokeHandler handler,String[] methdIncludes,
                                     IAopMethodFilter filter){
        String subClassName=sourceClass.getCanonicalName()+"$ASM";
        Class<?> subClass=null;
        try {
            subClass=Class.forName(subClassName);
        } catch (ClassNotFoundException e) {
            //出错未生成此类。用ASM操作字节码生成一个新的class，此Class继承于 sourceClass
            try {
                subClass=new ClassLoader(sourceClass.getClassLoader()){
                    public Class<?> defineClass(String className,byte[] data) throws ClassNotFoundException{
                        try{
                            Class<?> clazz = super.defineClass(className,data, 0, data.length);
                            super.resolveClass(clazz);
                            return clazz;
                        }catch(Exception e){
                        	LogUtils.error("加载动态生成的类失败：" + className, e);
                        }
                        return null;
                    }
                }.defineClass(subClassName,getSubClassByte(sourceClass,methdIncludes,filter));
            } catch (ClassNotFoundException e1) {
            	LogUtils.error("出现错误！",e1);
            }
        }
        if(subClass!=null){
            try {
                Object obj=subClass.newInstance();
                //初始化Invoker对象。
                java.lang.reflect.Method method=subClass.getMethod("setInvokeHander",InvokeHandler.class);
                method.invoke(obj,handler);
                return obj;
            }  catch (Exception e) {
            	LogUtils.error("出现错误！", e);
            }
        }
        return null;
    }

    
    public static Object getInstance(Class<?> sourceClass,InvokeHandler handler,String[] methdIncludes  ){
        return getInstance(sourceClass,handler,methdIncludes,null);
    }
    
    public static Object getInstance(Class<?> sourceClass,InvokeHandler handler){
        return getInstance(sourceClass,handler,null,null);
    }

    
    private static byte[] getSubClassByte(Class<?> sourceClass,String[] methodIncludes,IAopMethodFilter filter){
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        //开始访问类
        String sourceInterName=Type.getType(sourceClass).getInternalName();
        String subInterName=sourceInterName+"$ASM";

        cw.visit(V1_1, ACC_PUBLIC, subInterName,null, sourceInterName, null);

        Type invokeType=Type.getType(InvokeHandler.class);
        //添加一个字段invkoeHander
        FieldVisitor fv=cw.visitField(Opcodes.ACC_PRIVATE, "invokeHander", invokeType.getDescriptor(),
                                      null, null);
        fv.visitEnd();

        //添加一个默认的构造函数。
        MethodVisitor mw = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null,null);
        mw.visitCode();
        mw.visitVarInsn(ALOAD, 0);
        mw.visitMethodInsn(INVOKESPECIAL, sourceInterName, "<init>", "()V");
        mw.visitInsn(RETURN);
        mw.visitMaxs(0,0); //自动计算退栈信息
        mw.visitEnd();

        //为新生成的类新增一个方法setInvokeHander,用于设置拦截器
        mw=cw.visitMethod(ACC_PUBLIC,"setInvokeHander","("+invokeType.getDescriptor()+")V",null,null);
        mw.visitCode();
        mw.visitVarInsn(ALOAD, 0);
        mw.visitVarInsn(ALOAD, 1);
        mw.visitFieldInsn(PUTFIELD, subInterName,"invokeHander", invokeType.getDescriptor());
        mw.visitInsn(RETURN);
        mw.visitMaxs(0,0);
        mw.visitEnd();

        
        List<MethodNode> methods=getMethodNodes(sourceClass);
        for(MethodNode methodNode:methods){
            if(methodNode.name.equals("<init>")
                    ||methodNode.access==ACC_PRIVATE){//构造函数不用继承，私有函数不虚继承
                continue;
            }
            //进行方法过滤
            if(methodIncludes!=null&&methodIncludes.length>0){
                boolean isContinue=false;
                for(String partternStr:methodIncludes){
                    Pattern pattern=Pattern.compile(partternStr);
                    Matcher matcher=pattern.matcher(methodNode.name);
                    if(matcher.matches()){
                        isContinue=true;
                        break;
                    }
                }
                if(!isContinue){
                    continue;
                }
            }
            Type[] argTypes=Type.getArgumentTypes(methodNode.desc);
            Type returnType=Type.getReturnType(methodNode.desc);
            if(filter!=null){
                Class<?>[] argClass=new Class[argTypes.length];
                int i=0;
                for(Type type:argTypes){
                    argClass[i++]=getWarpClass(type);
                }
                if(!filter.filter(sourceClass,methodNode.name,argClass)){
                    continue;
                }
            }
//            if(!methodNode.name.equals("querryForObjectArray")){
//                continue;
//            }

            //新增方法
            MethodVisitor visitor = cw.visitMethod(ACC_PUBLIC, methodNode.name,methodNode.desc
                    , methodNode.signature
                    ,(String[])methodNode.exceptions.toArray(new String[methodNode.exceptions.size()]));
            //push this指针
            visitor.visitVarInsn(ALOAD,0);
            visitor.visitFieldInsn(GETFIELD, subInterName , "invokeHander", invokeType.getDescriptor());
            visitor.visitVarInsn(ALOAD,0);
            int stackIndex=genArgsObject(argTypes,visitor,1);

            //访问beforeHander方法。
            visitor.visitMethodInsn(INVOKEVIRTUAL,invokeType.getInternalName()
                    ,"beforeHanderInvoke","(Ljava/lang/Object;[Ljava/lang/Object;)Z");
            //构造如果beforeHanderInvoke返回false，程序停止运行逻辑。
            Label label=new Label();
            visitor.visitJumpInsn(IFNE, label);

            visitDefaultValue(returnType,visitor);
            visitor.visitInsn(returnType.getOpcode(IRETURN));
            visitor.visitLabel(label);

            if(returnType.getOpcode(IRETURN)!=RETURN){
                visitDefaultValue(returnType, visitor);
                visitor.visitVarInsn(returnType.getOpcode(ISTORE),stackIndex);
            }
            //try catch 处理
            Label start=new Label();
            Label end=new Label();
            Label hander=new Label();
            visitor.visitTryCatchBlock(start,end,hander,Type.getType(Throwable.class).getInternalName());

            //try 代码段
            visitor.visitLabel(start);
            visitor.visitVarInsn(ALOAD,0);
            int count=1;
            for(Type arg:argTypes){
                visitor.visitVarInsn(arg.getOpcode(ILOAD),count++);
                if(arg.getSort()==Type.DOUBLE){
                    count++;
                }
            }
            //调用父类同名方法
            visitor.visitMethodInsn(INVOKESPECIAL,sourceInterName ,methodNode.name,methodNode.desc);
            if(returnType.getOpcode(IRETURN)!=RETURN){
                visitor.visitVarInsn(returnType.getOpcode(ISTORE),stackIndex);
            }

            //catch 代码段
            visitor.visitLabel(end);
            Label gotoLabel=new Label();
            visitor.visitJumpInsn(GOTO,gotoLabel);
            visitor.visitLabel(hander);
            //存储Exception
            visitor.visitVarInsn(ASTORE,stackIndex==1?1:++stackIndex);
            visitor.visitVarInsn(ALOAD,0);
            visitor.visitFieldInsn(GETFIELD, subInterName , "invokeHander", invokeType.getDescriptor());
            visitor.visitVarInsn(ALOAD,0);
            visitor.visitVarInsn(ALOAD,stackIndex);

            stackIndex=genArgsObject(argTypes,visitor,1);
            //访问ExceptionInvoke
            visitor.visitMethodInsn(INVOKEVIRTUAL,invokeType.getInternalName()
                    ,"exceptionHanderInvoke","(Ljava/lang/Object;Ljava/lang/Throwable;[Ljava/lang/Object;)V");
            visitDefaultValue(returnType,visitor);
            visitor.visitInsn(returnType.getOpcode(IRETURN));
            visitor.visitLabel(gotoLabel);

            //跳出trycath代码段

            visitor.visitVarInsn(ALOAD,0);
            visitor.visitFieldInsn(GETFIELD, subInterName , "invokeHander", invokeType.getDescriptor());
            visitor.visitVarInsn(ALOAD,0);
            if(returnType.getOpcode(IRETURN)!=RETURN){
                visitor.visitVarInsn(returnType.getOpcode(ILOAD),stackIndex);
            }else{
                visitDefaultValue(Type.getType(Object.class),visitor);
            }
            //装箱
            boxUp(returnType, visitor);

            stackIndex=genArgsObject(argTypes,visitor,1);
            //调用invoker  beforeHanderInvoke方法。
            visitor.visitMethodInsn(INVOKEVIRTUAL,invokeType.getInternalName()
                    ,"afterHanderInvoke","(Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
            if(returnType.getOpcode(IRETURN)!=RETURN){
                Class<?> warpClass=null;
                if((warpClass=getWarpClass(returnType))!=Object.class){
                    //检查返回类型。
                    visitor.visitTypeInsn(CHECKCAST,warpClass.isArray()?Type.getType(warpClass).getDescriptor()
                            :Type.getType(warpClass).getInternalName());
                }
                if(warpClass.isArray()){
                    visitor.visitTypeInsn(CHECKCAST,Type.getType(warpClass).getDescriptor());
                }
                //拆箱
                stripping(returnType,visitor);
            }else{
                visitor.visitInsn(POP);
            }
            visitor.visitInsn(returnType.getOpcode(IRETURN));
            visitor.visitMaxs(0,0);
            visitor.visitEnd();
        }
        cw.visitEnd();
//        try {
//            FileOutputStream out=new FileOutputStream("D:\\tydic-bi-meta\\WebRoot\\WEB-INF\\test3.class");
//            out.write(cw.toByteArray());
//        } catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }

        return cw.toByteArray();
    }

    
    private static List<MethodNode> getMethodNodes(Class<?> sourceClass){
        try {
            //用对应的classLoader加载class文件
            InputStream is=sourceClass.getClassLoader()
                    .getResourceAsStream(sourceClass.getCanonicalName().replace(".","/")+".class");
            ClassReader classReader = new ClassReader(is);
            ClassNode node=new ClassNode();
            ClassAdapter classAdapter = new ClassAdapter(node);
            classReader.accept(classAdapter,ClassReader.EXPAND_FRAMES);
            List<MethodNode> methods=(List<MethodNode>)node.methods;
            return methods;
        } catch (IOException e) {
        	LogUtils.error("获取方法节点失败",e);
        }
        return null;
    }

    
    private static int genArgsObject(Type[] argTypes,MethodVisitor visitor,int stackIndex){
        //根据函数的参数个数构造堆栈。
        visitor.visitInsn(ICONST_0+argTypes.length);
        //构造参数数组
        visitor.visitMultiANewArrayInsn("[Ljava/lang/Object;",1);
        for(int i=0;i<argTypes.length;i++){
            visitor.visitInsn(DUP);
            visitor.visitInsn(ICONST_0+i);
            //是否基本类型
            visitor.visitVarInsn(argTypes[i].getOpcode(ILOAD),stackIndex);
            //如果是Java基本类型，这里需要装箱。
            boxUp(argTypes[i],visitor);
            stackIndex++;
            //双精度暂两个索引
            if(argTypes[i].getSort()==Type.DOUBLE){
                stackIndex++;
            }
            visitor.visitInsn(AASTORE);
        }
        return stackIndex;
    }

    
    private static boolean boxUp(Type type,MethodVisitor visitor){
        switch (type.getSort()){
            case Type.BOOLEAN:{
                visitor.visitMethodInsn(INVOKESTATIC,Type.getType(Boolean.class).getInternalName(),
                                        "valueOf","(Z)Ljava/lang/Boolean;");
                break;
            }
            case Type.INT:{
                visitor.visitMethodInsn(INVOKESTATIC,Type.getType(Integer.class).getInternalName(),
                                        "valueOf","(I)Ljava/lang/Integer;");
                break;
            }
            case Type.CHAR:{
                visitor.visitMethodInsn(INVOKESTATIC,Type.getType(Character.class).getInternalName(),
                                        "valueOf","(C)Ljava/lang/Character;");
                break;
            }
            case  Type.SHORT:{
                visitor.visitMethodInsn(INVOKESTATIC,Type.getType(Short.class).getInternalName(),
                                        "valueOf","(S)Ljava/lang/Short;");
                break;
            }
            case  Type.BYTE:{
                visitor.visitMethodInsn(INVOKESTATIC,Type.getType(Byte.class).getInternalName(),
                                        "valueOf","(B)Ljava/lang/Byte;");
                break;
            }
            case  Type.LONG:{
                visitor.visitMethodInsn(INVOKESTATIC,Type.getType(Long.class).getInternalName(),
                                        "valueOf","(J)Ljava/lang/Long;");
                break;
            }
            case  Type.FLOAT:{
                visitor.visitMethodInsn(INVOKESTATIC,Type.getType(Float.class).getInternalName(),
                                        "valueOf","(F)Ljava/lang/Float;");
                break;
            }
            case  Type.DOUBLE:{
                visitor.visitMethodInsn(INVOKESTATIC,Type.getType(Double.class).getInternalName(),
                                        "valueOf","(D)Ljava/lang/Double;");
                break;
            }
            default:
                return false;
        }
        return true;
    }

    
    private static boolean stripping(Type type,MethodVisitor visitor){
        switch (type.getSort()){
            case Type.BOOLEAN:{
                visitor.visitMethodInsn(INVOKEVIRTUAL,Type.getType(Boolean.class).getInternalName(),
                                        "booleanValue","()Z");
                break;
            }
            case Type.INT:{
                visitor.visitMethodInsn(INVOKEVIRTUAL,Type.getType(Integer.class).getInternalName(),
                                        "intValue","()I");
                break;
            }
            case Type.CHAR:{
                visitor.visitMethodInsn(INVOKEVIRTUAL,Type.getType(Character.class).getInternalName(),
                                        "charValue","()C");
                break;
            }
            case  Type.SHORT:{
                visitor.visitMethodInsn(INVOKEVIRTUAL,Type.getType(Short.class).getInternalName(),
                                        "shortValue","()S");
                break;
            }
            case  Type.BYTE:{
                visitor.visitMethodInsn(INVOKEVIRTUAL,Type.getType(Byte.class).getInternalName(),
                                        "byteValue","()B");
                break;
            }
            case  Type.LONG:{
                visitor.visitMethodInsn(INVOKEVIRTUAL,Type.getType(Long.class).getInternalName(),
                                        "longValue","()J");
                break;
            }
            case  Type.FLOAT:{
                visitor.visitMethodInsn(INVOKEVIRTUAL,Type.getType(Float.class).getInternalName(),
                                        "floatValue","()F");
                break;
            }
            case  Type.DOUBLE:{
                visitor.visitMethodInsn(INVOKEVIRTUAL,Type.getType(Double.class).getInternalName(),
                                        "doubleValue","()D");
                break;
            }
            default:return false;
        }
        return true;
    }

    
    private static  Class<?>  getWarpClass(Type type){
        String className=null;
        if(type.getSort()>Type.VOID&&type.getSort()<=Type.DOUBLE){
            className="java.lang.";
            if(type.getClassName().toLowerCase().equals("char")){
                className+="Character";
            }else if(type.getClassName().toLowerCase().equals("int")){
                className+="Integer";
            }else{
                className+=type.getClassName().toLowerCase().substring(0,1).toUpperCase()
                        + type.getClassName().toLowerCase().substring(1);
            }
        }else{
            className=type.getClassName();
        }
        try {
            if(className.endsWith("[]")){
                //数组类型
                return Array.newInstance(Class.forName(className.replaceAll("\\[\\]", "")),0).getClass();
            }else{
                return Class.forName(className);
            }
        } catch (ClassNotFoundException e) {
        	LogUtils.error(null,e);
            return null;
        }
    }

    
    private static void visitDefaultValue(Type type,MethodVisitor visitor){
        //构造返回默认值。
        if(type.getOpcode(IRETURN)!=RETURN){
            //基本类型返回默认值0
            if(type.getSort()>=Type.BOOLEAN&&type.getSort()<=Type.DOUBLE){
                visitor.visitInsn(ICONST_0);
            }else{//对象类型返回null
                visitor.visitInsn(ACONST_NULL);
            }
        }

    }

}
