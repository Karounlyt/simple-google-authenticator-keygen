package com.karen;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.karen.entity.User;
import com.karen.util.Base32String;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.shiro.crypto.hash.SimpleHash;

public class Main {

  private static final int QR_WIDTH = 400;
  private static final int QR_HEIGHT = 400;
  private static final int LOGO_WIDTH = 88;
  private static final int LOGO_HEIGHT = 88;
  private static final int LOGO_X_START = (QR_WIDTH - LOGO_WIDTH) / 2;
  private static final int LOGO_Y_START = (QR_HEIGHT - LOGO_HEIGHT) / 2;
  private static final String BASIC_URL_FORMAT = "otpauth://totp/%s?secret=%s&issuer=%s";

  public static void main(String[] args) {
    String note = "simple-google-authenticator-keygen";
    User user = new User();
    user.setUsername("user");
    user.setPassword("password");

    // base32 密钥
    String base32Hash = generateKey(user.getUsername(), user.getPassword());
    generateKeyFile(base32Hash);
    generateKeyQRCode(note, base32Hash, user.getUsername(), true);
    generateKeyQRCode(note, base32Hash, user.getUsername(), false);
  }


  /**
   * 生成密钥
   * @param source  源文件
   * @param salt    加盐
   * @return
   */
  public static String generateKey(String source, String salt) {
    SimpleHash hash = new SimpleHash("SHA-1", source, salt, 0);
    return Base32String.encode(hash.getBytes());
  }


  /**
   * 生成密钥txt文件
   * @param key 密钥
   */
  public static void generateKeyFile(String key) {
    enableFileExists();
    try (FileOutputStream keyOutPutStream = new FileOutputStream("key/key.txt");) {
      keyOutPutStream.write(key.getBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  public static void enableFileExists() {
    File keyDir = new File("key");
    if (!keyDir.exists()) {
      keyDir.mkdir();
    }
  }

  /**
   * 生成二维码
   * @param note      标识
   * @param key       密钥
   * @param username  用户名
   */
  public static void generateKeyQRCode(String note, String key, String username, boolean hasLogo) {
    if (hasLogo) {
      generateKeyQRCodeWithLogo(note, key, username);
    } else {
      generateBasicKeyQRCode(note, key, username);
    }
  }

  /**
   * 生成二维码
   * @param note      标识
   * @param key       密钥
   * @param username  用户名
   */
  public static void generateBasicKeyQRCode(String note, String key, String username) {
    enableFileExists();
    QRCodeWriter qrCodeWriter = new QRCodeWriter();
    // 生成url
    String url = String.format(BASIC_URL_FORMAT, note, key, username);
    try {
      Map<EncodeHintType, Object> hints = new HashMap<>();
//      字符编码格式
//      hints.put(EncodeHintType.CHARACTER_SET, CharacterSetECI.UTF8);
//      纠错级别
      hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
      hints.put(EncodeHintType.MARGIN, 0);
//      指定QR版本  [1, 40] 自然数
//      hints.put(EncodeHintType.QR_VERSION, 5);

      BitMatrix matrix = qrCodeWriter
          .encode(url, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
      BufferedImage qrImage = new BufferedImage(QR_WIDTH, QR_HEIGHT,
          BufferedImage.TYPE_INT_RGB);
      qrImage.getGraphics().drawImage(MatrixToImageWriter.toBufferedImage(matrix), 0, 0, null);

//      生成图片
      ImageIO.write(qrImage, "jpg", new File("./key/qrcode.jpg"));

    } catch (WriterException | IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 生成带logo二维码
   * @param note      标识
   * @param key       密钥
   * @param username  用户名
   */
  public static void generateKeyQRCodeWithLogo(String note, String key, String username) {
    enableFileExists();
    QRCodeWriter qrCodeWriter = new QRCodeWriter();
    // 生成url
    String url = String.format(BASIC_URL_FORMAT, note, key, username);
    try {
      Map<EncodeHintType, Object> hints = new HashMap<>();
//      字符编码格式
//      hints.put(EncodeHintType.CHARACTER_SET, CharacterSetECI.UTF8);
//      纠错级别
      hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
      hints.put(EncodeHintType.MARGIN, 0);
//      指定QR版本  [1, 40] 自然数
//      hints.put(EncodeHintType.QR_VERSION, 5);

      BitMatrix matrix = qrCodeWriter
          .encode(url, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
      BufferedImage qrImage = new BufferedImage(QR_WIDTH, QR_HEIGHT,
          BufferedImage.TYPE_INT_RGB);
      qrImage.getGraphics().drawImage(MatrixToImageWriter.toBufferedImage(matrix), 0, 0, null);

//      加载图片
      BufferedImage logo = ImageIO.read(new File("build/resources/main/vue-logo.jpg"));
//      缩放
      Image scaledLogo = logo.getScaledInstance(LOGO_WIDTH, LOGO_HEIGHT, 0);

      qrImage.getGraphics().drawImage(scaledLogo, LOGO_X_START, LOGO_Y_START, null);

//      生成图片
      ImageIO.write(qrImage, "jpg", new File("./key/qrcode-withlogo.jpg"));

    } catch (WriterException | IOException e) {
      e.printStackTrace();
    }
  }
}
