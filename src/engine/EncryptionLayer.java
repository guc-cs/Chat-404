/************************************
 * Title: 	EncryptionLayer
 * Date:	10.8.2012
 * Purpose: Handles encryption and 
 * 			decryption of messages
 * Note: 	Not very secure,
 * 			initVector needs to be
 * 			randomized not static
 ************************************/

package engine;

import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionLayer {

	private static final byte[] secretKey = new byte[] { 0x4E, 0x6f, 0x75,
			0x68, 0x61, 0x6e, 0x53, 0x61, 0x6D, 0x79, 0x4E, 0x6F, 0x75, 0x68,
			0x61, 0x6E };
	private static final byte[] initVector = new byte[] { 0x49, 0x74, 0x68,
			0x69, 0x6E, 0x6b, 0x74, 0x68, 0x65, 0x72, 0x65, 0x66, 0x6f, 0x72,
			0x65, 0x49 };

	private SecretKeySpec secretKeySpec;
	private IvParameterSpec ivParaSpec;
	private Cipher cipher;

	public EncryptionLayer() {
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			secretKeySpec = new SecretKeySpec(secretKey, "AES");
			ivParaSpec = new IvParameterSpec(initVector);
		} catch (NoSuchAlgorithmException e) {
			System.out.println(e);
		} catch (NoSuchPaddingException e) {
			System.out.println(e);
		}

	}

	synchronized public byte[] encrypt(byte [] msg) {
		try {
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParaSpec);
			return cipher.doFinal(msg);
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}

	}

	synchronized public byte [] decrypt(byte[] encryptedMsg) {
		try {
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParaSpec);
			byte[] decryptedBytes = cipher.doFinal(encryptedMsg);
			return decryptedBytes;

		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
	}
}
