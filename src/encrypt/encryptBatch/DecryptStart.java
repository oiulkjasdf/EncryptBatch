package encrypt.encryptBatch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import encrypt.util.Utils;  
  
public class DecryptStart extends ClassLoader  
{  
	  // 这些对象在构造函数中设置，以后loadClass()方法将利用它们解密类  
	  private SecretKey key;  
	  private Cipher cipher;  
	  
	  // 构造函数：设置解密所需要的对象  
	  public DecryptStart(SecretKey key) throws GeneralSecurityException, IOException {  
	    this.key = key;  
	  
	    String algorithm = "DES";  
	    SecureRandom sr = new SecureRandom();  
	    System.err.println("[DecryptStart: creating cipher]");  
	    cipher = Cipher.getInstance(algorithm);  
	    cipher.init(Cipher.DECRYPT_MODE, key, sr);  
	  }  
	  
	  // main过程：在这里读入密匙，创建DecryptStart的实例，它就是定制ClassLoader。  
	  // 设置好ClassLoader以后，用它装入应用实例，  
	  // 最后，通过Java Reflection API调用应用实例的main方法  
	  public static void main(String args[]) throws Exception {  
		String filePath="file:///F:/key";  
	    String appName = "com.synjones.cloudcard.mng.manage.provideripml.ServiceMngProviderStarter";  
	  
	    // 传递给应用本身的参数  
	    String realArgs[] = new String[1];  
	  
	    // 读取密匙  
	    System.err.println( "[DecryptStart: reading key]" );  
	    Path path = Paths.get(new URI(filePath));  
		byte[] rawKey = Files.readAllBytes(path);  
	    DESKeySpec dks = new DESKeySpec(rawKey);  
	    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");  
	    SecretKey key = keyFactory.generateSecret(dks);  
	  
	    // 创建解密的ClassLoader  
	    DecryptStart dr = new DecryptStart(key);  
	  
	    // 创建应用主类的一个实例，通过ClassLoader装入它  
	    System.err.println("[DecryptStart: loading "+appName+"]");  
	    Class clasz = dr.loadClass(appName);  
	  
	    // 最后通过Reflection API调用应用实例  
	    // 的main()方法  
	  
	    // 获取一个对main()的引用  
	    String proto[] = new String[1];  
	    Class mainArgs[] = { (new String[1]).getClass() };  
	    Method main = clasz.getMethod("startMngService", mainArgs);  
	  
	    // 创建一个包含main()方法参数的数组  
	    Object argsArray[] = { realArgs };  
	    System.err.println("[DecryptStart: running "+appName+".main()]");  
	  
	    // 调用main()  
	    main.invoke(null, argsArray);  
	  }  
	  
	  public Class loadClass(String name, boolean resolve) throws ClassNotFoundException {  
	    try {  
	      // 要创建的Class对象  
	      Class clasz = null;  
	  
	      // 必需的步骤1：如果类已经在系统缓冲之中，不必再次装入它  
	      clasz = findLoadedClass(name);  
	  
	      if (clasz != null)  
	          return clasz;  
	  
	      // 下面是定制部分  
	      try{  
	          //读取经过加密的类文件  
	          byte classData[] = Utils.readFile(name.substring(name.lastIndexOf(".")+1,name.length())+".class");  
	          if(classData != null){  
	            byte decryptedClassData[] = cipher.doFinal(classData);  //解密  
	              clasz = defineClass( name, decryptedClassData, 0, decryptedClassData.length); // 再把它转换成一个类  
	         }                  
	      }catch(FileNotFoundException fnfe){  
	            
	      }  
	  
	      // 必需的步骤2：如果上面没有成功  
	      // 尝试用默认的ClassLoader装入它  
	      if (clasz == null)  
	          clasz = findSystemClass(name);          
	  
	      // 必需的步骤3：如有必要，则装入相关的类  
	      if (resolve && clasz != null)  
	          resolveClass(clasz);          
	        
	      return clasz;//把类返回给调用者  
	        
	    } catch(IOException ie) {  
	          throw new ClassNotFoundException(ie.toString());  
	    } catch(GeneralSecurityException gse) {  
	          throw new ClassNotFoundException( gse.toString());  
	    }  
	  }  
}  