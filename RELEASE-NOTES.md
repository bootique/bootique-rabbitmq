## 3.0.M1

* #9 Channel pool support in ChannelFactory
* #14 Allow multiple routing keys for a combination of exhcnage and queue
* #15 Make RmqConnectionManager injectable
* #16 Injectable RmqTopologyManager
* #17 Split RmqChannelFactory functions between RmqChannelManager and RmqTopologyManager
* #20 Queue config templates for sub endpoints (short-lived queues)

## 2.0.RC1

* #11 Cleaner shutdown
* #12 String value for "x-message-ttl" causes exception on "declareQueue"
* #13 Upgrade amqp-client from 5.12.0 to 5.14.0

## 2.0.B1

* #4 Configurable Publishers / Consumers
* #7 Upgrade Testcontainers to 1.15.0-rc2
* #8 ChannelFactory: clear unambiguous API
* #10 Upgrade RMQ client to 5.12.0

## 2.0.M1

* #1 Migrate "bootique-rabbitmq-client" to "bootique-rabbitmq"
* #2 More straightforward channel access
* #3 Upgrade to the latest amqp-client
* #5 bootique-rabbitmq-junit5
* #6 Preserve connection defaults when not set explicitly

_The task numbering below this is coming from the old project at https://github.com/bootique/bootique-rabbitmq-client and was restarted
when we migrated to the new repo._

## 0.25

* #1 Exception when using ChannelFactory.openChannel()
* #4 Upgrade to bootique-modules-parent 0.8
* #8 Upgrade ampq-client dependency version
