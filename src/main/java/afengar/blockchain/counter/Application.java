package afengar.blockchain.counter;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.web3j.protocol.http.HttpService;
import org.web3j.quorum.Quorum;
import org.web3j.quorum.tx.ClientTransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

import afengar.blockchain.counter.handlers.CounterHandler;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public RouterFunction<ServerResponse> routerFunction(CounterHandler counterHandler) {
		return RouterFunctions.route(RequestPredicates.GET("/counter/increment"), counterHandler::increment)
				.andRoute(RequestPredicates.GET("/counter/decrement"), counterHandler::decrement)
				.andRoute(RequestPredicates.GET("/counter/counts"), counterHandler::counts)
				.andRoute(RequestPredicates.GET("/network/deployContract"), counterHandler::deployContract)
				.andRoute(RequestPredicates.GET("/network/loadContract"), counterHandler::loadContract);
	}

	@Bean
	public Quorum quorum(@Value("${network.rpc.url}") String networkRpcUrl) {
		return Quorum.build(new HttpService(networkRpcUrl));
	}

	@Bean
	public ClientTransactionManager publicClientTransactionManager(Quorum quorum,
			@Value("${network.voter.address}") String voterAddress,
			@Value("${network.privatefrom}") String privateFrom) {
		return new ClientTransactionManager(quorum, voterAddress, privateFrom, new ArrayList<>());
	}

	@Bean
	public ContractGasProvider contractGasProvider() {
		return new DefaultGasProvider();
	}
}
