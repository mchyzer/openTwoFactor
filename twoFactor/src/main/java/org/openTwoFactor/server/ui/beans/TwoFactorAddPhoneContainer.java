package org.openTwoFactor.server.ui.beans;


public class TwoFactorAddPhoneContainer {

  /**
   * qr code unique id so we dont have send two emails
   */
  private String qrCodeUniqueId;

  /**
   * qr code unique id so we dont have send two emails
   * @return qr code
   */
  public String getQrCodeUniqueId() {
    return this.qrCodeUniqueId;
  }

  /**
   * qr code unique id so we dont have send two emails
   * @param qrCodeUniqueId1
   */
  public void setQrCodeUniqueId(String qrCodeUniqueId1) {
    this.qrCodeUniqueId = qrCodeUniqueId1;
  }

  
  
}
