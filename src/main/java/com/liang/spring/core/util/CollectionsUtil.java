package com.liang.spring.core.util;

import java.util.Collection;

public class CollectionsUtil {


    public static boolean isEmpty(Collection collection){

        return collection == null || collection.isEmpty();

    }
}
