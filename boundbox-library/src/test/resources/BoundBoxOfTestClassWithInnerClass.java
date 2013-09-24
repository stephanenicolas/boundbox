import org.boundbox.BoundBox;

@SuppressWarnings("unused")
public class BoundBoxOfTestClassWithInnerClass {
    
    private TestClassWithInnerClass obj;
    public BoundBoxOfTestClassWithInnerClass( TestClassWithInnerClass obj) {
        this.obj=obj;
    }

    public class BoundBoxOfInnerClass {
        Class<?> boundClass = TestClassWithInnerClass.class.getDeclaredClasses()[0];
        Object obj;
        public BoundBoxOfInnerClass( Object obj ) {
            this.obj = obj;
        }
        
    }
    
    public class BoundBoxOfStaticInnerClass {
        
        Class<?> boundClass = TestClassWithInnerClass.class.getDeclaredClasses()[1];
        Object obj;
        public BoundBoxOfStaticInnerClass( Object obj) {
            this.obj = obj;
        }
        
    }

}
