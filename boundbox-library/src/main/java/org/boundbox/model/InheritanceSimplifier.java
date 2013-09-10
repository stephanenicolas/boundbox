package org.boundbox.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InheritanceSimplifier {

    public void simplifyInheritance( List<FieldInfo> listFieldInfos) {
        //get min inheritance level of Field.
        Map<String, FieldInfo> mapFieldNameToMinFieldInfo = new HashMap<String, FieldInfo>();
        for( FieldInfo fieldInfo : listFieldInfos ) {
            if( ! mapFieldNameToMinFieldInfo.containsKey(fieldInfo.getFieldName())) {
                mapFieldNameToMinFieldInfo.put( fieldInfo.getFieldName(), fieldInfo);
            } else {
                FieldInfo minFieldInfo = mapFieldNameToMinFieldInfo.get(fieldInfo.getFieldName());
                if( minFieldInfo.getInheritanceLevel() > fieldInfo.getInheritanceLevel() ) {
                    mapFieldNameToMinFieldInfo.put( fieldInfo.getFieldName(), fieldInfo);
                }
            }
        }
        
        //and replace it to 0
        for( FieldInfo minFields : mapFieldNameToMinFieldInfo.values() ) {
            minFields.setInheritanceLevel(0);
        }
    }
    
    //TODO same for methods
}
