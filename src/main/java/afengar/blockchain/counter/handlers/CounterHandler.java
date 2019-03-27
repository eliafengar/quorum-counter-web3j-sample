package afengar.blockchain.counter.handlers;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.quorum.Quorum;
import org.web3j.quorum.tx.ClientTransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import afengar.blockchain.counter.Counter;
import reactor.core.publisher.Mono;

@Component
public class CounterHandler {

	@Autowired
	private Quorum quorum;

	@Autowired
	private ClientTransactionManager transactionManager;

	@Autowired
	private ContractGasProvider contractGasProvider;

	@Value("${network.contract.public.address}")
	private String publicContractAddress;

	@Value("${network.contract.private.address}")
	private String privateContractAddress;

	@Value("#{'${network.privatefor}'.split(',')}")
	private List<String> privateFor;

	private Counter contract;

	public CounterHandler() {

	}

	@PostConstruct
	public void init() {
		try {
			Web3ClientVersion web3ClientVersion = quorum.web3ClientVersion().sendAsync().get();
			String clientVersion = web3ClientVersion.getWeb3ClientVersion();
			System.out.println(clientVersion);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	public Mono<ServerResponse> counts(ServerRequest request) {
		BigInteger counts = new BigInteger("-1");
		CompletableFuture<BigInteger> result = this.contract.counts().sendAsync();
		try {
			counts = result.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return ServerResponse.status(HttpStatus.BAD_REQUEST).build();
		}
		return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).syncBody(counts);
	}

	public Mono<ServerResponse> increment(ServerRequest request) {
		BigInteger blockNumber = new BigInteger("-1");
		CompletableFuture<TransactionReceipt> result = this.contract.increment().sendAsync();
		try {
			TransactionReceipt reciept = result.get();
			blockNumber = reciept.getBlockNumber();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return ServerResponse.status(HttpStatus.BAD_REQUEST).build();
		}
		return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).syncBody("Block Number:" + blockNumber);
	}

	public Mono<ServerResponse> decrement(ServerRequest request) {
		BigInteger blockNumber = new BigInteger("-1");
		CompletableFuture<TransactionReceipt> result = this.contract.decrement().sendAsync();
		try {
			TransactionReceipt reciept = result.get();
			blockNumber = reciept.getBlockNumber();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return ServerResponse.status(HttpStatus.BAD_REQUEST).build();
		}
		return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).syncBody("Block Number:" + blockNumber);
	}

	public Mono<ServerResponse> deployContract(ServerRequest request) {
		try {
			this.transactionManager.setPrivateFor(new ArrayList<>());
			boolean isPrivate = this.isPrivate(request);
			if (isPrivate) {
				this.transactionManager.setPrivateFor(this.privateFor);
			}
			CompletableFuture<Counter> result = Counter
					.deploy(this.quorum, this.transactionManager, this.contractGasProvider, BigInteger.ZERO)
					.sendAsync();
			this.contract = result.get();

			if(isPrivate){
				this.privateContractAddress = this.contract.getContractAddress();
			}else {
				this.publicContractAddress = this.contract.getContractAddress();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ServerResponse.status(HttpStatus.BAD_REQUEST).build();
		}
		return ServerResponse.ok().syncBody(this.contract.getContractAddress());
	}

	public Mono<ServerResponse> loadContract(ServerRequest request) {
		try {
			String contractAddress = this.publicContractAddress;
			this.transactionManager.setPrivateFor(new ArrayList<>());
			if (this.isPrivate(request)) {
				if (this.privateContractAddress != null && this.privateContractAddress != "") {
					contractAddress = this.privateContractAddress;
					this.transactionManager.setPrivateFor(this.privateFor);
				}
			}
			this.contract = Counter.load(contractAddress, this.quorum, this.transactionManager,
					this.contractGasProvider);
		} catch (Exception e) {
			e.printStackTrace();
			return ServerResponse.status(HttpStatus.BAD_REQUEST).build();
		}
		return ServerResponse.ok().build();
	}

	private boolean isPrivate(ServerRequest request) {
		String isPrivateStr = request.queryParam("private").get();
		return isPrivateStr != null && isPrivateStr != "" ? Boolean.parseBoolean(isPrivateStr) : false;
	}
}
