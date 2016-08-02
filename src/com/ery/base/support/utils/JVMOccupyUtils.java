package com.ery.base.support.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;


public class JVMOccupyUtils {

    //这8个方法不写不行，否则occupyof(int x)会自动重载到occupyof(Object o),并且无法在方法中判断
    public static int occupyof(boolean variable) {
        return 1;
    }

    public static int occupyof(byte variable) {
        return 1;
    }

    public static int occupyof(short variable) {
        return 2;
    }

    public static int occupyof(char variable) {
        return 2;
    }

    public static int occupyof(int variable) {
        return 4;
    }

    public static int occupyof(float variable) {
        return 4;
    }

    public static int occupyof(long variable) {
        return 8;
    }

    public static int occupyof(double variable) {
        return 8;
    }


    public JVMOccupyUtils(byte nullReferenceSize, byte emptyObjectSize, byte emptyArrayVarSize) {
        this.NULL_REFERENCE_SIZE = nullReferenceSize;
        this.EMPTY_OBJECT_SIZE = emptyObjectSize;
        this.EMPTY_ARRAY_VAR_SIZE = emptyArrayVarSize;
    }

    public static JVMOccupyUtils forJRockitVM() {
        return new JVMOccupyUtils((byte) 4, (byte) 8, (byte) 8);
    }

    public static JVMOccupyUtils forSun32BitsVM() {
        return new JVMOccupyUtils((byte) 4, (byte) 8, (byte) 4);
    }

    public static JVMOccupyUtils forSun64BitsVM() {
        return new JVMOccupyUtils((byte) 8, (byte) 16, (byte) 8);
    }

    public static JVMOccupyUtils forDetectedVM(){
        return null;
    }

    private final byte NULL_REFERENCE_SIZE;
    private final byte EMPTY_OBJECT_SIZE;
    private final byte EMPTY_ARRAY_VAR_SIZE;

    private static class ref{
        public ref(Object obj){
            this.obj = obj;
        }

        final Object obj;

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof ref) && ((ref)obj).obj == this.obj;
        }

        @Override
        public int hashCode() {
            return obj.hashCode();
        }
    }

    private List dedup = new ArrayList();

    
    public long occupyof(Object object){
        dedup.clear();
        return occupyof0(object);
    }

    private long occupyof0(Object object) {
        if (object == null)
            return 0;

        ref r = new ref(object);
        if(dedup.contains(r))
            return 0;

        dedup.add(r);
        long varSize = 0;//对象中的值类型、引用类型变量大小
        long objSize = 0;//对象中的引用类型指向的对象实例的大小
        for (Class clazz = object.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {

//      System.out.println(clazz);
            if (clazz.isArray()) {//当前对象的数组
                varSize += EMPTY_ARRAY_VAR_SIZE;
                Class<?> componentType = clazz.getComponentType();
                if (componentType.isPrimitive()) {//当前数组是原生类型的数组
                    varSize += lengthOfPrimitiveArray(object) * sizeofPrimitiveClass(componentType);
                    return occupyOfSize(EMPTY_OBJECT_SIZE, varSize, 0);
                }

                Object[] array = (Object[]) object;
                varSize += NULL_REFERENCE_SIZE * array.length;//当前数组有length个引用，每个占用4字节

                for (Object o : array)
                    objSize += occupyof0(o);

                return occupyOfSize(EMPTY_OBJECT_SIZE, varSize, objSize);
            }

            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers()))
                    continue;//类成员不计

                //System.out.println(field.getDeclaringClass());
                if(clazz != field.getDeclaringClass())
                    continue;

                Class<?> type = field.getType();
                if (type.isPrimitive())
                    varSize += sizeofPrimitiveClass(type);
                else {
                    varSize += NULL_REFERENCE_SIZE;//一个引用型变量占用4个字节
                    try {
                        field.setAccessible(true);//可以访问非public类型的变量
                        objSize += occupyof0(field.get(object));
                    } catch (Exception e) {
                        objSize += occupyofConstructor(object, field);
                    }
                }
            }
        }

        return occupyOfSize(EMPTY_OBJECT_SIZE, varSize, objSize);
    }

    public static int sizeof(boolean variable) {
        return 1;
    }

    public static int sizeof(byte variable) {
        return 1;
    }

    public static int sizeof(short variable) {
        return 2;
    }

    public static int sizeof(char variable) {
        return 2;
    }

    public static int sizeof(int variable) {
        return 4;
    }

    public static int sizeof(float variable) {
        return 4;
    }

    public static int sizeof(long variable) {
        return 8;
    }

    public static int sizeof(double variable) {
        return 8;
    }


    
    public long sizeof(Object object) {
        if (object == null)
            return 0;

        long size = EMPTY_OBJECT_SIZE;
        Class clazz = object.getClass();
        if (clazz.isArray()) {
            size += EMPTY_ARRAY_VAR_SIZE;//length变量是int型
            Class<?> componentType = clazz.getComponentType();
            if (componentType.isPrimitive())
                return size + lengthOfPrimitiveArray(object) * sizeofPrimitiveClass(componentType);

            Object[] array = (Object[]) object;
            size += 4 * array.length;
            for (Object o : array)
                size += sizeof(o);

            return size;
        }

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()))
                continue;//类成员不计

            Class<?> type = field.getType();
            if (type.isPrimitive())
                size += sizeofPrimitiveClass(type);
            else {
                size += 4;//一个引用型变量占用4个字节
                try {
                    field.setAccessible(true);//可以访问非public类型的变量
                    size += sizeof(field.get(object));
                } catch (Exception e) {
                    size += sizeofConstructor(object, field);
                }
            }
        }

        return size;
    }

    private static long occupyofConstructor(Object object, Field field) {
        throw new UnsupportedOperationException("field type Constructor not accessible: " + object.getClass() + " field:" + field);
    }

    private static long sizeofConstructor(Object object, Field field) {
        throw new UnsupportedOperationException("field type Constructor not accessible: " + object.getClass() + " field:" + field);
    }

    

    private static long occupyOfSize(long size) {
        return (size + 7) / 8 * 8;
    }

    private static long occupyOfSize(long selfSize, long varsSize, long objsSize) {
//    System.out.println("self=" + selfSize + " vars=" + varsSize + " objs=" + objsSize);
        return occupyOfSize(selfSize) + occupyOfSize(varsSize) + objsSize;
    }

    private static int sizeofPrimitiveClass(Class clazz) {
        return clazz == boolean.class || clazz == byte.class ? 1
                : clazz == char.class || clazz == short.class ? 2
                : clazz == int.class || clazz == float.class ? 4
                : 8;
    }

    private static int lengthOfPrimitiveArray(Object object) {
        Class<?> clazz = object.getClass();
        return clazz == boolean[].class ? ((boolean[]) object).length
                : clazz == byte[].class ? ((byte[]) object).length
                : clazz == char[].class ? ((char[]) object).length
                : clazz == short[].class ? ((short[]) object).length
                : clazz == int[].class ? ((int[]) object).length
                : clazz == float[].class ? ((float[]) object).length
                : clazz == long[].class ? ((long[]) object).length
                : ((double[]) object).length;
    }

}
