package unipi.cloudstorage.shared.security;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import unipi.cloudstorage.shared.FileManager;

import javax.crypto.Cipher;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

@Component
@AllArgsConstructor
public class FileEncoder {

    private final FileManager fileManager;

//    private final char[] password = "3=vyBL!YF9~#".toCharArray();
    private final char[] PASSWORD = "Szzu4@$sMbwnm24Vz#OpFwTXWeBlWoJ".toCharArray();
//    private final char[] password = "PASSWORD".toCharArray();

    public byte[] generateDigitalSignature(byte[] fileBytes) {

        // Hash the files using SHA-256
        byte[] fileHash = this.hash(fileBytes);

        // Encrypt the hash using the Private Key
        return this.encrypt(fileHash);
    }

    public boolean validateDigitalSignature(byte[] fileBytes, byte[] digitalSignature) {

        // Decrypt digital signature using the Public Key
        byte[] decryptedDigitalSignature = this.decrypt(digitalSignature);

        // Hash once again the file received by the client
        byte[] receivedFileHashed = this.hash(fileBytes);

        // If the hashes are the same that means that the validation is correct
        return Arrays.equals(decryptedDigitalSignature, receivedFileHashed);
    }

    public byte[] encrypt(byte[] hashedBytes) {
        byte[] digitalSignature = null;
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, this.getPrivateKey());
            digitalSignature = cipher.doFinal(hashedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return digitalSignature;
    }

    public byte[] decrypt(byte[] digitalSignature) {
        byte[] decryptedDigitalSignature;
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, this.getPublicKey());
            decryptedDigitalSignature = cipher.doFinal(digitalSignature);
        } catch (Exception e) {
            return null;
        }
        return decryptedDigitalSignature;
    }

    public PrivateKey getPrivateKey() throws Exception{
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        InputStream privateKeyFileStream = fileManager.getFileFromResourceAsStream(
                "static/certificates/privateKey.p12",
                this.getClass().getClassLoader()
        );
        keyStore.load(privateKeyFileStream, PASSWORD);
        return (PrivateKey) keyStore.getKey("senderKeyPair", PASSWORD);
    }

    public PublicKey getPublicKey() throws Exception {
        InputStream publicKeyFileStream = fileManager.getFileFromResourceAsStream(
                "static/certificates/publicKey.cer",
                this.getClass().getClassLoader()
        );
        CertificateFactory f = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) f.generateCertificate(publicKeyFileStream);
        return certificate.getPublicKey();
    }

    public byte[] hash(byte[] fileBytes) {
        byte[] fileHash = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            fileHash = messageDigest.digest(fileBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return fileHash;
    }

    public  String byteArrayToHexString(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public  byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
