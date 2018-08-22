# WardRI
This is the Reference Implementation Prototype of the Decentralized Ward Network

Warning: This Project is currently under development

WardRI is is Java-built open-source Implementation fo the Ward Network as described in the <a href="https://wardnetwork.org/whitepaper">Ward Whitepaper</a>

# Usage

To run WardRI you need to have installed Java1.8+

Further Information on how to user WardRI will be delivered after a alpha version is running (approximately early September 2018)


## Run



Run WardRI by executing the released or self-compiled jar-File



    java -jar ward_ri.jar



## Configuration



Below are all configuration fields which you can use. Use the parameters like shown in the example below.

    java -jar ward_ri.jar -example_parameter example_value



Parameters can be shortened (f.e. neighbor -> n). Optional parameters are marked with *.



* `neighbor`: Neighbor Address to connect to. (Example: "testserver.org:80")

* `selfport`: Port which other nodes can connect to and is open from otherside your LAN

* *`self`: Your own external IP-Address, if you don´t want to use outside services

* *`publickey`: Your public key, if not given, a random keypair will be generated. The Keypair is being saved for future uses

* *`privatekey`: You private key