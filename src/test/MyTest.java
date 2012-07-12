package test;

/**
 * Just a simple test class to be able to easily change the input for Controller.
 *
 * @author kentcdodds
 */
public class MyTest {

  public static void main(String[] args) {
    String propFile = "point to properties file";
    controller.Controller.main(new String[]{propFile});
  }
}
