package encrypt.encryptBatch;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * 
 * 本技术点着眼于修改spring源码重写classloader实现项目加密
 * 即 即时解密加密后的class文件  
 * 需要修改spring 源码实现功能
 * 
 * 其大体思路是   制作key   ---》      使用key 将class文件加密   
 * ----》 项目启动时将自定义类加载器作为当前线程类加载器
 * spring 通过xml 加载类文件时  使用我们的自定义类加载器加载class  从而达到加密解密目的 
 * 
 * @author Sunx
 *
 */
public class MyClassLoader extends ClassLoader {
	// 这些对象在构造函数中设置，以后loadClass()方法将利用它们解密类
	private SecretKey key;
	private Cipher cipher;
	//key文件路径   日后考虑 从网络中获取key
	String filePath = System.getProperties().getProperty("user.home")+"/key";
	//本项目路径
	String startPath = ClassLoader.getSystemClassLoader().getResource("").getPath().toString()+"com/synjones/cloudcard/mng/manage";

	//方法仅供改过源码的 ↓  使用   
	//	org.springframework.core.type.classreading.SimpleMetadataReader.SimpleMetadataReader(Resource, ClassLoader)
	
	//修改后的源码：
	//	InputStream inputStream = null;
	//	ClassReader classReader = null;
	//	try {
	//		此处做了初级筛选  筛选出非class及  jar 包中的class
	//		if ((resource.getURI().toString().indexOf("jar:file") == -1 )&&
	//		(resource.getURI().toString().endsWith(".class") )){
	//			inputStream = classLoader.getResourceAsStream(resource.getURI().getPath());
	//		}else{
	//			inputStream = resource.getInputStream();
	//		}
	//		classReader = new ClassReader(inputStream);
	//	}
	//	-------------------若干行--------------------------------------------
	//	catch (IllegalArgumentException ex) {
	//		throw new NestedIOException("ASM ClassReader failed to parse class file - " +
	//				"probably due to a new Java class file version that isn't supported yet: " + resource, ex);
	//	}
	//	finally {
	//		if(inputStream!=null){
	//			inputStream.close();
	//		}
	//	}	
	@Override
	public InputStream getResourceAsStream(String name) {

		File file = new File(name);
		if (!file.exists()) {
			return super.getResourceAsStream(name);
		}
		byte[] classData = null;
		try {
			classData = file2byte(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//本身入口、类加载器、jar包内容 不涉及解密 排除掉
		if (!name.endsWith("MyClassLoader.class") && classData != null
				&& !name.endsWith("ServiceMngProviderStarter.class")
				&& !name.endsWith("ServiceMngProviderStarter$1.class") 
				&& name.indexOf("jar:file") == -1
				&& name.startsWith(startPath)) {
			try {
				// 读取密匙
				SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
				key = keyFactory.generateSecret(new DESKeySpec(file2byte(new File(filePath.replace("\\", "/")))));
				cipher = Cipher.getInstance("DES");
				cipher.init(Cipher.DECRYPT_MODE, key, new SecureRandom());
				// 读取经过加密的类文件
				byte decryptedClassData[] = cipher.doFinal(classData); // 解密
				return new ByteArrayInputStream(decryptedClassData);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return new ByteArrayInputStream(classData);
		}
		return super.getResourceAsStream(name);
	}

	@Override
	public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class<?> clasz = null;
		clasz = findLoadedClass(name);
		if (clasz != null)
			return clasz;
		//以下方法类似上面方法   即时解密class 文件
		try {
			File file = new File(ClassLoader.getSystemClassLoader().getResource("").getPath().toString()+name.replace(".", "/")+".class");
			if (file.exists()) {
				// 读取密匙
				SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
				key = keyFactory.generateSecret(new DESKeySpec(file2byte(new File(filePath.replace("\\", "/")))));
				cipher = Cipher.getInstance("DES");
				cipher.init(Cipher.DECRYPT_MODE, key, new SecureRandom());
				// 读取经过加密的类文件
				byte decryptedClassData[] = cipher.doFinal(file2byte(file)); // 解密
				clasz = defineClass(name, decryptedClassData, 0, decryptedClassData.length); // 再把它转换成一个类
				return clasz;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//上述无效后尽量解决
		if (clasz == null)
			clasz = findSystemClass(name);
		if (resolve && clasz != null)
			resolveClass(clasz);
		return clasz;
	}

	// 把文件读入byte数组
	@SuppressWarnings("resource")
	static public byte[] readFile(String filename) throws IOException {
		File file = new File(filename);
		long len = file.length();
		byte data[] = new byte[(int) len];
		FileInputStream fin = new FileInputStream(file);
		int r = fin.read(data);
		if (r != len)
			throw new IOException("Only read " + r + " of " + len + " for " + file);
		fin.close();
		return data;
	}

	//工具类方法  功能如其名   file to byte
	public static byte[] file2byte(File file) throws Exception {
		byte[] buffer = null;
		FileInputStream fis = null;
		ByteArrayOutputStream bos = null;
		try {
			fis = new FileInputStream(file);
			bos = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			int n;
			while ((n = fis.read(b)) != -1) {
				bos.write(b, 0, n);
			}
			buffer = bos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			fis.close();
			bos.close();
		}
		return buffer;
	}
}
