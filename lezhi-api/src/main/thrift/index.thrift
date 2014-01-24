namespace java com.buzzinate.lezhi.thrift

enum StatusEnum {
    NORMAL = 0,
    PRIOR = 1,
    HIDDEN = 2,
}

struct Metadata {
    1: required string url,
    2: optional string thumbnail, 
    3: optional string title, 
    4: optional string keywords, 
    5: optional string blackKeywords, 
    6: optional StatusEnum status, 
}

struct SearchResult {
    1: required i32 totalHit,
    2: required list<Metadata> docs
}

service ElasticIndexService {
    
    //根据title模糊匹配包含该title的页面，并分页返回页面对应的url结果集
    SearchResult searchUrls(1:required list<string> sitePrefixes, 2:required string title, 3:required i32 start, 4:required i32 size),
    
    //查找对应urls的内容管理索引
    list<Metadata> getByUrls(1:required list<string> urls),

    //根据网站的sitePrefixes查找和过滤，如果结果为空，则返回domain查找的结果
    SearchResult matchAll(1:required list<string> sitePrefixes, 2:required i32 start, 3:required i32 size),
    
    // 根据urls批量删除elastic search上的索引
    void deleteIndexes(1:required list<string> urls),
    
    //修改Metadate信息
    void updateMetadata(1:required Metadata metadata)
}