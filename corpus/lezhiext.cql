CREATE KEYSPACE corpus WITH placement_strategy = 'org.apache.cassandra.locator.NetworkTopologyStrategy' AND strategy_options = {DC1:3};
use corpus;
CREATE COLUMN FAMILY ngram WITH comparator = UTF8Type AND default_validation_class=CounterColumnType;
