/*     */ package love.cq.library;
/*     */ 
/*     */ import java.io.BufferedReader;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.InputStream;
/*     */ import java.util.List;
/*     */ import love.cq.domain.Branch;
/*     */ import love.cq.domain.Forest;
/*     */ import love.cq.domain.Value;
/*     */ import love.cq.domain.WoodInterface;
/*     */ import love.cq.util.IOUtil;
/*     */ 
/*     */ public class Library
/*     */ {
/*     */   public static Forest makeForest(String path)
/*     */     throws Exception
/*     */   {
/*  17 */     return makeForest(new FileInputStream(path));
/*     */   }
/*     */ 
/*     */   public static Forest makeForest(InputStream inputStream) throws Exception {
/*  21 */     return makeForest(IOUtil.getReader(inputStream, "UTF-8"));
/*     */   }
/*     */ 
/*     */   public static Forest makeForest(BufferedReader br) throws Exception {
/*  25 */     return makeLibrary(br, new Forest());
/*     */   }
/*     */ 
/*     */   public static Forest makeForest(List<Value> values)
/*     */   {
/*  36 */     Forest forest = new Forest();
/*  37 */     for (Value value : values) {
/*  38 */       insertWord(forest, value.toString());
/*     */     }
/*  40 */     return forest;
/*     */   }
/*     */ 
/*     */   private static Forest makeLibrary(BufferedReader br, Forest forest) throws Exception {
/*     */     try {
/*  45 */       String temp = null;
/*  46 */       while ((temp = br.readLine()) != null)
/*  47 */         insertWord(forest, temp);
/*     */     }
/*     */     catch (Exception e) {
/*  50 */       e.printStackTrace();
/*     */     } finally {
/*  52 */       br.close();
/*     */     }
/*  54 */     return forest;
/*     */   }
/*     */ 
/*     */   public static void insertWord(Forest forest, Value value) {
/*  58 */     insertWord(forest, value.toString());
/*     */   }
/*     */ 
/*     */   public static void insertWord(Forest forest, String temp)
/*     */   {
/*  68 */     String[] param = temp.split("\t");
/*     */ 
/*  70 */     temp = param[0];
/*     */ 
/*  72 */     String[] resultParams = (String[])null;
/*     */ 
/*  74 */     WoodInterface branch = forest;
/*  75 */     char[] chars = temp.toCharArray();
/*  76 */     for (int i = 0; i < chars.length; i++) {
/*  77 */       if (chars.length == i + 1) {
/*  78 */         resultParams = new String[param.length - 1];
/*  79 */         for (int j = 1; j < param.length; j++) {
/*  80 */           resultParams[(j - 1)] = param[j];
/*     */         }
/*  82 */         branch.add(new Branch(chars[i], 3, resultParams));
/*     */       } else {
/*  84 */         branch.add(new Branch(chars[i], 1, null));
/*     */       }
/*  86 */       branch = branch.get(chars[i]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void removeWord(Forest forest, String word)
/*     */   {
/*  97 */     WoodInterface branch = forest;
/*  98 */     char[] chars = word.toCharArray();
/*     */ 
/* 100 */     for (int i = 0; i < chars.length; i++) {
/* 101 */       if (branch == null)
/* 102 */         return;
/* 103 */       if (chars.length == i + 1) {
/* 104 */         branch.add(new Branch(chars[i], -1, null));
/*     */       }
/* 106 */       branch = branch.get(chars[i]);
/*     */     }
/*     */   }
/*     */ }

/* Location:           J:\workspace\ansj_seg\ansj_seg.jar
 * Qualified Name:     love.cq.library.Library
 * JD-Core Version:    0.6.1
 */