package com.arialyy.aria.util;

import com.arialyy.aria.core.DownloadManager;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by Aria.Lao on 2017/1/11.
 */

public class SSLContextUtil {

  /**
   * 颁发服务器证书的 CA 未知
   *
   * @param caAlias CA证书别名
   * @param caPath 保存在assets目录下的CA证书完整路径
   */
  public static SSLContext getSSLContext(String caAlias, String caPath) {
    // Load CAs from an InputStream
    // (could be from a resource or ByteArrayInputStream or ...)
    CertificateFactory cf = null;
    try {
      cf = CertificateFactory.getInstance("X.509");
      InputStream caInput = DownloadManager.APP.getAssets().open(caPath);
      Certificate ca;
      ca = cf.generateCertificate(caInput);
      System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());

      // Create a KeyStore containing our trusted CAs
      String keyStoreType = KeyStore.getDefaultType();
      KeyStore keyStore = KeyStore.getInstance(keyStoreType);
      keyStore.load(null, null);
      keyStore.setCertificateEntry(caAlias, ca);

      // Create a TrustManager that trusts the CAs in our KeyStore
      String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
      TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
      tmf.init(keyStore);
      KeyManagerFactory kmf =
          KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      kmf.init(keyStore, null);

      // Create an SSLContext that uses our TrustManager
      SSLContext context = SSLContext.getInstance("TLS");
      context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
      return context;
    } catch (CertificateException | NoSuchAlgorithmException | IOException | KeyStoreException | KeyManagementException | UnrecoverableKeyException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 服务器证书不是由 CA 签署的，而是自签署时，获取默认的SSL
   */
  public static SSLContext getDefaultSLLContext() {
    SSLContext sslContext = null;
    try {
      sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, new TrustManager[] { trustManagers }, new SecureRandom());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return sslContext;
  }

  /**
   * 创建自己的 TrustManager，这次直接信任服务器证书。这种方法具有前面所述的将应用与证书直接关联的所有弊端，但可以安全地操作。
   */
  private static TrustManager trustManagers = new X509TrustManager() {

    @Override public void checkClientTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {
    }

    @Override public void checkServerTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {

    }

    @Override public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
    }
  };

  public static final HostnameVerifier HOSTNAME_VERIFIER = new HostnameVerifier() {
    public boolean verify(String hostname, SSLSession session) {
      return true;
    }
  };
}