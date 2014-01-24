/*    */ package love.cq.util;
/*    */ 
/*    */ import java.io.BufferedReader;
/*    */ import java.io.File;
/*    */ import java.io.FileInputStream;
/*    */ import java.io.FileNotFoundException;
/*    */ import java.io.FileOutputStream;
/*    */ import java.io.IOException;
/*    */ import java.io.InputStream;
/*    */ import java.io.InputStreamReader;
/*    */ import java.io.ObjectOutputStream;
/*    */ import java.io.RandomAccessFile;
/*    */ import java.io.Serializable;
/*    */ import java.io.UnsupportedEncodingException;
/*    */ 
/*    */ public class IOUtil
/*    */ {
/* 18 */   private static InputStream is = null;
/* 19 */   private static FileOutputStream fos = null;
/*    */ 
/*    */   public static InputStream getInputStream(String path) {
/*    */     try {
/* 23 */       File f = new File(path);
/* 24 */       return new FileInputStream(path);
/*    */     } catch (FileNotFoundException e) {
/* 26 */       e.printStackTrace();
/*    */     }
/* 28 */     return null;
/*    */   }
/*    */ 
/*    */   public static BufferedReader getReader(String path, String charEncoding) throws UnsupportedEncodingException {
/* 32 */     is = getInputStream(path);
/* 33 */     return new BufferedReader(new InputStreamReader(is, charEncoding));
/*    */   }
/*    */ 
/*    */   public static RandomAccessFile getRandomAccessFile(String path, String charEncoding) throws FileNotFoundException {
/* 37 */     is = getInputStream(path);
/* 38 */     if (is != null) {
/* 39 */       return new RandomAccessFile(new File(path), "r");
/*    */     }
/* 41 */     return null;
/*    */   }
/*    */ 
/*    */   public static void Writer(String path, String charEncoding, String content) {
/*    */     try {
/* 46 */       fos = new FileOutputStream(new File(path));
/* 47 */       fos.write(content.getBytes());
/* 48 */       fos.close();
/*    */     } catch (FileNotFoundException e) {
/* 50 */       e.printStackTrace();
/*    */     } catch (IOException e) {
/* 52 */       e.printStackTrace();
/*    */     }
/*    */   }
/*    */ 
/*    */   public void close() {
/* 57 */     if (is != null) {
/*    */       try {
/* 59 */         is.close();
/*    */       } catch (IOException e) {
/* 61 */         is = null;
/*    */ 
/* 63 */         e.printStackTrace();
/*    */       }
/* 65 */       is = null;
/*    */     }
/*    */   }
/*    */ 
/*    */   public static BufferedReader getReader(InputStream inputStream, String charEncoding) throws UnsupportedEncodingException {
/* 70 */     return new BufferedReader(new InputStreamReader(inputStream, charEncoding));
/*    */   }
/*    */ 
/*    */   public static void WriterObj(String path, Serializable hm)
/*    */     throws FileNotFoundException, IOException
/*    */   {
/* 82 */     ObjectOutputStream objectOutputStream = null;
/*    */     try {
/* 84 */       objectOutputStream = new ObjectOutputStream(new FileOutputStream(path));
/* 85 */       objectOutputStream.writeObject(hm);
/*    */     } finally {
/* 87 */       if (objectOutputStream != null)
/* 88 */         objectOutputStream.close();
/*    */     }
/*    */   }
/*    */ }

/* Location:           J:\workspace\ansj_seg\ansj_seg.jar
 * Qualified Name:     love.cq.util.IOUtil
 * JD-Core Version:    0.6.1
 */