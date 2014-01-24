/*    */ package love.cq.util;
/*    */ 
/*    */ public class ObjectBean
/*    */ {
/*    */   public static int getInt(String str, int def)
/*    */   {
/*    */     try
/*    */     {
/*  6 */       return Integer.parseInt(str);
/*    */     }
/*    */     catch (NumberFormatException e) {
/*  9 */       e.printStackTrace();
/* 10 */     }return def;
/*    */   }
/*    */ }

/* Location:           J:\workspace\ansj_seg\ansj_seg.jar
 * Qualified Name:     love.cq.util.ObjectBean
 * JD-Core Version:    0.6.1
 */