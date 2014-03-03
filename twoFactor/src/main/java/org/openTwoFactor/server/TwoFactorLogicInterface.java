package org.openTwoFactor.server;

import java.io.File;

/**
 * logic for two factor for qr, hotp, hotp
 * @author mchyzer
 *
 */
public interface TwoFactorLogicInterface {

  /**
   * generate QR image
   * @param data
   * @param imageFileGif
   * @param imageFileWidth 
   */
  public void generateQrFile(String data, File imageFileGif, int imageFileWidth);
  
  /**
   * generate a new base 32 secret
   * @param secretSizeInBytes 
   * @return the secret
   */
  public String generateBase32secret(int secretSizeInBytes);
  
  /**
   * generate a new base 32 secret
   * @param secret 
   * @param timeDiv30000 
   * @return the secret
   */
  public int totpPassword(byte[] secret, long timeDiv30000);
  
  /**
   * generate a new base 32 secret
   * @param secret 
   * @param sequence 
   * @return the secret
   */
  public int hotpPassword(byte[] secret, long sequence);
  
}
