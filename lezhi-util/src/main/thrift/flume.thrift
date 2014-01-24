namespace java org.apache.flume.thrift

struct ThriftFlumeEvent {
  1: required map <string, string> headers,
  2: required binary body,
}

enum Status {
  OK,
  FAILED,
  ERROR,
  UNKNOWN
}

service ThriftSourceProtocol {
  Status append(1: ThriftFlumeEvent event),
  Status appendBatch(1: list<ThriftFlumeEvent> events),
}