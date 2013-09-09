
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.boundbox.BoundBoxException;

public final class BoundBoxOfTestClassWithSingleField {

  private TestClassWithSingleField boundObject;

  public BoundBoxOfTestClassWithSingleField(TestClassWithSingleField boundObject) {
    this.boundObject = boundObject;
  }

  // ******************************
  // 	Access to constructors
  // ******************************

  // ******************************
  // 	Direct access to fields
  // ******************************

  public String boundBox_getFoo() {
    try {
      Field field = boundObject.getClass().getDeclaredField("foo");
      field.setAccessible(true);
      return (java.lang.String) field.get(boundObject);
    }
    catch( Exception e ) {
      throw new BoundBoxException(e);
    }
  }

  public void boundBox_setFoo(String foo) {
    try {
      Field field = boundObject.getClass().getDeclaredField("foo");
      field.setAccessible(true);
      field.set(boundObject, foo);
    }
    catch( Exception e ) {
      throw new BoundBoxException(e);
    }
  }
  // ******************************
  // 	Access to methods
  // ******************************

}
