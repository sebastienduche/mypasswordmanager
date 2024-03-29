package com.passwordmanager.data;

import com.passwordmanager.encryption.FileEncoder;
import com.passwordmanager.exception.InvalidContentException;
import com.passwordmanager.exception.InvalidPasswordException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "passwordDataList", "lastModified"
})
@XmlRootElement(name = "credentials")
public class PasswordListData {

    @XmlElement(name = "credential")
    private List<PasswordData> passwordDataList;

    private String lastModified;

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public List<PasswordData> getPasswordDataList() {
        return passwordDataList;
    }

    public void setPasswordDataList(List<PasswordData> passwordDataList) {
        this.passwordDataList = passwordDataList;
    }

    public static PasswordListData load(final File file, String password) throws InvalidPasswordException, InvalidContentException {
        try {
            final String decodeFile = FileEncoder.decodeFile(file, password);
            JAXBContext jaxbContext = JAXBContext.newInstance(PasswordListData.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            assert decodeFile != null;
            PasswordListData passwordListData = (PasswordListData) jaxbUnmarshaller.unmarshal(new StringReader(decodeFile));
            if (passwordListData.getPasswordDataList() == null) {
                throw new InvalidContentException("Incorrect file content!");
            }
            passwordListData.passwordDataList.sort(Comparator.comparing(PasswordData::getName, String.CASE_INSENSITIVE_ORDER));
            return passwordListData;
        } catch (IOException | JAXBException e) {
            throw new InvalidContentException();
        }
    }

    public static boolean save(File f, PasswordListData passwordListData, String password) throws InvalidContentException {
        try {
            JAXBContext jc = JAXBContext.newInstance(PasswordListData.class);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            final StringWriter writer = new StringWriter();
            m.marshal(passwordListData, new StreamResult(writer));
            FileEncoder.encodeFile(f, writer.toString(), password);
        } catch (Exception e) {
            throw new InvalidContentException();
        }
        return true;
    }
}
