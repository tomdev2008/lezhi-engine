namespace java com.buzzinate.lezhi.thrift

enum RecommendType {
    INSITE,     // 站内推荐
    OUTSITE,    // 站外推荐
    TRENDING,   // 热门文章
    PERSONALIZED,   // 个性化
    ITEMCF,     //基于内容推荐
}

enum PicType {
    TEXT,        // 文本文章,无需匹配图片
    INPAGE,     // 匹配到文章图片
    INSITE,     // 匹配到站内图片
    PROVIDED,   // 使用乐知提供的图片
}

struct RecommendTypeParam {
    1: required i32 order , //请求次序
    2: required RecommendType recommendType, //推荐类型
    3: required PicType matchPic, //是否图文匹配
    4: required i32 count = 5,   //请求条数
}

struct RecommendParam {
    1: required string url,
    2: required list<RecommendTypeParam> types,
    3: optional string title,
    4: optional string siteprefix,
    5: optional string userid = "1",
    6: optional string keywords,
    7: optional string canonicalUrl,
    8: optional string customThumbnail,
    9: optional string customTitle,
}

struct RecommendItem {
    1: required string url,
    2: required string title,
    3: optional string pic,
    4: optional double score,
    5: optional double hotScore,
}

struct RecommendItemList {
    1: required RecommendTypeParam typeParam,
    2: required list<RecommendItem> items,
}

struct RecommendResult {
    1: required list<RecommendItemList> results,
    2: required string thumbnail,
}

struct ClickParam {
    1: required string tourl,
    2: required string fromurl,
    3: optional RecommendType type,
    4: optional string siteprefix,
    5: optional string userid = "1",
}

service RecommendServices {
    RecommendResult recommend(1:RecommendParam param),
    void click(1:ClickParam param),
    void recrawl(1:required string url),
    void correctImg(1:required string url, 2:required string rightImg, 3:optional string userAgent),
}