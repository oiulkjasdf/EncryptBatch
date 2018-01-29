package encrypt.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class Utils {
	
	public static List<File> getFileList(String strPath,List<File> filelist) {
        File dir = new File(strPath);
        File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组
		if (files != null) {
            for (int i = 0; i < files.length; i++) {
            	String name = files[i].getName();
            	String path = files[i].getPath();
                if (files[i].isDirectory()) { // 判断是文件还是文件夹
                    getFileList(files[i].getAbsolutePath(),filelist); // 获取文件绝对路径
                } else if(name.endsWith(".class")&&path.startsWith("F:\\gitWorkspace\\mng\\mng-manage-provider-impl\\target\\classes\\com\\synjones\\cloudcard\\mng\\manage")&&!"MyClassLoader.class".equals(name)&&!"ServiceMngProviderStarter.class".equals(name)&&!"ServiceMngProviderStarter$1.class".equals(name)){
                	filelist.add(files[i]);
                }else{
                	continue;
                }
            }

        }
        return filelist;
    }
	
	  // 把文件读入byte数组  
	  @SuppressWarnings("resource")
	static public byte[] readFile(String filename) throws IOException {  
	    File file = new File(filename);  
	    long len = file.length();  
	    byte data[] = new byte[(int)len];  
	    FileInputStream fin = new FileInputStream(file);  
	    int r = fin.read(data);  
	    if (r != len)  
	      throw new IOException("Only read "+r+" of "+len+" for "+file);  
	    fin.close();  
	    return data;  
	  }  
	  
	  // 把byte数组写出到文件  
	  static public void writeFile(String filename, byte data[]) throws IOException {  
	    FileOutputStream fout = new FileOutputStream(filename);  
	    fout.write(data);  
	    fout.close();  
	  }  
	  
}
