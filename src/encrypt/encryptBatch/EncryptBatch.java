package encrypt.encryptBatch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import encrypt.util.Utils;

public class EncryptBatch {

	public static void main(String[] args) throws Exception {
		
		String filePath=(System.getProperties().getProperty("user.home"))+"/key";
		String fileClassPath="F:/gitWorkspace/mng/mng-manage-provider-impl/target/classes";
	    SecureRandom sr = new SecureRandom();  
	    String algorithm = "DES";  
		byte[] rawKey = file2byte(new File(filePath.replace("\\", "/")));
	    DESKeySpec dks = new DESKeySpec(rawKey);  
	    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance( algorithm );  
	    SecretKey key = keyFactory.generateSecret(dks); 
	    
	    Cipher ecipher = Cipher.getInstance(algorithm);  
	    ecipher.init(Cipher.ENCRYPT_MODE, key, sr);  
	  
		List<File> fileList =new ArrayList<File>();
		Utils.getFileList(fileClassPath,fileList);
		for (File file : fileList) {
		      byte classData[] = Utils.readFile(file.getPath());  //读入类文件  
		      byte encryptedClassData[] = ecipher.doFinal(classData);  //加密  
		      Utils.writeFile(file.getPath(), encryptedClassData);  // 保存加密后的内容  
		      System.out.println("Encrypted "+file.getName());  
		}
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
