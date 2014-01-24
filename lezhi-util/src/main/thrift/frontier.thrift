
namespace java com.buzzinate.thrift.frontier

struct Item {
    1: required string id,
    2: required string data,
    3: required string queue,
    4: optional i32 checkInterval
}

service UrlFrontier {
    void offer(1: list<Item> items),
    list<Item> pop(1: i32 max),
    void ack(1: list<string> ids),
    void reset(),
    list<string> queues(),
    list<string> peek(1: string queue, 2: i32 max),
    list<string> recent()
}
