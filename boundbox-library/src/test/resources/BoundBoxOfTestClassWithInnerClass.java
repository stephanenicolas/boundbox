import org.boundbox.BoundBox;

@SuppressWarnings("unused")
public class BoundBoxOfTestClassWithInnerClass {
    
    private TestClassWithStaticInnerClass obj;
    public BoundBoxOfTestClassWithInnerClass( TestClassWithStaticInnerClass obj) {
        this.obj=obj;
    }

    public class BoundBoxOfInnerClass {
        Class<?> boundClass = TestClassWithStaticInnerClass.class.getDeclaredClasses()[0];
        Object obj;
        public BoundBoxOfInnerClass( Object obj ) {
            this.obj = obj;
        }
        
    }
    
    public class BoundBoxOfStaticInnerClass {
        
        Class<?> boundClass = TestClassWithStaticInnerClass.class.getDeclaredClasses()[1];
        Object obj;
        public BoundBoxOfStaticInnerClass( Object obj) {
            this.obj = obj;
        }
        
    }

}
