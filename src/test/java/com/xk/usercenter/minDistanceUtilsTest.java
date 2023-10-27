package com.xk.usercenter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xk.usercenter.utils.MinDistanceUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 编辑距离算法测试
 */
public class minDistanceUtilsTest {
    @Test
    public void minDistanceTest() {
        List<String> list1 = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        List<String> list3 = new ArrayList<>();
        List<String> list4 = new ArrayList<>();
        list1.add("篮球");
        list1.add("运动");
        list1.add("男");
        list2.add("篮球");
        list2.add("运动");
        list2.add("女");
        list3.add("篮球");
        list3.add("运动");
        list3.add("男");
        list3.add("乒乓球");
        list4.add("篮球");
        list4.add("运动");
        list4.add("女");
        list4.add("棒球");
//        String tag1 ="[\"篮球\",\"运动\",\"男\"]";
//        String tag2 ="[\"篮球\",\"运动\",\"女\"]";
//        String tag3 ="[\"篮球\",\"运动\",\"男\",\"乒乓球\"]";
//        String tag4 ="[\"篮球\",\"排球\",\"男\",\"棒球\"]";
//
//        Gson gson = new Gson();
//        Type type = new TypeToken<List<String>>() {}.getType();
//        List<String> list1 = gson.fromJson(tag1, type);
//        List<String> list2 = gson.fromJson(tag2, type);
//        List<String> list3 = gson.fromJson(tag3, type);
//        List<String> list4 = gson.fromJson(tag4, type);
        System.out.println(list2.size());
        System.out.println(list3.size());
        System.out.println(list4.size());

        int scoere = MinDistanceUtils.minDistanceList(list1, list2);
        int scoere1 = MinDistanceUtils.minDistanceList(list1, list3);
        int scoere2 = MinDistanceUtils.minDistanceList(list1, list4);
        System.out.println(scoere);
        System.out.println(scoere1);
        System.out.println(scoere2);
    }
}
