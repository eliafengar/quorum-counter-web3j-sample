# quorum-counter-web3j-sample
Sample Counter Quorum Smart Contract Web3j API

## Purpose and Motivation
The purpose of this project is to enable a Java server side REST API to interact with Quorum Transaction node.
<br />
The project demonstrates a Counter Smart Contract which has the following methods: increment, decrement and counts.
<br />
The application enables to deploy "Public" and "Private" Smart Contracts and interact with them.
<br/ >
The REST API exposes the above Smart Contract methods and also the following methods: deployContract and loadContract.

## Build
Fill in the values in application.properties
<br />
In Case of Existing Smart Contracts - Update the Contract Addresses
*****
mvn clean
<br />
mvn package - this will start the embedded Apache Tomcat server.
*****
Deploy Contract using: http://localhost:8080/network/deployContract?private=<true|false>
<br />
Load Existing Contract using: http://localhost:8080/network/loadContract?private=<true|false>
*****
Interact with the Ethereum Smart Contract using:
<br />http://localhost:8080/counter/increment
<br />http://localhost:8080/counter/decrement
<br />http://localhost:8080/counter/counts
