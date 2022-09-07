package test;

import com.passwordmanager.data.PasswordData;
import com.passwordmanager.data.PasswordListData;
import com.passwordmanager.exception.InvalidContentException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.ArrayList;

import static com.passwordmanager.encryption.EncryptorAesGcmPassword.decrypt;
import static com.passwordmanager.encryption.EncryptorAesGcmPassword.encrypt;
import static java.nio.charset.StandardCharsets.UTF_8;

public class TestAESEncryption {

    public static void main(String[] args) throws Exception {

        String OUTPUT_FORMAT = "%-30s:%s";
        String PASSWORD = "this is a password";

        PasswordListData passwordListData = new PasswordListData();
        final ArrayList<PasswordData> passwordDataList = new ArrayList<>();
        final PasswordData passwordData = new PasswordData();
        passwordData.setPassword("test");
        passwordData.setUser("bobo");
        passwordDataList.add(passwordData);
        passwordListData.setPasswordDataList(passwordDataList);

        try {
            JAXBContext jc = JAXBContext.newInstance(PasswordListData.class);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            final StringWriter writer = new StringWriter();
            m.marshal(passwordListData, new StreamResult(writer));
            final String pText = writer.toString();
            String encryptedTextBase64 = encrypt(pText.getBytes(UTF_8), PASSWORD);

            System.out.println("\n------ AES GCM Password-based Encryption ------");
            System.out.println(String.format(OUTPUT_FORMAT, "Input (plain text)", pText));
            System.out.println(String.format(OUTPUT_FORMAT, "Encrypted (base64) ", encryptedTextBase64));

            System.out.println("\n------ AES GCM Password-based Decryption ------");
            System.out.println(String.format(OUTPUT_FORMAT, "Input (base64)", encryptedTextBase64));

            String decryptedText = decrypt(encryptedTextBase64.getBytes(UTF_8), PASSWORD);
            System.out.println(String.format(OUTPUT_FORMAT, "Decrypted (plain text)", decryptedText));

        } catch (JAXBException e) {
            throw new InvalidContentException();
        }

    }
}
