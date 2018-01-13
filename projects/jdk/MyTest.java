import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public class MyTest {

  private String password = new String("pass");

  public void test() throws Exception {
    FileInputStream fis = new FileInputStream("test");
    MessageDigest md = MessageDigest.getInstance("SHA");
    DigestInputStream dis = new DigestInputStream(fis, md);
    ObjectInputStream ois = new ObjectInputStream(dis);
    Object o = ois.readObject();
    if (!(o instanceof String)) {
      System.out.println("Unexpected data in file");
      System.exit(-1);
    }
    String data = (String) o;
    System.out.println("Got message " + data);
    dis.on(false);
    o = ois.readObject();
    if (!(o instanceof byte[])) {
      System.out.println("Unexpected data in file");
      System.exit(-1);
    }
    byte origDigest[] = (byte[]) o;
    origDigest = data.getBytes();
    System.out.println(MessageDigest.isEqual(md.digest(), origDigest));
  }
}

