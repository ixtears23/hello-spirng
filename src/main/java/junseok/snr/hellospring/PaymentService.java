package junseok.snr.hellospring;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class PaymentService {
    public Payment prepare(Long orderId, String currency, BigDecimal foreignCurrencyAmount) throws IOException {
        // 환율 가져오기
        final URL url = new URL("https://open.er-api.com/v6/latest/" + currency);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        final BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        final String response = br.lines().collect(Collectors.joining());
        br.close();

        final ObjectMapper objectMapper = new ObjectMapper();
        final ExRateData data = objectMapper.readValue(response, ExRateData.class);
        final BigDecimal exRate = data.rates().get("KRW");

        // 금액 계산
        final BigDecimal convertedAmount = foreignCurrencyAmount.multiply(exRate);

        // 유효 시간 계산
        final LocalDateTime validUntil = LocalDateTime.now().plusMinutes(30);

        return new Payment(orderId, currency, foreignCurrencyAmount, exRate, convertedAmount, validUntil);
    }

    public static void main(String[] args) throws IOException {
        final PaymentService paymentService = new PaymentService();
        final Payment payment = paymentService.prepare(100L, "USD", BigDecimal.valueOf(50.7));

        System.out.println(payment);
    }
}
