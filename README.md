Spring JMS sample for use with both Broker-J and Azure Service Bus
==================================================================

This code sample shows how to use SpringJMS with AMQPS to connect to
queues and topics in both Apache QPID Broker-J and Azure Service Bus.

Using SpringJMS with AMQPS is a more generic solution than using the
native Azure Service Bus library. This model also allows developers
to spin up a local copy of Apache QPID Broker-J for testing offline.

Broker-J Installation and Config
--------------------------------

The installation process is quite straightforward, and documented here:
https://qpid.apache.org/releases/qpid-broker-j-7.0.6/book/index.html

You need to do some things after installation:

1. Create a keystore for use with SSL (can create a self-signed keystore
from within the Broker-J admin tool)
2. Create an AMQP port on 5671, which uses the SSL transport (for TLS)
2. Add a durable queue called `myqueue` for testing queues
3. Add a durable exchange of type 'topic' called `test.topic` for testing
topics and subscriptions.

You can do this on a running instance of Broker-J from the admin tool
at http://localhost:8080 (login guest/guest), or use the sample config
file supplied with this source.

Note: the `subscription` is created automatically as a durable queue
bound to the test.topic exchange when the code first starts. You may
find that the first time the app starts, the topic message is lost
because this queue is not yet created - however subsequent runs
will work fine.

Code Notes
----------

Tested as-is against both Apache Qpid Broker-J (configured as above) and
Azure Service Bus configured with a queue and topic/subscription. For
ASB, you use the Access Policy name as the username (e.g. *RootManageSharedAccessKey* - excpet don't use that one, create one with fewer permissions) - and use the *primary key* as the password.

JMS and AMQP 1.0 have slightly different ideas of how Topics and Subscriptions
should work - AMQP 1.0 treats a subscription like a queue, but JMS treats it 
as something a little different, that only one client can connect to. This 
may lead to an issue if we need multiple instances of the service
connecting to the same subscription (i.e. load balanced) as the Qpid
library (at least, when connecting to Broker-J), creates a subscription
queue name using the client ID, which therefore has
to be unique for every connection. When connecting to Azure this will
hopefully not be so much of an issue as the TopicSubscription name will 
be preset/hardwired in the infrastructure config rather than created on the fly, and
Azure subscriptions allow multiple client connections.

In order to use a self-signed certificate on the local Broker-J instance,
I have included a very crude "trust all" SSL Context. This is only enabled
if the config property `amqp.trustAllCerts` is set and should *never* be used
in production, or when talking to Azure Service Bus in general - only for
development purposes against Broker-J.


References
----------

The bulk of the code used here came from this blog post by Ed Hillman:
http://ramblingstechnical.blogspot.com/p/using-azure-service-bus-with-spring-jms.html

See also Microsoft's own documentation on using AMQP with JMS:
https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-java-how-to-use-jms-api-amqp
(although note this does not use SpringJMS)
