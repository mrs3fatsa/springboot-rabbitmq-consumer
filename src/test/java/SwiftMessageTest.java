import com.prowidesoftware.swift.io.ConversionService;
import com.prowidesoftware.swift.model.SwiftMessage;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class SwiftMessageTest {

    private static ConversionService conversionService;

    @BeforeAll
    public static void setup(){
        conversionService = new ConversionService();
    }

    public void testMT101Conversion(){
        try {
            InputStream inputStreamMT101 = getClass().getClassLoader().getResourceAsStream("absa-spring-boot-request.txt");
            String encodedMT101Request = IOUtils.toString(inputStreamMT101, StandardCharsets.UTF_8);
            SwiftMessage sm = SwiftMessage.parse(encodedMT101Request);

            String encodedMT101Xml = conversionService.getXml(encodedMT101Request);
            Document doc = Jsoup.parse(encodedMT101Xml, "", Parser.xmlParser());

            assertEquals(doc.select("logicalTerminal").text(), sm.getSender());
            assertEquals(doc.select("receiverAddress").text(), sm.getReceiver());
        } catch(Exception e){
            fail();
        }
    }
}
