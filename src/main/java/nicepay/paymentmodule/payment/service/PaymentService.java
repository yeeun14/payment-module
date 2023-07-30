package nicepay.paymentmodule.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nicepay.paymentmodule.payment.domain.Payment;
import nicepay.paymentmodule.payment.domain.PaymentCard;
import nicepay.paymentmodule.payment.repository.PaymentCardRepository;
import nicepay.paymentmodule.payment.repository.PaymentRepository;
import org.apache.commons.codec.binary.Hex;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentCardRepository paymentCardRepository;

    private static final String MID = "나이스 Mid";
    private static final String MERCHANTKEY = "나이스 Key";

    // HttpURLConnection 방식 (POST)
    public static String postRequest(String data, String targetUrl) {

        String response = "";

        try {

            URL url = new URL(targetUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST"); // 전송 방식
            conn.setConnectTimeout(5000); // 연결 타임아웃 설정(5초)
            conn.setReadTimeout(5000); // 읽기 타임아웃 설정(5초)
            conn.setDoOutput(true);	// URL 연결을 출력용으로 사용(true)

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
            bw.write(data);
            bw.flush();
            bw.close();

            System.out.println("getContentType():" + conn.getContentType()); // 응답 콘텐츠 유형 구하기
//            System.out.println("getResponseCode():"    + conn.getResponseCode()); // 응답 코드 구하기
//            System.out.println("getResponseMessage():" + conn.getResponseMessage()); // 응답 메시지 구하기

            Charset charset = Charset.forName("UTF-8");
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), charset));

            String inputLine;
            StringBuffer sb = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            br.close();

            response = sb.toString();
            log.info("나이스 페이먼츠 서버응답 결과={}", response);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }


    /**
     * SHA-256 암호화
     */
    public String shaDecrypt(String text) {
        String sha = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.reset();
            md.update(text.getBytes());
            byte[] data = md.digest();
            sha = encodeHex(data);

        } catch (Exception e) {
            log.error("SHA 암호화 에러={}", e.toString());
        }
        return sha;
    }

    public String encodeHex(byte[] data) {
        char[] c = Hex.encodeHex(data);
        return new String(c);
    }

    /**
     * AES 암호화
     */
    public static String aesDecrypt(String text, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] crypted = cipher.doFinal(text.getBytes());

            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < crypted.length; i++) {
                String hex = Integer.toHexString(crypted[i] & 0xFF);
                if (hex.length() == 1) {
                    hex = '0' + hex;
                }
                sb.append(hex.toUpperCase());
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("AES 암호화 에러={}", e.toString());
        }
        return null;
    }

    /**
     * json형태의 String을 HashMap으로 변환
     */
    private static HashMap jsonToHashMap(String text) {
        HashMap hashMap = new HashMap<>();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(text);
            JSONObject json = (JSONObject) obj;

            Iterator<String> keyStr = json.keySet().iterator();
            while (keyStr.hasNext()) {
                String key = keyStr.next();
                Object value = json.get(key);

                hashMap.put(key, value);
            }
        } catch (Exception e) {
            log.info("jsonToHashMap={}",e.toString());
        }
        return hashMap;
    }

    /**
     * 현재 날짜 생성 (ediDate)
     * YYYYMMDDHHmmss
     */
    public final String getEdiDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return dateFormat.format(new Date());
    }

    /**
     * TID 생성
     */
    public final String getTID(String mid, String svc, String billing) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
        String date = dateFormat.format(new Date());

        StringBuffer str = new StringBuffer();
        Random random = new Random();
        String resultNum = "";
        for (int i=0; i<4; i++) {
            String num = Integer.toString(random.nextInt(10));
            resultNum += num;
        }
        str.append(mid);
        str.append(svc);
        str.append(billing);
        str.append(date);
        str.append(resultNum);

        log.info("getTID={}", str);
        return str.toString();
    }

    /**
     * 빌키 발급
     */
    public JSONObject billkeyRegister(JSONObject param) {

        String Moid = (String) param.get("Moid");
        String CardNo = (String) param.get("CardNo");
        String ExpYear = (String) param.get("ExpYear");
        String ExpMonth = (String) param.get("ExpMonth");
        String IDNo = (String) param.get("IDNo");
        String CardPw = (String) param.get("CardPw");

        // EncData 생성
        StringBuffer str = new StringBuffer();
        str.append("CardNo=").append(CardNo).append("&");
        str.append("ExpYear=").append(ExpYear).append("&");
        str.append("ExpMonth=").append(ExpMonth).append("&");
        str.append("IDNo=").append(IDNo).append("&");
        str.append("CardPw=").append(CardPw);

        String EncData = aesDecrypt(str.toString(), MERCHANTKEY.substring(0, 16));

        // SignData
        String EdiDate = getEdiDate();
        String SignData = shaDecrypt(MID + EdiDate + Moid + MERCHANTKEY);

        // 빌키 발급
        StringBuffer request = new StringBuffer();
        request.append("MID=").append(MID).append("&");
        request.append("EdiDate=").append(EdiDate).append("&");
        request.append("Moid=").append(Moid).append("&");
        request.append("EncData=").append(EncData).append("&");
        request.append("SignData=").append(SignData).append("&");
        request.append("CharSet=").append("utf-8");

        // EdiType을 별도로 설정하지 않았으므로 JSON 형태로 반환
        String resultBillkey = postRequest(request.toString(), "https://webapi.nicepay.co.kr/webapi/billing/billing_regist.jsp");

        HashMap resultData = new HashMap();
        resultData = jsonToHashMap(resultBillkey);

        String resultCode = "9999"; // 결과코드
        String resultMsg = "통신오류"; // 결과 메세지
        String TID = ""; // 거래번호
        String BID = ""; // 빌키
        String authDate = ""; // 인증일자
        String cardName = ""; // 카드사

        if(!"ERROR".equals(resultBillkey)){
            resultCode = (String) resultData.get("ResultCode");
            resultMsg = (String) resultData.get("ResultMsg");
            TID = (String) resultData.get("TID");
            BID = (String) resultData.get("BID");
            authDate = (String) resultData.get("AuthDate");
            cardName = (String) resultData.get("CardName");
        }

        JSONObject result = new JSONObject();
        result.put("resultCode", resultCode);
        result.put("resultMsg", resultMsg);
        result.put("TID", TID);
        result.put("BID", BID);
        result.put("authDate", authDate);
        result.put("cardName", cardName);

        return result;
    }

    public Long insertCard(PaymentCard paymentCard) {
        paymentCardRepository.save(paymentCard);
        return paymentCard.getId();
    }

    public List<PaymentCard> findCard() {
        List<PaymentCard> result = paymentCardRepository.findAll();
        log.info("result={}", result);
        return result;
    }

    /**
     * 빌키 결제
     */
    public JSONObject billingApprove(Long id, String price) {
        PaymentCard cardId = paymentCardRepository.findById(id).get();
        String BID = cardId.getBID();
        String TID = getTID(MID, "01", "16");
        String EdiDate = getEdiDate();
        String Moid = UUID.randomUUID().toString();
        String Amt = price.toString();
        String GoodsName = "전기차 충전";
        String CardInterest = "0";
        String CardQuota = "00";

        // SignData
        String SignData = shaDecrypt(MID + EdiDate + Moid + Amt + BID + MERCHANTKEY);

        // 결제 요청
        // 빌키 발급
        StringBuffer request = new StringBuffer();
        request.append("BID=").append(BID).append("&");
        request.append("MID=").append(MID).append("&");
        request.append("TID=").append(TID).append("&");
        request.append("EdiDate=").append(EdiDate).append("&");
        request.append("Moid=").append(Moid).append("&");
        request.append("Amt=").append(Amt).append("&");
        request.append("GoodsName=").append(GoodsName).append("&");
        request.append("SignData=").append(SignData).append("&");
        request.append("CardInterest=").append(CardInterest).append("&");
        request.append("CardQuota=").append(CardQuota).append("&");
        request.append("CharSet=").append("utf-8");

        // EdiType을 별도로 설정하지 않았으므로 JSON 형태로 반환
        String resultBillkey = postRequest(request.toString(), "https://webapi.nicepay.co.kr/webapi/billing/billing_approve.jsp");

        HashMap resultData = new HashMap();
        resultData = jsonToHashMap(resultBillkey);


        String resultCode = "9999"; // 결과코드
        String resultMsg = "통신오류"; // 결과 메세지
        String reusltTID = ""; // 거래번호
        String resultMoid = ""; // 주문번호
        String resultAmt = ""; // 거래금액
        String authCode = ""; // 승인번호
        String authDate = ""; // 승인날짜
        String acquCardCode = ""; // 매입카드사 코드
        String cardNo = ""; // 카드번호
        String cardCode = ""; // 카드사 코드
        String cardQuota = ""; // 할부개월
        String cardCl = ""; // 카드타입
        String ccPartCl  = ""; // 부분취소 가능여부

        if(!"ERROR".equals(resultBillkey)){
            resultCode = (String) resultData.get("ResultCode");
            resultMsg = (String) resultData.get("ResultMsg");
            reusltTID = (String) resultData.get("TID");
            resultMoid = (String) resultData.get("Moid");
            resultAmt = (String) resultData.get("Amt");
            authCode = (String) resultData.get("AuthCode");
            authDate = (String) resultData.get("AuthDate");
            acquCardCode = (String) resultData.get("AcquCardCode");
            cardNo = (String) resultData.get("CardNo");
            cardCode = (String) resultData.get("CardCode");
            cardQuota = (String) resultData.get("CardQuota");
            cardCl = (String) resultData.get("CardCl");
            ccPartCl = (String) resultData.get("CcPartCl");
        }

        JSONObject result = new JSONObject();
        result.put("resultCode", resultCode);
        result.put("resultMsg", resultMsg);
        result.put("reusltTID", reusltTID);
        result.put("resultMoid", resultMoid);
        result.put("resultAmt", resultAmt);
        result.put("authCode", authCode);
        result.put("authDate", authDate);
        result.put("acquCardCode", acquCardCode);
        result.put("cardNo", cardNo);
        result.put("cardCode", cardCode);
        result.put("cardQuota", cardQuota);
        result.put("cardCl", cardCl);
        result.put("ccPartCl", ccPartCl);

        return result;
    }

    /**
     * 결제결과 정보 저장
     */
    public Long insertPayment(Payment payment) {
        paymentRepository.save(payment);
        return payment.getId();
    }
}
