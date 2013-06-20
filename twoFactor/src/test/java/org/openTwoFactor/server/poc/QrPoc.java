package org.openTwoFactor.server.poc;

import java.io.File;
import java.io.IOException;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;


/**
 * 
 */
public class QrPoc {

  /**
   * @param args
   */
  public static void main(String[] args) {
    QRCodeWriter writer = new QRCodeWriter();
    BitMatrix bitMatrix = null;
    try {
        bitMatrix = writer.encode("V7I7VOGZ4L24OC3J", BarcodeFormat.QR_CODE, 300, 300);
        MatrixToImageWriter.writeToFile(bitMatrix, "gif", new File("C:\\temp\\output.gif"));
        } catch (WriterException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
  }

}
