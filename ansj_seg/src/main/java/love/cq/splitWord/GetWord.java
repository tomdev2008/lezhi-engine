/*     */ package love.cq.splitWord;
/*     */ 
/*     */ import java.io.BufferedReader;
/*     */ import java.io.PrintStream;
/*     */ import java.io.StringReader;
/*     */ import java.util.Arrays;
/*     */ import love.cq.domain.Forest;
/*     */ import love.cq.domain.WoodInterface;
/*     */ import love.cq.library.Library;
/*     */ 
/*     */ public class GetWord
/*     */ {
/*     */   private static final String EMPTYSTRING = "";
/*     */   private Forest forest;
/*     */   private char[] chars;
/*     */   private String str;
/*     */   public int offe;
/*     */   private int tempOffe;
/*     */   private String[] param;
/*  20 */   byte status = 0;
/*     */ 
/*  22 */   WoodInterface branch = this.forest;
/*     */ 
/*  24 */   int root = 0;
/*  25 */   int i = this.root;
/*  26 */   boolean isBack = false;
/*     */ 
/*     */   public GetWord(Forest forest, String content) {
/*  29 */     this.chars = Arrays.copyOf(content.toCharArray(), content.length() + 1);
/*  30 */     this.forest = forest;
/*  31 */     this.branch = forest;
/*     */   }
/*     */ 
/*     */   public String getAllWords() {
/*  35 */     String temp = allWords();
/*  36 */     while ("".equals(temp)) {
/*  37 */       temp = allWords();
/*     */     }
/*  39 */     return temp;
/*     */   }
/*     */ 
/*     */   public String getFrontWords() {
/*  43 */     String temp = frontWords();
/*  44 */     while ("".equals(temp)) {
/*  45 */       temp = frontWords();
/*     */     }
/*  47 */     return temp;
/*     */   }
/*     */ 
/*     */   private String allWords() {
/*  51 */     if ((!this.isBack) || (this.i == this.chars.length - 1)) {
/*  52 */       this.i = (this.root - 1);
/*     */     }
/*  54 */     for (this.i += 1; this.i < this.chars.length; this.i += 1) {
/*  55 */       this.branch = this.branch.get(this.chars[this.i]);
/*  56 */       if (this.branch == null) {
/*  57 */         this.root += 1;
/*  58 */         this.branch = this.forest;
/*  59 */         this.i = (this.root - 1);
/*  60 */         this.isBack = false;
/*     */       } else {
/*  62 */         switch (this.branch.getStatus()) {
/*     */         case 2:
/*  64 */           this.isBack = true;
/*  65 */           this.offe = (this.tempOffe + this.root);
/*  66 */           this.param = this.branch.getParams();
/*  67 */           return new String(this.chars, this.root, this.i - this.root + 1);
/*     */         case 3:
/*  69 */           this.offe = (this.tempOffe + this.root);
/*  70 */           this.str = new String(this.chars, this.root, this.i - this.root + 1);
/*  71 */           this.param = this.branch.getParams();
/*  72 */           this.branch = this.forest;
/*  73 */           this.isBack = false;
/*  74 */           this.root += 1;
/*  75 */           return this.str;
/*     */         }
/*     */       }
/*     */     }
/*  79 */     this.tempOffe += this.chars.length;
/*  80 */     return null;
/*     */   }
/*     */ 
/*     */   private String frontWords() {
/*  84 */     for (; this.i < this.chars.length; this.i += 1) {
/*  85 */       this.branch = this.branch.get(this.chars[this.i]);
/*  86 */       if (this.branch == null) {
/*  87 */         this.branch = this.forest;
/*  88 */         if (this.isBack) {
/*  89 */           this.str = new String(this.chars, this.root, this.tempOffe);
/*     */ 
/*  91 */           if ((this.root > 0) && (isE(this.chars[(this.root - 1)])) && (isE(this.str.charAt(0)))) {
/*  92 */             this.str = "";
/*     */           }
/*     */ 
/*  95 */           if ((this.str.length() != 0) && (this.root + this.tempOffe < this.chars.length) && (isE(this.str.charAt(this.str.length() - 1))) && 
/*  96 */             (isE(this.chars[(this.root + this.tempOffe)]))) {
/*  97 */             this.str = "";
/*     */           }
/*  99 */           if (this.str.length() == 0) {
/* 100 */             this.root += 1;
/* 101 */             this.i = this.root;
/*     */           } else {
/* 103 */             this.offe = (this.tempOffe + this.root);
/* 104 */             this.i = (this.root + this.tempOffe);
/* 105 */             this.root = this.i;
/*     */           }
/* 107 */           this.isBack = false;
/*     */ 
/* 109 */           if ("".equals(this.str)) {
/* 110 */             return "";
/*     */           }
/* 112 */           return this.str;
/*     */         }
/* 114 */         this.i = this.root;
/* 115 */         this.root += 1;
/*     */       } else {
/* 117 */         switch (this.branch.getStatus()) {
/*     */         case 2:
/* 119 */           this.isBack = true;
/* 120 */           this.tempOffe = (this.i - this.root + 1);
/* 121 */           this.param = this.branch.getParams();
/* 122 */           break;
/*     */         case 3:
/* 124 */           this.offe = (this.tempOffe + this.root);
/* 125 */           this.str = new String(this.chars, this.root, this.i - this.root + 1);
/* 126 */           String temp = this.str;
/*     */ 
/* 128 */           if ((this.root > 0) && (isE(this.chars[(this.root - 1)])) && (isE(this.str.charAt(0)))) {
/* 129 */             this.str = "";
/*     */           }
/*     */ 
/* 132 */           if ((this.str.length() != 0) && (this.i + 1 < this.chars.length) && (isE(this.str.charAt(this.str.length() - 1))) && 
/* 133 */             (isE(this.chars[(this.i + 1)]))) {
/* 134 */             this.str = "";
/*     */           }
/* 136 */           this.param = this.branch.getParams();
/* 137 */           this.branch = this.forest;
/* 138 */           this.isBack = false;
/* 139 */           if (temp.length() > 0) {
/* 140 */             this.i += 1;
/* 141 */             this.root = this.i;
/*     */           } else {
/* 143 */             this.i = (this.root + 1);
/*     */           }
/* 145 */           if ("".equals(this.str)) {
/* 146 */             return "";
/*     */           }
/* 148 */           return this.str;
/*     */         }
/*     */       }
/*     */     }
/* 152 */     this.tempOffe += this.chars.length;
/* 153 */     return null;
/*     */   }
/*     */ 
/*     */   public boolean isE(char c) {
/* 157 */     if ((c >= 'a') && (c <= 'z')) {
/* 158 */       return true;
/*     */     }
/* 160 */     switch (c) {
/*     */     case '.':
/* 162 */       return true;
/*     */     case '-':
/* 164 */       return true;
/*     */     case '/':
/* 166 */       return true;
/*     */     case '#':
/* 168 */       return true;
/*     */     case '?':
/* 170 */       return true;
/*     */     }
/* 172 */     return false;
/*     */   }
/*     */ 
/*     */   public void reset(String content) {
/* 176 */     this.offe = 0;
/* 177 */     this.status = 0;
/* 178 */     this.root = 0;
/* 179 */     this.i = this.root;
/* 180 */     this.isBack = false;
/* 181 */     this.tempOffe = 0;
/* 182 */     this.chars = content.toCharArray();
/* 183 */     this.branch = this.forest;
/*     */   }
/*     */ 
/*     */   public String getParam(int i) {
/* 187 */     if ((this.param == null) || (this.param.length < i + 1)) {
/* 188 */       return null;
/*     */     }
/* 190 */     return this.param[i];
/*     */   }
/*     */ 
/*     */   public static void main(String[] args)
/*     */     throws Exception
/*     */   {
/* 197 */     String dic = "java学习\t10\nc\t100\nC++\t10\nc++\t5\nc#\t100".toLowerCase();
/* 198 */     Forest forest = Library.makeForest(new BufferedReader(new StringReader(dic)));
/*     */ 
/* 203 */     Library.removeWord(forest, "中国");
/*     */ 
/* 207 */     Library.insertWord(forest, "中国人");
/* 208 */     String content = "c c++ c++ c++ ".toLowerCase();
/* 209 */     GetWord udg = forest.getWord(content.toLowerCase());
/*     */ 
/* 211 */     String temp = null;
/* 212 */     while ((temp = udg.getFrontWords()) != null)
/* 213 */       System.out.println(temp + "\t\t" + udg.getParam(0) + "\t\t" + udg.getParam(2));
/*     */   }
/*     */ }

/* Location:           J:\workspace\ansj_seg\ansj_seg.jar
 * Qualified Name:     love.cq.splitWord.GetWord
 * JD-Core Version:    0.6.1
 */