# Flow control

 Designed and implemented two flow control approaches, Stop&Wait and Go-Back-B(GBN). Aim of this project was to get to know sockets, datagram packets and threads and to design a protocol i.e. packet layout and packet handling, for communication between two nodes.


### Stop & Wait 

The client sends a number of packets, one after the other, also ensured the receiver is able to handle the incoming packet. The implementation assignts alternating numbers, 0 and 1, to packets and the acknowledgements by the Server will have to indicate the number of the packet the Server expects next. Stop & Wait includes time-outs and a retransmission mechanism of an already transmitted packet. The setSoTimeout method of the DatagramSocket class causes an exception to be thrown if a receive method has not returned within a given time.


![picture1](https://user-images.githubusercontent.com/17296281/28249322-7d377a64-6a4b-11e7-9f7b-ffca09513c95.png)



### Go-Back-N

In GBN, the client establishes a connection to the server, take consecutive chunks of a buffer and then sends these chunks to the server. The server acknowledges the chunks that it receives.



#### For more information about the implemetnation here is a detailed report:
[flowControlDoc.pdf](https://github.com/ammarqureshi/flow-control/files/1151149/flowControlDoc.pdf)






