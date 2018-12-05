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

This is currently untested against Azure, assumed to work as it reuses
code from the blog post. The Azure `namespace` is not set in code,
presumably it is part of the host connection URL supplied?

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
