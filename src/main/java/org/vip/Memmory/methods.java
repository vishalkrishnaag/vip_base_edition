package org.vip.Memmory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class methods {
    String methodName;
    Integer classId;


    public int getCallCount() {
        return callCount;
    }

    public void setCallCount(int callCount) {
        this.callCount = callCount;
    }

    int callCount; // to find most used method

    public methods(String methodName,Integer classId) {
        this.methodName = methodName;
        this.classId = classId;

    }
}